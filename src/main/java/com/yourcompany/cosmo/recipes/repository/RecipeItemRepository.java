package com.yourcompany.cosmo.recipes.repository;

import com.yourcompany.cosmo.recipes.service.RecipeService.Item;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Repository
public class RecipeItemRepository {

    private final DatabaseClient db;

    public RecipeItemRepository(DatabaseClient db) {
        this.db = db;
    }

    public Mono<Void> saveAll(UUID recipeId, List<Item> items) {
        return Mono.when(
                items.stream()
                        .map(item -> db.sql("""
                                INSERT INTO recipe_item(recipe_id, raw_material_id, amount_gram)
                                VALUES (:recipeId, :rawMaterialId, :amountGram)
                                """)
                                .bind("recipeId", recipeId)
                                .bind("rawMaterialId", item.rawMaterialId())
                                .bind("amountGram", item.amountGram())
                                .then())
                        .toList()
        ).then();
    }
}