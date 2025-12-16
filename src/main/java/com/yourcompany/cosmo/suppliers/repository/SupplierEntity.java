package com.yourcompany.cosmo.suppliers.repository;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Поставщик сырья (контакты и основная информация).
 * Содержит основные данные о поставщике: наименование, контакты, статус и время создания.
 */
@Table("supplier")
@Value
@Builder(toBuilder = true)
public class SupplierEntity {

    /**
     * Уникальный идентификатор поставщика.
     */
    @Id
    UUID id;

    /**
     * Наименование поставщика (компания или ФИО).
     * Пример: "ООО 'Поставщик'", "ИП Петров"
     */
    String name;

    /**
     * Контактный телефон.
     * Может содержать код страны и форматированный номер.
     */
    String phone;

    /**
     * Email для связи.
     */
    String email;

    /**
     * Дополнительные заметки о поставщике.
     * Например: график работы, особенности доставки и т.д.
     */
    String notes;

    /**
     * Признак активности.
     * true — поставщик активен; false — деактивирован.
     */
    boolean isActive;

    /**
     * Дата и время создания записи.
     */
    OffsetDateTime createdAt;

    public static SupplierEntity create(String name, String phone, String email, String notes) {
        return SupplierEntity.builder()
                .id(UUID.randomUUID())
                .name(name)
                .phone(phone)
                .email(email)
                .notes(notes)
                .isActive(true)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    /**
     * Создаёт обновлённую версию сущности с новыми значениями полей.
     * Сохраняет неизменные поля: id и createdAt.
     *
     * @param name имя поставщика
     * @param phone телефон
     * @param email email
     * @param notes заметки
     * @param isActive активен ли поставщик
     * @return новая неизменяемая сущность с обновлёнными полями
     */
    public SupplierEntity update(String name, String phone, String email, String notes, boolean isActive) {
        return toBuilder()
                .name(name)
                .phone(phone)
                .email(email)
                .notes(notes)
                .isActive(isActive)
                .build();
    }

}
