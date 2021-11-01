package kitchenpos.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity
public class OrderTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private TableGroup tableGroup;

    private int numberOfGuests;
    private boolean empty;

    @Embedded
    private Orders orders;

    public OrderTable() {
    }

    private OrderTable(Builder builder) {
        this.id = builder.id;
        this.tableGroup = builder.tableGroup;
        this.numberOfGuests = builder.numberOfGuests;
        this.empty = builder.empty;
    }

    public void registerTableGroup(TableGroup tableGroup) {
        this.tableGroup = tableGroup;
    }

    public void ungroupFromTableGroup() {
        this.orders.checkAllOrderCompleted();
        this.tableGroup = null;
        this.empty = false;
    }

    public void changeEmpty(Boolean empty) {
        if (Objects.nonNull(this.tableGroup)) {
            throw new IllegalArgumentException();
        }
        this.orders.checkAllOrderCompleted();
        this.empty = empty;
    }

    public void changeNumberOfGuests(Integer numberOfGuests) {
        if (numberOfGuests < 0) {
            throw new IllegalArgumentException();
        }
        if (this.isEmpty()) {
            throw new IllegalArgumentException();
        }
        this.numberOfGuests = numberOfGuests;
    }

    public void makeNotEmpty() {
        this.empty = false;
    }

    public Long getId() {
        return id;
    }

    public TableGroup getTableGroup() {
        return tableGroup;
    }

    public Long getTableGroupId() {
        if (Objects.isNull(tableGroup)) {
            return null;
        }
        return tableGroup.getId();
    }

    public int getNumberOfGuests() {
        return numberOfGuests;
    }

    public boolean isEmpty() {
        return empty;
    }

    public static class Builder {
        private Long id;
        private TableGroup tableGroup;
        private int numberOfGuests;
        private boolean empty;

        public Builder() {
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder tableGroup(TableGroup tableGroup) {
            this.tableGroup = tableGroup;
            return this;
        }

        public Builder numberOfGuests(int numberOfGuests) {
            this.numberOfGuests = numberOfGuests;
            return this;
        }


        public Builder empty(boolean empty) {
            this.empty = empty;
            return this;
        }

        public OrderTable build() {
            return new OrderTable(this);
        }
    }
}
