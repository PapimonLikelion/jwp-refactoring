package kitchenpos.domain;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;
import java.util.List;
import java.util.Objects;

@Embeddable
public class OrderTables {

    @OneToMany(mappedBy = "tableGroup")
    private List<OrderTable> orderTables;

    public OrderTables() {
    }

    public OrderTables(List<OrderTable> orderTables) {
        this.orderTables = orderTables;
    }

    public void registerTableGroup(TableGroup tableGroup) {
        for (OrderTable orderTable : orderTables) {
            validateRegisterTableGroupPossible(orderTable);
            orderTable.registerTableGroup(tableGroup);
            orderTable.makeNotEmpty();
        }
    }

    private void validateRegisterTableGroupPossible(OrderTable orderTable) {
        if (!orderTable.isEmpty() || Objects.nonNull(orderTable.getTableGroup())) {
            throw new IllegalArgumentException();
        }
    }

    public List<OrderTable> getOrderTables() {
        return orderTables;
    }

    public void checkSameSize(int size) {
        if (size != orderTables.size()) {
            throw new IllegalArgumentException();
        }
    }
}
