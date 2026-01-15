package it.unisa.educat.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import it.unisa.educat.dao.DatasourceManager;

@WebServlet("/ProvaConnessione")
public class ProvaConnessione extends HttpServlet {
	private static final long serialVersionUID = 1L;
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Connection conn = DatasourceManager.getConnection();
			if(conn!=null) {
				System.out.println("Tutto a posto");
				conn.close();
			}
			else 
				System.out.println("null");
			
		} catch (SQLException e) {
			System.out.println("Errore");
			e.printStackTrace();
		}
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

}