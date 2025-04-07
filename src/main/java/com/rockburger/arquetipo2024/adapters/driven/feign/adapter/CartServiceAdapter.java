package com.rockburger.arquetipo2024.adapters.driven.feign.adapter;

import com.rockburger.arquetipo2024.adapters.driven.feign.CartFeignClient;
import com.rockburger.arquetipo2024.adapters.driven.feign.dto.CartResponse;
import com.rockburger.arquetipo2024.adapters.driven.feign.dto.AddCartItemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartServiceAdapter {
    private final CartFeignClient cartFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public CartResponse getActiveCart(String authToken) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("getActiveCart");
        return circuitBreaker.run(() -> cartFeignClient.getActiveCart("Bearer " + authToken).getBody(),
                throwable -> {
                    log.error("Error getting active cart", throwable);
                    return getFallbackCart();
                });
    }

    public CartResponse addItemToCart(String authToken, Long articleId, String articleName, int quantity, double price) {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setArticleId(articleId);
        request.setArticleName(articleName);
        request.setQuantity(quantity);
        request.setPrice(price);

        return cartFeignClient.addItemToCart("Bearer " + authToken, request).getBody();
    }

    public CartResponse removeItemFromCart(String authToken, Long articleId) {
        return cartFeignClient.removeItemFromCart("Bearer " + authToken, articleId).getBody();
    }

    public void clearCart(String authToken) {
        cartFeignClient.clearCart("Bearer " + authToken);
    }

    private CartResponse getFallbackCart() {
        // Return an empty cart as fallback
        CartResponse fallbackCart = new CartResponse();
        fallbackCart.setItems(new ArrayList<>());
        fallbackCart.setTotal(0.0);
        fallbackCart.setStatus("FALLBACK");
        return fallbackCart;
    }
}
