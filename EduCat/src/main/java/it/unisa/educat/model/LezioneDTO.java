package it.unisa.educat.model;

import java.time.LocalDateTime;
import java.util.List;

public class LezioneDTO {
	private int idLezione;
	private String materia;
	private LocalDateTime data; // Usato LocalDateTime invece di Date per includere ora
	private float durata;
	private float prezzo;
	private ModalitaLezione modalitaLezione;
	private UtenteDTO tutor; // Riferimento al tutor
	private String città;

	private List<SlotDTO> slotDisponibili;
	
	// Enum per modalità lezione
	public enum ModalitaLezione {
		ONLINE,
		PRESENZA
	}

	// Costruttore
	public LezioneDTO() {}
	public LezioneDTO(int idLezione, String materia, LocalDateTime data, float durata, 
			float prezzo, ModalitaLezione modalitaLezione, UtenteDTO tutor, String città) {
		this.idLezione = idLezione;
		this.materia = materia;
		this.data = data;
		this.durata = durata;
		this.prezzo = prezzo;
		this.modalitaLezione = modalitaLezione;
		this.tutor = tutor;
		this.città = città;
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

	public LocalDateTime getData() {
		return data;
	}

	public void setData(LocalDateTime data) {
		this.data = data;
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
		return città;
	}

	public void setCitta(String città) {
		this.città = città;
	}
	public String getCittà() {
		return città;
	}
	public void setCittà(String città) {
		this.città = città;
	}
	
	public List<SlotDTO> getSlotDisponibili() {
		return slotDisponibili;
	}
	public void setSlotDisponibili(List<SlotDTO> slotDisponibili) {
		this.slotDisponibili = slotDisponibili;
	}
	
	
}
