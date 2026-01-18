package rs.raf.sk.api;


import java.util.HashMap;
import java.util.Map;
/**
 * Ova klasa predstavlja prostor/mesto/medijum na kom se odrzava neki termin.
 */

public abstract class Prostorija {
    protected String naziv;
    protected Map<String, Object> atributi;
    /**
     * Ova funkcija kreira jednu instancu prostorije sa dodatim osobinama
     */

    public Prostorija(String naziv, Map<String, Object> atributi) {
        this.naziv = naziv;
        this.atributi = atributi;
    }
    /**
     * Ova funkcija kreira jednu instancu prostorije bez dodatnih osobina
     */

    public Prostorija(String naziv) {
        this.naziv = naziv;
        atributi = new HashMap<>();
    }

    /**
     * Ova funkcija omogucava dodavanje novih osobina prostoriji
     */

    public boolean dodajAtribut(String nazivAtributa, Object opis){
        if(atributi==null)atributi = new HashMap<>();
        if(atributi.containsKey(nazivAtributa)){
            if(atributi.get(nazivAtributa).equals(opis)){
                return false;
            }else{
                atributi.replace(nazivAtributa,opis);
                return true;
            }
        }else{
            atributi.put(nazivAtributa,opis);
        }
        return true;
    }
    /**
     * Ova funkcija omogucava brisanje neke osobina prostoriji
     */

    public void obrisiAtribut(String naziv){
        atributi.remove(naziv);
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public Map<String, Object> getAtributi() {
        return atributi;
    }

    public void setAtributi(Map<String, Object> atributi) {
        this.atributi = atributi;
    }
    public boolean daLiSadrziUslove(Map<String,Object> uslovi){
        /*
        for(String s:uslovi.keySet()){
            if(!atributi.containsKey(s)){
                return false;
            }else{
                if(!((String)atributi.get(s)).matches((String) uslovi.get(s))){
                    return false;
                }
            }
        }
        */
        return true;
    }
}
