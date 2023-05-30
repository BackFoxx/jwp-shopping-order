package cart.discountpolicy.application;

import cart.cart.Cart;
import cart.cart.domain.cartitem.CartItem;
import cart.discountpolicy.discountcondition.DiscountCondition;
import cart.discountpolicy.discountcondition.DiscountTarget;
import cart.discountpolicy.discountcondition.DiscountUnit;
import cart.member.Member;
import cart.product.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountPolicyServiceTest {
    private DiscountPolicyService discountPolicyService;
    private DiscountPolicyRepository discountPolicyRepository;

    @BeforeEach
    void setup() {
        this.discountPolicyRepository = new DiscountPolicyRepository();
        this.discountPolicyService = new DiscountPolicyService(
                discountPolicyRepository
        );
    }

    @Test
    @DisplayName("특정 상품에 할인을 적용하겠다는 선언, 할인을 적용할 상품의 목록, 할인 방식, 할인 정도를 넘겨주면 할인 정책이 등록돼요.")
    void saveDiscountPolicyForSpecific() {
        final var discountCondition = DiscountCondition.makeConditionForSpecificProducts(List.of(1L, 2L, 3L), DiscountUnit.PERCENTAGE, 50);
        final var discountPolicyId = discountPolicyService.savePolicy(discountCondition);
        assertThat(discountPolicyId).isNotNull();
    }

    @Test
    @DisplayName("배송비에 할인을 적용하겠다는 선언, 할인 방식, 할인 정도를 넘겨주면 할인 정책이 등록돼요.")
    void saveDiscountPolicyForDelivery() {
        final var discountCondition = DiscountCondition.from(DiscountTarget.DELIVERY, DiscountUnit.ABSOLUTE, 1500);
        final var discountPolicyId = discountPolicyService.savePolicy(discountCondition);
        assertThat(discountPolicyId).isNotNull();
    }

    @Test
    @DisplayName("모든 상품에 할인을 적용하겠다는 선언, 할인 방식, 할인 정도를 넘겨주면 할인 정책이 등록돼요.")
    void saveDiscountPolicyForAll() {
        final var discountCondition = DiscountCondition.from(DiscountTarget.ALL, DiscountUnit.PERCENTAGE, 60);
        final var discountPolicyId = discountPolicyService.savePolicy(discountCondition);
        assertThat(discountPolicyId).isNotNull();
    }

    @Test
    @DisplayName("치킨에만 적용되는 할인정책을 적용하면 치킨 가격만 할인돼 있어요.")
    void discountForChicken() {
        final var cart = getTestCart();

        final var discountCondition = DiscountCondition.makeConditionForSpecificProducts(List.of(1L), DiscountUnit.PERCENTAGE, 50);
        final var discountPolicyId = discountPolicyService.savePolicy(discountCondition);

        final var discountPolicy = discountPolicyRepository.findById(discountPolicyId);
        discountPolicy.discount(cart);

        assertThat(cart.getCartItems())
                .extracting(CartItem::getDiscountPrice)
                .containsExactly(10_000, 0);
        assertThat(cart.getDeliveryPrice())
                .isEqualTo(3_000);
    }

    @Test
    @DisplayName("배송비에만 적용되는 할인정책을 적용하면 배송비 가격만 할인돼 있어요.")
    void discountForDelivery() {
        final var cart = getTestCart();

        final var discountCondition = DiscountCondition.from(DiscountTarget.DELIVERY, DiscountUnit.ABSOLUTE, 1500);
        final var discountPolicyId = discountPolicyService.savePolicy(discountCondition);

        final var discountPolicy = discountPolicyRepository.findById(discountPolicyId);
        discountPolicy.discount(cart);

        assertThat(cart.getCartItems())
                .extracting(CartItem::getDiscountPrice)
                .containsExactly(0, 0);
        assertThat(cart.getDeliveryPrice())
                .isEqualTo(1500);
    }

    @Test
    @DisplayName("전체 가격에 적용되는 할인정책을 적용하면 전체 가격이 할인돼 있어요.")
    void discountForAllProduct() {
        final var cart = getTestCart();

        final var discountCondition = DiscountCondition.from(DiscountTarget.ALL, DiscountUnit.PERCENTAGE, 60);
        final var discountPolicyId = discountPolicyService.savePolicy(discountCondition);

        final var discountPolicy = discountPolicyRepository.findById(discountPolicyId);
        discountPolicy.discount(cart);

        assertThat(cart.getCartItems())
                .extracting(CartItem::getDiscountPrice)
                .containsExactly(8_000, 12_000);
        assertThat(cart.getDeliveryPrice())
                .isEqualTo(3_000);
    }

    public Cart getTestCart() {
        final var 백여우 = new Member(1L, "fox@gmail.com", "1234");
        final var 피자 = new Product(1L, "피자", 20_000, "img");
        final var 치킨 = new Product(2L, "치킨", 30_000, "img");

        final var 백여우가담은피자 = new CartItem(1L, 3, 피자, 백여우);
        final var 백여우가담은치킨 = new CartItem(2L, 2, 치킨, 백여우);

        return new Cart(
                List.of(백여우가담은피자, 백여우가담은치킨),
                List.of()
        );
    }
}