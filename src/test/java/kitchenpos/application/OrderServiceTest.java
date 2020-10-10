package kitchenpos.application;

import static kitchenpos.utils.TestObjects.*;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import kitchenpos.domain.Menu;
import kitchenpos.domain.MenuGroup;
import kitchenpos.domain.MenuProduct;
import kitchenpos.domain.Order;
import kitchenpos.domain.OrderLineItem;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.domain.Product;
import kitchenpos.domain.TableGroup;

class OrderServiceTest extends ServiceTest {

    @Autowired
    private OrderService orderService;

    private TableGroup tableGroup;

    private OrderTable orderTable;

    private OrderLineItem orderLineItem;

    @BeforeEach
    void setUp() {
        tableGroup = tableGroupDao.save(createTableGroup());
        orderTable = orderTableDao.save(createOrderTable(tableGroup.getId(), 5, false));

        final Product product = productDao.save(createProduct("매콤치킨", BigDecimal.valueOf(16000)));
        final MenuGroup menuGroup = menuGroupDao.save(createMenuGroup("이십마리메뉴"));
        final MenuProduct menuProduct = menuProductDao.save(creatMenuProduct(1L, product.getId(), 1L));
        final Menu menu = menuDao.save(createMenu("후라이드치킨", BigDecimal.valueOf(16000), menuGroup.getId(), Collections.singletonList(menuProduct)));
        orderLineItem = createOrderLineItem(menu.getId(), 1L);
    }

    @DisplayName("create: 주문 생성")
    @Test
    void create() {
        final Order order = createOrder(orderTable.getId(), null, Collections.singletonList(orderLineItem));
        final Order actual = orderService.create(order);

        assertThat(actual).isNotNull();
        assertThat(actual.getOrderTableId()).isEqualTo(orderTable.getId());
        assertThat(actual.getOrderStatus()).isEqualTo(OrderStatus.COOKING.name());
        assertThat(actual.getOrderLineItems()).isNotEmpty();
    }

    @DisplayName("create: 주문 항목이 비어있을 때 예외 처리")
    @Test
    void create_IfOrderLineItemEmpty_Exception() {
        final Order order = createOrder(orderTable.getId(), null, Collections.emptyList());

        assertThatThrownBy(() -> orderService.create(order))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("create: 주문 항목의 메뉴와 메뉴에서 조회한 것이 다를 때 예외 처리")
    @Test
    void create_IfOrderLineItemNotSameMenus_Exception() {
        orderLineItem.setMenuId(0L);
        final Order order = createOrder(orderTable.getId(), null, Collections.singletonList(orderLineItem));

        assertThatThrownBy(() -> orderService.create(order))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("create: 주문 테이블이 없을 때 예외 처리")
    @Test
    void create_IfOrderTableDoesNotExist_Exception() {
        final Order order = createOrder(0L, null, Collections.singletonList(orderLineItem));

        assertThatThrownBy(() -> orderService.create(order))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("create: 주문 테이블이 비어 있을 때 예외 처리")
    @Test
    void create_IfOrderTableEmpty_Exception() {
        final OrderTable emptyOrderTable = orderTableDao.save(createOrderTable(tableGroup.getId(), 5, true));
        final Order order = createOrder(emptyOrderTable.getId(), null, Collections.singletonList(orderLineItem));

        assertThatThrownBy(() -> orderService.create(order))
        .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("list: Order 전체 조회")
    @Test
    void list() {
        final Order order = createOrder(orderTable.getId(), null, Collections.singletonList(orderLineItem));
        orderService.create(order);

        final List<Order> orders = orderService.list();

        assertThat(orders).isNotEmpty();
    }

    @DisplayName("changeOrderStatus: OrderStatus를 변경")
    @Test
    void changeOrderStatus() {
        final Order order = orderService.create(createOrder(orderTable.getId(), null, Collections.singletonList(orderLineItem)));
        final Order orderStatusMeal = createOrder(null, "MEAL", null);

        final Order actual = orderService.changeOrderStatus(order.getId(), orderStatusMeal);

        assertThat(actual.getOrderStatus()).isEqualTo(OrderStatus.MEAL.name());
    }
}