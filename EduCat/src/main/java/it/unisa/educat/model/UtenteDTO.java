package it.unisa.educat.model;

import java.time.LocalDate;

public class UtenteDTO {
	private int UID;
	private String nome;
	private String cognome;
	private String email;
	private String password; // Hashed
	private LocalDate dataNascita;
	private String indirizzo; // Composto da Via, Civico, Città, CAP
	
	private enum TipoUtente{STUDENTE, GENITORE, TUTOR, AMMINISTRATORE_UTENTI}

	private TipoUtente tipo;
	
	// Costruttore
	public UtenteDTO() {}
	
	public UtenteDTO(String nome, String cognome, String email, String password, 
			LocalDate dataNascita, String indirizzo, TipoUtente tipo) {
		this.nome = nome;
		this.cognome = cognome;
		this.email = email;
		this.password = password;
		this.dataNascita = dataNascita;
		this.indirizzo = indirizzo;
		this.tipo = tipo;
	}

	//Getters e Setters
	public String getNome() {
		return nome;
	}

	public int getUID() {
		return UID;
	}

	public void setUID(int uID) {
		UID = uID;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getCognome() {
		return cognome;
	}

	public void setCognome(String cognome) {
		this.cognome = cognome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDate getDataNascita() {
		return dataNascita;
	}

	public void setDataNascita(LocalDate dataNascita) {
		this.dataNascita = dataNascita;
	}

	public String getIndirizzo() {
		return indirizzo;
	}

	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}

	public TipoUtente getTipo() {
		return tipo;
	}

	public void setTipo(TipoUtente tipo) {
		this.tipo = tipo;
	}
	
	
}
