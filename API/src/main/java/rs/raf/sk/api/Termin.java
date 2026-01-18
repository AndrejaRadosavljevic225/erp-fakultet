package rs.raf.sk.api;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
/**
 * Ova apstraktna klasa predstavlja termin izvrsavanja necega (casa, sastanka, projekcije,...).
 */

public abstract class Termin implements Comparable{

    protected Prostorija prostorija;
    protected LocalTime vremeOd;
    protected LocalTime vremeDo;
    protected LocalDate datum;
    protected Map<String, Object> osobine;

/**
 * Ova funkcija kreira jednu instancu termina sa dodatim osobinama
 */

    public Termin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, LocalDate datum, Map<String, Object> osobine) {
        this.prostorija = prostorija;
        this.vremeOd = vremeOd;
        this.vremeDo = vremeDo;
        this.datum = datum;
        this.osobine = osobine;
    }
    /**
     * Ova funkcija kreira jednu instancu termina bez dodatnih osobinama
     */

    public Termin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, LocalDate datum) {
        this.prostorija = prostorija;
        this.vremeOd = vremeOd;
        this.vremeDo = vremeDo;
        this.datum = datum;
        osobine = new HashMap<>();
    }

    public Termin() {
    }

    //public boolean odgovaraUslovima(HashMap<String,String>uslov){
      //  for(String s:uslov.)
        //return
    //}
    /**
     * Ova funkcija proveava da li postoji preklapanje izmedju dva termina
     * vraca:
     * 0 - ako nije isti datum u pitanju
     * 1 - ako je isti datum ali razlicita prostorija
     * //naredne vrednosti se podrazumevaju da je ista prostorija
     * 2 - termin t se nalazi u okviru naseg termina
     * 3 - termin t se poklapa sa delom kraja naseg termina
     * 4 - termin t se poklapa sa delom pocetka naseg termina
     * 5 - t je posle naseg termina
     * 6 - t je pre naseg termina
     */

    public int imaLiPreklapanja(Termin t){
        if(this.datum.compareTo(t.datum)!=0)return 0;
        if(this.prostorija.getNaziv().matches(t.prostorija.getNaziv())){
            if(!this.vremeOd.isBefore(t.vremeOd)){
                if(!this.vremeOd.isAfter(t.vremeDo)){
                    if(!this.vremeDo.isAfter(t.vremeDo)){
                        return 2;
                    }else{
                        return 3;
                    }
                }else{
                    return 5;
                }
            }else{
                if(!t.vremeOd.isAfter(this.vremeDo)){
                    return 4;
                }else{
                    return 6;
                }
            }
        }


        return 1;
    }

//dodatno ispraviti compareTo kako bi moglo da se koristi prilikom sortiranja
    @Override
    public int compareTo(Object o) {
        if(prostorija.getNaziv().compareTo(((Prostorija)o).getNaziv())!=0)return prostorija.getNaziv().compareTo(((Prostorija)o).getNaziv())%10;
        if(datum.compareTo(((Termin)o).getDatum())!=0)return datum.compareTo(((Termin)o).getDatum())%10*10;
        if(vremeOd.compareTo(((Termin) o).getVremeOd())!=0)return vremeOd.compareTo(((Termin) o).getVremeOd())%10*100;
        if(vremeDo.compareTo(((Termin) o).getVremeDo())!=0)return vremeDo.compareTo(((Termin) o).getVremeDo())%10*1000;
        return 0;
    }


    public Prostorija getProstorija() {
        return prostorija;
    }

    public void setProstorija(Prostorija prostorija) {
        this.prostorija = prostorija;
    }

    public LocalTime getVremeOd() {
        return vremeOd;
    }

    public void setVremeOd(LocalTime vremeOd) {
        this.vremeOd = vremeOd;
    }

    public LocalTime getVremeDo() {
        return vremeDo;
    }

    public void setVremeDo(LocalTime vremeDo) {
        this.vremeDo = vremeDo;
    }

    public LocalDate getDatum() {
        return datum;
    }

    public void setDatum(LocalDate datum) {
        this.datum = datum;
    }

    public Map<String, Object> getOsobine() {
        return osobine;
    }

    public void setOsobine(Map<String, Object> osobine) {
        this.osobine = osobine;
    }


}
