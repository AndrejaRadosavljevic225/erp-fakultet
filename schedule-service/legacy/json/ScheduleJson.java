package com.aradosavljevic.schedule_service.domain.json;

import java.util.List;

public class ScheduleJson {
    public List<RoomJson> prostorije;
    public List<ReservationJson> termini;
    public String vaziOd;
    public String vaziDo;
    public List<String> slobodniDani;

    public ScheduleJson(List<RoomJson> prostorije, List<ReservationJson> termini, String vaziOd, String vaziDo, List<String> slobodniDani) {
        this.prostorije = prostorije;
        this.termini = termini;
        this.vaziOd = vaziOd;
        this.vaziDo = vaziDo;
        this.slobodniDani = slobodniDani;
    }
}
