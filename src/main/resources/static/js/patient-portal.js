document.addEventListener('DOMContentLoaded', () => {
  const loginForm = document.getElementById('loginForm');
  const demoLoginBtn = document.getElementById('demoLoginBtn');
  const quickLoginBtn = document.getElementById('quickLoginBtn');
  const quickLoginBtn2 = document.getElementById('quickLoginBtn2');
  const logoutBtn = document.getElementById('logoutBtn');
  const dashboardSection = document.getElementById('dashboardSection');
  const loginSection = document.getElementById('loginSection');
  const portalToast = document.getElementById('portalToast');
  const deptFilter = document.getElementById('deptFilter');
  const prescriptionList = document.getElementById('prescriptionList');
  const labReportList = document.getElementById('labReportList');
  const appointmentForm = document.getElementById('appointmentForm');
  const appointmentHistory = document.getElementById('appointmentHistory');
  const paymentSummary = document.getElementById('paymentSummary');
  const welcomeUser = document.getElementById('welcomeUser');
  const patientIntro = document.getElementById('patientIntro');
  const deptCount = document.getElementById('deptCount');
  const prescriptionCount = document.getElementById('prescriptionCount');
  const labCount = document.getElementById('labCount');
  const dueAmount = document.getElementById('dueAmount');

  const state = {
    patient: {
      name: 'Priya Sharma',
      email: 'priya.sharma@example.com',
      departments: ['Cardiology', 'Radiology', 'Neurology'],
      prescriptions: [
        {
          department: 'Cardiology',
          title: 'Heart care medication',
          date: '2026-06-10',
          notes: 'Take twice daily after meals'
        },
        {
          department: 'Radiology',
          title: 'Follow-up imaging review',
          date: '2026-06-09',
          notes: 'Carry previous reports'
        },
        {
          department: 'Neurology',
          title: 'Migraine management',
          date: '2026-06-12',
          notes: 'Use as needed for pain relief'
        }
      ],
      labReports: [
        {
          title: 'Complete Blood Count',
          department: 'Pathology',
          date: '2026-06-08',
          fileName: 'cbc-report.pdf'
        },
        {
          title: 'MRI Brain Scan',
          department: 'Radiology',
          date: '2026-06-09',
          fileName: 'mri-brain-report.pdf'
        }
      ],
      appointments: [
        {
          department: 'Cardiology',
          slot: '2026-06-21 11:00 AM',
          status: 'Confirmed'
        }
      ],
      payments: [
        {
          description: 'Consultation fee',
          amount: 1200,
          status: 'Paid'
        },
        {
          description: 'Imaging advance',
          amount: 900,
          status: 'Due'
        }
      ]
    }
  };

  const departments = Array.from(new Set(state.patient.prescriptions.map(p => p.department)));

  function showToast(message, type = 'success') {
    portalToast.textContent = message;
    portalToast.className = `portal-toast is-visible ${type}`;
    window.clearTimeout(showToast.hideTimer);
    showToast.hideTimer = window.setTimeout(() => {
      portalToast.className = 'portal-toast';
    }, 3200);
  }

  function openDashboard() {
    loginSection.classList.add('hidden');
    dashboardSection.classList.remove('hidden');
    welcomeUser.textContent = `Hello, ${state.patient.name}`;
    patientIntro.textContent = 'Your patient dashboard makes it easy to manage prescriptions, reports, appointments, and payments.';
    deptCount.textContent = departments.length;
    prescriptionCount.textContent = state.patient.prescriptions.length;
    labCount.textContent = state.patient.labReports.length;
    dueAmount.textContent = `₹${state.patient.payments.filter(item => item.status === 'Due').reduce((sum, item) => sum + item.amount, 0)}`;
    renderDepartments();
    renderPrescriptions('All');
    renderLabReports();
    renderAppointments();
    renderPayments();
  }

  function closeDashboard() {
    loginSection.classList.remove('hidden');
    dashboardSection.classList.add('hidden');
  }

  function renderDepartments() {
    deptFilter.innerHTML = '<option value="All">All departments</option>' + departments.map(dept => `<option value="${dept}">${dept}</option>`).join('');
  }

  function renderPrescriptions(filter) {
    const filtered = state.patient.prescriptions.filter(p => filter === 'All' || p.department === filter);
    prescriptionList.innerHTML = filtered.map(p => {
      return `
        <article class="patient-portal-item">
          <div class="portal-card-head">
            <div>
              <h4>${p.title}</h4>
              <p>${p.department} · ${p.date}</p>
            </div>
            <span class="portal-pill info">${p.department}</span>
          </div>
          <p>${p.notes}</p>
        </article>
      `;
    }).join('');
  }

  function renderLabReports() {
    labReportList.innerHTML = state.patient.labReports.map(report => {
      return `
        <article class="patient-portal-item">
          <div class="portal-card-head">
            <div>
              <h4>${report.title}</h4>
              <p>${report.department} · ${report.date}</p>
            </div>
            <button type="button" class="hms-landing-btn hms-landing-btn-secondary-lg" data-download="${report.fileName}">Download</button>
          </div>
          <p>Report file: ${report.fileName}</p>
        </article>
      `;
    }).join('');
  }

  function renderAppointments() {
    appointmentHistory.innerHTML = state.patient.appointments.length ? state.patient.appointments.map(appointment => {
      return `
        <article class="patient-portal-item">
          <div class="portal-card-head">
            <div>
              <h4>${appointment.department}</h4>
              <p>${appointment.slot}</p>
            </div>
            <span class="portal-pill success">${appointment.status}</span>
          </div>
        </article>
      `;
    }).join('') : '<p>No upcoming appointments yet.</p>';
  }

  function renderPayments() {
    paymentSummary.innerHTML = state.patient.payments.map(payment => {
      const statusClass = payment.status === 'Paid' ? 'success' : 'warning';
      return `
        <article class="patient-portal-item">
          <div class="portal-card-head">
            <div>
              <h4>${payment.description}</h4>
              <p>${payment.status}</p>
            </div>
            <strong>₹${payment.amount}</strong>
          </div>
          <div class="patient-portal-action-row">
            <button type="button" class="hms-landing-btn hms-landing-btn-secondary-lg" data-pay="${payment.description}">${payment.status === 'Paid' ? 'View Receipt' : 'Pay Now'}</button>
          </div>
        </article>
      `;
    }).join('');
  }

  loginForm.addEventListener('submit', event => {
    event.preventDefault();
    showToast('Logged in successfully. Dashboard loaded.');
    openDashboard();
  });

  demoLoginBtn.addEventListener('click', () => {
    showToast('Demo login activated.');
    openDashboard();
  });

  quickLoginBtn && quickLoginBtn.addEventListener('click', () => {
    showToast('Demo login activated.');
    closeDashboard();
    openDashboard();
  });

  quickLoginBtn2 && quickLoginBtn2.addEventListener('click', () => {
    showToast('Demo login activated.');
    openDashboard();
  });

  logoutBtn.addEventListener('click', () => {
    showToast('Logged out.');
    closeDashboard();
  });

  deptFilter.addEventListener('change', event => {
    renderPrescriptions(event.target.value);
  });

  appointmentForm.addEventListener('submit', event => {
    event.preventDefault();
    const department = document.getElementById('appointmentDepartment').value;
    const slot = document.getElementById('appointmentSlot').value;
    const reason = document.getElementById('appointmentReason').value.trim();
    if (!slot) {
      showToast('Please choose a date and time.', 'error');
      return;
    }

    state.patient.appointments.push({
      department,
      slot: new Date(slot).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }),
      status: 'Confirmed'
    });
    renderAppointments();
    showToast('Appointment booked successfully.');
    appointmentForm.reset();
  });

  document.body.addEventListener('click', event => {
    const downloadTarget = event.target.closest('[data-download]');
    const payTarget = event.target.closest('[data-pay]');

    if (downloadTarget) {
      const fileName = downloadTarget.getAttribute('data-download');
      showToast(`Preparing ${fileName} for download...`);
      const element = document.createElement('a');
      element.href = '#';
      element.download = fileName;
      document.body.appendChild(element);
      element.click();
      document.body.removeChild(element);
    }

    if (payTarget) {
      const description = payTarget.getAttribute('data-pay');
      showToast(`${description} payment simulated successfully.`);
    }
  });

  renderDepartments();
  closeDashboard();
});
