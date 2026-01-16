package it.unisa.educat.model;

import java.time.LocalDateTime;

public class SlotDTO {
    private int idSlot;
    private LezioneDTO lezione;
    private UtenteDTO tutor;
    private LocalDateTime dataOraInizio;
    private LocalDateTime dataOraFine;
    private StatoSlot stato;
    private UtenteDTO studente; // null se non prenotato
    private PrenotazioneDTO prenotazione; // null se non prenotato
    private Float prezzo; // prezzo effettivo (può essere scontato)
    
    public enum StatoSlot {
        DISPONIBILE, PRENOTATO, CONCLUSA, ANNULLATO
    }
    
    public SlotDTO() {}
    public SlotDTO(int idSlot, LezioneDTO lezione, UtenteDTO tutor, LocalDateTime dataOraInizio,
			LocalDateTime dataOraFine, StatoSlot stato, UtenteDTO studente, PrenotazioneDTO prenotazione,
			Float prezzo) {
		super();
		this.idSlot = idSlot;
		this.lezione = lezione;
		this.tutor = tutor;
		this.dataOraInizio = dataOraInizio;
		this.dataOraFine = dataOraFine;
		this.stato = stato;
		this.studente = studente;
		this.prenotazione = prenotazione;
		this.prezzo = prezzo;
	}

	// Metodo utile per verificare se lo slot è prenotabile
    public boolean isPrenotabile() {
        return stato == StatoSlot.DISPONIBILE && 
               dataOraInizio.isAfter(LocalDateTime.now().plusHours(1));
    }

	public int getIdSlot() {
		return idSlot;
	}

	public void setIdSlot(int idSlot) {
		this.idSlot = idSlot;
	}

	public LezioneDTO getLezione() {
		return lezione;
	}

	public void setLezione(LezioneDTO lezione) {
		this.lezione = lezione;
	}

	public UtenteDTO getTutor() {
		return tutor;
	}

	public void setTutor(UtenteDTO tutor) {
		this.tutor = tutor;
	}

	public LocalDateTime getDataOraInizio() {
		return dataOraInizio;
	}

	public void setDataOraInizio(LocalDateTime dataOraInizio) {
		this.dataOraInizio = dataOraInizio;
	}

	public LocalDateTime getDataOraFine() {
		return dataOraFine;
	}

	public void setDataOraFine(LocalDateTime dataOraFine) {
		this.dataOraFine = dataOraFine;
	}

	public StatoSlot getStato() {
		return stato;
	}

	public void setStato(StatoSlot stato) {
		this.stato = stato;
	}

	public UtenteDTO getStudente() {
		return studente;
	}

	public void setStudente(UtenteDTO studente) {
		this.studente = studente;
	}

	public PrenotazioneDTO getPrenotazione() {
		return prenotazione;
	}

	public void setPrenotazione(PrenotazioneDTO prenotazione) {
		this.prenotazione = prenotazione;
	}

	public Float getPrezzo() {
		return prezzo;
	}

	public void setPrezzo(Float prezzo) {
		this.prezzo = prezzo;
	}
    
    
}
