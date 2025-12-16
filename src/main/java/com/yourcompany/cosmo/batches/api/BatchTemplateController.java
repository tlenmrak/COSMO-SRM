package com.yourcompany.cosmo.batches.api;

import com.yourcompany.cosmo.batches.service.BatchTemplateService;
import com.yourcompany.cosmo.batches.repository.BatchTemplateEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер работы с шаблонами партий (инфомоделями партии).
 * <p>
 * Шаблон партии (инфомодель) — это набор продуктов и их количества, который используется
 * для быстрого создания производственных партий.
 * </p>
 */
@Tag(
        name = "Шаблоны партий",
        description = "Управление инфомоделями партий: набор продуктов и их количества"
)
@RestController
@RequestMapping("/api/batch-templates")
public class BatchTemplateController {

  private final BatchTemplateService service;

  public BatchTemplateController(BatchTemplateService service) {
    this.service = service;
  }

  /**
   * DTO одного элемента шаблона партии: продукт и количество.
   */
  public record ItemReq(

          @Schema(
                  description = "Идентификатор продукта (товара), входящего в шаблон партии",
                  example = "a3d1f4b2-7c8d-4a9f-8d4a-2f1a9a3d2e77"
          )
          @Valid UUID productId,

          @Schema(
                  description = "Количество единиц продукта в партии",
                  example = "10"
          )
          @Positive int quantity
  ) {}

  /**
   * DTO запроса на создание шаблона партии.
   */
  public record CreateReq(

          @Schema(
                  description = "Название шаблона партии (инфомодели)",
                  example = "Партия декабрь — бальзам + крем"
          )
          @NotBlank String name,

          @Schema(
                  description = "Описание/комментарий к шаблону партии",
                  example = "Шаблон для регулярного производства под маркетплейсы"
          )
          String description,

          @Schema(
                  description = "Список продуктов и их количеств, входящих в шаблон партии"
          )
          @NotEmpty List<@Valid ItemReq> items
  ) {}

  /**
   * Создать новый шаблон партии (инфомодель).
   * <p>
   * Шаблон используется для быстрого создания партии и последующего расчёта потребности в сырье
   * и себестоимости на основе рецептов продуктов.
   * </p>
   *
   * @param req запрос на создание шаблона партии
   * @return созданный шаблон партии
   */
  @Operation(
          summary = "Создать шаблон партии",
          description = """
          Создаёт шаблон партии (инфомодель), который содержит:
          - название и описание,
          - список продуктов и их количества.
          Шаблон используется для создания партий и расчётов.
          """
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Шаблон партии успешно создан"),
          @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных")
  })
  @PostMapping
  public Mono<BatchTemplateEntity> create(@RequestBody @Valid CreateReq req) {
    var items = req.items().stream()
            .map(i -> new BatchTemplateService.Item(i.productId(), i.quantity()))
            .toList();
    return service.create(req.name(), req.description(), items);
  }
}