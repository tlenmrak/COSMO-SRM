package com.yourcompany.cosmo.suppliers.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Репозиторий поставщиков.
 */
public interface SupplierRepository extends ReactiveCrudRepository<SupplierEntity, UUID> {

    @Query("""
        SELECT id, name, phone, email, notes, is_active, created_at
        FROM supplier
        WHERE (:q IS NULL OR lower(name) LIKE :q)
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<SupplierEntity> findByQuery(String q, int limit, int offset);
}

