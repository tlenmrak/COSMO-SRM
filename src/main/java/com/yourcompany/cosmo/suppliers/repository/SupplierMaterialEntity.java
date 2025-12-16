package com.yourcompany.cosmo.suppliers.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Предложение поставщика (offer): поставщик + сырьё + фасовка.
 */
@Table("supplier_material")
public record SupplierMaterialEntity(
    @Id UUID id,
    UUID supplierId,
    UUID rawMaterialId,
    Double packageSize,
    String packageUnit,
    String sku,
    String link,
    boolean isActive,
    OffsetDateTime createdAt
) {

    // Статический фабричный метод для создания нового предложения поставщика
    public static SupplierMaterialEntity create(
            UUID supplierId,
            UUID rawMaterialId,
            double packageSize,
            String packageUnit,
            String sku,
            String link
    ) {
        return new SupplierMaterialEntity(
                UUID.randomUUID(),
                supplierId,
                rawMaterialId,
                packageSize,
                packageUnit,
                sku,
                link,
                true,
                OffsetDateTime.now()
        );
    }
    // Метод для обновления сущности с новыми значениями (создаёт копию)
    public SupplierMaterialEntity update(
            double packageSize,
            String packageUnit,
            String sku,
            String link,
            boolean isActive
    ) {
        return new SupplierMaterialEntity(
                this.id(),
                this.supplierId(),
                this.rawMaterialId(),
                packageSize,
                packageUnit,
                sku,
                link,
                isActive,
                this.createdAt()
        );
    }
}
