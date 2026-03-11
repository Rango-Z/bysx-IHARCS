package com.bysx.datav.web.dto;

import java.util.List;
import java.util.Map;

public record UsageStatResponse(
        List<String> days,
        Map<String, List<Double>> deviceHoursByDay
) {
}

