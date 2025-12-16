package com.yourcompany.cosmo.suppliers.api;

import com.yourcompany.cosmo.suppliers.service.SupplierService;
import com.yourcompany.cosmo.suppliers.repository.SupplierEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * API поставщиков.
 */
@RestController
@RequestMapping("/api/suppliers")
@Validated
public class SupplierController {

  private final SupplierService service;

  public SupplierController(SupplierService service) {
    this.service = service;
  }

  public record CreateSupplierRequest(
      @NotBlank String name,
      String phone,
      String email,
      String notes
  ) {}

  public record UpdateSupplierRequest(
      @NotBlank String name,
      String phone,
      String email,
      String notes,
      Boolean isActive
  ) {}

  @PostMapping
  public Mono<SupplierEntity> create(@RequestBody @Valid CreateSupplierRequest req) {
    return service.create(req.name(), req.phone(), req.email(), req.notes());
  }

  @PutMapping("/{id}")
  public Mono<SupplierEntity> update(@PathVariable UUID id, @RequestBody @Valid UpdateSupplierRequest req) {
    boolean active = req.isActive() == null ? true : req.isActive();
    return service.update(id, req.name(), req.phone(), req.email(), req.notes(), active);
  }

  @GetMapping("/{id}")
  public Mono<SupplierEntity> get(@PathVariable UUID id) {
    return service.get(id);
  }

  @GetMapping
  public Flux<SupplierEntity> list(
      @RequestParam(name = "q", required = false) String q,
      @RequestParam(name = "limit", defaultValue = "50") int limit,
      @RequestParam(name = "offset", defaultValue = "0") int offset
  ) {
    int lim = Math.max(1, Math.min(limit, 200));
    int off = Math.max(0, offset);
    return service.list(q, lim, off);
  }
}
