package kitchenpos.application;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.TableDao;
import kitchenpos.dao.TableGroupDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.Table;
import kitchenpos.domain.TableGroup;
import kitchenpos.dto.TableGroupCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
public class TableGroupService {

    private final OrderDao orderDao;
    private final TableDao tableDao;
    private final TableGroupDao tableGroupDao;

    public TableGroupService(final OrderDao orderDao, final TableDao tableDao, final TableGroupDao tableGroupDao) {
        this.orderDao = orderDao;
        this.tableDao = tableDao;
        this.tableGroupDao = tableGroupDao;
    }

    @Transactional
    public TableGroup create(final TableGroupCreateRequest request) {
        final List<Long> tableIds = request.getTableIds();

        if (CollectionUtils.isEmpty(tableIds) || tableIds.size() < 2) {
            throw new IllegalArgumentException();
        }
        final List<Table> savedTables = tableDao.findAllByIdIn(tableIds);

        if (tableIds.size() != savedTables.size()) {
            throw new IllegalArgumentException();
        }
        for (final Table savedTable : savedTables) {
            if (!savedTable.isEmpty() || Objects.nonNull(savedTable.getTableGroupId())) {
                throw new IllegalArgumentException();
            }
        }
        TableGroup tableGroup = new TableGroup();
        tableGroup.setTables(savedTables);
        tableGroup.setCreatedDate(LocalDateTime.now());

        final TableGroup savedTableGroup = tableGroupDao.save(tableGroup);

        final Long tableGroupId = savedTableGroup.getId();
        for (final Table savedTable : savedTables) {
            savedTable.putInGroup(tableGroupId);
            tableDao.save(savedTable);
        }
        savedTableGroup.setTables(savedTables);

        return savedTableGroup;
    }

    @Transactional
    public void ungroup(final Long tableGroupId) {
        final List<Table> tables = tableDao.findAllByTableGroupId(tableGroupId);

        final List<Long> orderTableIds = tables.stream()
            .map(Table::getId)
            .collect(Collectors.toList());

        if (isNotMealOver(orderTableIds)) {
            throw new IllegalArgumentException();
        }
        for (final Table table : tables) {
            table.excludeFromGroup();
            tableDao.save(table);
        }
    }

    private boolean isNotMealOver(List<Long> orderTableIds) {
        return orderDao.existsByTableIdInAndOrderStatusIn(
            orderTableIds, Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name()));
    }
}
