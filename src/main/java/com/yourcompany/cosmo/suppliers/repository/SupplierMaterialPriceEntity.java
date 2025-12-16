package com.yourcompany.cosmo.suppliers.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * История цен на предложение поставщика (offer).
 */
@Table("supplier_material_price")
public record SupplierMaterialPriceEntity(
    @Id UUID id,
    UUID supplierMaterialId,
    Double pricePerPackage,
    String currency,
    LocalDate validFrom,
    LocalDate validTo,
    OffsetDateTime createdAt
) {
    public static SupplierMaterialPriceEntity create(
            UUID supplierMaterialId,
            double pricePerPackage,
            String currency,
            LocalDate validFrom,
            LocalDate validTo
    ) {
        return new SupplierMaterialPriceEntity(
                UUID.randomUUID(),
                supplierMaterialId,
                pricePerPackage,
                currency,
                validFrom,
                validTo,
                OffsetDateTime.now()
        );
    }
}
