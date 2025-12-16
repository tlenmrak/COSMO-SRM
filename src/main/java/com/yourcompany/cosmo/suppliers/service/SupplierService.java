package com.yourcompany.cosmo.suppliers.service;

import com.yourcompany.cosmo.suppliers.repository.SupplierEntity;
import com.yourcompany.cosmo.suppliers.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Сценарии работы с поставщиками.
 */
@Service
@RequiredArgsConstructor
public class SupplierService {

  private final SupplierRepository repo;
  private final DatabaseClient db;

  /**
   * Создать поставщика.
   */
  public Mono<SupplierEntity> create(String name, String phone, String email, String notes) {
    var supplier = SupplierEntity.create(name, phone, email, notes);
    return repo.save(supplier);
  }

  /**
   * Обновить данные поставщика.
   */
  public Mono<SupplierEntity> update(UUID id, String name, String phone, String email, String notes, boolean isActive) {
    return repo.findById(id)
            .switchIfEmpty(Mono.error(new IllegalArgumentException("Supplier not found")))
            .flatMap(existing -> repo.save(existing.update(name, phone, email, notes, isActive)));
  }

  /**
   * Получить поставщика по id.
   */
  public Mono<SupplierEntity> get(UUID id) {
    return repo.findById(id);
  }

  /**
   * Листинг поставщиков с поиском по имени.
   */
  public Flux<SupplierEntity> list(String q, int limit, int offset) {
    String searchQuery = (q == null || q.isBlank()) ? null : "%" + q.trim().toLowerCase() + "%";
    return repo.findByQuery(searchQuery, limit, offset);
  }
}
