package com.yourcompany.cosmo.suppliers.service;

import com.yourcompany.cosmo.suppliers.repository.RawMaterialDefaultOfferRepository;
import com.yourcompany.cosmo.suppliers.repository.SupplierMaterialEntity;
import com.yourcompany.cosmo.suppliers.repository.SupplierMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Сценарии работы с предложениями поставщиков (offers).
 */
@Service
@RequiredArgsConstructor
public class SupplierOfferService {

  private final SupplierMaterialRepository repo;
  private final RawMaterialDefaultOfferRepository defaultOfferRepo;

  /**
   * Создать offer для поставщика на конкретное сырьё.
   */
  public Mono<SupplierMaterialEntity> createOffer(UUID supplierId, UUID rawMaterialId, double packageSize,
                                                  String packageUnit, String sku, String link) {
    var offer = SupplierMaterialEntity.create(supplierId, rawMaterialId, packageSize, packageUnit, sku, link);
    return repo.save(offer);
  }

  /**
   * Обновить offer.
   */
  public Mono<SupplierMaterialEntity> updateOffer(UUID offerId, double packageSize, String packageUnit,
                                                  String sku, String link, boolean isActive) {
    return repo.findById(offerId)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Offer not found")))
            .flatMap(existing -> repo.save(existing.update(packageSize, packageUnit, sku, link, isActive)));
  }

  /**
   * Листинг offers по сырью.
   */
  public Flux<SupplierMaterialEntity> listOffersByRawMaterial(UUID rawMaterialId) {
    return repo.findByRawMaterialId(rawMaterialId);
  }

  /**
   * Листинг offers по поставщику.
   */
  public Flux<SupplierMaterialEntity> listOffersBySupplier(UUID supplierId) {
    return repo.findBySupplierId(supplierId);
  }

  /**
   * Получить offer по id.
   */
  public Mono<SupplierMaterialEntity> get(UUID offerId) {
    return repo.findById(offerId);
  }

  /**
   * Установить дефолтный offer для сырья.
   */
  public Mono<Void> setDefaultOffer(UUID rawMaterialId, UUID offerId) {
    return defaultOfferRepo.setDefaultOffer(rawMaterialId, offerId);
  }

  /**
   * Снять дефолтный offer.
   */
  public Mono<Void> clearDefaultOffer(UUID rawMaterialId) {
    return defaultOfferRepo.clearDefaultOffer(rawMaterialId);
  }

}
