package com.yourcompany.cosmo.batches.service;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис выбора предложений поставщиков (offers) для сырья внутри партии.
 * Позволяет при открытии партии выбрать поставщика для каждого ингредиента.
 */
@Service
public class BatchSupplierService {

  private final DatabaseClient db;
  private final TransactionalOperator tx;

  public BatchSupplierService(DatabaseClient db, TransactionalOperator tx) {
    this.db = db;
    this.tx = tx;
  }

  public record OfferOption(
      UUID offerId,
      UUID supplierId,
      String supplierName,
      BigDecimal packageSize,
      String packageUnit
  ) {}

  public record IngredientConfig(
      UUID rawMaterialId,
      String rawMaterialName,
      BigDecimal gramsPerProductUnit,
      UUID selectedOfferId,
      List<OfferOption> offers
  ) {}

  public record ProductConfig(
      UUID productId,
      String productName,
      int quantity,
      List<IngredientConfig> ingredients
  ) {}

  public record BatchSupplierConfigResponse(
      UUID batchId,
      LocalDate pricingDate,
      List<ProductConfig> products
  ) {}

  private record IngredientRow(UUID productId, String productName, Integer productQty,
                               UUID rawId, String rawName, BigDecimal gramsPerUnit) {}

  private record OfferRow(UUID offerId, UUID supplierId, String supplierName,
                          UUID rawId, BigDecimal packageSize, String packageUnit) {}

  /**
   * Конфигурация партии для UI: продукты -> ингредиенты -> доступные offers и текущий выбранный offer.
   */
  public Mono<BatchSupplierConfigResponse> getConfig(UUID batchId) {
    Mono<LocalDate> pricingDateMono = db.sql("SELECT pricing_date FROM batch WHERE id = :bid")
        .bind("bid", batchId)
        .map((row, meta) -> row.get("pricing_date", LocalDate.class))
        .one()
        .defaultIfEmpty(LocalDate.now());

    Mono<List<IngredientRow>> ingredientRowsMono =
        db.sql("""
            SELECT
              p.id  AS product_id,
              p.name AS product_name,
              bti.quantity AS product_qty,
              rm.id AS raw_id,
              rm.name AS raw_name,
              ri.amount_gram AS grams_per_unit
            FROM batch b
            JOIN batch_template_item bti ON b.template_id = bti.template_id
            JOIN product p ON p.id = bti.product_id
            JOIN recipe_item ri ON ri.recipe_id = p.recipe_id
            JOIN raw_material rm ON rm.id = ri.raw_material_id
            WHERE b.id = :bid
            ORDER BY p.name, rm.name
            """)
          .bind("bid", batchId)
          .map((row, meta) -> new IngredientRow(
              row.get("product_id", UUID.class),
              row.get("product_name", String.class),
              row.get("product_qty", Integer.class),
              row.get("raw_id", UUID.class),
              row.get("raw_name", String.class),
              new BigDecimal(row.get("grams_per_unit").toString())
          ))
          .all()
          .collectList();

    return Mono.zip(pricingDateMono, ingredientRowsMono)
        .flatMap(tuple -> {
          LocalDate pricingDate = tuple.getT1();
          List<IngredientRow> ingredientRows = tuple.getT2();

          Set<UUID> rawIds = ingredientRows.stream().map(r -> r.rawId).collect(Collectors.toSet());

          Mono<Map<UUID, UUID>> selectedOffersMono = loadSelectedOffers(batchId, rawIds);
          Mono<Map<UUID, List<OfferOption>>> offersByRawMono = loadOffersByRaw(rawIds);

          return Mono.zip(selectedOffersMono, offersByRawMono)
              .map(t2 -> {
                Map<UUID, UUID> selectedOffers = t2.getT1();
                Map<UUID, List<OfferOption>> offersByRaw = t2.getT2();

                // Group rows by product
                Map<UUID, List<IngredientRow>> byProduct = ingredientRows.stream()
                    .collect(Collectors.groupingBy(r -> r.productId));

                List<ProductConfig> products = new ArrayList<>();
                for (var e : byProduct.entrySet()) {
                  UUID productId = e.getKey();
                  List<IngredientRow> rows = e.getValue();
                  String productName = rows.get(0).productName;
                  int qty = rows.get(0).productQty == null ? 0 : rows.get(0).productQty;

                  List<IngredientConfig> ingredients = rows.stream().map(r -> new IngredientConfig(
                      r.rawId,
                      r.rawName,
                      r.gramsPerUnit,
                      selectedOffers.get(r.rawId),
                      offersByRaw.getOrDefault(r.rawId, List.of())
                  )).toList();

                  products.add(new ProductConfig(productId, productName, qty, ingredients));
                }

                products.sort(Comparator.comparing(ProductConfig::productName));
                return new BatchSupplierConfigResponse(batchId, pricingDate, products);
              });
        });
  }

