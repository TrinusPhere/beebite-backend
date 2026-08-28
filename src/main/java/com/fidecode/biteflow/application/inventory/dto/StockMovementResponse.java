package com.fidecode.biteflow.application.inventory.dto;
import java.math.BigDecimal;
public record StockMovementResponse(String id,String ingredientId,String ingredientName,String unit,String type,BigDecimal quantity,BigDecimal previousStock,BigDecimal resultingStock,String reason,String document,String createdAt) {}
