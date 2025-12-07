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
public class RoomBookedEvent implements Serializable {
    private UUID bookingId;
    private UUID roomId;
    private UUID workerId;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String teachingType;
    private LocalDateTime bookedAt;
}
