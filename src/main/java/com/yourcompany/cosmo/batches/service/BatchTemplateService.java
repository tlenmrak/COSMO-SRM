package com.yourcompany.cosmo.batches.service;

import com.yourcompany.cosmo.batches.repository.BatchTemplateEntity;
import com.yourcompany.cosmo.batches.repository.BatchTemplateRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BatchTemplateService {
  private final BatchTemplateRepository repo;
  private final DatabaseClient db;
  private final TransactionalOperator tx;

  public BatchTemplateService(BatchTemplateRepository repo, DatabaseClient db, TransactionalOperator tx) {
    this.repo = repo;
    this.db = db;
    this.tx = tx;
  }

  public record Item(UUID productId, int quantity) {}

  public Mono<BatchTemplateEntity> create(String name, String description, List<Item> items) {
    UUID id = UUID.randomUUID();
    var entity = new BatchTemplateEntity(id, name, description, OffsetDateTime.now());

    Mono<Void> insertItems = Mono.when(
        items.stream().map(i ->
            db.sql("""
                INSERT INTO batch_template_item(template_id, product_id, quantity)
                VALUES (:tid, :pid, :qty)
                """)
              .bind("tid", id)
              .bind("pid", i.productId())
              .bind("qty", i.quantity())
              .then()
        ).toList()
    );

    return tx.transactional(repo.save(entity).flatMap(saved -> insertItems.thenReturn(saved)));
  }
}
