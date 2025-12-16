package com.yourcompany.cosmo.rawmaterials.repository;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Table("raw_material")
public record RawMaterialEntity(
    @Id UUID id,
    String name,
    String unit,
    boolean isActive,
    String notes,
    OffsetDateTime createdAt
) {
     public static RawMaterialEntity create(String name, String unit, String notes) {
         return new RawMaterialEntity(
                 UUID.randomUUID(),
                 name,
                 unit,
                 true,
                 notes,
                 OffsetDateTime.now()
         );
     }
}
