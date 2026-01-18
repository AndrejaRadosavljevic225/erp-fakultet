package com.aradosavljevic.schedule_service.domain.entity;

import com.aradosavljevic.schedule_service.domain.json.ConfigMaper;
import com.aradosavljevic.schedule_service.domain.json.ScheduleJson;
import com.aradosavljevic.schedule_service.domain.json.ReservationJson;
import com.aradosavljevic.schedule_service.domain.json.RoomJson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import rs.raf.sk.api.Prostorija;
import rs.raf.sk.api.Raspored;
import rs.raf.sk.api.Termin;

import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.*;

public class RasporedPredavanja extends Raspored {
    public RasporedPredavanja(List<Prostorija> prostorije, List<Termin> termini, LocalDate vaziOd, LocalDate vaziDo) {
        super(prostorije, termini,vaziOd,vaziDo);

    }
    public RasporedPredavanja(List<Prostorija> prostorije, List<Termin> termini) {
        super(prostorije, termini);

    }

    public RasporedPredavanja() {
        super();
    }

    @Override
    public void dodajProstoriju(String naziv, Map<String, Object> atributi) {
        for(Prostorija p:prostorije){
            if(p.getNaziv().compareTo(naziv)==0)return;
        }
        prostorije.add(new Room(naziv, atributi));
    }
    public Prostorija postojiUcionica(Prostorija u){
        for(Prostorija p:prostorije){
            if(p.getNaziv().compareTo(u.getNaziv())==0)return p;
        }
        return u;
    }
    //mora da se doradi dosta
    @Override
    public boolean dodajTermin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, Map<String, Object> osobine, LocalDate datumOd, LocalDate datumDo) {
        if((datumOd.isBefore(vaziOd)) || (datumDo.isAfter(vaziDo)))return false;
        for(LocalDate d:slobodniDani){
            if(d.getDayOfWeek().getValue()==datumOd.getDayOfWeek().getValue()) {
                LocalDate temp = datumOd.plusDays(0);
                while(!temp.isAfter(datumDo)){
                    if(d.isEqual(temp))return false;
                }

            }
        }
        Prostorija p = postojiUcionica(prostorija);
        if(!prostorije.contains(p))prostorije.add(p);
        if(daLiJeZauzetTermin(p,vremeOd,vremeDo,datumOd,datumDo)){
            return false;
        }
        termini.add(new Reservation(p,vremeOd,vremeDo,datumOd,osobine,(this.razlikaDatumaUDanima(datumOd,datumDo))/7));

        termini.sort(Termin::compareTo);
        return true;
    }

    @Override
    public boolean dodajTermin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, Map<String, Object> osobine, LocalDate datum) {
        if((datum.isBefore(vaziOd)) || (datum.isAfter(vaziDo)))return false;
        for(LocalDate d:slobodniDani){
            if(datum.isEqual(d))return false;
        }
        Prostorija p = postojiUcionica(prostorija);
        if(!prostorije.contains(p))prostorije.add(p);

        if(daLiJeZauzetTermin(p,vremeOd,vremeDo,datum)){
            return false;
        }
        termini.add(new Reservation(p,vremeOd,vremeDo,datum,osobine,1));
        termini.sort(Termin::compareTo);
        return true;
    }

    @Override
    public boolean daLiJeZauzetTermin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, LocalDate datum) {
        for(Termin t:termini) {
            if (t.getProstorija().getNaziv().compareTo(prostorija.getNaziv())==0) {
                if(datum.getDayOfWeek().equals(t.getDatum().getDayOfWeek())){
                    LocalDate d = t.getDatum().plusDays(0);
                    LocalDate end = t.getDatum().plusDays(7*((Reservation)t).getBrojTermina());
                    while(!d.isAfter(end)){
                        if(datum.isEqual(d)){
                            LocalTime x = t.getVremeOd();
                            LocalTime y = t.getVremeDo();
                            if ( (!vremeOd.isBefore(x) && vremeOd.isBefore(y))
                                    || (vremeDo.isAfter(x) && !vremeDo.isAfter(y))
                                    ||(!vremeOd.isAfter(x) && !vremeDo.isBefore(y)) )
                                return true;
                        }
                        d = d.plusDays(7);
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean daLiJeZauzetTermin(Prostorija prostorija, LocalTime vremeOd, LocalTime vremeDo, LocalDate datumOd, LocalDate datumDo) {
        LocalDate d = datumOd.plusDays(0);
        while(!d.isAfter(datumDo)){
            if(daLiJeZauzetTermin(prostorija, vremeOd, vremeDo, d))return true;
            d = d.plusDays(7);
        }
        return false;
    }

    @Override
    public boolean brisanjeTermina(Prostorija prostorija, LocalTime vreme, LocalDate datum) {
        List<Termin> temp = new ArrayList<>();
        for(Termin t: termini){
            if(t.getProstorija().getNaziv().matches(prostorija.getNaziv())
                    && ( (!t.getVremeOd().isAfter(vreme)) && (!t.getVremeDo().isBefore(vreme)) )){
                if(t.getDatum().compareTo(datum)==0){
                    if(((Reservation)t).getBrojTermina()==1){
                        temp.add(t);
                    }else{
                        ((Reservation) t).setNumber((long) (((Reservation) t).getBrojTermina()-1));
                        t.setDatum(t.getDatum().plusDays(7));
                    }
                }
                LocalDate x = t.getDatum().plusDays(((Reservation)t).getDan());
                for(int i = 1;i<((Reservation) t).getBrojTermina()-1;i++){
                    x = x.plusDays(7);
                    if(x.compareTo(datum)==0){
                        x = x.plusDays(7);
                        LocalDate s = x.plusDays(0);
                        s = s.plusDays(7*((Reservation) t).getBrojTermina()-i);
                        dodajTermin(prostorija,vreme,t.getVremeDo(), (Map<String, Object>)t.getOsobine(),x,s);
                        ((Reservation)t).setNumber((long) i);
                    }
                }
                x = x.plusDays(7);
                if(x.compareTo(datum)==0){
                    if(((Reservation)t).getBrojTermina()==1){
                        temp.add(t);
                    }else{
                        ((Reservation)t).setNumber((long) (((Reservation) t).getBrojTermina()-1));
                    }
                }

            }
        }
        for(Termin t: termini) {
            if(((Reservation)t).getBrojTermina()<1)temp.add(t);
        }
        termini.removeAll(temp);
        return false;
    }

    @Override
    public boolean brisanjeTermineUPeriodu(Prostorija prostorija, LocalTime vreme, LocalDate datumOd, LocalDate datumDo) {
        while(!datumOd.isAfter(datumDo)){
            brisanjeTermina(prostorija,vreme,datumOd);
            datumOd = datumOd.plusDays(7);
        }
        brisanjeTermina(prostorija,vreme,datumOd);
        return true;
    }


    @Override
    public List<Termin> slobodniTermini(LocalDate datumOd, LocalDate datumDo, Map<String, Object> uslovi) {
        List<Termin> sTermini = new ArrayList<>();
        while(!datumOd.isAfter(datumDo)){
            sTermini.addAll(slobodniTermini(datumOd,uslovi));
            datumOd = datumOd.plusDays(1);
        }
        return sTermini;
    }
    //ovo mora da se ispravi poditno
    @Override
    public List<Termin> slobodniTermini(LocalDate datum, Map<String, Object> uslovi) {
        List<Prostorija> dobreUcionice = new ArrayList<>();
        List<Termin> sTermini = new ArrayList<>();
        List<String> naslovi = new ArrayList<>();
        int x;
        naslovi.addAll(uslovi.keySet());
        for (Prostorija p : prostorije) {
            x = 0;
            if(!naslovi.isEmpty()){
                for(String s:naslovi){
                    if(p.getAtributi().get(s).toString().compareTo(uslovi.get(s).toString())!=0)
                        x++;
                }
            }
            if(x==0)dobreUcionice.add(p);
        }
        List<Termin> zauzeti = zauzetiTermini(datum, uslovi);
        zauzeti.sort(Termin::compareTo);
        for(Prostorija p:dobreUcionice){
            LocalTime start = LocalTime.of(0,0,0);
            LocalTime end;
            for(Termin z:zauzeti){
                if(p.getNaziv().compareTo(z.getProstorija().getNaziv())==0){
                    end = z.getVremeOd();
                    if(end.isAfter(start)){
                        sTermini.add(new Reservation(p,start,end,datum));
                    }
                    start = z.getVremeDo();
                }

            }

            end = LocalTime.of(23,59,59);
            if(start.isBefore(end)){
                sTermini.add(new Reservation(p,start,end,datum));
            }
        }

        sTermini.sort(Termin::compareTo);
        return sTermini;
    }

    //pretrpava memoriju nekako mora se to resiti vrv petlja inf. koja to radi
    @Override
    public List<Termin> zauzetiTermini(LocalDate datumOd, LocalDate datumDo, Map<String, Object> uslovi) {
        List<Termin> zauzeti = new ArrayList<>();
        while(!datumOd.isAfter(datumDo)){
            zauzeti.addAll(zauzetiTermini(datumOd,uslovi));
            datumOd = datumOd.plusDays(7);
        }
        return zauzeti;
    }

    @Override
    public List<Termin> zauzetiTermini(LocalDate datum, Map<String, Object> uslovi) {
        List<Termin> terminList=new ArrayList<>();
        List<String> naslovi = new ArrayList<>();
        naslovi.addAll(uslovi.keySet());
        int x;
        for(Termin t:getTermini()){
            x = 0;
            if(!naslovi.isEmpty()){
                for(String s:naslovi){
                    if(t.getOsobine().get(s).toString().compareTo(uslovi.get(s).toString())!=0)
                        x++;
                }
            }
            if(x>0)continue;
            LocalDate d = t.getDatum().plusDays(0);
            for(int i = 0; i< ((Reservation)t).getBrojTermina();i++){
                if(d.compareTo(datum)==0){
                    terminList.add(new Reservation(t.getProstorija(),t.getVremeOd(),t.getVremeDo(),d,t.getOsobine(),1));
                }
                d = d.plusDays(7);
            }
        }
        terminList.sort(Termin::compareTo);
        return terminList;
    }

    @Override
    public boolean premestiTermin(Prostorija staraProstorija, Prostorija novaProstorija, LocalTime stariPocetak, LocalTime noviPocetak, LocalTime noviKraj, LocalDate stariDatum, LocalDate noviDatum, Integer trajanje) {
        Termin temp = null;
        for(Termin t:termini){
            if(t.getDatum().compareTo(stariDatum)==0&&t.getProstorija().getNaziv().matches(staraProstorija.getNaziv())&&t.getVremeOd().compareTo(stariPocetak)==0){
                LocalDate d = noviDatum.plusDays(((Reservation)t).getDan());
                d = d.plusDays(trajanje);
                if(dodajTermin(novaProstorija,noviPocetak,noviKraj, t.getOsobine(),noviDatum,d)) {
                    temp = t;
                    break;
                }

            }
        }
        if(temp==null)return false;
        termini.remove(temp);
        return true;
    }

    @Override
    public boolean ucitajCSV(String imeFajla, String configFajl) throws IOException {
        ucitajApache(imeFajla,configFajl);
        return true;
    }

    public void ucitajApache(String imeFajla, String configFajlIme)throws IOException{
        List<ConfigMaper> columnMapiranja = procitajConfig(configFajlIme);
        Map<Integer, String> mappings = new HashMap<>();

        for(ConfigMaper cm : columnMapiranja) {
            mappings.put(cm.getIndex(), cm.getOriginal());
        }
        System.out.println(mappings);
        FileReader fr= new FileReader(imeFajla);
        CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(fr);

        //DateTimeFormatter formater = DateTimeFormatter.ofPattern(mappings.get(-1));
        int i = 0;
        for (CSVRecord record : parser) {
            Reservation appointment = new Reservation();

            for (ConfigMaper entry : columnMapiranja) {
                int columnIndex = entry.getIndex();


                String columnName = entry.getCustom();

                switch (mappings.get(columnIndex)) {
                    case "prostorija":
                        Prostorija p = new Room(record.get(columnIndex), new HashMap<>());
                        appointment.setProstorija(p);
                        break;
                    case "vremeOd":
                        LocalTime startTime = LocalTime.parse(record.get(columnIndex));
                        appointment.setVremeOd(startTime);
                        break;
                    case "vremeDo":
                        LocalTime endTime = LocalTime.parse(record.get(columnIndex));
                        appointment.setVremeDo(endTime);
                        break;
                    case "datum":
                        LocalDate endDate = LocalDate.parse(record.get(columnIndex));
                        appointment.setDatum(endDate);
                        break;
                    case "brojTermina":
                        appointment.setNumber((long) Integer.parseInt(record.get(columnIndex)));
                        break;
                    case "dan":
                        appointment.setDan(Integer.parseInt(record.get(columnIndex)));
                        break;
                    case "osobine":
                        appointment.getOsobine().put(columnName, record.get(columnIndex));
                        break;
                }
            }

            getTermini().add(appointment);
        }
    }

    private static List<ConfigMaper>  procitajConfig(String filePath) throws FileNotFoundException {
        List<ConfigMaper> mappings = new ArrayList<>();

        File file = new File(filePath);
        Scanner scanner = new Scanner(file);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] splitLine = line.split(" ", 3);

            mappings.add(new ConfigMaper(Integer.valueOf(splitLine[0]), splitLine[1], splitLine[2]));
        }

        scanner.close();


        return mappings;
    }
    @Override
    public boolean snimiCSV(String imeFajla) throws IOException {
        ispisiApache(imeFajla);
        return true;
    }
    private void ispisiApache(String path) throws IOException {
        // Create a FileWriter and CSVPrinter
        FileWriter fileWriter = new FileWriter(path);
        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.DEFAULT);
        csvPrinter.printRecord(
                "prostorija",
                "vremeOd",
                "vremeDo",
                "datum",
                "brojTermina",
                "dan"
        );
        for (Termin appointment : super.getTermini()) {
            csvPrinter.printRecord(
                    appointment.getProstorija().getNaziv(),
                    appointment.getVremeOd(),
                    appointment.getVremeDo(),
                    appointment.getDatum(),
                    ((Reservation)appointment).getBrojTermina(),
                    ((Reservation)appointment).getDan()
            );
        }

        csvPrinter.close();
        fileWriter.close();
    }

    @Override
    public boolean ucitajJson(String imeFajla, String configFajl) throws IOException {
        Gson gson = new Gson();
        if(termini==null)termini = new ArrayList<>();

        ScheduleJson temp;

        try (FileReader fr = new FileReader(imeFajla)) {
            temp = gson.fromJson(fr, ScheduleJson.class);
        }

        List<Prostorija> prostorije = new ArrayList<>();
        for(RoomJson p: temp.prostorije){
            prostorije.add(new Room(p.naziv,p.atributi));
        }

        List<Termin>termini = new ArrayList<>();
        for(ReservationJson t: temp.termini){
            Prostorija x = null;
            for(Prostorija u:prostorije){
                if(u.getNaziv().matches(t.prostorija.naziv)){
                    x = u;
                    break;
                }
            }
            termini.add(new Reservation(x,LocalTime.parse(t.vremeOd),LocalTime.parse(t.vremeDo),LocalDate.parse(t.datum),t.atributi,t.brojTermina));
        }
        List<LocalDate> slobodniDani = new ArrayList<>();
        for(String s: temp.slobodniDani){
            slobodniDani.add(LocalDate.parse(s));
        }


        setTermini(termini);
        setProstorije(prostorije);
        setVaziOd(LocalDate.parse(temp.vaziOd));
        setVaziDo(LocalDate.parse(temp.vaziDo));
        setSlobodniDani(slobodniDani);


        return true;
    }

    @Override
    public boolean snimiJson(String imeFajla) throws IOException {
        List<RoomJson> prostorije = new ArrayList<>();
        for(Prostorija p:this.prostorije){
            prostorije.add(new RoomJson(p.getNaziv(),p.getAtributi()));
        }

        List<ReservationJson>termini = new ArrayList<>();
        for(Termin t:this.termini){
            RoomJson x = null;
            for(RoomJson u:prostorije){
                if(u.naziv.matches(t.getProstorija().getNaziv())){
                    x = u;
                    break;
                }
            }
            termini.add(new ReservationJson(x, t.getVremeOd(), t.getVremeDo(), t.getDatum(),((Reservation)t).getBrojTermina(), (int) ((Reservation) t).getDan(), t.getOsobine()));
        }

        List<String> slobodniDani = new ArrayList<>();
        for(LocalDate d:this.slobodniDani){
            slobodniDani.add(d.toString());
        }


        ScheduleJson raspored = new ScheduleJson(prostorije,termini,vaziOd.toString(),vaziDo.toString(),slobodniDani);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter fw = new FileWriter(imeFajla)) {
            gson.toJson(raspored, fw);
        }catch (Exception e){
            System.out.println("Greska pri snimanju");
        }
        return true;
    }

    @Override
    public boolean snimiPDF(String imeFajla) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);
        int pageH = (int)page.getBBox().getHeight();
        int pageW = (int)page.getBBox().getWidth();
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        contentStream.setStrokingColor(Color.DARK_GRAY);
        contentStream.setLineWidth(1);
        int x = 25, y = pageH-25;

        int cellH = 15, cellW = 60;


        int colC = termini.get(1).getOsobine().size() + 6, rowC = termini.size()+1;

        for(int i = 0; i < rowC; i++){
            for(int j = 0; j < colC; j++){
                contentStream.addRect(x,y,cellW,-cellH);

                contentStream.beginText();
                contentStream.newLineAtOffset(x+5,y-cellH+5);
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN),9);

                List<String> dodatniNaslovi =new ArrayList<>( termini.get(0).getOsobine().keySet());
                if(i==0){
                    switch (j){
                        case 0:
                            contentStream.showText("Ucionica");
                            break;
                        case 1:
                            contentStream.showText("Pocetak casa");
                            break;
                        case 2:
                            contentStream.showText("Kraj casa");
                            break;
                        case 3:
                            contentStream.showText("Datum");
                            break;
                        case 4:
                            contentStream.showText("Broj casova");
                            break;
                        case 5:
                            contentStream.showText("Dan u nedelji");
                            break;
                        default:
                            contentStream.showText(dodatniNaslovi.get(j-6));
                    }
                }else{
                    switch (j){
                        case 0:
                            contentStream.showText(termini.get(i-1).getProstorija().getNaziv());
                            break;
                        case 1:
                            contentStream.showText(termini.get(i-1).getVremeOd().toString());
                            break;
                        case 2:
                            contentStream.showText(termini.get(i-1).getVremeDo().toString());
                            break;
                        case 3:
                            contentStream.showText(termini.get(i-1).getDatum().toString());
                            break;
                        case 4:
                            contentStream.showText(String.valueOf(((Reservation)termini.get(i-1)).getBrojTermina()));
                            break;
                        case 5:
                            contentStream.showText(String.valueOf(((Reservation)termini.get(i-1)).getDan()));
                            break;
                        default:
                            contentStream.showText(termini.get(i-1).getOsobine().get(dodatniNaslovi.get(j-6)).toString());;
                    }
                }
                contentStream.endText();

                x+=cellW;
            }
            x=25;
            y-=cellH;
        }
        contentStream.stroke();

        contentStream.close();

        document.save(imeFajla);
        document.close();
        return true;
    }

    @Override
    public List<Prostorija> getProstorije() {
        return super.getProstorije();
    }

    @Override
    public void setProstorije(List<Prostorija> prostorije) {
        super.setProstorije(prostorije);
    }

    @Override
    public List<Termin> getTermini() {
        termini.sort(Termin::compareTo);
        return super.getTermini();
    }

    @Override
    public void setTermini(List<Termin> termini) {
        super.setTermini(termini);
    }

    @Override
    public void prikaziRaspored() {
        System.out.println("Validan od "+vaziOd+", do"+vaziDo);
        System.out.println("Slobodni dani:\n"+slobodniDani);
        System.out.println("Prostorije dani:\n"+prostorije);
        StringBuilder sb = new StringBuilder();
        sb.append("|Prostorija|Pocetak|Kraj|Datum|Dan|Broj Casova|");
        if(!termini.isEmpty()){
            for(String s:termini.get(0).getOsobine().keySet()){
                sb.append(s+'|');
            }
            System.out.println(sb.toString());
            int i=0;
            for(Termin t:getTermini()){
                StringBuilder sb1 = new StringBuilder();
                sb1.append('|'+t.getProstorija().getNaziv()+'|'+t.getVremeOd()+'|'+t.getVremeDo()+'|'+t.getDatum()+'|'+((Reservation)t).getDan()+'|'+((Reservation) t).getBrojTermina()+'|');
                for(String s:t.getOsobine().keySet()){
                    sb1.append(((String)t.getOsobine().get(s))+'|');
                }
                System.out.println(sb1.toString());
            }
        }else {
            System.out.println(sb.toString());

        }

    }
}
