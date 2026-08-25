package com.kfpcl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusBreakdownDto {

    private long pending;
    private long confirmed;
    private long processing;
    private long shipped;
    private long delivered;
    private long cancelled;
    private long returned;
    private Map<String, Long> statusCounts;
}
