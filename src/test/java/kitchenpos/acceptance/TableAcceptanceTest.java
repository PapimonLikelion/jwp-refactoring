package kitchenpos.acceptance;

import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("테이블 관련 기능")
class TableAcceptanceTest extends AcceptanceTest {

    OrderTable 주문_테이블1 = new OrderTable();
    OrderTable 주문_테이블2 = new OrderTable();

    Order 요리중인_주문 = new Order();
    Order 식사중인_주문 = new Order();

    @BeforeEach
    void setUp() {
        주문_테이블1.setNumberOfGuests(4);
        주문_테이블1.setEmpty(false);
        주문_테이블1 = orderTableDao.save(주문_테이블1);

        주문_테이블2.setNumberOfGuests(2);
        주문_테이블2.setEmpty(false);
        주문_테이블2 = orderTableDao.save(주문_테이블2);

        요리중인_주문.setOrderStatus(OrderStatus.COOKING.name());
        요리중인_주문.setOrderTableId(주문_테이블2.getId());
        요리중인_주문.setOrderedTime(LocalDateTime.now());
        orderDao.save(요리중인_주문);

        식사중인_주문.setOrderStatus(OrderStatus.MEAL.name());
        식사중인_주문.setOrderTableId(주문_테이블2.getId());
        식사중인_주문.setOrderedTime(LocalDateTime.now());
        orderDao.save(식사중인_주문);
    }

    @DisplayName("매장에서 주문이 발생하는 테이블들에 대한 정보를 반환한다")
    @Test
    void getOrders() {
        // when
        ResponseEntity<OrderTable[]> response = testRestTemplate.getForEntity("/api/tables", OrderTable[].class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @DisplayName("매장에서 주문이 발생하는 테이블에 대한 정보를 추가한다")
    @Test
    void createOrder() {
        // given
        OrderTable 주문_테이블3 = new OrderTable();
        주문_테이블3.setNumberOfGuests(0);
        주문_테이블3.setEmpty(true);

        // when
        ResponseEntity<OrderTable> response = testRestTemplate.postForEntity("/api/tables", 주문_테이블3, OrderTable.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        OrderTable 응답된_주문_테이블 = response.getBody();
        assertThat(응답된_주문_테이블.getNumberOfGuests()).isZero();
        assertThat(응답된_주문_테이블.isEmpty()).isTrue();
    }

    @DisplayName("매장에서 주문이 발생하는 테이블 중 tableId에 해당하는 테이블의 empty 여부를 변경한다")
    @Test
    void changeEmptyStatus() {
        // given
        OrderTable 변경할_주문_테이블 = new OrderTable();
        변경할_주문_테이블.setEmpty(true);
        Long 주문_테이블1_ID = 주문_테이블1.getId();

        // when
        testRestTemplate.put("/api/tables/" + 주문_테이블1_ID + "/empty", 변경할_주문_테이블);

        // then
        OrderTable 변경된_주문_테이블1 = orderTableDao.findById(주문_테이블1_ID).get();
        assertThat(변경된_주문_테이블1.getId()).isEqualTo(주문_테이블1_ID);
        assertThat(변경된_주문_테이블1.isEmpty()).isTrue();
    }

    @DisplayName("매장에서 주문이 발생하는 테이블 중 tableId에 해당하는 테이블의 empty 여부를 변경시, 해당 테이블의 주문 상태가 모두 COMPLETION 아니라면 변경할 수 없다")
    @Test
    void cannotChangeEmptyStatus() {
        // given
        OrderTable 변경할_주문_테이블 = new OrderTable();
        변경할_주문_테이블.setEmpty(true);
        Long 주문_테이블2_ID = 주문_테이블2.getId();

        // when
        ResponseEntity<Void> response = testRestTemplate.exchange("/api/tables/" + 주문_테이블2_ID + "/empty",
                HttpMethod.PUT, new HttpEntity<>(변경할_주문_테이블), Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DisplayName("매장에서 주문이 발생하는 테이블 중 tableId에 해당하는 테이블의 number-of-guest를 변경한다")
    @Test
    void changeNumberOfGuests() {
        // given
        OrderTable 변경할_주문_테이블 = new OrderTable();
        변경할_주문_테이블.setNumberOfGuests(100);
        Long 주문_테이블1_ID = 주문_테이블1.getId();

        // when
        testRestTemplate.put("/api/tables/" + 주문_테이블1_ID + "/number-of-guests", 변경할_주문_테이블);

        // then
        OrderTable 변경된_주문_테이블1 = orderTableDao.findById(주문_테이블1_ID).get();
        assertThat(변경된_주문_테이블1.getId()).isEqualTo(주문_테이블1_ID);
        assertThat(변경된_주문_테이블1.getNumberOfGuests()).isEqualTo(100);
    }

    @DisplayName("매장에서 주문이 발생하는 테이블 중 tableId에 해당하는 테이블의 number-of-guest를 변경시, 음수이면 안된다")
    @Test
    void cannotChangeNumberOfGuestsToNegative() {
        // given
        OrderTable 변경할_주문_테이블 = new OrderTable();
        변경할_주문_테이블.setNumberOfGuests(-1000);
        Long 주문_테이블1_ID = 주문_테이블1.getId();

        // when
        ResponseEntity<Void> response = testRestTemplate.exchange("/api/tables/" + 주문_테이블1_ID + "/number-of-guests",
                HttpMethod.PUT, new HttpEntity<>(변경할_주문_테이블), Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DisplayName("매장에서 주문이 발생하는 테이블 중 tableId에 해당하는 테이블의 number-of-guest를 변경시, 이미 비워진 테이블이라면 안된다")
    @Test
    void cannotChangeNumberOfGuestsIfEmpty() {
        // given
        OrderTable 비워진_주문_테이블 = new OrderTable();
        비워진_주문_테이블.setNumberOfGuests(4);
        비워진_주문_테이블.setEmpty(true);
        비워진_주문_테이블 = orderTableDao.save(비워진_주문_테이블);

        OrderTable 변경할_주문_테이블 = new OrderTable();
        변경할_주문_테이블.setNumberOfGuests(1000);
        Long 비워진_주문_테이블_ID = 비워진_주문_테이블.getId();

        // when
        ResponseEntity<Void> response = testRestTemplate.exchange("/api/tables/" + 비워진_주문_테이블_ID + "/number-of-guests",
                HttpMethod.PUT, new HttpEntity<>(변경할_주문_테이블), Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
