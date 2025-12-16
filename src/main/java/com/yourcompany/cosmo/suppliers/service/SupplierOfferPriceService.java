package com.yourcompany.cosmo.suppliers.service;

import com.yourcompany.cosmo.suppliers.repository.SupplierMaterialPriceEntity;
import com.yourcompany.cosmo.suppliers.repository.SupplierMaterialPriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Сценарии работы с ценами offers.
 */
@Service
@RequiredArgsConstructor
public class SupplierOfferPriceService {

  private final SupplierMaterialPriceRepository repo;


  /**
   * Добавить цену на offer. (Не закрывает предыдущую автоматически — это можно добавить позже.)
   */
  public Mono<SupplierMaterialPriceEntity> addPrice(UUID offerId, double pricePerPackage, String currency,
                                                    LocalDate validFrom, LocalDate validTo) {
    var offer = SupplierMaterialPriceEntity.create(offerId, pricePerPackage, currency, validFrom, validTo);
    return repo.save(offer);
  }

  /**
   * Листинг цен offer'а.
   */
  public Flux<SupplierMaterialPriceEntity> list(UUID offerId) {
    return repo.findBySupplierMaterialId(offerId);
  }
}
