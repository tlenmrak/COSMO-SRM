package com.yourcompany.cosmo.batches.api;

import com.yourcompany.cosmo.batches.service.BatchService;
import com.yourcompany.cosmo.batches.repository.BatchEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Контроллер работы с партиями производства.
 * <p>
 * Партия — это конкретный производственный запуск, основанный на шаблоне партии
 * (инфомодели), с фиксированной датой ценообразования и выбранными поставщиками
 * сырья.
 * </p>
 */
@Tag(
        name = "Партии",
        description = "Управление производственными партиями и расчёт их себестоимости"
)
@RestController
@RequestMapping("/api/batches")
public class BatchController {

  private final BatchService service;

  public BatchController(BatchService service) {
    this.service = service;
  }

  /**
   * DTO запроса на создание партии.
   */
  public record CreateBatchRequest(

          @Schema(
                  description = "Идентификатор шаблона партии (инфомодели), на основе которого создаётся партия",
                  example = "b8b9f1c2-8a3b-4c1e-9d4a-2f1a9a3d2e77"
          )
          @NotNull
          UUID templateId
  ) {}

  /**
   * Создать новую партию в статусе PLANNED.
   * <p>
   * Партия создаётся на основе шаблона партии (инфомодели).
   * На этом этапе партия ещё не участвует в расчётах себестоимости.
   * </p>
   */
  @Operation(
          summary = "Создать партию",
          description = """
          Создаёт новую производственную партию в статусе PLANNED
          на основе выбранного шаблона партии.
          """
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Партия успешно создана"),
          @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных"),
          @ApiResponse(responseCode = "404", description = "Шаблон партии не найден")
  })
  @PostMapping
  public Mono<BatchEntity> create(@RequestBody @Valid CreateBatchRequest req) {
    return service.create(req.templateId());
  }

  /**
   * Открыть партию и зафиксировать дату ценообразования.
   * <p>
   * Переводит партию из статуса PLANNED в статус OPEN и фиксирует дату,
   * по которой будут рассчитываться цены сырья и себестоимость партии.
   * </p>
   */
  @Operation(
          summary = "Открыть партию",
          description = """
          Переводит партию в статус OPEN.
          При открытии партии фиксируется дата ценообразования,
          которая используется при расчёте себестоимости.
          """
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Партия успешно открыта"),
          @ApiResponse(responseCode = "400", description = "Партия не может быть открыта из текущего статуса"),
          @ApiResponse(responseCode = "404", description = "Партия не найдена")
  })
  @PostMapping("/{id}/open")
  public Mono<BatchEntity> open(@PathVariable UUID id) {
    return service.open(id);
  }

  /**
   * Рассчитать себестоимость партии.
   * <p>
   * Возвращает детализированный расчёт себестоимости партии:
   * <ul>
   *   <li>потребность в сырье</li>
   *   <li>выбранные поставщики и источники цен</li>
   *   <li>стоимость сырья</li>
   *   <li>итоговую себестоимость партии</li>
   * </ul>
   * </p>
   */
  @Operation(
          summary = "Рассчитать себестоимость партии",
          description = """
          Выполняет расчёт себестоимости партии с учётом:
          - рецептов продуктов,
          - выбранных поставщиков сырья,
          - дефолтных или ручных цен,
          - зафиксированной даты ценообразования.
          """
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Себестоимость успешно рассчитана"),
          @ApiResponse(responseCode = "404", description = "Партия не найдена")
  })
  @GetMapping("/{id}/cost")
  public Mono<BatchService.CostResponse> cost(@PathVariable UUID id) {
    return service.calculateCost(id);
  }
}
