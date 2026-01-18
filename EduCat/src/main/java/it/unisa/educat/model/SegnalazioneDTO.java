package it.unisa.educat.model;

public class SegnalazioneDTO {

	private int idSegnalazione;
	private String descrizione;
	private int idSegnalante;
	private int idSegnalato;
	
	public enum StatoSegnalazione {ATTIVA, RISOLTA};
	private StatoSegnalazione stato;
	
	private UtenteDTO segnalante;
	private UtenteDTO segnalato;
	
	public SegnalazioneDTO() {}
	
	public SegnalazioneDTO(int idSegnalazione, String descrizione, int idSegnalante, int idSegnalato, StatoSegnalazione stato) {
		this.idSegnalazione = idSegnalazione;
		this.descrizione = descrizione;
		this.idSegnalante = idSegnalante;
		this.idSegnalato = idSegnalato;
		this.stato = stato;
	}


	public int getIdSegnalazione() {
		return idSegnalazione;
	}
	public void setIdSegnalazione(int idSegnalazione) {
		this.idSegnalazione = idSegnalazione;
	}
	public String getDescrizione() {
		return descrizione;
	}
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}
	public int getIdSegnalante() {
		return idSegnalante;
	}
	public void setIdSegnalante(int idSegnalante) {
		this.idSegnalante = idSegnalante;
	}
	public int getIdSegnalato() {
		return idSegnalato;
	}
	public void setIdSegnalato(int idSegnalato) {
		this.idSegnalato = idSegnalato;
	}

	public StatoSegnalazione getStato() {
		return stato;
	}

	public void setStato(StatoSegnalazione stato) {
		this.stato = stato;
	}

	public UtenteDTO getSegnalante() {
		return segnalante;
	}

	public void setSegnalante(UtenteDTO segnalante) {
		this.segnalante = segnalante;
	}

	public UtenteDTO getSegnalato() {
		return segnalato;
	}

	public void setSegnalato(UtenteDTO segnalato) {
		this.segnalato = segnalato;
	}
	
	

	
}
