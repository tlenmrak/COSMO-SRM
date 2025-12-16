package com.yourcompany.cosmo.products.service;

import com.yourcompany.cosmo.products.repository.ProductEntity;
import com.yourcompany.cosmo.products.repository.ProductRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class ProductService {
  private final ProductRepository repo;

  public ProductService(ProductRepository repo) {
    this.repo = repo;
  }

  public Mono<ProductEntity> create(String name, String sku, UUID recipeId) {
    var entity = new ProductEntity(UUID.randomUUID(), name, sku, recipeId, true, OffsetDateTime.now());
    return repo.save(entity);
  }
}