  private Mono<Map<UUID, List<OfferOption>>> loadOffersByRaw(Set<UUID> rawIds) {
    if (rawIds.isEmpty()) return Mono.just(Map.of());

    return db.sql("""
        SELECT
          sm.id AS offer_id,
          s.id AS supplier_id,
          s.name AS supplier_name,
          sm.raw_material_id AS raw_id,
          sm.package_size AS package_size,
          sm.package_unit AS package_unit
        FROM supplier_material sm
        JOIN supplier s ON s.id = sm.supplier_id
        WHERE sm.raw_material_id = ANY(:rawIds)
          AND sm.is_active = true
          AND s.is_active = true
        ORDER BY s.name, sm.package_size
        """)
      .bind("rawIds", rawIds.toArray(new UUID[0]))
      .map((row, meta) -> new OfferRow(
          row.get("offer_id", UUID.class),
          row.get("supplier_id", UUID.class),
          row.get("supplier_name", String.class),
          row.get("raw_id", UUID.class),
          new BigDecimal(row.get("package_size").toString()),
          row.get("package_unit", String.class)
      ))
      .all()
      .collectList()
      .map(rows -> {
        Map<UUID, List<OfferOption>> m = new HashMap<>();
        for (var r : rows) {
          m.computeIfAbsent(r.rawId, k -> new ArrayList<>())
              .add(new OfferOption(r.offerId, r.supplierId, r.supplierName, r.packageSize, r.packageUnit));
        }
        return m;
      });
  }

  private Mono<Map<UUID, UUID>> loadSelectedOffers(UUID batchId, Set<UUID> rawIds) {
    if (rawIds.isEmpty()) return Mono.just(Map.of());

    // selection priority: batch override -> default offer
    return db.sql("""
        SELECT
          rm.id AS raw_id,
          COALESCE(bss.supplier_material_id, rmd.supplier_material_id) AS selected_offer_id
        FROM raw_material rm
        LEFT JOIN batch_supplier_selection bss
          ON bss.batch_id = :bid AND bss.raw_material_id = rm.id
        LEFT JOIN raw_material_default_offer rmd
          ON rmd.raw_material_id = rm.id
        WHERE rm.id = ANY(:rawIds)
        """)
      .bind("bid", batchId)
      .bind("rawIds", rawIds.toArray(new UUID[0]))
      .map((row, meta) -> Map.entry(
          row.get("raw_id", UUID.class),
          row.get("selected_offer_id", UUID.class)
      ))
      .all()
      .collectList()
      .map(list -> {
        Map<UUID, UUID> m = new HashMap<>();
        for (var e : list) {
          if (e.getValue() != null) m.put(e.getKey(), e.getValue());
        }
        return m;
      });
  }

  public record Selection(UUID rawMaterialId, UUID supplierMaterialId) {}

  /**
   * Сохранить выбор offers для партии (override на партию).
   * Полностью перезаписывает выбор по переданным rawMaterialId.
   */
  public Mono<Void> saveSelections(UUID batchId, List<Selection> selections) {
    if (selections == null || selections.isEmpty()) return Mono.empty();

    return tx.transactional(
        Mono.when(selections.stream().map(s -> db.sql("""
                INSERT INTO batch_supplier_selection(batch_id, raw_material_id, supplier_material_id, created_at)
                VALUES (:bid, :rid, :oid, now())
                ON CONFLICT (batch_id, raw_material_id)
                DO UPDATE SET supplier_material_id = EXCLUDED.supplier_material_id
                """)
              .bind("bid", batchId)
              .bind("rid", s.rawMaterialId())
              .bind("oid", s.supplierMaterialId())
              .then()
        ).toList())
    );
  }

  /**
   * Удалить override выбора offer для конкретного сырья в партии.
   */
  public Mono<Void> clearSelection(UUID batchId, UUID rawMaterialId) {
    return db.sql("DELETE FROM batch_supplier_selection WHERE batch_id = :bid AND raw_material_id = :rid")
        .bind("bid", batchId)
        .bind("rid", rawMaterialId)
        .then();
  }
}
