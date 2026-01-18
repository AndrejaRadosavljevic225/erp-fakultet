package com.aradosavljevic.schedule_service.domain.entity;


import com.aradosavljevic.schedule_service.domain.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import rs.raf.sk.api.Prostorija;
import rs.raf.sk.api.Termin;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
public class Reservation extends Termin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long timeSlotId;
    private Long workerId;

    private String description;

    private boolean repeating;
    private Long number;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "reservation", orphanRemoval = true)
    private List<Tag> tags=new ArrayList<>();


    private BookingStatus status = BookingStatus.REQUESTED;

    public Reservation() {

    }
    public Reservation(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, LocalDate datum, Map<String, Object> osobine, Long id, Long timeSlotId, Long workerId, String description, boolean repeating, Long number, List<Tag> tags, BookingStatus status) {
        super(prostorija, vremeOd, vremeDo, datum, osobine);
        this.id = id;
        this.timeSlotId = timeSlotId;
        this.workerId = workerId;
        this.description = description;
        this.repeating = repeating;
        this.number = number;
        this.tags = tags;
        this.status = status;
    }
    public Reservation(Long id, Long timeSlotId, Long workerId, String description, boolean repeating, Long number, List<Tag> tags, BookingStatus status) {
        this.id = id;
        this.timeSlotId = timeSlotId;
        this.workerId = workerId;
        this.description = description;
        this.repeating = repeating;
        this.number = number;
        this.tags = tags;
        this.status = status;
    }

    public Reservation(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, LocalDate datum, Long id, Long timeSlotId, Long workerId, String description, boolean repeating, Long number, List<Tag> tags, BookingStatus status) {
        super(prostorija, vremeOd, vremeDo, datum);
        this.id = id;
        this.timeSlotId = timeSlotId;
        this.workerId = workerId;
        this.description = description;
        this.repeating = repeating;
        this.number = number;
        this.tags = tags;
        this.status = status;
    }

    public Reservation(Prostorija p, LocalTime vremeOd, LocalTime vremeDo, LocalDate datumOd, Map<String, Object> osobine, int i) {
    }

    public Reservation(Prostorija p, LocalTime start, LocalTime end, LocalDate datum) {
    }


    @Override
    public String toString() {
        return "";
    }
    @Override
    public int compareTo(Object o) {
        return 0;
    }


    @Override
    public void setProstorija(Prostorija prostorija) {
        super.setProstorija(prostorija);
        List<String> naslovi = new ArrayList<>();
        naslovi.addAll(osobine.keySet());
        for(String s: naslovi){
            prostorija.getAtributi().put(s,osobine.get(s));
        }
    }

    public int getBrojTermina() {
        return 0;
    }

    public long getDan() {
        return 1;
    }

    public void setDan(int i) {
    }
}
