package shop.le_paysan.backend.shop.dto;

import java.math.BigDecimal;

import shop.le_paysan.backend.shop.ShopDeliveryCity;

public record ShopDeliveryCityResponse(Long id, String cityName, BigDecimal price) {

    public static ShopDeliveryCityResponse from(ShopDeliveryCity city) {
        return new ShopDeliveryCityResponse(city.getId(), city.getCityName(), city.getPrice());
    }
}
