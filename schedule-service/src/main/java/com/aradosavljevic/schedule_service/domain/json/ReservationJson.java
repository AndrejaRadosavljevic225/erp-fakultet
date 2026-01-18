package com.aradosavljevic.schedule_service.domain.json;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;

public class ReservationJson {
    public RoomJson prostorija;
    public String vremeOd;
    public String vremeDo;
    public String datum;
    public int brojTermina;
    public int dan;
    public Map<String,Object> atributi;

    public ReservationJson(RoomJson prostorija, LocalTime vremeOd, LocalTime vremeDo, LocalDate datum, int brojTermina, int dan, Map<String, Object> atributi) {
        this.prostorija = prostorija;
        this.vremeOd = vremeOd.toString();
        this.vremeDo = vremeDo.toString();
        this.datum = datum.toString();
        this.brojTermina = brojTermina;
        this.dan = dan;
        this.atributi = atributi;
    }
}
