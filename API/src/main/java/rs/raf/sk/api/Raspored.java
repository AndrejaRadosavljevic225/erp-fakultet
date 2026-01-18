package rs.raf.sk.api;



import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
public abstract class Raspored {
    // inicijalizacija/konstruktor, dodavanje prostorija sa osobinama, dodavanje termina,
    // provera postojanja termina i zauzetosti, premestanje postojeceg termina, pretraga po uslovu,
    // ucitavanje i snimanje u fajl
    protected List<Prostorija> prostorije;
    protected List<Termin> termini;
    protected List<LocalDate> slobodniDani;

    protected LocalDate vaziOd;
    protected LocalDate vaziDo;

    public Raspored(List<Prostorija> prostorije, List<Termin> termini,LocalDate datumOd,LocalDate datumDo) {
        this.prostorije = prostorije;
        this.termini = termini;
        this.vaziOd = datumOd;
        this.vaziDo = datumDo;
        slobodniDani = new ArrayList<>();
    }
    public Raspored(List<Prostorija> prostorije, List<Termin> termini) {
        this.prostorije = prostorije;
        this.termini = termini;
        slobodniDani = new ArrayList<>();
    }

    public Raspored() {
        prostorije = new ArrayList<>();
        termini = new ArrayList<>();
        slobodniDani = new ArrayList<>();
    }


    /**
     * Ova funkcija omogucava dodavanje nove prostorije u listu prostorija
     * @param naziv Naziv prostorije
     * @param atributi Atributi prostorije
     */
    //Kreira prostoriju i ubacuje u spisak
    public abstract void dodajProstoriju(String naziv, Map<String, Object> atributi);

