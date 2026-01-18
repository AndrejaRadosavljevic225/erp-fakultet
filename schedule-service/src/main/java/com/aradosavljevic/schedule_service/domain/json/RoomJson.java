package com.aradosavljevic.schedule_service.domain.json;

import java.util.Map;

public class RoomJson {
    public String naziv;
    public Map<String, Object> atributi;

    public RoomJson(String naziv, Map<String, Object> atributi) {
        this.naziv = naziv;
        this.atributi = atributi;
    }
}
