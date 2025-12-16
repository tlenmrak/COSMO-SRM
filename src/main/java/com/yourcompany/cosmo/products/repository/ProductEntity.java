package com.yourcompany.cosmo.products.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("product")
public record ProductEntity(
    @Id UUID id,
    String name,
    String sku,
    UUID recipeId,
    boolean isActive,
    OffsetDateTime createdAt
) {}
