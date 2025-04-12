package com.rockburger.arquetipo2024.adapters.driven.feign.adapter;

import com.rockburger.arquetipo2024.adapters.driven.feign.CartFeignClient;
import com.rockburger.arquetipo2024.adapters.driven.feign.dto.CartResponse;
import com.rockburger.arquetipo2024.adapters.driven.feign.dto.AddCartItemRequest;
import com.rockburger.arquetipo2024.adapters.driven.feign.dto.CartItemResponse;
import com.rockburger.arquetipo2024.adapters.driven.feign.exception.CartServiceExceptionTranslator;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CartServiceAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CartServiceAdapter.class);

    private final CartFeignClient cartFeignClient;
    private final CartServiceExceptionTranslator exceptionTranslator;

    @CircuitBreaker(name = "getActiveCart", fallbackMethod = "getActiveCartFallback")
    @Retry(name = "getActiveCart")
    @TimeLimiter(name = "getActiveCart")
    public CompletableFuture<CartResponse> getActiveCartAsync() {
        logger.info("Getting active cart");
        return CompletableFuture.supplyAsync(() -> {
            try {
                return cartFeignClient.getActiveCart().getBody();
            } catch (Exception e) {
                logger.error("Error getting active cart", e);
                if (e instanceof feign.FeignException) {
                    throw exceptionTranslator.translate((feign.FeignException) e);
                }
                throw e;
            }
        });
    }

    public CartResponse getActiveCart() {
        try {
            return getActiveCartAsync().join();
        } catch (Exception e) {
            logger.error("Error in getActiveCart", e);
            return getFallbackCart();
        }
    }

    @CircuitBreaker(name = "addItemToCart", fallbackMethod = "addItemToCartFallback")
    @Retry(name = "addItemToCart")
    @TimeLimiter(name = "addItemToCart")
    public CompletableFuture<CartResponse> addItemToCartAsync(Long articleId, String articleName, int quantity, double price) {
        logger.info("Adding item to cart: articleId={}, name={}, quantity={}, price={}",
                articleId, articleName, quantity, price);

        return CompletableFuture.supplyAsync(() -> {
            try {
                AddCartItemRequest request = new AddCartItemRequest();
                request.setArticleId(articleId);
                request.setArticleName(articleName);
                request.setQuantity(quantity);
                request.setPrice(price);

                return cartFeignClient.addItemToCart(request).getBody();
            } catch (Exception e) {
                logger.error("Error adding item to cart", e);
                if (e instanceof feign.FeignException) {
                    throw exceptionTranslator.translate((feign.FeignException) e);
                }
                throw e;
            }
        });
    }

    public CartResponse addItemToCart(Long articleId, String articleName, int quantity, double price) {
        try {
            return addItemToCartAsync(articleId, articleName, quantity, price).join();
        } catch (Exception e) {
            logger.error("Error in addItemToCart", e);
            return getFallbackCart();
        }
    }

    @CircuitBreaker(name = "removeItemFromCart", fallbackMethod = "removeItemFromCartFallback")
    @Retry(name = "removeItemFromCart")
    @TimeLimiter(name = "removeItemFromCart")
    public CompletableFuture<CartResponse> removeItemFromCartAsync(Long articleId) {
        logger.info("Removing item from cart: articleId={}", articleId);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return cartFeignClient.removeItemFromCart(articleId).getBody();
            } catch (Exception e) {
                logger.error("Error removing item from cart", e);
                if (e instanceof feign.FeignException) {
                    throw exceptionTranslator.translate((feign.FeignException) e);
                }
                throw e;
            }
        });
    }

    public CartResponse removeItemFromCart(Long articleId) {
        try {
            return removeItemFromCartAsync(articleId).join();
        } catch (Exception e) {
            logger.error("Error in removeItemFromCart", e);
            return getFallbackCart();
        }
    }

    @CircuitBreaker(name = "clearCart", fallbackMethod = "clearCartFallback")
    @Retry(name = "clearCart")
    @TimeLimiter(name = "clearCart")
    public CompletableFuture<Void> clearCartAsync() {
        logger.info("Clearing cart");

        return CompletableFuture.supplyAsync(() -> {
            try {
                cartFeignClient.clearCart();
                return null;
            } catch (Exception e) {
                logger.error("Error clearing cart", e);
                if (e instanceof feign.FeignException) {
                    throw exceptionTranslator.translate((feign.FeignException) e);
                }
                throw e;
            }
        });
    }

    public void clearCart() {
        try {
            clearCartAsync().join();
        } catch (Exception e) {
            logger.error("Error in clearCart", e);
        }
    }

    // Fallback methods
    private CompletableFuture<CartResponse> getActiveCartFallback(Exception e) {
        logger.warn("Fallback for getActiveCart", e);
        return CompletableFuture.completedFuture(getFallbackCart());
    }

    private CompletableFuture<CartResponse> addItemToCartFallback(Long articleId, String articleName, int quantity, double price, Exception e) {
        logger.warn("Fallback for addItemToCart: article={}", articleId, e);
        return CompletableFuture.completedFuture(getFallbackCart());
    }

    private CompletableFuture<CartResponse> removeItemFromCartFallback(Long articleId, Exception e) {
        logger.warn("Fallback for removeItemFromCart: article={}", articleId, e);
        return CompletableFuture.completedFuture(getFallbackCart());
    }

    private CompletableFuture<Void> clearCartFallback(Exception e) {
        logger.warn("Fallback for clearCart", e);
        return CompletableFuture.completedFuture(null);
    }

    private CartResponse getFallbackCart() {
        logger.warn("Returning fallback empty cart");
        CartResponse fallbackCart = new CartResponse();
        fallbackCart.setItems(new ArrayList<>());
        fallbackCart.setTotal(0.0);
        fallbackCart.setStatus("FALLBACK");
        return fallbackCart;
    }
}