    /**
     * Ova funkcija omogucava dodavanje novog termina
     * @param prostorija Prostorija za termin koji se dodaje
     * @param vremeOd Vreme pocetka termina
     * @param vremeDo Vreme kraja termina
     * @param osobine Osobine termina
     * @param datumOd Datum pocetka termina
     * @param datumDo Datum kraja termina
     * @return boolean, true ako se uspesno doda termin
     */
    //Kreira i dodaje termin
    public abstract boolean dodajTermin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, Map<String, Object> osobine, LocalDate datumOd, LocalDate datumDo);

    /**
     * Ova funkcija omogucava dodavanje novog termina
     * @param prostorija Prostorija za termin
     * @param vremeOd Vreme pocetka termina
     * @param vremeDo Vreme kraja termina
     * @param osobine Osobine termina
     * @param datum Datum termina
     * @return boolean, true ako se uspesno doda termin
     */
    public abstract boolean dodajTermin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, Map<String, Object> osobine, LocalDate datum);

    /**
     * Ova funkcija proverava da li postoji termin sa dobijenim podacima
     * @param prostorija Prostorija za termin koji se proverava
     * @param vremeOd Vreme pocetka termina
     * @param vremeDo Vreme kraja termina
     * @param datum Datum termina
     * @return boolean, false ako nije zauzet termin
     */
    public abstract boolean daLiJeZauzetTermin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, LocalDate datum);

    /**
     * Ova funkcija proverava da li postoji termin sa dobijenim podacima
     * @param prostorija Prostorija za termin koji se proverava
     * @param vremeOd Vreme pocetka termina
     * @param vremeDo Vreme kraja termina
     * @param datumOd Datum pocetka termina
     * @param datumDo Datum kraja termina
     * @return boolean, false ako nije zauzet termin
     */
    public abstract boolean daLiJeZauzetTermin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, LocalDate datumOd, LocalDate datumDo);

    /**
     * Ova funkcija omogucava brisanje termina u nekoj prostoriji na odredjeni dan, u odredjeno vreme
     * @param prostorija Prostorija za termin koji se brise
     * @param vreme Vreme pocetka termina
     * @param datum Datum termina
     * @return boolean, true ako se uspesno obrise termin
     */
    public abstract boolean brisanjeTermina(Prostorija prostorija, LocalTime vreme, LocalDate datum);

    /**
     * Ova funkcija omogucava brisanje termina i nekoj prostoriji u odredjenim danima, u odredjeno vreme
     * @param prostorija Prostorija za termin koji se brise
     * @param vreme Vreme pocetka termina
     * @param datumOd Datum pocetka termina
     * @param datumDo Datum kraja termina
     * @return boolean, true ako se uspesno obrise termin
     */
    public abstract boolean brisanjeTermineUPeriodu(Prostorija prostorija, LocalTime vreme, LocalDate datumOd, LocalDate datumDo);

    /**
     * Ova funkcija vraca vremena kada je prostorija sa odgovarajucim uslovima slobodna
     * @param datumOd Datum od kog se gledaju slobodni termini
     * @param datumDo Datum do kog se gledaju slobodni termini
     * @param uslovi Uslovi koji ogranicavaju pretragu
     * @return lista slobodnih termina
     */
    public abstract List<Termin> slobodniTermini(LocalDate datumOd, LocalDate datumDo, Map<String, Object> uslovi);

    /**
     /** Ova funkcija vraca vremena kada je prostorija sa odgovarajucim uslovima slobodna
     *
     * @param datum Datum za koji se gledaju slobodni termini
     * @param uslovi Uslovi koji ogranicavaju pretragu
     * @return lista slobodnih termina
     */
    public abstract List<Termin> slobodniTermini(LocalDate datum, Map<String, Object> uslovi);

    /**
     * Ova funkcija vraca spisak svih zauzetih termina koji odgovaraju uslovima
     * @param datumOd Datum od kog se gledaju zauzeti termini
     * @param datumDo Datum do kog se gledaju zauzeti termini
     * @param uslovi Uslovi koji ogranicavaju pretragu
     * @return lista zauzetih termina
     */
    public abstract List<Termin> zauzetiTermini(LocalDate datumOd, LocalDate datumDo, Map<String, Object> uslovi);

    /**
     * Ova funkcija vraca spisak svih zauzetih termina koji odgovaraju uslovima
     * @param datum Datum za koji se gledaju zauzeti termini
     * @param uslovi Uslovi koji ogranicavaju pretragu
     * @return lista zauzetih termina
     */
    public abstract List<Termin> zauzetiTermini(LocalDate datum, Map<String, Object> uslovi);

    /**
     *
     */
    public abstract void prikaziRaspored();


    /**
     * Ova funkcija omogucava premestanje termina
     * @param staraProstorija Stara prostorija iz koje se premesta termin
     * @param novaProstorija Nova prostorija u koju se premesta termin
     * @param stariPocetak Staro vreme pocetka termina
     * @param noviPocetak Novo vreme pocetka termina
     * @param noviKraj Novo vreme kraja termina
     * @param stariDatum Stari datum termina
     * @param noviDatum Novi datum termina
     * @param trajanje trajanje
     * @return boolean, true ako je uspesno premestanje termina
     */
    public abstract boolean premestiTermin(Prostorija staraProstorija, Prostorija novaProstorija, LocalTime stariPocetak, LocalTime noviPocetak, LocalTime noviKraj, LocalDate stariDatum, LocalDate noviDatum, Integer trajanje);

    /**
     * Ova funkcija sluzi za ucitavanje rasporeda koji je sacuvan u csv fajlu
     * @param imeFajla ime fajla iz kog se ucitava csv
     * @param configFajl ime konfiguracionog fajla
     * @return boolean, true ako je uspesno ucitavanje
     * @throws IOException desila se greska prilikom ucitavanja iz fajla
     */
    public abstract boolean ucitajCSV(String imeFajla,String configFajl)throws IOException;

    /**
     * Ova funkcija sluzi za cuvanje rasporeda u csv fajlu
     * @param imeFajla ime csv fajla u koji se snima
     * @return boolean, true ako je uspesno upisivanje
     * @throws IOException desila se greska prilikom snimanja u fajl
     */
    public abstract boolean snimiCSV(String imeFajla) throws IOException;

    /**
     * Ova funkcija sluzi za ucitavanje rasporeda koji je sacuvan u json fajlu
     * @param imeFajla ime fajla iz kog se ucitava json
     * @param configFajl ime konfiguracionog fajla
     * @return boolean, true ako je uspesno ucitavanje
     * @throws IOException desila se greska prilikom ucitavanja iz fajla
     */
    public abstract boolean ucitajJson(String imeFajla,String configFajl)throws IOException;

    /**
     *Ova funkcija sluzi za cuvanje rasporeda u json fajlu
     * @param imeFajla ime json fajla u koji se snima
     * @return boolean, true ako je uspesno upisivanje
     * @throws IOException desila se greska prilikom snimanja u fajl
     */
    public abstract boolean snimiJson(String imeFajla)throws IOException;

    /**
     * Ova funkcija sluzi za cuvanje rasporeda u pdf fajlu
     * @param imeFajla ime pdf fajla u koji se snima
     * @return boolean, true ako je uspesno upisivanje
     * @throws IOException desila se greska prilikom snimanja u fajl
     */
    public abstract boolean snimiPDF(String imeFajla) throws IOException;

    public List<Prostorija> getProstorije() {
        return prostorije;
    }

    public void setProstorije(List<Prostorija> prostorije) {
        this.prostorije = prostorije;
    }
    public List<Termin> getTermini() {
        return termini;
    }

    public void setTermini(List<Termin> termini) {
        this.termini = termini;
    }

    public int razlikaDatumaUDanima(LocalDate a, LocalDate b){
        long t = Math.abs(a.getDayOfYear()- b.getDayOfYear());
        return (int) TimeUnit.DAYS.convert(t, TimeUnit.MILLISECONDS);
    }

    public List<LocalDate> getSlobodniDani() {
        return slobodniDani;
    }

    public void setSlobodniDani(List<LocalDate> slobodniDani) {
        this.slobodniDani = slobodniDani;
    }

    public LocalDate getVaziOd() {
        return vaziOd;
    }

    public void setVaziOd(LocalDate vaziOd) {
        this.vaziOd = vaziOd;
    }

    public LocalDate getVaziDo() {
        return vaziDo;
    }

    public void setVaziDo(LocalDate vaziDo) {
        this.vaziDo = vaziDo;
    }

    public void dodajSlobodanDan(LocalDate date){
        slobodniDani.add(date);
    }
}
