package com.yourcompany.cosmo.batches.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface BatchTemplateRepository extends ReactiveCrudRepository<BatchTemplateEntity, UUID> {}
