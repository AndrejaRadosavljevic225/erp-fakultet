1. HR je zaduzen za vodjenjem evidencije svim zaposlenima, njihovim rolama, permisijama, pozicijama. Sto se tice nastavnog osoblja bitno je voditi evidenciju o potrebnim casovima u godini, dodatnim aktivnostima. Za svo osoblje naravno treba takodje imati evidenciju o slobodnim danima, odmorima, bolovanju i slicno.
2. Scheduler je zaduzen da upravlja prostorijama fakulteta, njihovom zauzecu po terminima. Fokus je samo na rezervisanju datog termina od strane zaposlenih a ne sta se konkretno desava u tim prostorijama izuzev dva slucaja: redovnih aktivnosti i dodatnih (dodatni casovi, mentorstvo,...). Takodje treba imati u vidu kojoj skolskoj godini pripadaju.
3. Finansijski mikroservis pokriva isplate plata, svih odliva, svih prihoda ( fokus na dugovanjima studenata i isplatama tih istih).Plate se racunaju na osnovu zvanja, fonda casova i dodatnih aktivnosti iz HR segmenta.

Akteri:
Admin
HR
HR Manager (Maybe)
Scheduler Manager
Payrole Manager
Student service Manager
Worker (Nastavno i nenastavno osoblje)


HR
	-Pregled svih zaposlenih
		--pretrga/filteri
		--Detalji zaposlenog
			---Izmena zaposlenog
			---Brisanje zaposlenog
			---Pregled trenutne skolske godine u slucaju da je nastavno osoblej (njegov fond casova, dodatne broj dodatnih aktivnosti i mentorstva)
		--Kreiranje zaposlenog
	-Pregled svih user profila (njihovih permisija)
		--pretrga/filteri
		--Detalji usera
			---Izmena usera
			---Brisanje usera
		--Kreiranje usera
Schedule
	-Pregled svih termina (Rezervisanih i slobodnih sa filterima) (Trenutna godina)
		--Podnosenje rezervacije (Automacki prihvacen ako je scheduler menager ili neko sa visom pozicijom)
			---Unosenje detalja (ponavljajuci, redovni/dodatni/mentorstvo)
		--pregled svih termina za neku prostoriju 
			---Podnosenje rezervacije
		--pregled svih termina za neku osobu
			---Podnosenje rezervacije
		--Detaljan prikaz odredjenog rezervisanog termina
			---Izmena rezervacije
			---otkazivanje		
	-Pregled Svih rezervacija (Trenutna godina)
		--pregled svih rezervacija za neku prostoriju 
			---Prihvatanje rezervacije
			---Odbijanje rezervacije	
		--pregled svih rezervacija za neku osobu
			---Prihvatanje rezervacije
			---Odbijanje rezervacije	
 	-Pregled svih prostorija
		--Dodavalje prostorije
		--Detaljan prikaz prostorija
			---Brisanje prostorije
			---Izmena prostorije
	-Pregled skolskih godina
		--pretraga/filtriranje
		--Detaljan prikaz godine
Finance
	-Pregled svih prihoda
		-Pretraga/filteri
		-pregled svih uplata studenata
	-Pregled dugovanja svih studenata
		--Detaljan pregled dugovanja studenta
			---pregled svih uplata studenata
	-Pregled svih odliva
		--Pretraga/filteri
		--Detaljan prikaz odliva
		--Pregled svih plata
			---Detaljan pregled plate
	-Isplata Svih plata
		-Isplata jedne plate
