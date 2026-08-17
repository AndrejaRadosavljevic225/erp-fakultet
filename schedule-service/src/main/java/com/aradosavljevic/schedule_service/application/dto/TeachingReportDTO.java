package com.aradosavljevic.schedule_service.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Izvestaj fonda casova za profesora u skolskoj godini:
 * kvota (norma) vs. odradjeno vs. odstupanje.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeachingReportDTO {
    private Long workerId;
    private Long schoolYearId;
    private Integer requiredHours;   // kvota iz norme
    private double realizedHours;    // odradjeno (iz odrzanih termina)
    private double deviation;        // odradjeno - kvota
    private double extraHours;       // koliko preko kvote (0 ako nije prekoraceno)
    private boolean fulfilled;       // da li je kvota dostignuta
}
