package kitchenpos.table.application;

import kitchenpos.exception.BadRequestException;
import kitchenpos.exception.ErrorType;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.domain.OrderTables;
import kitchenpos.table.domain.TableGroup;
import kitchenpos.table.domain.repository.OrderTableRepository;
import kitchenpos.table.domain.repository.TableGroupRepository;
import kitchenpos.table.ui.request.OrderTableIdRequest;
import kitchenpos.table.ui.request.TableGroupRequest;
import kitchenpos.table.ui.response.TableGroupResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TableGroupService {
    private final OrderTableRepository orderTableRepository;
    private final TableGroupRepository tableGroupRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public TableGroupService(
            final OrderTableRepository orderTableRepository,
            final TableGroupRepository tableGroupRepository,
            final ApplicationEventPublisher applicationEventPublisher
    ) {
        this.orderTableRepository = orderTableRepository;
        this.tableGroupRepository = tableGroupRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public TableGroupResponse create(final TableGroupRequest tableGroupRequest) {
        final TableGroup tableGroup = createTableGroup();
        final OrderTables orderTables = createOrderTables(tableGroupRequest, tableGroup);
        return TableGroupResponse.of(tableGroup, orderTables);
    }

    private TableGroup createTableGroup() {
        final TableGroup tableGroup = new TableGroup.Builder()
                .createdDate(LocalDateTime.now())
                .build();
        return tableGroupRepository.save(tableGroup);
    }

    private OrderTables createOrderTables(TableGroupRequest tableGroupRequest, TableGroup tableGroup) {
        final OrderTables orderTables = findOrderTables(tableGroupRequest.getOrderTables());
        orderTables.registerTableGroup(tableGroup);
        final List<OrderTable> savedOrderTables = orderTableRepository.saveAll(orderTables.getOrderTables());
        return new OrderTables(savedOrderTables);
    }

    private OrderTables findOrderTables(List<OrderTableIdRequest> orderTableIdRequests) {
        if (CollectionUtils.isEmpty(orderTableIdRequests) || orderTableIdRequests.size() < 2) {
            throw new BadRequestException(ErrorType.TABLE_GROUP_ERROR);
        }

        final List<Long> orderTableIds = orderTableIdRequests.stream()
                .map(OrderTableIdRequest::getId)
                .collect(Collectors.toList());

        final OrderTables foundOrderTables = new OrderTables(orderTableRepository.findAllById(orderTableIds));
        foundOrderTables.checkSameSize(orderTableIdRequests.size());
        return foundOrderTables;
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        final TableGroup tableGroup = tableGroupRepository.findById(tableGroupId)
                .orElseThrow(() -> new BadRequestException(ErrorType.TABLE_NOT_FOUND));

        final List<OrderTable> orderTables = orderTableRepository.findAllByTableGroup(tableGroup);
        for (OrderTable orderTable : orderTables) {
            applicationEventPublisher.publishEvent(new OrdersCompletedCheckEvent(orderTable.getId()));
            orderTable.ungroupFromTableGroup();
        }
    }
}
