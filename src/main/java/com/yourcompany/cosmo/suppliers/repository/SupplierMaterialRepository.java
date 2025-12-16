package com.yourcompany.cosmo.suppliers.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Репозиторий предложений поставщиков (offers).
 */
public interface SupplierMaterialRepository extends ReactiveCrudRepository<SupplierMaterialEntity, UUID> {

  Flux<SupplierMaterialEntity> findByRawMaterialId(UUID rawMaterialId);

  Flux<SupplierMaterialEntity> findBySupplierId(UUID supplierId);
}
