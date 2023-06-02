package cart.cart.presentation.dto;

import cart.cartitem.CartItem;
import cart.product.presentation.dto.ProductResponse;

public class CartItemResponse {
    private long id;
    private int quantity;
    private ProductResponse product;

    public CartItemResponse(long id, int quantity, ProductResponse product) {
        this.id = id;
        this.quantity = quantity;
        this.product = product;
    }

    public long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public ProductResponse getProduct() {
        return product;
    }

    public static CartItemResponse from(CartItem cartItem) {
        final var productResponse = new ProductResponse(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(),
                cartItem.getProduct().getImageUrl(),
                cartItem.isOnSale(),
                cartItem.getDiscountPrice());
        return new CartItemResponse(cartItem.getId(), cartItem.getQuantity(), productResponse);
    }
}
