package rs.raf.sk.api;

import java.time.LocalDate;
import java.util.List;

public interface IRasporedFactory {
    public abstract Raspored create();

    public abstract Raspored create(List<Prostorija> prostorije, List<Termin> termini);

    public abstract Raspored create(List<Prostorija> prostorije, List<Termin> termini, LocalDate datumOd, LocalDate datumDo);
}
