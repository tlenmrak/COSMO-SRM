package com.yourcompany.cosmo.suppliers.api;

import com.yourcompany.cosmo.suppliers.service.SupplierOfferPriceService;
import com.yourcompany.cosmo.suppliers.repository.SupplierMaterialPriceEntity;
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
 * API цен предложений (offers).
 */
@RestController
public class SupplierOfferPriceController {

  private final SupplierOfferPriceService service;

  public SupplierOfferPriceController(SupplierOfferPriceService service) {
    this.service = service;
  }

  public record CreateOfferPriceRequest(
      @Positive double pricePerPackage,
      @NotBlank String currency,
      @NotNull LocalDate validFrom,
      LocalDate validTo
  ) {}

  @PostMapping("/api/offers/{offerId}/prices")
  public Mono<SupplierMaterialPriceEntity> addPrice(@PathVariable UUID offerId, @RequestBody @Valid CreateOfferPriceRequest req) {
    return service.addPrice(offerId, req.pricePerPackage(), req.currency(), req.validFrom(), req.validTo());
  }

  @GetMapping("/api/offers/{offerId}/prices")
  public Flux<SupplierMaterialPriceEntity> list(@PathVariable UUID offerId) {
    return service.list(offerId);
  }
}
