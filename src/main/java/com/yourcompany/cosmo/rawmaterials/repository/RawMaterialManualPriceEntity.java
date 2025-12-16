package com.yourcompany.cosmo.rawmaterials.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ручная (fallback) цена сырья, используется если не выбран поставщик/offer.
 */
@Table("raw_material_manual_price")
public record RawMaterialManualPriceEntity(
    @Id UUID id,
    UUID rawMaterialId,
    Double pricePerGram,
    String currency,
    LocalDate validFrom,
    LocalDate validTo,
    OffsetDateTime createdAt
) {}
