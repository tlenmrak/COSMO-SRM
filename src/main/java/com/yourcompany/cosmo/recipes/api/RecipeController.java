package com.yourcompany.cosmo.recipes.api;

import com.yourcompany.cosmo.recipes.service.RecipeService;
import com.yourcompany.cosmo.recipes.repository.RecipeEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recipes")
@Validated
public class RecipeController {
  private final RecipeService service;

  public RecipeController(RecipeService service) {
    this.service = service;
  }

  public record ItemReq(
      UUID rawMaterialId,
      @Positive double amountGram
  ) {}

  public record CreateRecipeRequest(
      @NotBlank String name,
      Double yieldAmount,
      String yieldUnit,
      @NotEmpty List<@Valid ItemReq> items
  ) {}

  @PostMapping
  public Mono<RecipeEntity> create(@RequestBody @Valid CreateRecipeRequest req) {
    var items = req.items().stream()
        .map(i -> new RecipeService.Item(i.rawMaterialId(), i.amountGram()))
        .toList();
    return service.create(req.name(), req.yieldAmount(), req.yieldUnit(), items);
  }
}
