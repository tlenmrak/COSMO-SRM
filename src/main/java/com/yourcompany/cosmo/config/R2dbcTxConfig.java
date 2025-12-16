package com.yourcompany.cosmo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
public class R2dbcTxConfig {
  @Bean
  TransactionalOperator transactionalOperator(R2dbcTransactionManager txm) {
    return TransactionalOperator.create(txm);
  }
}
