package com.yourcompany.cosmo.batches.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Table("batch")
public record BatchEntity(
    @Id UUID id,
    UUID templateId,
    String status,
    LocalDate pricingDate,
    OffsetDateTime createdAt
) {}
