<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:if test="${empty dbConnected && empty errorMessage}">
    <c:redirect url="/home" />
</c:if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Digital Pass Generator - Event Registration</title>
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
                    <div class="brand-subtitle">Campus Event Passes</div>
                </div>
            </a>
            <nav class="nav-links">
                <a href="#register" class="nav-link active">Register Pass</a>
                <a href="#lookup" class="nav-link">Find My Pass</a>
                <a href="${pageContext.request.contextPath}/verify-pass" class="nav-link nav-btn">Gate Scanner</a>
            </nav>
        </div>
    </header>

    <main>

        <!-- System Status Pill -->
        <c:choose>
            <c:when test="${dbConnected == true}">
                <div class="status-pill status-pill-success">
                    <span class="status-dot"></span>
                    <span>System Online &bull; ${registrationCount} Passes Issued</span>
                </div>
            </c:when>
            <c:otherwise>
                <div class="status-pill status-pill-error">
                    <span class="status-dot"></span>
                    <span>Database Offline. Please check MySQL connection.</span>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- Notification Alert -->
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-error">
                <strong>Notice:</strong> ${errorMessage}
            </div>
        </c:if>

        <!-- Hero Section -->
        <section class="hero-section">
            <h1 class="hero-title">Get Your Event Pass Instantly</h1>
            <p class="hero-description">
                Select your event, choose your team size, and fill in student details to generate official digital entry passes.
            </p>
        </section>

        <!-- Registration Layout -->
        <div class="registration-layout">

            <!-- Registration Form Card -->
            <div class="card registration-card" id="register">
                <div class="card-header">
                    <h2>Pass Registration</h2>
                    <p>Select an event and specify your team size to register</p>
                </div>

                <form action="${pageContext.request.contextPath}/generate-pass" method="post" class="pass-form" id="passForm">
                    
                    <!-- Step 1: Event Selection -->
                    <div class="form-group">
                        <label for="eventId">1. Select Campus Event <span class="required">*</span></label>
                        <select name="eventId" id="eventId" required class="form-control" onchange="onEventChanged()">
                            <option value="">-- Choose an Event --</option>
                            <c:forEach var="evt" items="${events}">
                                <option value="${evt.id}"
                                        data-type="${evt.participationType}"
                                        data-min="${evt.minTeamSize}"
                                        data-max="${evt.maxTeamSize}"
                                        data-name="${evt.eventName}">
                                    ${evt.eventName} &bull; [${evt.participationType}] &bull; Max Team: ${evt.maxTeamSize}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <!-- Step 2: Team Size Selector -->
                    <div class="form-group" id="teamSizeGroup" style="display: none;">
                        <label for="teamSize">2. Select Your Team Size (Total Members) <span class="required">*</span></label>
                        <select name="teamSize" id="teamSize" class="form-control" onchange="renderStudentSections()">
                            <!-- Dynamically populated via JavaScript based on event max team size -->
                        </select>
                        <span id="teamSizeHint" style="font-size: 0.775rem; color: var(--text-muted); margin-top: 0.2rem;"></span>
                    </div>

                    <!-- Step 3: Team Name (Displayed if teamSize > 1) -->
                    <div class="form-group" id="teamNameBox" style="display: none;">
                        <label for="teamName">Team Name <span class="required">*</span></label>
                        <input type="text" id="teamName" name="teamName" placeholder="e.g. CodeCommanders" class="form-control">
                        <span style="font-size: 0.75rem; color: var(--text-muted);">This team name will appear on all team members' entry passes</span>
                    </div>

                    <!-- Step 4: Dynamic Student Member Sections Container -->
                    <div id="studentsContainer" style="display: flex; flex-direction: column; gap: 1.25rem;">
                        <!-- Will be populated dynamically by JavaScript -->
                    </div>

                    <!-- Submit Button -->
                    <button type="submit" id="submitBtn" class="btn btn-primary btn-block" style="display: none; margin-top: 0.5rem; font-size: 1.05rem;">
                        Generate Digital Passes &rarr;
                    </button>
                </form>
            </div>

            <!-- Sidebar -->
            <div class="sidebar-layout">

                <!-- Find Existing Pass Card -->
                <div class="card" id="lookup">
                    <div class="card-header">
                        <h3>Already Registered?</h3>
                        <p>Retrieve and re-print your existing pass</p>
                    </div>

                    <form action="${pageContext.request.contextPath}/lookup-pass" method="post" class="lookup-form">
                        <div class="form-group">
                            <label for="query">Pass Code, PRN, or Email</label>
                            <input type="text" id="query" name="query" placeholder="e.g. DP-8K3M2QX7 or PRN" required class="form-control">
                        </div>
                        <button type="submit" class="btn btn-secondary btn-block">
                            Find My Pass
                        </button>
                    </form>
                </div>

                <!-- Upcoming Events Schedule -->
                <div class="card">
                    <div class="card-header">
                        <h3>Active Events</h3>
                        <p>Available for registration</p>
                    </div>

                    <div class="event-mini-list">
                        <c:forEach var="evt" items="${events}">
                            <div class="event-mini-item">
                                <div class="event-badge">${evt.participationType}</div>
                                <div class="event-mini-name">${evt.eventName}</div>
                                <div class="event-mini-meta">
                                    <span>📍 ${evt.location}</span> &bull; 
                                    <span>Max Team: ${evt.maxTeamSize}</span>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </div>

            </div>

        </div>

    </main>

    <footer>
        <div class="footer-container">
            <p>&copy; 2026 Digital Pass Generator &bull; Secure Event Access Management</p>
        </div>
    </footer>

    <!-- Dynamic Team Size and Member Sections Script -->
    <script>
        function onEventChanged() {
            var eventSelect = document.getElementById('eventId');
            var selectedOption = eventSelect.options[eventSelect.selectedIndex];
            var teamSizeGroup = document.getElementById('teamSizeGroup');
            var teamSizeSelect = document.getElementById('teamSize');
            var submitBtn = document.getElementById('submitBtn');
            var teamSizeHint = document.getElementById('teamSizeHint');

            if (!eventSelect.value) {
                teamSizeGroup.style.display = 'none';
                document.getElementById('teamNameBox').style.display = 'none';
                document.getElementById('studentsContainer').innerHTML = '';
                submitBtn.style.display = 'none';
                return;
            }

            var maxTeam = parseInt(selectedOption.getAttribute('data-max') || '1', 10);
            var minTeam = parseInt(selectedOption.getAttribute('data-min') || '1', 10);
            var partType = selectedOption.getAttribute('data-type') || 'INDIVIDUAL';

            // Clear old options
            teamSizeSelect.innerHTML = '';

            // Generate options from 1 up to maxTeam
            for (var i = 1; i <= maxTeam; i++) {
                var opt = document.createElement('option');
                opt.value = i;
                if (i === 1) {
                    opt.text = '1 Person (Solo Entry)';
                } else {
                    opt.text = i + ' Members (1 Leader + ' + (i - 1) + ' Member' + (i > 2 ? 's' : '') + ')';
                }
                teamSizeSelect.appendChild(opt);
            }

            // Set default: if event requires min team > 1, select that, otherwise 1
            if (minTeam > 1 && partType === 'TEAM') {
                teamSizeSelect.value = minTeam;
                teamSizeHint.textContent = 'Note: This event requires at least ' + minTeam + ' members per team (up to ' + maxTeam + ').';
            } else {
                teamSizeSelect.value = 1;
                teamSizeHint.textContent = 'Choose 1 for Solo or up to ' + maxTeam + ' for a Team.';
            }

            teamSizeGroup.style.display = 'block';
            submitBtn.style.display = 'block';

            renderStudentSections();
        }

        function renderStudentSections() {
            var teamSize = parseInt(document.getElementById('teamSize').value, 10);
            var teamNameBox = document.getElementById('teamNameBox');
            var teamNameInput = document.getElementById('teamName');
            var container = document.getElementById('studentsContainer');

            // Show or hide Team Name box
            if (teamSize > 1) {
                teamNameBox.style.display = 'block';
                teamNameInput.setAttribute('required', 'required');
            } else {
                teamNameBox.style.display = 'none';
                teamNameInput.removeAttribute('required');
                teamNameInput.value = '';
            }

            // Build dynamic sections HTML
            var html = '';

            // SECTION 1: Team Leader (or Solo Attendee)
            // Fields: Name, Phone, PRN, Email
            var leaderTitle = (teamSize > 1) ? '👑 Section 1: Team Leader (Student #1)' : '👤 Participant Details (Solo Entry)';
            var leaderSubtitle = (teamSize > 1) ? 'Leader is primary point of contact & recipient of team communications' : 'Official registration details';

            html += '<div class="member-section member-section-leader">';
            html += '  <div class="member-section-header">';
            html += '    <h3 class="member-section-title">' + leaderTitle + '</h3>';
            html += '    <span class="member-role-badge badge-leader">' + (teamSize > 1 ? 'LEADER' : 'SOLO') + '</span>';
            html += '  </div>';
            html += '  <p class="member-section-desc">' + leaderSubtitle + '</p>';
            html += '  <div class="member-grid-leader">';
            html += '    <div class="form-group">';
            html += '      <label>Full Name <span class="required">*</span></label>';
            html += '      <input type="text" name="leaderName" placeholder="e.g. Aarav Sharma" required class="form-control">';
            html += '    </div>';
            html += '    <div class="form-group">';
            html += '      <label>Contact Phone Number <span class="required">*</span></label>';
            html += '      <input type="tel" name="leaderPhone" placeholder="e.g. 9876543210" required class="form-control">';
            html += '    </div>';
            html += '    <div class="form-group">';
            html += '      <label>Student PRN / Roll No <span class="required">*</span></label>';
            html += '      <input type="text" name="leaderPrn" placeholder="e.g. PRN2024001" required class="form-control" style="text-transform: uppercase;">';
            html += '    </div>';
            html += '    <div class="form-group">';
            html += '      <label>College Email <span class="required">*</span></label>';
            html += '      <input type="email" name="leaderEmail" placeholder="e.g. aarav@student.edu" required class="form-control">';
            html += '    </div>';
            html += '  </div>';
            html += '</div>';

            // SECTIONS 2 to N: Team Members
            // Fields: Name, PRN, Email ONLY (Phone is not needed as specified)
            for (var i = 2; i <= teamSize; i++) {
                html += '<div class="member-section">';
                html += '  <div class="member-section-header">';
                html += '    <h3 class="member-section-title">👤 Section ' + i + ': Team Member #' + i + '</h3>';
                html += '    <span class="member-role-badge badge-member">MEMBER</span>';
                html += '  </div>';
                html += '  <p class="member-section-desc">Entry pass will be generated specifically under this member\'s PRN</p>';
                html += '  <div class="member-grid-member">';
                html += '    <div class="form-group">';
                html += '      <label>Member Full Name <span class="required">*</span></label>';
                html += '      <input type="text" name="memberName_' + i + '" placeholder="e.g. Diya Patel" required class="form-control">';
                html += '    </div>';
                html += '    <div class="form-group">';
                html += '      <label>Member PRN / Roll No <span class="required">*</span></label>';
                html += '      <input type="text" name="memberPrn_' + i + '" placeholder="e.g. PRN202400' + i + '" required class="form-control" style="text-transform: uppercase;">';
                html += '    </div>';
                html += '    <div class="form-group" style="grid-column: 1 / -1;">';
                html += '      <label>Member College Email <span class="required">*</span></label>';
                html += '      <input type="email" name="memberEmail_' + i + '" placeholder="e.g. member' + i + '@student.edu" required class="form-control">';
                html += '    </div>';
                html += '  </div>';
                html += '</div>';
            }

            container.innerHTML = html;
        }
    </script>

</body>
</html>
