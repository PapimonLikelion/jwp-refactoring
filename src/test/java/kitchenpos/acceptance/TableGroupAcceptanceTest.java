package kitchenpos.acceptance;

import kitchenpos.domain.Order;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.TableGroup;
import kitchenpos.ui.request.OrderTableIdRequest;
import kitchenpos.ui.request.TableGroupRequest;
import kitchenpos.ui.response.TableGroupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("테이블 그룹 관련 기능")
class TableGroupAcceptanceTest extends AcceptanceTest {

    TableGroup 테이블_그룹1;
    OrderTable 주문_테이블1;
    OrderTable 주문_테이블2;

    Order 주문1 = new Order();
    Order 주문2 = new Order();

    @BeforeEach
    void setUp() {
        주문_테이블1 = new OrderTable.Builder()
                .numberOfGuests(4)
                .empty(true)
                .build();

        주문_테이블2 = new OrderTable.Builder()
                .numberOfGuests(2)
                .empty(true)
                .build();

        테이블_그룹1 = new TableGroup.Builder()
                .createdDate(LocalDateTime.now())
                .orderTables(Arrays.asList(주문_테이블1, 주문_테이블2))
                .build();
        tableGroupRepository.save(테이블_그룹1);
        orderTableRepository.save(주문_테이블1);
        orderTableRepository.save(주문_테이블2);

        주문1.setOrderTable(주문_테이블1);
        주문1.setOrderStatus(OrderStatus.COMPLETION);
        주문1.setOrderedTime(LocalDateTime.now());
        orderRepository.save(주문1);

        주문2.setOrderTable(주문_테이블2);
        주문2.setOrderStatus(OrderStatus.COMPLETION);
        주문2.setOrderedTime(LocalDateTime.now());
        orderRepository.save(주문2);
    }

