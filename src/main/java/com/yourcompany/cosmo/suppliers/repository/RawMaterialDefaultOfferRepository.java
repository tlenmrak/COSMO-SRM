package com.yourcompany.cosmo.suppliers.repository;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Репозиторий для управления таблицей raw_material_default_offer.
 * Выполняет upsert операцию: вставляет или обновляет запись о дефолтном предложении для сырья.
 */
@Repository
public class RawMaterialDefaultOfferRepository {

    private final DatabaseClient db;

    public RawMaterialDefaultOfferRepository(DatabaseClient db) {
        this.db = db;
    }

    /**
     * Устанавливает дефолтный offer для указанного сырья.
     * Если запись уже существует — обновляет supplier_material_id.
     */
    public Mono<Void> setDefaultOffer(UUID rawMaterialId, UUID supplierMaterialId) {
        return db.sql("""
                INSERT INTO raw_material_default_offer(raw_material_id, supplier_material_id, created_at)
                VALUES (:rawMaterialId, :supplierMaterialId, NOW())
                ON CONFLICT (raw_material_id)
                DO UPDATE SET supplier_material_id = EXCLUDED.supplier_material_id
                """)
                .bind("rawMaterialId", rawMaterialId)
                .bind("supplierMaterialId", supplierMaterialId)
                .then();
    }

    public Mono<Void> clearDefaultOffer(UUID rawMaterialId) {
        return db.sql("DELETE FROM raw_material_default_offer WHERE raw_material_id = :rawMaterialId")
                .bind("rawMaterialId", rawMaterialId)
                .then();
    }
}