<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Digital Pass - ${registration.eventName}</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        @media print {
            header, footer, .pass-actions, .nav-links, .status-pill, .alert, .no-print {
                display: none !important;
            }
            body {
                background: #ffffff !important;
            }
            .pass-card {
                box-shadow: none !important;
                border: 2px solid #000000 !important;
                page-break-after: always;
                margin-bottom: 2rem !important;
            }
            .pass-wrapper {
                max-width: 100% !important;
            }
        }
    </style>
</head>
<body>

    <!-- Header Navigation -->
    <header>
        <div class="header-container">
            <a href="${pageContext.request.contextPath}/home" class="logo-group" style="text-decoration: none;">
                <span class="logo-badge">DP</span>
                <div>
                    <div class="brand-title">Digital Pass Generator</div>
                    <div class="brand-subtitle">Campus Event Passes</div>
                </div>
            </a>
            <nav class="nav-links">
                <a href="${pageContext.request.contextPath}/home" class="nav-link">&larr; Back to Registration</a>
                <a href="${pageContext.request.contextPath}/verify-pass" class="nav-link nav-btn">Gate Scanner</a>
            </nav>
        </div>
    </header>

    <main>

        <c:if test="${justGenerated == true}">
            <div class="alert alert-success" style="max-width: 680px; margin: 0 auto 1.5rem auto;">
                🎉 <strong>Registration Successful!</strong>
                <c:choose>
                    <c:when test="${not empty teamPasses}">
                        Official digital entry passes for all <strong>${teamPasses.size()} team members</strong> have been issued.
                    </c:when>
                    <c:otherwise>
                        Your official digital entry pass has been generated.
                    </c:otherwise>
                </c:choose>
            </div>
        </c:if>

        <div class="pass-wrapper">

            <!-- Team Header Summary if multiple passes generated -->
            <c:if test="${not empty teamPasses && teamPasses.size() > 1}">
                <div class="card no-print" style="margin-bottom: 1.5rem; text-align: center; background: #eff6ff; border-color: #bfdbfe;">
                    <h2 style="color: var(--primary); margin-bottom: 0.25rem;">
                        🏆 Team: ${registration.teamName}
                    </h2>
                    <p style="color: var(--text-muted); font-size: 0.95rem;">
                        ${registration.eventName} &bull; ${teamPasses.size()} Member Passes Generated
                    </p>
                    <div style="margin-top: 1rem;">
                        <button onclick="window.print()" class="btn btn-primary">
                            🖨️ Print All ${teamPasses.size()} Passes
                        </button>
                    </div>
                </div>
            </c:if>

            <!-- Render Pass Cards (Iterates over teamPasses if present, else renders single registration) -->
            <div class="team-passes-container">

                <c:set var="passList" value="${not empty teamPasses ? teamPasses : [registration]}" />

                <c:forEach var="item" items="${passList}" varStatus="status">

                    <div class="pass-card" id="pass-${item.passCode}">

                        <!-- Ticket Header -->
                        <div class="pass-header ${item.memberRole == 'LEADER' ? 'pass-header-vip' : ''}">
                            <div class="pass-header-top">
                                <span class="pass-org">COLLEGE CAMPUS EVENT ENTRY PASS</span>
                                <span class="pass-type-badge ${item.memberRole == 'LEADER' ? 'badge-vip' : 'badge-general'}">
                                    ${item.memberRole} PASS
                                </span>
                            </div>
                            <h1 class="pass-event-title">${item.eventName}</h1>
                            <div class="pass-event-meta">
                                <span>📍 ${item.location}</span>
                            </div>
                        </div>

                        <!-- Perforated Tear Line -->
                        <div class="pass-tear-line">
                            <div class="tear-notch-left"></div>
                            <div class="tear-dashes"></div>
                            <div class="tear-notch-right"></div>
                        </div>

                        <!-- Ticket Body -->
                        <div class="pass-body">

                            <!-- Pass Code Callout -->
                            <div class="pass-code-banner">
                                <div class="pass-code-label">OFFICIAL ENTRY PASS CODE</div>
                                <div class="pass-code-value">${item.passCode}</div>
                                <div class="pass-status-pill status-${item.status.toLowerCase()}">
                                    ● ${item.status}
                                </div>
                            </div>

                            <!-- Details Grid -->
                            <div class="pass-details-grid">
                                <div class="detail-block">
                                    <span class="detail-label">PARTICIPANT NAME</span>
                                    <span class="detail-value">${item.name}</span>
                                </div>
                                <div class="detail-block">
                                    <span class="detail-label">STUDENT PRN</span>
                                    <span class="detail-value" style="font-family: monospace;">${item.prn}</span>
                                </div>
                                <div class="detail-block">
                                    <span class="detail-label">COLLEGE EMAIL</span>
                                    <span class="detail-value">${item.email}</span>
                                </div>
                                <div class="detail-block">
                                    <span class="detail-label">CONTACT PHONE</span>
                                    <span class="detail-value">${not empty item.phone ? item.phone : 'N/A'}</span>
                                </div>
                                <c:if test="${not empty item.teamName}">
                                    <div class="detail-block">
                                        <span class="detail-label">TEAM NAME</span>
                                        <span class="detail-value" style="color: var(--primary);">${item.teamName}</span>
                                    </div>
                                    <div class="detail-block">
                                        <span class="detail-label">TEAM ROLE</span>
                                        <span class="detail-value">${item.memberRole}</span>
                                    </div>
                                </c:if>
                                <div class="detail-block">
                                    <span class="detail-label">EVENT DATE</span>
                                    <span class="detail-value">
                                        <fmt:formatDate value="${item.eventDate}" pattern="dd MMM yyyy, hh:mm a" />
                                    </span>
                                </div>
                            </div>

                            <!-- Barcode Graphic -->
                            <div class="barcode-container">
                                <div class="barcode-lines">
                                    <span></span><span></span><span></span><span></span><span></span>
                                    <span></span><span></span><span></span><span></span><span></span>
                                    <span></span><span></span><span></span><span></span><span></span>
                                    <span></span><span></span><span></span><span></span><span></span>
                                    <span></span><span></span><span></span><span></span><span></span>
                                    <span></span><span></span><span></span><span></span><span></span>
                                    <span></span><span></span><span></span><span></span><span></span>
                                    <span></span><span></span><span></span><span></span><span></span>
                                </div>
                                <div class="barcode-code">${item.passCode} &bull; ${item.prn}</div>
                            </div>

                        </div>

                    </div>

                </c:forEach>

            </div>

            <!-- Global Action Buttons -->
            <div class="pass-actions no-print">
                <button onclick="window.print()" class="btn btn-primary">
                    🖨️ Print / Save PDF
                </button>
                <a href="${pageContext.request.contextPath}/home" class="btn btn-secondary">
                    + Register Another Pass
                </a>
            </div>

        </div>

    </main>

    <footer>
        <div class="footer-container">
            <p>&copy; 2026 Digital Pass Generator &bull; Secure Event Access Management</p>
        </div>
    </footer>

</body>
</html>
