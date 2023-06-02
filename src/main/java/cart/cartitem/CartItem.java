package cart.cartitem;

import cart.cartitem.application.exception.CartItemException;
import cart.member.Member;
import cart.product.Product;

import java.util.Objects;

public class CartItem {
    private Long id;
    private int quantity;
    private int discountPrice;
    private final Product product;
    private final Member member;

    public CartItem(Member member, Product product) {
        this.quantity = 1;
        this.member = member;
        this.product = product;
        this.discountPrice = 0;
    }

    public CartItem(Long id, int quantity, Product product, Member member) {
        this.id = id;
        this.quantity = quantity;
        this.product = product;
        this.member = member;
        this.discountPrice = 0;
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getDiscountPrice() {
        return discountPrice;
    }

    public void checkOwner(Member member) {
        if (!Objects.equals(this.member.getId(), member.getId())) {
            throw new CartItemException.IllegalMember(this, member);
        }
    }

    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isOnSale() {
        return this.discountPrice != 0;
    }

    public void setDiscountPrice(int discountPrice) {
        this.discountPrice = discountPrice;
    }

    public int getDiscountedPrice() {
        return this.product.getPrice() - this.discountPrice;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", discountPrice=" + discountPrice +
                ", product=" + product +
                ", member=" + member +
                '}';
    }
}