package com.bysx.datav.web.dto;

import java.util.List;

public record EnergyPieResponse(
        List<Item> items
) {
    public record Item(String deviceId, String name, double kwh) {}
}

