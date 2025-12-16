package com.yourcompany.cosmo.rawmaterials.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Репозиторий ручных цен сырья (fallback).
 */
public interface RawMaterialManualPriceRepository extends ReactiveCrudRepository<RawMaterialManualPriceEntity, UUID> {
  Flux<RawMaterialManualPriceEntity> findByRawMaterialId(UUID rawMaterialId);
}
