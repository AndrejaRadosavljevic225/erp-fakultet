package com.aradosavljevic.erp_common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollProcessedEvent implements Serializable {
    private UUID payrollId;
    private UUID workerId;
    private int month;
    private int year;
    private BigDecimal grossAmount;
    private BigDecimal netAmount;
    private LocalDateTime processedAt;
}
