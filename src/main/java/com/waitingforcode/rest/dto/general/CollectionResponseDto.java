package com.waitingforcode.rest.dto.general;

import com.google.common.base.MoreObjects;

import java.util.List;

public class CollectionResponseDto<T> {

    private long allElements;

    private List<T> items;

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public long getAllElements() {
        return allElements;
    }

    public void setAllElements(long allElements) {
        this.allElements = allElements;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("allElements", allElements).add("items", items).toString();
    }
}
