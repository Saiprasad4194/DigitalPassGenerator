package com.digitalpass.servlet;

import com.digitalpass.dao.DBConnection;
import com.digitalpass.dao.EventDAO;
import com.digitalpass.dao.RegistrationDAO;
import com.digitalpass.model.Event;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Controller servlet for the Home / Event Registration landing page.
 *
 * Architecture Role: Servlet Controller
 * - Rule: NO SQL queries inside servlets.
 * - Enforces clean separation: JSP -> Servlet -> DAO -> MySQL.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home", "/index"})
public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final EventDAO eventDAO = new EventDAO();
    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try (Connection conn = DBConnection.getConnection()) {
            boolean isConnected = (conn != null && !conn.isClosed());

            if (isConnected) {
                int regCount = registrationDAO.getTotalRegistrationCount();
                List<Event> events = eventDAO.getOpenEvents();

                request.setAttribute("dbConnected", true);
                request.setAttribute("registrationCount", regCount);
                request.setAttribute("events", events);
            } else {
                request.setAttribute("dbConnected", false);
                request.setAttribute("errorMessage", "Database is not connected.");
            }

        } catch (SQLException e) {
            request.setAttribute("dbConnected", false);
            request.setAttribute("errorMessage", "Database SQL Error: " + e.getMessage());
        } catch (Exception e) {
            request.setAttribute("dbConnected", false);
            request.setAttribute("errorMessage", "Unexpected Error: " + e.getMessage());
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