    @DisplayName("통합 계산을 위해 개별 테이블을 그룹화하는 테이블 그룹을 생성한다")
    @Test
    void createTableGroup() {
        // given
        OrderTable 주문_테이블3 = new OrderTable.Builder()
                .numberOfGuests(100)
                .empty(true)
                .build();
        주문_테이블3 = orderTableRepository.save(주문_테이블3);

        OrderTable 주문_테이블4 = new OrderTable.Builder()
                .numberOfGuests(200)
                .empty(true)
                .build();
        주문_테이블4 = orderTableRepository.save(주문_테이블4);

        TableGroupRequest 테이블_그룹_요청 = new TableGroupRequest();
        OrderTableIdRequest 주문_테이블3_ID = new OrderTableIdRequest();
        주문_테이블3_ID.setId(주문_테이블3.getId());
        OrderTableIdRequest 주문_테이블4_ID = new OrderTableIdRequest();
        주문_테이블4_ID.setId(주문_테이블4.getId());
        테이블_그룹_요청.setOrderTables(Arrays.asList(주문_테이블3_ID, 주문_테이블4_ID));

        // when
        ResponseEntity<TableGroupResponse> response = testRestTemplate.postForEntity("/api/table-groups", 테이블_그룹_요청, TableGroupResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        TableGroupResponse 응답_테이블_그룹 = response.getBody();
        assertThat(응답_테이블_그룹.getOrderTables().get(0).getId()).isEqualTo(주문_테이블3.getId());
        assertThat(응답_테이블_그룹.getOrderTables().get(1).getId()).isEqualTo(주문_테이블4.getId());
    }

    @DisplayName("통합 계산을 위해 개별 테이블을 그룹화하는 테이블 그룹을 생성 시, 테이블이 최소 2개가 안되면 예외가 발생한다.")
    @Test
    void cannotCreateTableGroupWithSingleTable() {
        // given
        OrderTableIdRequest 단일_주문_테이블_요청 = new OrderTableIdRequest();
        단일_주문_테이블_요청.setId(주문_테이블1.getId());
        TableGroupRequest 유효하지_않은_테이블_그룹_요청 = new TableGroupRequest();
        유효하지_않은_테이블_그룹_요청.setOrderTables(Arrays.asList(단일_주문_테이블_요청));

        // when
        ResponseEntity<TableGroupResponse> response = testRestTemplate.postForEntity("/api/table-groups", 유효하지_않은_테이블_그룹_요청, TableGroupResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DisplayName("테이블 그룹 요청에 담긴 주문 테이블 ID가 중복된다면, 예외가 발생한다.")
    @Test
    void cannotCreateTableGroupWhenRequestOrderTableIdDuplicated() {
        // given
        OrderTable 주문_테이블3 = new OrderTable.Builder()
                .numberOfGuests(100)
                .empty(true)
                .build();
        주문_테이블3 = orderTableRepository.save(주문_테이블3);

        TableGroupRequest 테이블_그룹_요청 = new TableGroupRequest();
        OrderTableIdRequest 주문_테이블3_ID = new OrderTableIdRequest();
        주문_테이블3_ID.setId(주문_테이블3.getId());
        테이블_그룹_요청.setOrderTables(Arrays.asList(주문_테이블3_ID, 주문_테이블3_ID));

        // when
        ResponseEntity<TableGroupResponse> response = testRestTemplate.postForEntity("/api/table-groups", 테이블_그룹_요청, TableGroupResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DisplayName("그룹화 하려는 주문 테이블이 비어있지 않다면, 그룹화할 수 없다.")
    @Test
    void cannotCreateTableGroupWhenIsEmptyFalse() {
        // given
        OrderTable 주문_테이블3 = new OrderTable.Builder()
                .numberOfGuests(100)
                .empty(false)
                .build();
        주문_테이블3 = orderTableRepository.save(주문_테이블3);

        OrderTable 주문_테이블4 = new OrderTable.Builder()
                .numberOfGuests(200)
                .empty(true)
                .build();
        주문_테이블4 = orderTableRepository.save(주문_테이블4);

        TableGroupRequest 테이블_그룹_요청 = new TableGroupRequest();
        OrderTableIdRequest 주문_테이블3_ID = new OrderTableIdRequest();
        주문_테이블3_ID.setId(주문_테이블3.getId());
        OrderTableIdRequest 주문_테이블4_ID = new OrderTableIdRequest();
        주문_테이블4_ID.setId(주문_테이블4.getId());
        테이블_그룹_요청.setOrderTables(Arrays.asList(주문_테이블3_ID, 주문_테이블4_ID));

        // when
        ResponseEntity<TableGroupResponse> response = testRestTemplate.postForEntity("/api/table-groups", 테이블_그룹_요청, TableGroupResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DisplayName("그룹화 하려는 주문 테이블이 이미 그룹화되어 있다면, 그룹화할 수 없다.")
    @Test
    void cannotCreateTableGroupWhenOrderTableAlreayInGroup() {
        // given
        OrderTable 주문_테이블3 = new OrderTable.Builder()
                .numberOfGuests(100)
                .empty(true)
                .build();
        주문_테이블3 = orderTableRepository.save(주문_테이블3);

        TableGroupRequest 테이블_그룹_요청 = new TableGroupRequest();
        OrderTableIdRequest 주문_테이블1_ID = new OrderTableIdRequest();
        주문_테이블1_ID.setId(주문_테이블1.getId());
        OrderTableIdRequest 주문_테이블3_ID = new OrderTableIdRequest();
        주문_테이블3_ID.setId(주문_테이블3.getId());
        테이블_그룹_요청.setOrderTables(Arrays.asList(주문_테이블1_ID, 주문_테이블3_ID));

        // when
        ResponseEntity<TableGroupResponse> response = testRestTemplate.postForEntity("/api/table-groups", 테이블_그룹_요청, TableGroupResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DisplayName("통합 계산을 위해 개별 테이블을 그룹화하는 tableGroupId에 해당하는 테이블 그룹을 삭제한다")
    @Test
    void deleteTableGroup() {
        // when
        Long 테이블_그룹1_ID = 테이블_그룹1.getId();
        testRestTemplate.delete("/api/table-groups/" + 테이블_그룹1_ID);

        // then
        OrderTable 변경된_주문_테이블1 = orderTableRepository.findById(주문_테이블1.getId()).get();
        assertThat(변경된_주문_테이블1.getTableGroup()).isNull();

        OrderTable 변경된_주문_테이블2 = orderTableRepository.findById(주문_테이블2.getId()).get();
        assertThat(변경된_주문_테이블2.getTableGroup()).isNull();
    }

    @DisplayName("통합 계산을 위해 개별 테이블을 그룹화하는 tableGroupId에 해당하는 테이블 그룹을 삭제할 때, 테이블 그룹의 테이블들의 주문 상태가 COMPLETION이 아니면 에러가 발생한다.")
    @Test
    void cannotDeleteTableGroupWhenTableOrderIsNotCompletion() {
        // when
        Order 주문3 = new Order();
        주문3.setOrderTable(주문_테이블1);
        주문3.setOrderStatus(OrderStatus.COOKING);
        주문3.setOrderedTime(LocalDateTime.now());
        orderRepository.save(주문3);

        Long 테이블_그룹1_ID = 테이블_그룹1.getId();

        // when
        ResponseEntity<Void> response = testRestTemplate.exchange("/api/table-groups/" + 테이블_그룹1_ID,
                HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
