package com.yourcompany.cosmo.suppliers.api;

import com.yourcompany.cosmo.suppliers.service.SupplierOfferService;
import com.yourcompany.cosmo.suppliers.repository.SupplierMaterialEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * API предложений поставщиков (offers).
 */
@RestController
public class SupplierOfferController {

  private final SupplierOfferService service;

  public SupplierOfferController(SupplierOfferService service) {
    this.service = service;
  }

  public record CreateOfferRequest(
      @NotNull UUID rawMaterialId,
      @Positive double packageSize,
      @NotBlank String packageUnit,
      String sku,
      String link
  ) {}

  public record UpdateOfferRequest(
      @Positive double packageSize,
      @NotBlank String packageUnit,
      String sku,
      String link,
      Boolean isActive
  ) {}

  @PostMapping("/api/suppliers/{supplierId}/offers")
  public Mono<SupplierMaterialEntity> createOffer(@PathVariable UUID supplierId, @RequestBody @Valid CreateOfferRequest req) {
    return service.createOffer(supplierId, req.rawMaterialId(), req.packageSize(), req.packageUnit(), req.sku(), req.link());
  }

  @PutMapping("/api/offers/{offerId}")
  public Mono<SupplierMaterialEntity> updateOffer(@PathVariable UUID offerId, @RequestBody @Valid UpdateOfferRequest req) {
    boolean active = req.isActive() == null ? true : req.isActive();
    return service.updateOffer(offerId, req.packageSize(), req.packageUnit(), req.sku(), req.link(), active);
  }

  @GetMapping("/api/offers/{offerId}")
  public Mono<SupplierMaterialEntity> getOffer(@PathVariable UUID offerId) {
    return service.get(offerId);
  }

  @GetMapping("/api/raw-materials/{rawMaterialId}/offers")
  public Flux<SupplierMaterialEntity> listOffersByRaw(@PathVariable UUID rawMaterialId) {
    return service.listOffersByRawMaterial(rawMaterialId);
  }

  @GetMapping("/api/suppliers/{supplierId}/offers")
  public Flux<SupplierMaterialEntity> listOffersBySupplier(@PathVariable UUID supplierId) {
    return service.listOffersBySupplier(supplierId);
  }

  @PutMapping("/api/raw-materials/{rawMaterialId}/default-offer/{offerId}")
  public Mono<Void> setDefaultOffer(@PathVariable UUID rawMaterialId, @PathVariable UUID offerId) {
    return service.setDefaultOffer(rawMaterialId, offerId);
  }

  @DeleteMapping("/api/raw-materials/{rawMaterialId}/default-offer")
  public Mono<Void> clearDefaultOffer(@PathVariable UUID rawMaterialId) {
    return service.clearDefaultOffer(rawMaterialId);
  }
}
