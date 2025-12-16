package com.yourcompany.cosmo.rawmaterials.service;

import com.yourcompany.cosmo.rawmaterials.repository.RawMaterialManualPriceEntity;
import com.yourcompany.cosmo.rawmaterials.repository.RawMaterialManualPriceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Сценарии работы с ручными (fallback) ценами сырья.
 */
@Service
public class RawMaterialManualPriceService {

  private final RawMaterialManualPriceRepository repo;

  public RawMaterialManualPriceService(RawMaterialManualPriceRepository repo) {
    this.repo = repo;
  }

  public Mono<RawMaterialManualPriceEntity> add(UUID rawMaterialId, double pricePerGram, String currency,
                                                LocalDate validFrom, LocalDate validTo) {
    var e = new RawMaterialManualPriceEntity(
        UUID.randomUUID(),
        rawMaterialId,
        pricePerGram,
        currency,
        validFrom,
        validTo,
        OffsetDateTime.now()
    );
    return repo.save(e);
  }

  public Flux<RawMaterialManualPriceEntity> list(UUID rawMaterialId) {
    return repo.findByRawMaterialId(rawMaterialId);
  }
}
