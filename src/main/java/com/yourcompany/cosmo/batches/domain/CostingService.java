package com.yourcompany.cosmo.batches.domain;

import java.math.BigDecimal;
import java.util.*;

public final class CostingService {

  public record ProductInBatch(UUID productId, int quantity) {}
  public record RecipeItem(UUID rawMaterialId, BigDecimal gramsPerProductUnit) {}
  public record RawCost(UUID rawMaterialId, BigDecimal grams, BigDecimal unitPrice, BigDecimal cost) {}
  public record Result(List<RawCost> rows, BigDecimal materialsTotal) {}

  public static Result calculate(
      List<ProductInBatch> products,
      Map<UUID, List<RecipeItem>> recipeByProduct,
      Map<UUID, BigDecimal> pricePerGramByRaw
  ) {
    Map<UUID, BigDecimal> need = new HashMap<>();

    for (var p : products) {
      var items = recipeByProduct.getOrDefault(p.productId(), List.of());
      for (var it : items) {
        var add = it.gramsPerProductUnit().multiply(BigDecimal.valueOf(p.quantity()));
        need.merge(it.rawMaterialId(), add, BigDecimal::add);
      }
    }

    List<RawCost> rows = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    for (var e : need.entrySet()) {
      var rawId = e.getKey();
      var grams = e.getValue();
      var unitPrice = pricePerGramByRaw.getOrDefault(rawId, BigDecimal.ZERO);
      var cost = grams.multiply(unitPrice);
      rows.add(new RawCost(rawId, grams, unitPrice, cost));
      total = total.add(cost);
    }

    rows.sort(Comparator.comparing(r -> r.rawMaterialId().toString()));
    return new Result(rows, total);
  }

  private CostingService() {}
}
