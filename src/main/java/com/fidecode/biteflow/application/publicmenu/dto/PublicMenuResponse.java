package com.fidecode.biteflow.application.publicmenu.dto;
import java.math.BigDecimal; import java.util.List;
public record PublicMenuResponse(StoreView store,List<ProductView> products){
 public record StoreView(String id,String name,String logoUrl,boolean isOpen,String openingHours,String address){}
 public record ProductView(String id,String name,String categoryId,String category,String description,BigDecimal price,String imageUrl,String productType,String saleUnit){}
}
