package com.kompu.api.infrastructure.config.db.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kompu.api.infrastructure.config.db.schema.OrderItemSchema;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemSchema, UUID> {

    List<OrderItemSchema> findByOrderId(UUID orderId);
}
