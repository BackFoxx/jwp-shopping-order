package cart.order;

public class OrderCoupon {
    private final Long couponId;
    private final String couponName;

    public OrderCoupon(Long couponId, String couponName) {
        this.couponId = couponId;
        this.couponName = couponName;
    }

    public Long getCouponId() {
        return couponId;
    }

    public String getCouponName() {
        return couponName;
    }
}