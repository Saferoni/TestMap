package com.safercript.testmap.entity;

import java.util.LinkedList;

public class ListAddresses {
    LinkedList<LatLngAddress> list;

    public ListAddresses(LinkedList<LatLngAddress> list) {
        this.list = list;
    }

    public LinkedList<LatLngAddress> getList() {
        return list;
    }

    public void setList(LinkedList<LatLngAddress> list) {
        this.list = list;
    }
}
