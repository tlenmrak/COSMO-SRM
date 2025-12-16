package com.yourcompany.cosmo.rawmaterials.service;

import com.yourcompany.cosmo.rawmaterials.repository.RawMaterialEntity;
import com.yourcompany.cosmo.rawmaterials.repository.RawMaterialRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class RawMaterialService {
  private final RawMaterialRepository repo;

  public RawMaterialService(RawMaterialRepository repo) {
    this.repo = repo;
  }

  public Mono<RawMaterialEntity> create(String name, String unit, String notes) {
    var entity = RawMaterialEntity.create(name, unit, notes);
    return repo.save(entity);
  }

  public Flux<RawMaterialEntity> list() {
    return repo.findAll();
  }

  public Mono<RawMaterialEntity> get(UUID id) {
    return repo.findById(id);
  }
}
