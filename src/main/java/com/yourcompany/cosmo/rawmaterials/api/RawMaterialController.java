package com.yourcompany.cosmo.rawmaterials.api;

import com.yourcompany.cosmo.rawmaterials.service.RawMaterialService;
import com.yourcompany.cosmo.rawmaterials.repository.RawMaterialEntity;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/raw-materials")
@Validated
public class RawMaterialController {
  private final RawMaterialService service;

  public RawMaterialController(RawMaterialService service) {
    this.service = service;
  }

  public record CreateRawMaterialRequest(
      @NotBlank String name,
      @NotBlank String unit,
      String notes
  ) {}

  @PostMapping
  public Mono<RawMaterialEntity> create(@RequestBody @Validated CreateRawMaterialRequest req) {
    return service.create(req.name(), req.unit(), req.notes());
  }

  @GetMapping
  public Flux<RawMaterialEntity> list() {
    return service.list();
  }

  @GetMapping("/{id}")
  public Mono<RawMaterialEntity> get(@PathVariable UUID id) {
    return service.get(id);
  }
}
