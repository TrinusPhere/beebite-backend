package com.fidecode.beebite.infrastructure.persistence.panache.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_item_modifier")
public class OrderItemModifierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    public OrderItemEntity orderItem;

    @Column(name = "modifier_group_id")
    public UUID modifierGroupId;

    @Column(name = "group_name")
    public String groupName;

    @Column(name = "modifier_option_id")
    public UUID modifierOptionId;

    @Column(name = "option_name")
    public String optionName;

    @Column(name = "price", nullable = false)
    public BigDecimal price;
}
