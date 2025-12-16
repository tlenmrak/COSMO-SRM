package com.yourcompany.cosmo.rawmaterials.api;

import com.yourcompany.cosmo.rawmaterials.service.RawMaterialManualPriceService;
import com.yourcompany.cosmo.rawmaterials.repository.RawMaterialManualPriceEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * API ручных цен сырья (fallback).
 */
@RestController
public class RawMaterialManualPriceController {

  private final RawMaterialManualPriceService service;

  public RawMaterialManualPriceController(RawMaterialManualPriceService service) {
    this.service = service;
  }

  public record AddManualPriceRequest(
      @Positive double pricePerGram,
      @NotBlank String currency,
      @NotNull LocalDate validFrom,
      LocalDate validTo
  ) {}

  @PostMapping("/api/raw-materials/{rawMaterialId}/manual-prices")
  public Mono<RawMaterialManualPriceEntity> add(@PathVariable UUID rawMaterialId, @RequestBody @Valid AddManualPriceRequest req) {
    return service.add(rawMaterialId, req.pricePerGram(), req.currency(), req.validFrom(), req.validTo());
  }

  @GetMapping("/api/raw-materials/{rawMaterialId}/manual-prices")
  public Flux<RawMaterialManualPriceEntity> list(@PathVariable UUID rawMaterialId) {
    return service.list(rawMaterialId);
  }
}
