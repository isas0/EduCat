package it.unisa.educat.model;

import java.time.LocalDate;

public class PrenotazioneDTO {
	private int idPrenotazione;
	private LocalDate dataPrenotazione;
	private StatoPrenotazione stato;
	private float importoPagato;
	
	
	// Riferimenti (non specificati ma necessari)
	private UtenteDTO studente;
	private LezioneDTO lezione;
	private SlotDTO slot;
	
	// Enum per stato prenotazione
	public enum StatoPrenotazione {
		ATTIVA,
		ANNULLATA,
		CONCLUSA
	}

	// Costruttore
	public PrenotazioneDTO() {}
	

	public PrenotazioneDTO(int idPrenotazione, LocalDate dataPrenotazione, StatoPrenotazione stato, float importoPagato,
			UtenteDTO studente, LezioneDTO lezione, SlotDTO slot) {
		super();
		this.idPrenotazione = idPrenotazione;
		this.dataPrenotazione = dataPrenotazione;
		this.stato = stato;
		this.importoPagato = importoPagato;
		this.studente = studente;
		this.lezione = lezione;
		this.slot = slot;
	}


	public SlotDTO getSlot() {
		return slot;
	}


	public void setSlot(SlotDTO slot) {
		this.slot = slot;
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
	
}