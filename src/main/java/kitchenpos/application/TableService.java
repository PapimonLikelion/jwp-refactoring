package kitchenpos.application;

import kitchenpos.domain.OrderTable;
import kitchenpos.repository.OrderTableRepository;
import kitchenpos.ui.request.OrderTableEmptyRequest;
import kitchenpos.ui.request.OrderTableNumberOfGuestRequest;
import kitchenpos.ui.request.OrderTableRequest;
import kitchenpos.ui.response.OrderTableResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TableService {
    private final OrderTableRepository orderTableRepository;

    public TableService(final OrderTableRepository orderTableRepository) {
        this.orderTableRepository = orderTableRepository;
    }

    @Transactional
    public OrderTableResponse create(final OrderTableRequest orderTableRequest) {
        OrderTable newOrderTable = new OrderTable.Builder()
                .tableGroup(null)
                .numberOfGuests(orderTableRequest.getNumberOfGuests())
                .empty(orderTableRequest.isEmpty())
                .build();

        orderTableRepository.save(newOrderTable);
        return OrderTableResponse.of(newOrderTable);
    }

    public List<OrderTableResponse> list() {
        final List<OrderTable> orderTables = orderTableRepository.findAllFetchJoinTableGroup();
        return OrderTableResponse.toList(orderTables);
    }

    @Transactional
    public OrderTableResponse changeEmpty(final Long orderTableId, final OrderTableEmptyRequest orderTableEmptyRequest) {
        final OrderTable foundOrderTable = findOrderTable(orderTableId);
        foundOrderTable.changeEmpty(orderTableEmptyRequest.getEmpty());
        return OrderTableResponse.of(foundOrderTable);
    }

    @Transactional
    public OrderTableResponse changeNumberOfGuests(final Long orderTableId, final OrderTableNumberOfGuestRequest orderTableNumberOfGuestRequest) {
        final OrderTable foundOrderTable = findOrderTable(orderTableId);
        foundOrderTable.changeNumberOfGuests(orderTableNumberOfGuestRequest.getNumberOfGuests());
        return OrderTableResponse.of(foundOrderTable);
    }

    private OrderTable findOrderTable(Long orderTableId) {
        return orderTableRepository.findById(orderTableId)
                .orElseThrow(IllegalArgumentException::new);
    }
}
