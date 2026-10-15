<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Gate Scanner - Digital Pass Verification</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>

    <!-- Header Navigation -->
    <header>
        <div class="header-container">
            <a href="${pageContext.request.contextPath}/home" class="logo-group" style="text-decoration: none;">
                <span class="logo-badge">DP</span>
                <div>
                    <div class="brand-title">Digital Pass Generator</div>
                    <div class="brand-subtitle">Gate Entry Terminal</div>
                </div>
            </a>
            <nav class="nav-links">
                <a href="${pageContext.request.contextPath}/home" class="nav-link">&larr; Back to Registration</a>
            </nav>
        </div>
    </header>

    <main>

        <section class="hero-section" style="text-align: center; margin-bottom: 2rem;">
            <h1 class="hero-title">Gate Verification Terminal</h1>
            <p class="hero-description" style="margin: 0 auto;">
                Admin check-in terminal. Scan or enter attendee pass code to admit participants.
            </p>
        </section>

        <div style="max-width: 580px; margin: 0 auto;">

            <!-- Scanner Form Card -->
            <div class="card" style="margin-bottom: 2rem;">
                <form action="${pageContext.request.contextPath}/verify-pass" method="post" class="lookup-form">
                    <div class="form-group">
                        <label for="passCode">Scan or Enter Pass Code</label>
                        <input type="text" id="passCode" name="passCode"
                               value="${scannedCode}"
                               placeholder="e.g. DP-8K3M2QX7"
                               required
                               autofocus
                               class="form-control"
                               style="font-family: monospace; font-size: 1.25rem; text-transform: uppercase; letter-spacing: 1px;">
                    </div>
                    <button type="submit" class="btn btn-primary btn-block" style="font-size: 1.05rem;">
                        ⚡ Check-In Attendee
                    </button>
                </form>
            </div>

            <!-- Verification Result Display -->
            <c:if test="${not empty verificationResult}">
                <c:choose>
                    <c:when test="${verificationResult == 'VALID'}">
                        <div class="card" style="border-left: 6px solid var(--success); background: #f0fdf4;">
                            <div style="display: flex; align-items: center; gap: 0.75rem; margin-bottom: 1rem;">
                                <span style="font-size: 2rem;">✅</span>
                                <div>
                                    <h2 style="color: var(--success); margin: 0; font-size: 1.4rem;">CHECK-IN SUCCESSFUL</h2>
                                    <p style="margin: 0; color: #166534; font-size: 0.9rem;">Pass validated &amp; participant admitted</p>
                                </div>
                            </div>
                            <c:if test="${not empty scannedPass}">
                                <div style="background: #ffffff; padding: 1rem; border-radius: 8px; border: 1px solid #bbf7d0;">
                                    <p><strong>Name:</strong> ${scannedPass.name} (PRN: <code>${scannedPass.prn}</code>)</p>
                                    <p><strong>Event:</strong> ${scannedPass.eventName}</p>
                                    <c:if test="${not empty scannedPass.teamName}">
                                        <p><strong>Team:</strong> ${scannedPass.teamName} (${scannedPass.memberRole})</p>
                                    </c:if>
                                    <p><strong>Pass Code:</strong> <code>${scannedPass.passCode}</code></p>
                                </div>
                            </c:if>
                        </div>
                    </c:when>

                    <c:otherwise>
                        <div class="card" style="border-left: 6px solid var(--danger); background: #fef2f2;">
                            <div style="display: flex; align-items: center; gap: 0.75rem;">
                                <span style="font-size: 2rem;">❌</span>
                                <div>
                                    <h2 style="color: var(--danger); margin: 0; font-size: 1.4rem;">ENTRY REJECTED</h2>
                                    <p style="margin: 0; color: #991b1b; font-size: 0.9rem;">
                                        ${errorMessage}
                                    </p>
                                </div>
                            </div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:if>

        </div>

    </main>

    <footer>
        <div class="footer-container">
            <p>&copy; 2026 Digital Pass Generator &bull; Secure Event Access Management</p>
        </div>
    </footer>

</body>
</html>
