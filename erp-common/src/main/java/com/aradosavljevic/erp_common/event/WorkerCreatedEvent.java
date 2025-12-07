package com.aradosavljevic.erp_common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerCreatedEvent implements Serializable {
    private UUID workerId;
    private String firstName;
    private String lastName;
    private String email;
    private String employmentType;
    private LocalDateTime createdAt;
}
