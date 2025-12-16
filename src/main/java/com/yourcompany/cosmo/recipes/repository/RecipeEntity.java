package com.yourcompany.cosmo.recipes.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("recipe")
public record RecipeEntity(
    @Id UUID id,
    String name,
    String status,
    Double yieldAmount,
    String yieldUnit,
    OffsetDateTime createdAt
) {}
