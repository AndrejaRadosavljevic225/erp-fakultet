package rs.raf.sk.api;

import java.util.Map;

public interface IProstorijaFactory {
    public abstract Prostorija create(String naziv, Map<String, Object> atributi);

    public abstract Prostorija create(String naziv);
}
