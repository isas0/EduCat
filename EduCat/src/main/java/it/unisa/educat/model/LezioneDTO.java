package it.unisa.educat.model;

import java.time.LocalDateTime;

public class LezioneDTO {
	private int idLezione;
	private String materia;
	private LocalDateTime dataInizio;
	private LocalDateTime dataFine;
	private float durata; // in ore, calcolata da dataInizio/dataFine
	private float prezzo;
	private ModalitaLezione modalitaLezione;
	private UtenteDTO tutor;
	private String citta;
	private StatoLezione stato;

	// Aggiungi se vuoi gestire prenotazione diretta
	private Integer idStudentePrenotato; // null se non prenotata
	private Integer idPrenotazione; // null se non prenotata

	// Enum per modalità lezione
	public enum ModalitaLezione {
		ONLINE,
		PRESENZA
	}

	// Enum per stato lezione
	public enum StatoLezione {
		PIANIFICATA,    // Creata dal tutor, disponibile
		PRENOTATA,      // Prenotata da uno studente
		CONCLUSA,       // Lezione completata
		ANNULLATA       // Lezione cancellata
	}

	// Costruttori
	public LezioneDTO() {}

	public LezioneDTO(String materia, LocalDateTime dataInizio, LocalDateTime dataFine, 
			float prezzo, ModalitaLezione modalita, UtenteDTO tutor, String citta) {
		this.materia = materia;
		this.dataInizio = dataInizio;
		this.dataFine = dataFine;
		this.prezzo = prezzo;
		this.modalitaLezione = modalita;
		this.tutor = tutor;
		this.citta = citta;
		this.stato = StatoLezione.PIANIFICATA;
		this.durata = calculateDuration(dataInizio, dataFine);
	}

	// Calcola durata in ore
	private float calculateDuration(LocalDateTime inizio, LocalDateTime fine) {
		long minutes = java.time.Duration.between(inizio, fine).toMinutes();
		return minutes / 60.0f;
	}

	// Getters e Setters
	public int getIdLezione() { 
		return idLezione; 
	}
	public void setIdLezione(int idLezione) { 
		this.idLezione = idLezione; 
	}

	public String getMateria() { 
		return materia; 
	}

	public void setMateria(String materia) { 
		this.materia = materia; 
	}

	public LocalDateTime getDataInizio() { 
		return dataInizio; 
	}
	
	public void setDataInizio(LocalDateTime dataInizio) { 
		this.dataInizio = dataInizio;
		if (this.dataFine != null) {
			this.durata = calculateDuration(dataInizio, this.dataFine);
		}
	}

	public LocalDateTime getDataFine() { 
		return dataFine; 
	}
	
	public void setDataFine(LocalDateTime dataFine) { 
		this.dataFine = dataFine;
		if (this.dataInizio != null) {
			this.durata = calculateDuration(this.dataInizio, dataFine);
		}
	}

	public float getDurata() { 
		return durata; 
	}
	
	public void setDurata(float durata) { 
		this.durata = durata; 
	}

	public float getPrezzo() { 
		return prezzo; 
	}
	
	public void setPrezzo(float prezzo) { 
		this.prezzo = prezzo; 
	}

	public ModalitaLezione getModalitaLezione() { 
		return modalitaLezione; 
	}
	
	public void setModalitaLezione(ModalitaLezione modalitaLezione) { 
		this.modalitaLezione = modalitaLezione; 
	}
	
	public UtenteDTO getTutor() {
		return tutor;
	}

	public void setTutor(UtenteDTO tutor) {
		this.tutor = tutor;
	}

	public String getCitta() {
		return citta;
	}

	public void setCitta(String citta) {
		this.citta = citta;
	}

	public StatoLezione getStato() {
		return stato;
	}

	public void setStato(StatoLezione stato) {
		this.stato = stato;
	}

	public Integer getIdStudentePrenotato() { 
		return idStudentePrenotato; 
	}
	
	public void setIdStudentePrenotato(Integer idStudentePrenotato) { 
		this.idStudentePrenotato = idStudentePrenotato; 
	}

	public Integer getIdPrenotazione() { 
		return idPrenotazione; 
	}
	
	public void setIdPrenotazione(Integer idPrenotazione) { 
		this.idPrenotazione = idPrenotazione; 
	}

	// Metodo utile per verificare se la lezione è prenotabile
	public boolean isPrenotabile() {
		return stato == StatoLezione.PIANIFICATA && 
				dataInizio.isAfter(LocalDateTime.now().plusHours(1));
	}
}