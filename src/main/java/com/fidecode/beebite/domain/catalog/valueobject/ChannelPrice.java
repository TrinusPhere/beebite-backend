package com.fidecode.beebite.domain.catalog.valueobject;

import java.math.BigDecimal;
import java.util.Objects;

public class ChannelPrice {
    private String channel;
    private BigDecimal price;

    public ChannelPrice(String channel, BigDecimal price) {
        if (channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("Channel is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be null or negative");
        }
        this.channel = channel;
        this.price = price;
    }

    public String getChannel() {
        return channel;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelPrice that = (ChannelPrice) o;
        return Objects.equals(channel, that.channel) && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, price);
    }
}
