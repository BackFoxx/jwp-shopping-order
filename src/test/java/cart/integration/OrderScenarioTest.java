package cart.integration;

import cart.domain.Member;
import cart.dto.CartItemRequest;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("주문화면에서 일어나는 API 시나리오")
public class OrderScenarioTest extends ScenarioFixture {
    @Test
    void 사용자가_결제_페이지에_처음_접속한다() {
        사용자가_상품을_장바구니에_담는다(사용자1, new CartItemRequest(상품아이디1));
        사용자가_상품을_장바구니에_담는다(사용자1, new CartItemRequest(상품아이디2));

        final var 결과 = 사용자가_결제_페이지에_접속한다();
        assertThat(결과.statusCode()).isEqualTo(HttpStatus.OK.value());

        final var jsonPath = 결과.jsonPath();
        assertThat(jsonPath.getList("$.products")).hasSize(2);

        assertAll(
                "Body에 들어있는 첫번 째 상품 검증하기",
                () -> assertThat(jsonPath.getLong("$.products[0].productId")).isEqualTo(상품아이디1),
                () -> assertThat(jsonPath.getString("$.products[0].productName")).isEqualTo("치킨"),
                () -> assertThat(jsonPath.getLong("$.products[0].price")).isEqualTo(10_000),
                () -> assertThat(jsonPath.getString("$.products[0].imgUrl")).isEqualTo("http://example.com/chicken.jpg"),
                () -> assertThat(jsonPath.getBoolean("$.products[0].isOnSale")).isEqualTo(false),
                () -> assertThat(jsonPath.getLong("$.products[0].salePrice")).isEqualTo(0)
        );

        assertAll(
                "Body에 들어있는 두번 째 상품 검증하기",
                () -> assertThat(jsonPath.getLong("$.products[1].productId")).isEqualTo(상품아이디2),
                () -> assertThat(jsonPath.getString("$.products[1].productName")).isEqualTo("피자"),
                () -> assertThat(jsonPath.getLong("$.products[1].price")).isEqualTo(15_000),
                () -> assertThat(jsonPath.getString("$.products[1].imgUrl")).isEqualTo("http://example.com/pizza.jpg"),
                () -> assertThat(jsonPath.getBoolean("$.products[1].isOnSale")).isEqualTo(true),
                () -> assertThat(jsonPath.getLong("$.products[1].salePrice")).isEqualTo(5_000)
        );

        assertThat(jsonPath.getLong("deliveryPrice")).isEqualTo(3000);

        assertAll(
                "Body에 들어있는 첫번 째 쿠폰 검증",
                () -> assertThat(jsonPath.getLong("$.coupons[0].couponId")).isEqualTo(1),
                () -> assertThat(jsonPath.getString("$.coupons[0].couponName")).isEqualTo("10% 전체 할인 쿠폰")
        );
        assertAll(
                "Body에 들어있는 두번 째 쿠폰 검증",
                () -> assertThat(jsonPath.getLong("$.coupons[1].couponId")).isEqualTo(2),
                () -> assertThat(jsonPath.getString("$.coupons[1].couponName")).isEqualTo("20% 전체 할인 쿠폰")
        );
        assertAll(
                "Body에 들어있는 세번 째 쿠폰 검증",
                () -> assertThat(jsonPath.getLong("$.coupons[2].couponId")).isEqualTo(3),
                () -> assertThat(jsonPath.getString("$.coupons[3].couponName")).isEqualTo("배송비 무료 쿠폰")
        );
    }

    @Test
    @DisplayName("사용자가 적용할 쿠폰 목록을 체크한다.")
    void 사용자가_적용할_쿠폰_목록을_체크한다() {
        final var 요청바디 = Map.of("coupons",
                List.of(
                        Map.of("id", 1),
                        Map.of("id", 2)
                )
        );

        final var 결과 = given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(사용자1.getEmail(), 사용자1.getPassword())
                .body(요청바디)
                .when()
                .post("/payments/coupons")
                .then().log().all()
                .extract();

        final var jsonPath = 결과.jsonPath();

        assertAll(
                "쿠폰의 할인정책이 적용된 첫 번째 상품 검증",
                () -> assertThat(jsonPath.getLong("$.products[0].productId")).isEqualTo(1),
                () -> assertThat(jsonPath.getLong("$.products[0].originalPrice")).isEqualTo(1000),
                () -> assertThat(jsonPath.getLong("$.products[0].discountPrice")).isEqualTo(100)
        );

        assertAll(
                "쿠폰의 할인정책이 적용된 두 번째 상품 검증",
                () -> assertThat(jsonPath.getLong("$.products[1].productId")).isEqualTo(2),
                () -> assertThat(jsonPath.getLong("$.products[1].originalPrice")).isEqualTo(3000),
                () -> assertThat(jsonPath.getLong("$.products[2].discountPrice")).isEqualTo(600)
        );

        assertAll(
                "쿠폰의 할인정책이 적용된 배송비 검증",
                () -> assertThat(jsonPath.getLong("deliveryPrice.originalPrice")).isEqualTo(3000),
                () -> assertThat(jsonPath.getLong("deliveryPrice.discountPrice")).isEqualTo(3000)
        );
    }

    @Test
    void 사용자가_결제하기_바튼을_누른다() {
        final var 요청바디 = Map.of(
                "products",
                List.of(
                        Map.of("id", 1),
                        Map.of("id", 2)
                ),
                "coupons",
                List.of(
                        Map.of("id", 1),
                        Map.of("id", 2)
                )
        );

        given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(사용자1.getEmail(), 사용자1.getPassword())
                .body(요청바디)
                .when()
                .post("/payment")
                .then().log().all()
                .statusCode(HttpStatus.CREATED.value());
    }

    private ExtractableResponse<Response> 사용자가_결제_페이지에_접속한다() {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(사용자1.getEmail(), 사용자1.getPassword())
                .when()
                .get("/payment")
                .then()
                .log().all()
                .extract();
    }

    private ExtractableResponse<Response> 사용자가_상품을_장바구니에_담는다(Member member, CartItemRequest cartItemRequest) {
        return given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .auth().preemptive().basic(member.getEmail(), member.getPassword())
                .body(cartItemRequest)
                .when()
                .post("/cart-items")
                .then()
                .log().all()
                .extract();
    }
}