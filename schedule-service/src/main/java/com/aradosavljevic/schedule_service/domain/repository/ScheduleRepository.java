package com.aradosavljevic.schedule_service.domain.repository;

import com.aradosavljevic.schedule_service.domain.entity.Schedule;
import rs.raf.sk.api.IRasporedFactory;
import rs.raf.sk.api.Prostorija;
import rs.raf.sk.api.Raspored;
import rs.raf.sk.api.Termin;

import java.time.LocalDate;
import java.util.List;

public class ScheduleRepository  implements IRasporedFactory {
    @Override
    public Raspored create() {
        return new Schedule();
    }

    @Override
    public Raspored create(List<Prostorija> prostorije, List<Termin> termini) {
        return new Schedule(prostorije,termini);
    }

    @Override
    public Raspored create(List<Prostorija> prostorije, List<Termin> termini, LocalDate datumOd, LocalDate datumDo) {
        return new Schedule(prostorije,termini,datumOd,datumDo);
    }
}
