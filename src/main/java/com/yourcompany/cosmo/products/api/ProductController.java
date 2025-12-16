package com.yourcompany.cosmo.products.api;

import com.yourcompany.cosmo.products.service.ProductService;
import com.yourcompany.cosmo.products.repository.ProductEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {
  private final ProductService service;

  public ProductController(ProductService service) {
    this.service = service;
  }

  public record CreateProductRequest(
      @NotBlank String name,
      String sku,
      @NotNull UUID recipeId
  ) {}

  @PostMapping
  public Mono<ProductEntity> create(@RequestBody @Valid CreateProductRequest req) {
    return service.create(req.name(), req.sku(), req.recipeId());
  }
}
