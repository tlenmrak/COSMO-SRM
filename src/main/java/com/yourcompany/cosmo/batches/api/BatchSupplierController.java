package com.yourcompany.cosmo.batches.api;

import com.yourcompany.cosmo.batches.service.BatchSupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер выбора поставщиков сырья для конкретной партии.
 * <p>
 * Позволяет:
 * <ul>
 *   <li>получить конфигурацию партии для UI (продукты → ингредиенты → доступные предложения поставщиков)</li>
 *   <li>сохранить выбранные предложения поставщиков (override) для партии</li>
 *   <li>сбросить выбор для конкретного сырья, чтобы использовать значения по умолчанию</li>
 * </ul>
 * </p>
 *
 * <p>
 * Важно: изменения здесь НЕ меняют рецепты продуктов. Выбор поставщика применяется только к конкретной партии.
 * </p>
 */
@Tag(
        name = "Партии — поставщики",
        description = "Выбор поставщиков (предложений) для сырья в рамках конкретной партии"
)
@RestController
@RequestMapping("/api/batches/{batchId}")
public class BatchSupplierController {

  private final BatchSupplierService service;

  public BatchSupplierController(BatchSupplierService service) {
    this.service = service;
  }

  /**
   * Получить конфигурацию поставщиков для партии (для отображения в UI).
   * <p>
   * Возвращает структуру данных, необходимую для интерфейса выбора поставщиков:
   * продукты партии, их ингредиенты (сырьё), текущие выбранные предложения (если есть),
   * а также список доступных предложений поставщиков для каждого вида сырья.
   * </p>
   *
   * @param batchId идентификатор партии
   * @return конфигурация для UI выбора поставщиков
   */
  @Operation(
          summary = "Получить конфигурацию поставщиков для партии",
          description = """
          Возвращает данные для UI:
          - список продуктов партии,
          - ингредиенты рецептов (сырьё и граммовки),
          - текущий выбранный offer по каждому сырью (если задан),
          - список доступных offers поставщиков для каждого сырья.
          """
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Конфигурация успешно получена"),
          @ApiResponse(responseCode = "404", description = "Партия не найдена")
  })
  @GetMapping("/supplier-config")
  public Mono<BatchSupplierService.BatchSupplierConfigResponse> config(@PathVariable UUID batchId) {
    return service.getConfig(batchId);
  }

  /**
   * DTO запроса на сохранение выбора поставщика (offer) для конкретного сырья в партии.
   */
  public record SelectionRequest(
          @Schema(
                  description = "Идентификатор сырья (ингредиента), для которого выбирается поставщик",
                  example = "1f0e6c7b-4d6e-4a38-9f2a-7b4c2f7d1b2a"
          )
          @NotNull UUID rawMaterialId,

          @Schema(
                  description = "Идентификатор предложения поставщика (offer), выбранного для этого сырья",
                  example = "5b4a7d0d-2d3f-4e8a-9f5a-3f2a1b0c9d8e"
          )
          @NotNull UUID supplierMaterialId
  ) {}

  /**
   * Сохранить выбор поставщиков (offers) для партии.
   * <p>
   * Сохраняет переопределения (override) вида:
   * <b>сырьё → предложение поставщика</b> в рамках партии.
   * Эти значения будут иметь приоритет над дефолтными поставщиками сырья при расчётах себестоимости партии.
   * </p>
   *
   * @param batchId идентификатор партии
   * @param body список выборов (сырьё → offer)
   * @return Mono, завершающийся после сохранения
   */
  @Operation(
          summary = "Сохранить выбор поставщиков для партии",
          description = """
          Сохраняет override-выборы поставщиков в рамках партии.
          При расчёте себестоимости приоритет источников цен такой:
          1) выбор для партии (override),
          2) дефолтный offer для сырья,
          3) ручная (fallback) цена сырья.
          """
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Выборы успешно сохранены"),
          @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных"),
          @ApiResponse(responseCode = "404", description = "Партия/сырьё/offer не найдены")
  })
  @PutMapping("/supplier-selections")
  public Mono<Void> save(@PathVariable UUID batchId, @RequestBody @Valid List<SelectionRequest> body) {
    var selections = body.stream()
            .map(s -> new BatchSupplierService.Selection(s.rawMaterialId(), s.supplierMaterialId()))
            .toList();
    return service.saveSelections(batchId, selections);
  }

  /**
   * Сбросить выбор поставщика (offer) для конкретного сырья в партии.
   * <p>
   * Удаляет override-выбор, после чего при расчёте будет использоваться:
   * дефолтный offer для сырья (если задан) или fallback ручная цена.
   * </p>
   *
   * @param batchId идентификатор партии
   * @param rawMaterialId идентификатор сырья
   * @return Mono, завершающийся после удаления
   */
  @Operation(
          summary = "Сбросить выбор поставщика для сырья в партии",
          description = """
          Удаляет override-выбор offer для сырья в рамках партии.
          После сброса расчёт будет использовать дефолтный offer сырья,
          а если он не задан — fallback ручную цену.
          """
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Выбор успешно сброшен"),
          @ApiResponse(responseCode = "404", description = "Партия/сырьё не найдены")
  })
  @DeleteMapping("/supplier-selections/{rawMaterialId}")
  public Mono<Void> clear(@PathVariable UUID batchId, @PathVariable UUID rawMaterialId) {
    return service.clearSelection(batchId, rawMaterialId);
  }
}
