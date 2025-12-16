package com.yourcompany.cosmo.recipes.service;

import com.yourcompany.cosmo.recipes.repository.RecipeEntity;
import com.yourcompany.cosmo.recipes.repository.RecipeItemRepository;
import com.yourcompany.cosmo.recipes.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RecipeService {
  private final RecipeRepository recipeRepo;
  private final RecipeItemRepository recipeItemRepo;
  private final TransactionalOperator tx;

  public record Item(UUID rawMaterialId, double amountGram) {}

  public Mono<RecipeEntity> create(String name, Double yieldAmount, String yieldUnit, List<Item> items) {
    UUID recipeId = UUID.randomUUID();
    var recipe = new RecipeEntity(recipeId, name, "DRAFT", yieldAmount, yieldUnit, OffsetDateTime.now());

    return tx.transactional(
            recipeRepo.save(recipe)
                    .flatMap(saved -> recipeItemRepo.saveAll(recipeId, items).thenReturn(saved))
    );
  }
}
