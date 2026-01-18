package it.unisa.educat.model;

import java.time.LocalDate;

public class PrenotazioneDTO {
	private int idPrenotazione;
	private LocalDate dataPrenotazione;
	private StatoPrenotazione stato;
	private float importoPagato;
	
	private String indirizzoFatturazione;
	private String numeroCarta;
	private String dataScadenza;
	private String intestatario;
	private int cvv;
	
	private int idTutor;
	
	// Riferimenti (non specificati ma necessari)
	private UtenteDTO studente;
	private LezioneDTO lezione;
	
	// Enum per stato prenotazione
	public enum StatoPrenotazione {
		ATTIVA,
		ANNULLATA,
		CONCLUSA
	}

	// Costruttore
	public PrenotazioneDTO() {}
	

	public PrenotazioneDTO(int idPrenotazione, LocalDate dataPrenotazione, StatoPrenotazione stato, float importoPagato,
			UtenteDTO studente, LezioneDTO lezione) {
		super();
		this.idPrenotazione = idPrenotazione;
		this.dataPrenotazione = dataPrenotazione;
		this.stato = stato;
		this.importoPagato = importoPagato;
		this.studente = studente;
		this.lezione = lezione;
	}



	// Getters e Setters
	public int getIdPrenotazione() {
		return idPrenotazione;
	}

	public void setIdPrenotazione(int idPrenotazione) {
		this.idPrenotazione = idPrenotazione;
	}

	public LocalDate getDataPrenotazione() {
		return dataPrenotazione;
	}

	public void setDataPrenotazione(LocalDate dataPrenotazione) {
		this.dataPrenotazione = dataPrenotazione;
	}

	public StatoPrenotazione getStato() {
		return stato;
	}

	public void setStato(StatoPrenotazione stato) {
		this.stato = stato;
	}

	public UtenteDTO getStudente() {
		return studente;
	}

	public void setStudente(UtenteDTO studente) {
		this.studente = studente;
	}

	public LezioneDTO getLezione() {
		return lezione;
	}

	public void setLezione(LezioneDTO lezione) {
		this.lezione = lezione;
	}
	public float getImportoPagato() {
		return importoPagato;
	}
	public void setImportoPagato(float importoPagato) {
		this.importoPagato = importoPagato;
	}


	public String getIndirizzoFatturazione() {
		return indirizzoFatturazione;
	}


	public void setIndirizzoFatturazione(String indirizzoFatturazione) {
		this.indirizzoFatturazione = indirizzoFatturazione;
	}


	public String getNumeroCarta() {
		return numeroCarta;
	}


	public void setNumeroCarta(String numeroCarta) {
		this.numeroCarta = numeroCarta;
	}


	public String getDataScadenza() {
		return dataScadenza;
	}


	public void setDataScadenza(String dataScadenza) {
		this.dataScadenza = dataScadenza;
	}


	public String getIntestatario() {
		return intestatario;
	}


	public void setIntestatario(String intestatario) {
		this.intestatario = intestatario;
	}


	public int getCvv() {
		return cvv;
	}


	public void setCvv(int cvv) {
		this.cvv = cvv;
	}

	

	public int getIdTutor() {
		return idTutor;
	}


	public void setIdTutor(int uid) {
		this.idTutor = uid;
	}
	
	
}