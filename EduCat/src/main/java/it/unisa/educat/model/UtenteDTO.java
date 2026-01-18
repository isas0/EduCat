package it.unisa.educat.model;

import java.time.LocalDate;

public class UtenteDTO {
	private int UID;
	private String nome;
	private String cognome;
	private String email;
	private String password; // Hashed
	private String dataNascita;
	
	private String via;
	private String città;
	private String CAP;
	private String civico;
	
	
	private String nomeFiglio;
	private String cognomeFiglio;
	private String dataNascitaFiglio;
	
	public enum TipoUtente{STUDENTE, GENITORE, TUTOR, AMMINISTRATORE_UTENTI}

	private TipoUtente tipo;
	
	// Costruttore
	public UtenteDTO() {}
	
	public UtenteDTO(int uID, String nome, String cognome, String email, String password, String dataNascita,
			String via, String città, String CAP, String civico, TipoUtente tipo) {
		super();
		UID = uID;
		this.nome = nome;
		this.cognome = cognome;
		this.email = email;
		this.password = password;
		this.dataNascita = dataNascita;
		this.via = via;
		this.città = città;
		this.CAP = CAP;
		this.civico = civico;
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

	public String getDataNascita() {
		return dataNascita;
	}

	public void setDataNascita(String dataNascita) {
		this.dataNascita = dataNascita;
	}

	

	public String getVia() {
		return via;
	}

	public void setVia(String via) {
		this.via = via;
	}

	public String getCittà() {
		return città;
	}

	public void setCittà(String città) {
		this.città = città;
	}

	public String getCAP() {
		return CAP;
	}

	public void setCAP(String cAP) {
		CAP = cAP;
	}

	public String getCivico() {
		return civico;
	}

	public void setCivico(String civico) {
		this.civico = civico;
	}

	public TipoUtente getTipo() {
		return tipo;
	}

	public void setTipo(TipoUtente tipo) {
		this.tipo = tipo;
	}

	public String getNomeFiglio() {
		return nomeFiglio;
	}

	public void setNomeFiglio(String nomeFiglio) {
		this.nomeFiglio = nomeFiglio;
	}

	public String getCognomeFiglio() {
		return cognomeFiglio;
	}

	public void setCognomeFiglio(String cognomeFiglio) {
		this.cognomeFiglio = cognomeFiglio;
	}

	public String getDataNascitaFiglio() {
		return dataNascitaFiglio;
	}

	public void setDataNascitaFiglio(String dataNascitaFiglio) {
		this.dataNascitaFiglio = dataNascitaFiglio;
	}
	
	
}
