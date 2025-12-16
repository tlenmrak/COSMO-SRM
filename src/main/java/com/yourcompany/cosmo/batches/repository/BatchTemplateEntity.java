package com.yourcompany.cosmo.batches.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("batch_template")
public record BatchTemplateEntity(
    @Id UUID id,
    String name,
    String description,
    OffsetDateTime createdAt
) {}
