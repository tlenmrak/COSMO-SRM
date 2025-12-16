package com.yourcompany.cosmo.recipes.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface RecipeRepository extends ReactiveCrudRepository<RecipeEntity, UUID> {}
