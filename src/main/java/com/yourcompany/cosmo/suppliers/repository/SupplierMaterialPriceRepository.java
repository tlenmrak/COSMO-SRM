package com.yourcompany.cosmo.suppliers.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Репозиторий цен предложений поставщиков.
 */
public interface SupplierMaterialPriceRepository extends ReactiveCrudRepository<SupplierMaterialPriceEntity, UUID> {

  Flux<SupplierMaterialPriceEntity> findBySupplierMaterialId(UUID supplierMaterialId);
}
