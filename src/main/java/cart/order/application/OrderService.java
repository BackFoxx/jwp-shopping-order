package cart.order.application;

import cart.cart.application.CartRepository;
import cart.cart.domain.cartitem.CartItem;
import cart.coupon.application.CouponService;
import cart.member.Member;
import cart.order.Order;
import cart.order.OrderCoupon;
import cart.order.OrderItem;
import cart.order.presentation.OrderCouponRequest;
import cart.order.presentation.OrderRequest;
import cart.order.presentation.OrderResponse;
import cart.order.presentation.OrdersResponse;
import cart.sale.SaleService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final CartRepository cartRepository;
    private final SaleService saleService;
    private final CouponService couponService;
    private final OrderRepository orderRepository;

    public OrderService(CartRepository cartRepository, SaleService saleService, CouponService couponService, OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.saleService = saleService;
        this.couponService = couponService;
        this.orderRepository = orderRepository;
    }

    public Long order(Member member, OrderRequest orderRequest) {
        // 1. cart 찾아오기
        final var cart = cartRepository.findCart(member);

        // 2. cart에 Sale 적용하기
        saleService.applySale(cart);

        // 3. cart에 Coupon 적용하기
        final var couponIds = orderRequest.getCoupons()
                .stream().map(OrderCouponRequest::getId)
                .collect(Collectors.toList());
        for (Long couponId : couponIds) {
            couponService.applyCoupon(couponId, cart);
        }

        // 4. OrderItems로 변환하기
        final var cartItems = cart.getCartItems();
        final var orderItems = new ArrayList<OrderItem>();
        for (CartItem cartItem : cartItems) {
            if (orderRequest.isCartItemOrdered(cartItem.getId())) {
                orderItems.add(convertCartItemToOrderItem(cartItem, orderRequest));
            }
        }

        // 5. 쿠폰 정보를 OrderCoupons로 변환하기
        final var orderCoupons = convertCouponsToOrderCoupons(couponIds);

        // 6. Order로 만들어 저장하기
        final var order = new Order(
                orderItems,
                orderCoupons,
                cart.getDeliveryPrice(),
                Timestamp.valueOf(LocalDateTime.now()),
                member.getId()
        );

        return orderRepository.save(order);

        // TODO: 주문한 만큼 장바구니에서 빼기

        // TODO: 주문할 때 사용한 쿠폰은 사용자에게서 빼기
    }

    private List<OrderCoupon> convertCouponsToOrderCoupons(List<Long> couponIds) {
        final var coupons = couponIds.stream()
                .map(couponService::findById)
                .collect(Collectors.toList());
        return coupons
                .stream().map(coupon -> new OrderCoupon(coupon.getId(), coupon.getName()))
                .collect(Collectors.toList());
    }

    private OrderItem convertCartItemToOrderItem(CartItem cartItem, OrderRequest orderRequest) {
        final var quantity = orderRequest.findQuantityByCartItemId(cartItem.getId());
        return new OrderItem(
                cartItem.getProduct().getId(),
                cartItem.getProduct().getName(),
                (cartItem.getProduct().getPrice() - cartItem.getDiscountPrice()) * quantity,
                quantity,
                cartItem.getProduct().getImageUrl()
        );
    }

    public OrdersResponse findOrderHistories(Long memberId) {
        final var orders = orderRepository.findAllByMemberId(memberId);
        return OrdersResponse.from(orders);
    }

    public OrderResponse findOrderHistory(Long orderId) {
        final var order = orderRepository.findById(orderId);
        return OrderResponse.from(order);
    }
}