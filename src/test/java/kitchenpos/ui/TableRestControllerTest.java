package kitchenpos.ui;

import static kitchenpos.fixture.OrderTableFixture.createOrderTable;
import static kitchenpos.fixture.OrderTableFixture.createOrderTableRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import kitchenpos.application.TableService;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.OrderTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

public class TableRestControllerTest extends AbstractControllerTest {
    @Autowired
    private TableService tableService;

    @Autowired
    private OrderTableDao orderTableDao;

    @DisplayName("주문 테이블을 생성할 수 있다.")
    @Test
    void create() throws Exception {
        OrderTable orderTableRequest = createOrderTableRequest(true, 0);

        mockMvc.perform(post("/api/tables")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(orderTableRequest))
        )
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath(("$.tableGroupId")).doesNotExist())
            .andExpect(jsonPath("$.numberOfGuests").value(orderTableRequest.getNumberOfGuests()))
            .andExpect(jsonPath("$.empty").value(orderTableRequest.isEmpty()));
    }

    @DisplayName("주문 테이블 목록을 조회할 수 있다.")
    @Test
    void list() throws Exception {
        List<OrderTable> orderTables = orderTableDao.findAll();

        String json = mockMvc.perform(get("/api/tables"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        List<OrderTable> response = objectMapper.readValue(json,
            objectMapper.getTypeFactory().constructCollectionType(List.class, OrderTable.class));

        assertThat(response).usingFieldByFieldElementComparator().containsAll(orderTables);
    }

    @DisplayName("주문 테이블의 빈 테이블 여부를 변경할 수 있다.")
    @Test
    void changeEmpty() throws Exception {
        OrderTable orderTable = orderTableDao.save(createOrderTable(null, true, 0, null));
        OrderTable orderTableRequest = createOrderTableRequest(false, 1);

        mockMvc.perform(put("/api/tables/{orderTableId}/empty", orderTable.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(orderTableRequest))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderTable.getId()))
            .andExpect(jsonPath(("$.tableGroupId")).value(orderTable.getTableGroupId()))
            .andExpect(jsonPath("$.numberOfGuests").value(orderTable.getNumberOfGuests()))
            .andExpect(jsonPath("$.empty").value(orderTableRequest.isEmpty()));
    }

    @DisplayName("주문 테이블의 손님 수를 변경할 수 있다.")
    @Test
    void changeOrderStatus() throws Exception {
        OrderTable orderTable = orderTableDao.save(createOrderTable(null, false, 2, null));
        OrderTable orderTableRequest = createOrderTableRequest(true, 1);

        mockMvc.perform(put("/api/tables/{orderTableId}/number-of-guests", orderTable.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writeValueAsString(orderTableRequest))
        )
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(orderTable.getId()))
            .andExpect(jsonPath(("$.tableGroupId")).value(orderTable.getTableGroupId()))
            .andExpect(jsonPath("$.numberOfGuests").value(orderTableRequest.getNumberOfGuests()))
            .andExpect(jsonPath("$.empty").value(orderTable.isEmpty()));
    }
}