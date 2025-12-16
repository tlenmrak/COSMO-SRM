package com.yourcompany.cosmo.rawmaterials.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface RawMaterialRepository extends ReactiveCrudRepository<RawMaterialEntity, UUID> {}
