package com.digitalpass.servlet;

import com.digitalpass.dao.EventDAO;
import com.digitalpass.dao.RegistrationDAO;
import com.digitalpass.dao.TeamDAO;
import com.digitalpass.model.Event;
import com.digitalpass.model.Registration;
import com.digitalpass.util.AppException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller servlet for Public User Pass Registration, Dynamic Team Registration,
 * Pass Lookup, and Gate Check-In.
 *
 * Architecture Role: Servlet Controller (JSP -> Servlet -> DAO -> MySQL).
 * Rule: NO SQL queries inside servlets.
 */
@WebServlet(name = "PassServlet", urlPatterns = {"/generate-pass", "/view-pass", "/lookup-pass", "/verify-pass"})
public class PassServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final EventDAO eventDAO = new EventDAO();
    private final TeamDAO teamDAO = new TeamDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        try {
            if ("/view-pass".equals(path)) {
                String code = request.getParameter("code");
                if (code != null && !code.trim().isEmpty()) {
                    Registration reg = registrationDAO.getRegistrationByPassCode(code.trim().toUpperCase());
                    if (reg != null) {
                        request.setAttribute("registration", reg);
                        if (reg.getTeamId() != null) {
                            List<Registration> teamPasses = registrationDAO.getRegistrationsByTeamId(reg.getTeamId());
                            request.setAttribute("teamPasses", teamPasses);
                            request.setAttribute("isTeamRegistration", true);
                        }
                        request.getRequestDispatcher("/pass-view.jsp").forward(request, response);
                        return;
                    }
                }
                request.setAttribute("errorMessage", "Digital pass not found for code: " + code);
                request.getRequestDispatcher("/home").forward(request, response);

            } else if ("/verify-pass".equals(path)) {
                request.getRequestDispatcher("/verify.jsp").forward(request, response);

            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }

        } catch (SQLException e) {
            request.setAttribute("errorMessage", "Database error: " + e.getMessage());
            request.getRequestDispatcher("/home").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getServletPath();

        try {
            if ("/generate-pass".equals(path)) {
                String eventIdStr = request.getParameter("eventId");
                String teamSizeStr = request.getParameter("teamSize");

                if (eventIdStr == null || eventIdStr.trim().isEmpty()) {
                    throw new AppException("Please select an event.");
                }

                int eventId = Integer.parseInt(eventIdStr.trim());
                int teamSize = 1;
                if (teamSizeStr != null && !teamSizeStr.trim().isEmpty()) {
                    try {
                        teamSize = Integer.parseInt(teamSizeStr.trim());
                    } catch (NumberFormatException ignored) {
                        teamSize = 1;
                    }
                }

                // Leader / Solo attendee fields
                String leaderName = request.getParameter("leaderName");
                String leaderPhone = request.getParameter("leaderPhone");
                String leaderPrn = request.getParameter("leaderPrn");
                String leaderEmail = request.getParameter("leaderEmail");

                // Fallback to legacy field names if submitted
                if (leaderName == null) leaderName = request.getParameter("name");
                if (leaderPhone == null) leaderPhone = request.getParameter("phone");
                if (leaderPrn == null) leaderPrn = request.getParameter("prn");
                if (leaderEmail == null) leaderEmail = request.getParameter("email");

                if (leaderName == null || leaderPrn == null || leaderEmail == null || leaderPhone == null ||
                    leaderName.trim().isEmpty() || leaderPrn.trim().isEmpty() ||
                    leaderEmail.trim().isEmpty() || leaderPhone.trim().isEmpty()) {
                    throw new AppException("Please fill in all required fields for the Team Leader / Solo attendee.");
                }

                Set<String> prnSet = new HashSet<>();
                prnSet.add(leaderPrn.trim().toUpperCase());

                if (teamSize <= 1) {
                    // Solo registration
                    Registration reg = registrationDAO.registerSolo(eventId, leaderName, leaderPhone, leaderPrn, leaderEmail);
                    request.setAttribute("registration", reg);
                    request.setAttribute("justGenerated", true);

                } else {
                    // Team registration
                    String teamName = request.getParameter("teamName");
                    if (teamName == null || teamName.trim().isEmpty()) {
                        throw new AppException("Please provide a Team Name for your team.");
                    }

                    // Validate all team members have name, prn, email
                    for (int i = 2; i <= teamSize; i++) {
                        String memberName = request.getParameter("memberName_" + i);
                        String memberPrn = request.getParameter("memberPrn_" + i);
                        String memberEmail = request.getParameter("memberEmail_" + i);

                        if (memberName == null || memberPrn == null || memberEmail == null ||
                            memberName.trim().isEmpty() || memberPrn.trim().isEmpty() || memberEmail.trim().isEmpty()) {
                            throw new AppException("Please fill in Name, PRN, and Email for Team Member #" + i + ".");
                        }

                        String cleanPrn = memberPrn.trim().toUpperCase();
                        if (!prnSet.add(cleanPrn)) {
                            throw new AppException("Duplicate PRN detected: " + cleanPrn + ". Each student in the team must have a unique PRN.");
                        }
                    }

                    // 1. Register Team Leader and create the team
                    Registration leaderReg = registrationDAO.registerTeam(
                        eventId, teamName.trim(), leaderName.trim(), leaderPhone.trim(), leaderPrn.trim(), leaderEmail.trim()
                    );
                    int teamId = leaderReg.getTeamId();

                    // 2. Register each Team Member (Members use leader's contact phone)
                    for (int i = 2; i <= teamSize; i++) {
                        String memberName = request.getParameter("memberName_" + i);
                        String memberPrn = request.getParameter("memberPrn_" + i);
                        String memberEmail = request.getParameter("memberEmail_" + i);

                        registrationDAO.registerTeamMember(
                            eventId, teamId, memberName.trim(), leaderPhone.trim(), memberPrn.trim(), memberEmail.trim()
                        );
                    }

                    // 3. Fetch all generated passes for the team
                    List<Registration> teamPasses = registrationDAO.getRegistrationsByTeamId(teamId);
                    request.setAttribute("registration", teamPasses.get(0)); // Leader pass as primary
                    request.setAttribute("teamPasses", teamPasses);
                    request.setAttribute("isTeamRegistration", true);
                    request.setAttribute("justGenerated", true);
                }

                request.getRequestDispatcher("/pass-view.jsp").forward(request, response);

            } else if ("/lookup-pass".equals(path)) {
                String query = request.getParameter("query");
                if (query == null || query.trim().isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/home");
                    return;
                }

                String clean = query.trim();
                if (clean.toUpperCase().startsWith("DP-")) {
                    Registration reg = registrationDAO.getRegistrationByPassCode(clean.toUpperCase());
                    if (reg != null) {
                        request.setAttribute("registration", reg);
                        if (reg.getTeamId() != null) {
                            List<Registration> teamPasses = registrationDAO.getRegistrationsByTeamId(reg.getTeamId());
                            request.setAttribute("teamPasses", teamPasses);
                            request.setAttribute("isTeamRegistration", true);
                        }
                        request.getRequestDispatcher("/pass-view.jsp").forward(request, response);
                        return;
                    }
                }

                List<Registration> list = registrationDAO.getRegistrationsByPrnOrEmail(clean);
                if (list != null && !list.isEmpty()) {
                    Registration first = list.get(0);
                    request.setAttribute("registration", first);
                    if (first.getTeamId() != null) {
                        List<Registration> teamPasses = registrationDAO.getRegistrationsByTeamId(first.getTeamId());
                        request.setAttribute("teamPasses", teamPasses);
                        request.setAttribute("isTeamRegistration", true);
                    }
                    request.setAttribute("multiplePasses", list);
                    request.getRequestDispatcher("/pass-view.jsp").forward(request, response);
                } else {
                    request.setAttribute("errorMessage", "No registered pass found for: " + clean);
                    request.getRequestDispatcher("/home").forward(request, response);
                }

            } else if ("/verify-pass".equals(path)) {
                String passCode = request.getParameter("passCode");
                if (passCode == null || passCode.trim().isEmpty()) {
                    response.sendRedirect(request.getContextPath() + "/verify-pass");
                    return;
                }

                try {
                    Registration reg = registrationDAO.checkInPass(passCode.trim().toUpperCase(), 1);
                    request.setAttribute("verificationResult", "VALID");
                    request.setAttribute("scannedPass", reg);
                    request.setAttribute("scannedCode", passCode.trim().toUpperCase());
                } catch (AppException e) {
                    request.setAttribute("verificationResult", "REJECTED");
                    request.setAttribute("errorMessage", e.getMessage());
                    request.setAttribute("scannedCode", passCode.trim().toUpperCase());
                }

                request.getRequestDispatcher("/verify.jsp").forward(request, response);

            } else {
                response.sendRedirect(request.getContextPath() + "/home");
            }

        } catch (AppException e) {
            request.setAttribute("errorMessage", e.getMessage());
            request.getRequestDispatcher("/home").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("errorMessage", "Database error: " + e.getMessage());
            request.getRequestDispatcher("/home").forward(request, response);
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "Invalid input parameters.");
            request.getRequestDispatcher("/home").forward(request, response);
        }
    }
}
