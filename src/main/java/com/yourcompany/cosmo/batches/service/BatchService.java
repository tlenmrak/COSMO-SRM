package com.yourcompany.cosmo.batches.service;

import com.yourcompany.cosmo.batches.domain.CostingService;
import com.yourcompany.cosmo.batches.repository.BatchEntity;
import com.yourcompany.cosmo.batches.repository.BatchRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class BatchService {
  private final BatchRepository repo;
  private final DatabaseClient db;
  private final TransactionalOperator tx;

  public BatchService(BatchRepository repo, DatabaseClient db, TransactionalOperator tx) {
    this.repo = repo;
    this.db = db;
    this.tx = tx;
  }

  public Mono<BatchEntity> create(UUID templateId) {
    var entity = new BatchEntity(UUID.randomUUID(), templateId, "PLANNED", null, OffsetDateTime.now());
    return repo.save(entity);
  }

  public Mono<BatchEntity> open(UUID batchId) {
    return tx.transactional(
        repo.findById(batchId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Batch not found")))
            .flatMap(b -> repo.save(new BatchEntity(b.id(), b.templateId(), "OPEN", LocalDate.now(), b.createdAt())))
    );
  }

  public record CostRow(UUID rawMaterialId, BigDecimal grams, BigDecimal unitPrice, BigDecimal cost) {}
  public record CostResponse(LocalDate pricingDate, List<CostRow> materials, BigDecimal materialsTotal) {}

  public Mono<CostResponse> calculateCost(UUID batchId) {
    return repo.findById(batchId)
        .switchIfEmpty(Mono.error(new IllegalArgumentException("Batch not found")))
        .flatMap(batch -> {
          LocalDate pricingDate = batch.pricingDate() != null ? batch.pricingDate() : LocalDate.now();

          Mono<List<CostingService.ProductInBatch>> productsMono =
              db.sql("""
                  SELECT bti.product_id, bti.quantity
                  FROM batch b
                  JOIN batch_template_item bti ON b.template_id = bti.template_id
                  WHERE b.id = :bid
                  """)
                .bind("bid", batchId)
                .map((row, meta) -> new CostingService.ProductInBatch(
                    row.get("product_id", UUID.class),
                    Objects.requireNonNull(row.get("quantity", Integer.class))
                ))
                .all()
                .collectList();

          Mono<Map<UUID, List<CostingService.RecipeItem>>> recipeByProductMono =
              db.sql("""
                  SELECT p.id AS product_id, ri.raw_material_id, ri.amount_gram
                  FROM batch b
                  JOIN batch_template_item bti ON b.template_id = bti.template_id
                  JOIN product p ON p.id = bti.product_id
                  JOIN recipe_item ri ON ri.recipe_id = p.recipe_id
                  WHERE b.id = :bid
                  """)
                .bind("bid", batchId)
                .map((row, meta) -> Map.entry(
                    row.get("product_id", UUID.class),
                    new CostingService.RecipeItem(
                        row.get("raw_material_id", UUID.class),
                        new BigDecimal(Objects.requireNonNull(row.get("amount_gram")).toString())
                    )
                ))
                .all()
                .collectList()
                .map(list -> {
                  Map<UUID, List<CostingService.RecipeItem>> m = new HashMap<>();
                  for (var e : list) m.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e.getValue());
                  return m;
                });

          Mono<Map<UUID, BigDecimal>> pricesMono =
              db.sql("""
                  WITH needed_raw AS (
                    SELECT DISTINCT ri.raw_material_id AS raw_id
                    FROM batch b
                    JOIN batch_template_item bti ON b.template_id = bti.template_id
                    JOIN product p ON p.id = bti.product_id
                    JOIN recipe_item ri ON ri.recipe_id = p.recipe_id
                    WHERE b.id = :bid
                  ),
                  selected AS (
                    SELECT nr.raw_id,
                           COALESCE(bss.supplier_material_id, rmd.supplier_material_id) AS offer_id
                    FROM needed_raw nr
                    LEFT JOIN batch_supplier_selection bss
                      ON bss.batch_id = :bid AND bss.raw_material_id = nr.raw_id
                    LEFT JOIN raw_material_default_offer rmd
                      ON rmd.raw_material_id = nr.raw_id
                  ),
                  offer_price AS (
                    SELECT s.raw_id,
                           (smp.price_per_package / NULLIF(sm.package_size, 0)) AS price_per_gram
                    FROM selected s
                    JOIN supplier_material sm ON sm.id = s.offer_id
                    LEFT JOIN LATERAL (
                      SELECT price_per_package
                      FROM supplier_material_price
                      WHERE supplier_material_id = s.offer_id
                        AND valid_from <= :pd
                        AND (valid_to IS NULL OR valid_to >= :pd)
                      ORDER BY valid_from DESC
                      LIMIT 1
                    ) smp ON TRUE
                  ),
                  manual_price AS (
                    SELECT nr.raw_id,
                      (
                        SELECT price_per_gram
                        FROM raw_material_manual_price rmp
                        WHERE rmp.raw_material_id = nr.raw_id
                          AND rmp.valid_from <= :pd
                          AND (rmp.valid_to IS NULL OR rmp.valid_to >= :pd)
                        ORDER BY rmp.valid_from DESC
                        LIMIT 1
                      ) AS price_per_gram
                    FROM needed_raw nr
                  )
                  SELECT nr.raw_id AS raw_id,
                         COALESCE(op.price_per_gram, mp.price_per_gram, 0) AS price_per_gram
                  FROM needed_raw nr
                  LEFT JOIN offer_price op ON op.raw_id = nr.raw_id
                  LEFT JOIN manual_price mp ON mp.raw_id = nr.raw_id
                  """)
                .bind("bid", batchId)
                .bind("pd", pricingDate)
                .map((row, meta) -> Map.entry(
                    row.get("raw_id", UUID.class),
                    row.get("price_per_gram") == null ? BigDecimal.ZERO : new BigDecimal(row.get("price_per_gram").toString())
                ))
                .all()
                .collectList()
                .map(list -> {
                  Map<UUID, BigDecimal> m = new HashMap<>();
                  for (var e : list) m.put(e.getKey(), e.getValue());
                  return m;
                });
return Mono.zip(productsMono, recipeByProductMono, pricesMono)
              .map(tuple -> {
                var products = tuple.getT1();
                var recipeByProduct = tuple.getT2();
                var prices = tuple.getT3();

                var res = CostingService.calculate(products, recipeByProduct, prices);
                var rows = res.rows().stream()
                    .map(r -> new CostRow(r.rawMaterialId(), r.grams(), r.unitPrice(), r.cost()))
                    .toList();

                return new CostResponse(pricingDate, rows, res.materialsTotal());
              });
        });
  }
}
