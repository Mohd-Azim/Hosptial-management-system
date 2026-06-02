(function () {
  function onReady(fn) {
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', fn);
    } else {
      fn();
    }
  }

  function showToast(message) {
    var toast = document.querySelector('[data-reception-toast]');
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add('is-visible');
    clearTimeout(showToast.timer);
    showToast.timer = setTimeout(function () {
      toast.classList.remove('is-visible');
    }, 2600);
  }

  function scrollToTarget(selector) {
    var target = document.querySelector(selector);
    if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
  }

  function normalizeText(value) {
    return String(value || '').trim().toLowerCase();
  }

  function openModal(id) {
    var modal = document.getElementById(id);
    if (!modal) return;
    modal.classList.add('is-open');
    modal.setAttribute('aria-hidden', 'false');
  }

  function closeModal(button) {
    var modal = button.closest('.reception-modal');
    if (!modal) return;
    modal.classList.remove('is-open');
    modal.setAttribute('aria-hidden', 'true');
  }

  function initHeaderShell() {
    var moduleTitle = document.querySelector('[data-module-title]');
    if (moduleTitle && document.querySelector('[data-reception-module]')) {
      moduleTitle.textContent = 'Reception Panel';
    }

    document.querySelectorAll('[data-sidebar-toggle]').forEach(function (button) {
      button.addEventListener('click', function () {
        document.body.classList.toggle('hms-sidebar-collapsed');
        var icon = button.querySelector('.material-symbols-outlined');
        if (icon) icon.textContent = document.body.classList.contains('hms-sidebar-collapsed') ? 'menu' : 'menu_open';
      });
    });

    var profileToggle = document.querySelector('[data-profile-toggle]');
    var profileDropdown = document.querySelector('[data-profile-dropdown]');
    if (profileToggle && profileDropdown) {
      profileToggle.addEventListener('click', function () {
        profileDropdown.classList.toggle('is-open');
      });
      document.addEventListener('click', function (event) {
        if (!profileToggle.contains(event.target) && !profileDropdown.contains(event.target)) {
          profileDropdown.classList.remove('is-open');
        }
      });
    }

    var notificationButton = document.querySelector('[data-header-notifications]');
    if (notificationButton) {
      notificationButton.addEventListener('click', function () {
        var title = document.getElementById('record-modal-title');
        var body = document.querySelector('[data-record-modal-body]');
        if (title) title.textContent = 'Reception alerts';
        if (body) {
          body.innerHTML = [
            '<div><small>Doctor delay</small><strong>Dr. Kabir Mehta delayed by 20 minutes</strong></div>',
            '<div><small>Payment pending</small><strong>2 appointments need billing follow-up</strong></div>',
            '<div><small>Schedule block</small><strong>Neurology slots unavailable today</strong></div>',
            '<div><small>Action</small><strong>Review appointment list</strong></div>'
          ].join('');
        }
        openModal('record-modal');
      });
    }
  }

  function initGlobalSearch() {
    var form = document.querySelector('[data-global-search-form]');
    var input = document.querySelector('[data-global-search-input]');
    var results = document.querySelector('[data-global-search-results]');
    if (!form || !input || !results) return;

    var records = [
      ['Patient', 'Neha Jain', 'MRN-2041 · +91 98765 43210'],
      ['Patient', 'Farhan Ali', 'MRN-2042 · Emergency'],
      ['Doctor', 'Dr. Ananya Rao', 'Cardiology · 8 free slots'],
      ['Appointment', '#AP-1009', 'Diya Rao · Payment due'],
      ['Token', 'T-022', 'Diya Rao · Waiting'],
      ['Bill', 'BILL-7781', '₹700 due']
    ];

    form.addEventListener('submit', function (event) {
      event.preventDefault();
      var query = input.value.trim().toLowerCase();
      var matches = records.filter(function (record) {
        return !query || record.join(' ').toLowerCase().indexOf(query) !== -1;
      });
      results.innerHTML = matches.map(function (record) {
        return '<tr><td><span class="rx-badge info">' + record[0] + '</span></td><td>' + record[1] + '</td><td>' + record[2] + '</td><td class="rx-actions"><button type="button" data-record-open="' + record[0].toLowerCase() + ':' + record[1] + '">View</button></td></tr>';
      }).join('');
      openModal('global-search-modal');
    });
  }

  function initTabs() {
    document.querySelectorAll('[data-tabs]').forEach(function (tabs) {
      tabs.querySelectorAll('[data-tab-button]').forEach(function (button) {
        button.addEventListener('click', function () {
          activateTab(tabs, button.getAttribute('data-tab-button'));
        });
      });
    });
  }

  function activateTab(tabs, name) {
    tabs.querySelectorAll('[data-tab-button]').forEach(function (item) {
      item.classList.toggle('active', item.getAttribute('data-tab-button') === name);
    });
    tabs.querySelectorAll('[data-tab-panel]').forEach(function (panel) {
      panel.classList.toggle('active', panel.getAttribute('data-tab-panel') === name);
    });
  }

  function openAppointmentTab(name) {
    var tabs = document.querySelector('#appointments [data-tabs]');
    if (!tabs) return;
    activateTab(tabs, name);
    scrollToTarget('#appointments');
  }

  function initTableSearch() {
    document.querySelectorAll('[data-table-search]').forEach(function (input) {
      var table = document.querySelector(input.getAttribute('data-table-search'));
      if (!table) return;
      var rows = Array.prototype.slice.call(table.querySelectorAll('tbody tr'));
      input.addEventListener('input', function () {
        var query = input.value.trim().toLowerCase();
        rows.forEach(function (row) {
          row.style.display = !query || row.textContent.toLowerCase().indexOf(query) !== -1 ? '' : 'none';
        });
      });
    });
  }

  function initColumnFilters() {
    document.querySelectorAll('[data-filter-table]').forEach(function (select) {
      select.addEventListener('change', function () {
        var table = document.querySelector(select.getAttribute('data-filter-table'));
        if (!table) return;
        var filters = Array.prototype.slice.call(document.querySelectorAll('[data-filter-table="' + select.getAttribute('data-filter-table') + '"]'));
        var rows = Array.prototype.slice.call(table.querySelectorAll('tbody tr'));
        rows.forEach(function (row) {
          var show = filters.every(function (filter) {
            var value = filter.value.trim().toLowerCase();
            var column = parseInt(filter.getAttribute('data-filter-column'), 10);
            var cell = row.children[column];
            return !value || (cell && cell.textContent.toLowerCase().indexOf(value) !== -1);
          });
          row.style.display = show ? '' : 'none';
        });
      });
    });
  }

  function initAppointmentFilters() {
    var table = document.getElementById('appointment-table');
    if (!table) return;
    var search = document.querySelector('[data-appointment-search]');
    var filterInputs = Array.prototype.slice.call(document.querySelectorAll('[data-appointment-filter]'));
    var dateFrom = document.querySelector('[data-date-from]');
    var dateTo = document.querySelector('[data-date-to]');
    var reset = document.querySelector('[data-reset-appointment-filters]');

    function applyFilters() {
      var query = normalizeText(search && search.value);
      var from = dateFrom && dateFrom.value ? dateFrom.value : '';
      var to = dateTo && dateTo.value ? dateTo.value : '';
      var filters = {};
      filterInputs.forEach(function (input) {
        filters[input.getAttribute('data-appointment-filter')] = normalizeText(input.value);
      });

      Array.prototype.slice.call(table.querySelectorAll('tbody tr')).forEach(function (row) {
        var rowDate = row.getAttribute('data-date') || '';
        var matchSearch = !query || normalizeText(row.textContent).indexOf(query) !== -1;
        var matchPeriod = !filters.period || filters.period === 'custom' || normalizeText(row.getAttribute('data-period')) === filters.period;
        var matchDoctor = !filters.doctor || normalizeText(row.getAttribute('data-doctor')) === filters.doctor;
        var matchDepartment = !filters.department || normalizeText(row.getAttribute('data-department')) === filters.department;
        var matchStatus = !filters.status || normalizeText(row.getAttribute('data-status')) === filters.status;
        var matchFrom = !from || rowDate >= from;
        var matchTo = !to || rowDate <= to;
        row.setAttribute('data-filtered-visible', matchSearch && matchPeriod && matchDoctor && matchDepartment && matchStatus && matchFrom && matchTo ? 'true' : 'false');
      });
      table.setAttribute('data-current-page', '1');
      paginateTable(table);
    }

    if (search) search.addEventListener('input', applyFilters);
    filterInputs.forEach(function (input) { input.addEventListener('change', applyFilters); });
    if (dateFrom) dateFrom.addEventListener('change', applyFilters);
    if (dateTo) dateTo.addEventListener('change', applyFilters);
    if (reset) {
      reset.addEventListener('click', function () {
        if (search) search.value = '';
        filterInputs.forEach(function (input) { input.value = ''; });
        if (dateFrom) dateFrom.value = '';
        if (dateTo) dateTo.value = '';
        applyFilters();
        showToast('Appointment filters reset.');
      });
    }
    applyFilters();
  }

  function initAppointmentSorting() {
    document.querySelectorAll('#appointment-table th[data-sort]').forEach(function (header, index) {
      header.setAttribute('role', 'button');
      header.setAttribute('tabindex', '0');

      function sortRows() {
        var table = header.closest('table');
        var tbody = table.querySelector('tbody');
        var direction = header.getAttribute('data-sort-direction') === 'asc' ? 'desc' : 'asc';
        table.querySelectorAll('th[data-sort-direction]').forEach(function (item) {
          item.removeAttribute('data-sort-direction');
        });
        header.setAttribute('data-sort-direction', direction);
        var type = header.getAttribute('data-sort');
        Array.prototype.slice.call(tbody.querySelectorAll('tr')).sort(function (left, right) {
          var a = left.children[index] ? left.children[index].textContent.trim() : '';
          var b = right.children[index] ? right.children[index].textContent.trim() : '';
          if (type === 'date') return direction === 'asc' ? a.localeCompare(b) : b.localeCompare(a);
          return direction === 'asc' ? a.localeCompare(b, undefined, { numeric: true }) : b.localeCompare(a, undefined, { numeric: true });
        }).forEach(function (row) {
          tbody.appendChild(row);
        });
        paginateTable(table);
      }

      header.addEventListener('click', sortRows);
      header.addEventListener('keydown', function (event) {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          sortRows();
        }
      });
    });
  }

  function initPagination() {
    document.querySelectorAll('[data-paginated-table]').forEach(function (table) {
      table.setAttribute('data-current-page', table.getAttribute('data-current-page') || '1');
      paginateTable(table);
    });
  }

  function paginateTable(table) {
    var perPage = parseInt(table.getAttribute('data-page-size') || '5', 10);
    var rows = Array.prototype.slice.call(table.querySelectorAll('tbody tr'));
    var visibleRows = rows.filter(function (row) {
      return row.getAttribute('data-filtered-visible') !== 'false';
    });
    var totalPages = Math.max(1, Math.ceil(visibleRows.length / perPage));
    var currentPage = Math.min(parseInt(table.getAttribute('data-current-page') || '1', 10), totalPages);
    table.setAttribute('data-current-page', String(currentPage));

    rows.forEach(function (row) { row.style.display = 'none'; });
    visibleRows.forEach(function (row, index) {
      row.style.display = index >= (currentPage - 1) * perPage && index < currentPage * perPage ? '' : 'none';
    });

    var pager = document.querySelector('[data-pagination-for="#' + table.id + '"]');
    if (!pager) return;
    pager.innerHTML = '';
    var summary = document.createElement('span');
    summary.textContent = visibleRows.length + ' records';
    pager.appendChild(summary);
    for (var page = 1; page <= totalPages; page += 1) {
      var button = document.createElement('button');
      button.type = 'button';
      button.textContent = String(page);
      button.className = page === currentPage ? 'active' : '';
      button.addEventListener('click', function () {
        table.setAttribute('data-current-page', this.textContent);
        paginateTable(table);
      });
      pager.appendChild(button);
    }
  }

  function initAppointmentCheckIn() {
    document.addEventListener('click', function (event) {
      var button = event.target.closest('[data-appointment-checkin]');
      if (!button) return;
      var row = button.closest('tr');
      if (!row) return;
      row.setAttribute('data-status', 'checked-in');
      var badge = row.querySelector('td:nth-child(7) .rx-badge, td:nth-child(7) .reception-pill');
      if (badge) {
        badge.textContent = 'checked-in';
        badge.className = 'rx-badge success';
      }
      button.textContent = 'Checked-in';
      button.setAttribute('disabled', 'disabled');
      showToast('Patient marked checked-in.');
    });
  }

  function initBookingGuard() {
    var slotInput = document.querySelector('[data-slot-input]');
    var doctorInput = document.querySelector('[data-booking-doctor]');
    var warning = document.querySelector('[data-slot-warning]');
    var submit = document.querySelector('[data-book-submit]');
    if (!slotInput || !warning || !submit) return;

    function updateSlotState() {
      var value = slotInput.value;
      var doctor = doctorInput ? doctorInput.value : '';
      var blocked = (doctor === 'Dr. Priya Menon' && value.indexOf('2026-05-22T') === 0) ||
        (doctor === 'Dr. Kabir Mehta' && value.indexOf('2026-05-22T13') === 0);
      warning.hidden = !blocked;
      submit.disabled = blocked;
      submit.classList.toggle('is-disabled', blocked);
    }

    slotInput.addEventListener('change', updateSlotState);
    slotInput.addEventListener('input', updateSlotState);
    if (doctorInput) doctorInput.addEventListener('change', updateSlotState);
    updateSlotState();
  }

  function initModals() {
    document.addEventListener('click', function (event) {
      var opener = event.target.closest('[data-modal-open]');
      if (opener) openModal(opener.getAttribute('data-modal-open'));

      var closer = event.target.closest('[data-modal-close]');
      if (closer) closeModal(closer);

      if (event.target.classList && event.target.classList.contains('reception-modal')) {
        event.target.classList.remove('is-open');
        event.target.setAttribute('aria-hidden', 'true');
      }
    });
  }

  function initTokenGenerator() {
    var board = document.querySelector('[data-token-board]');
    var count = 24;
    if (!board) return;

    document.addEventListener('click', function (event) {
      var button = event.target.closest('[data-token-generate]');
      if (!button) return;
      var article = document.createElement('article');
      article.setAttribute('data-token-card', '');
      article.innerHTML = '<strong>T-' + String(count).padStart(3, '0') + '</strong><span>Walk-in patient</span><small>General Medicine · Room pending · 00 min</small><em class="reception-pill info">Queued</em>';
      board.appendChild(article);
      updateQueueCount(1);
      showToast('Token T-' + String(count).padStart(3, '0') + ' generated.');
      count += 1;
    });
  }

  function updateQueueCount(delta) {
    var counter = document.querySelector('[data-queue-count]');
    if (!counter) return;
    var current = parseInt(counter.textContent, 10) || 0;
    counter.textContent = String(Math.max(0, current + delta));
  }

  function initQueueActions() {
    document.querySelectorAll('[data-queue-action]').forEach(function (button) {
      button.addEventListener('click', function () {
        var card = document.querySelector('[data-token-card]:not([data-token-card-done])');
        if (!card) {
          showToast('Queue is clear.');
          return;
        }
        var status = button.getAttribute('data-queue-action');
        var pill = card.querySelector('.reception-pill');
        if (pill) {
          pill.textContent = status;
          pill.className = 'reception-pill ' + (status === 'Completed' ? 'success' : status === 'No-show' ? 'danger' : status === 'Skipped' ? 'warning' : 'info');
        }
        if (status === 'Completed' || status === 'No-show' || status === 'Skipped') {
          card.setAttribute('data-token-card-done', 'true');
          boardMoveToEnd(card);
          updateQueueCount(-1);
        }
        showToast('Token marked: ' + status + '.');
      });
    });
  }

  function boardMoveToEnd(card) {
    if (card.parentNode) card.parentNode.appendChild(card);
  }

  function initToastButtons() {
    document.addEventListener('click', function (event) {
      var button = event.target.closest('[data-toast]');
      if (button) showToast(button.getAttribute('data-toast'));
    });
  }

  function initScrollButtons() {
    document.addEventListener('click', function (event) {
      var button = event.target.closest('[data-scroll-target]');
      if (!button) return;
      scrollToTarget(button.getAttribute('data-scroll-target'));
      var modal = button.closest('.reception-modal');
      if (modal) {
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
      }
    });
  }

  function initAppointmentButtons() {
    document.addEventListener('click', function (event) {
      var button = event.target.closest('[data-appointment-tab]');
      if (!button) return;
      openAppointmentTab(button.getAttribute('data-appointment-tab'));
    });

    document.querySelectorAll('[data-appointment-filter]').forEach(function (button) {
      button.addEventListener('click', function () {
        openAppointmentTab('history');
        var input = document.querySelector('[data-table-search="#appointment-table"]');
        if (input) {
          input.value = button.getAttribute('data-appointment-filter');
          input.dispatchEvent(new Event('input'));
        }
      });
    });
  }

  function initRecordViews() {
    document.addEventListener('click', function (event) {
      var button = event.target.closest('[data-record-open]');
      if (!button) return;
      var value = button.getAttribute('data-record-open');
      var parts = value.split(':');
      var type = parts.shift();
      var label = parts.join(':');
      var title = document.getElementById('record-modal-title');
      var body = document.querySelector('[data-record-modal-body]');
      if (title) title.textContent = type.charAt(0).toUpperCase() + type.slice(1) + ' · ' + label;
      if (body) {
        var row = button.closest('tr');
        if (type === 'appointment' && row) {
          body.innerHTML = [
            '<div><small>Patient</small><strong>' + row.children[1].textContent.trim() + '</strong></div>',
            '<div><small>Doctor</small><strong>' + row.children[2].textContent.trim() + '</strong></div>',
            '<div><small>Department</small><strong>' + row.children[3].textContent.trim() + '</strong></div>',
            '<div><small>Timing</small><strong>' + row.children[4].textContent.trim() + ' · ' + row.children[5].textContent.trim() + '</strong></div>',
            '<div><small>Status</small><strong>' + row.children[6].textContent.trim() + '</strong></div>',
            '<div><small>Payment</small><strong>' + row.children[7].textContent.trim() + '</strong></div>'
          ].join('');
        } else if (type === 'doctor' && row) {
          body.innerHTML = [
            '<div><small>Department</small><strong>' + row.children[1].textContent.trim() + '</strong></div>',
            '<div><small>Available slots</small><strong>' + row.children[2].textContent.trim() + '</strong></div>',
            '<div><small>Booked slots</small><strong>' + row.children[3].textContent.trim() + '</strong></div>',
            '<div><small>Blocked slots</small><strong>' + row.children[4].textContent.trim() + '</strong></div>',
            '<div><small>Status</small><strong>' + row.children[5].textContent.trim() + '</strong></div>',
            '<div><small>Unavailable details</small><strong>' + row.children[6].textContent.trim() + '</strong></div>'
          ].join('');
        } else if (type === 'bill' && row) {
          body.innerHTML = [
            '<div><small>Patient</small><strong>' + row.children[0].textContent.trim() + '</strong></div>',
            '<div><small>Appointment</small><strong>' + row.children[1].textContent.trim() + '</strong></div>',
            '<div><small>Amount</small><strong>' + row.children[2].textContent.trim() + '</strong></div>',
            '<div><small>Paid</small><strong>' + row.children[3].textContent.trim() + '</strong></div>',
            '<div><small>Due</small><strong>' + row.children[4].textContent.trim() + '</strong></div>',
            '<div><small>Status</small><strong>' + row.children[6].textContent.trim() + '</strong></div>'
          ].join('');
        } else {
          body.innerHTML = [
            '<div><small>Status</small><strong>Active</strong></div>',
            '<div><small>Owner</small><strong>Reception desk</strong></div>',
            '<div><small>Last update</small><strong>Today</strong></div>',
            '<div><small>Next action</small><strong>Review and proceed</strong></div>'
          ].join('');
        }
      }
      openModal('record-modal');
    });
  }

  function initRowActions() {
    document.addEventListener('click', function (event) {
      var button = event.target.closest('[data-row-status]');
      if (!button) return;
      var status = button.getAttribute('data-row-status');
      var row = button.closest('tr') || button.closest('article') || button.closest('div');
      var pill = row ? row.querySelector('.rx-badge, .reception-pill') : null;
      if (pill) {
        pill.textContent = status;
        pill.className = 'rx-badge ' + getStatusClass(status);
      }
      showToast('Status updated: ' + status + '.');
    });
  }

  function getStatusClass(status) {
    var normalized = status.toLowerCase();
    if (normalized.indexOf('reject') !== -1 || normalized.indexOf('unavailable') !== -1 || normalized.indexOf('no') !== -1) return 'danger';
    if (normalized.indexOf('block') !== -1 || normalized.indexOf('skip') !== -1 || normalized.indexOf('review') !== -1) return 'warning';
    if (normalized.indexOf('complete') !== -1 || normalized.indexOf('approve') !== -1 || normalized.indexOf('paid') !== -1 || normalized.indexOf('available') !== -1) return 'success';
    return 'info';
  }

  function initPrintButtons() {
    document.addEventListener('click', function (event) {
      var button = event.target.closest('[data-print-section]');
      if (!button) return;
      var target = document.querySelector(button.getAttribute('data-print-section'));
      if (target) target.classList.add('reception-print-target');
      window.print();
      setTimeout(function () {
        if (target) target.classList.remove('reception-print-target');
      }, 500);
    });
  }

  function initLocalForms() {
    document.querySelectorAll('[data-local-form]').forEach(function (form) {
      form.addEventListener('submit', function (event) {
        event.preventDefault();
        showToast('Information saved on screen.');
      });
    });
  }

  function initRefundCalculator() {
    document.addEventListener('click', function (event) {
      var button = event.target.closest('[data-refund-calc], [data-refund-policy]');
      if (!button) return;
      var percent = parseInt(button.getAttribute('data-refund-calc') || button.getAttribute('data-refund-policy'), 10);
      var field = document.querySelector('[data-refund-amount]');
      if (field) field.value = String(Math.round(700 * percent / 100));
      showToast('Refund policy selected: ' + percent + '%.');
    });
  }

  function initReportButtons() {
    document.querySelectorAll('[data-report-open]').forEach(function (button) {
      button.addEventListener('click', function () {
        var title = document.getElementById('record-modal-title');
        var body = document.querySelector('[data-record-modal-body]');
        if (title) title.textContent = button.getAttribute('data-report-open');
        if (body) {
          body.innerHTML = '<div><small>Total</small><strong>128</strong></div><div><small>Change</small><strong>+12%</strong></div><div><small>Export</small><strong>Use print view</strong></div>';
        }
        openModal('record-modal');
      });
    });
  }

  function initViewToggle() {
    document.querySelectorAll('[data-view-toggle]').forEach(function (button) {
      button.addEventListener('click', function () {
        var target = document.querySelector(button.getAttribute('data-view-toggle'));
        if (!target) return;
        target.classList.toggle('calendar-mode');
        showToast(target.classList.contains('calendar-mode') ? 'Calendar density enabled.' : 'List view enabled.');
      });
    });
  }

  function initSettingToggles() {
    document.querySelectorAll('[data-setting-toggle]').forEach(function (input) {
      input.addEventListener('change', function () {
        showToast(input.checked ? 'Preference enabled.' : 'Preference disabled.');
      });
    });
  }

  function initChecklistProgress() {
    var checklist = document.querySelector('[data-checklist]');
    var bar = document.querySelector('[data-checklist-progress]');
    var label = document.querySelector('[data-checklist-label]');
    if (!checklist || !bar || !label) return;

    function update() {
      var items = Array.prototype.slice.call(checklist.querySelectorAll('[data-checklist-item]'));
      var done = items.filter(function (item) { return item.checked; }).length;
      var total = items.length || 1;
      bar.style.width = Math.round(done / total * 100) + '%';
      label.textContent = done + ' of ' + total + ' completed';
      showToast('Checklist updated: ' + done + '/' + total + '.');
    }

    checklist.querySelectorAll('[data-checklist-item]').forEach(function (item) {
      item.addEventListener('change', update);
    });
    update();
  }

  function initDocumentVerification() {
    document.querySelectorAll('[data-document-toggle]').forEach(function (button) {
      button.addEventListener('click', function () {
        button.classList.toggle('is-verified');
        var small = button.querySelector('small');
        if (small) small.textContent = button.classList.contains('is-verified') ? 'Verified' : 'Needs review';
        showToast(button.classList.contains('is-verified') ? 'Document verified.' : 'Document returned to review.');
      });
    });
  }

  function initTaskBoard() {
    var list = document.querySelector('[data-task-list]');
    if (!list) return;

    document.querySelectorAll('[data-task-toggle]').forEach(function (button) {
      button.addEventListener('click', function () {
        toggleTask(button);
      });
    });

    document.querySelectorAll('[data-task-add]').forEach(function (button) {
      button.addEventListener('click', function () {
        var task = document.createElement('button');
        task.type = 'button';
        task.setAttribute('data-task-toggle', '');
        task.innerHTML = '<span class="reception-pill info">New</span><strong>Follow up counter request</strong><small>Due this shift</small>';
        task.addEventListener('click', function () { toggleTask(task); });
        list.prepend(task);
        showToast('Task added to worklist.');
      });
    });
  }

  function toggleTask(button) {
    button.classList.toggle('is-done');
    var pill = button.querySelector('.reception-pill');
    var small = button.querySelector('small');
    if (button.classList.contains('is-done')) {
      if (pill) {
        pill.textContent = 'Done';
        pill.className = 'reception-pill success';
      }
      if (small) small.textContent = 'Completed';
      showToast('Task marked complete.');
    } else {
      if (pill) {
        pill.textContent = 'Open';
        pill.className = 'reception-pill warning';
      }
      if (small) small.textContent = 'Reopened';
      showToast('Task reopened.');
    }
  }

  function initReveal() {
    var items = document.querySelectorAll('.reveal-panel');
    if (!('IntersectionObserver' in window)) {
      items.forEach(function (item) { item.classList.add('is-visible'); });
      return;
    }
    var observer = new IntersectionObserver(function (entries) {
      entries.forEach(function (entry) {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.08 });
    items.forEach(function (item, index) {
      item.style.transitionDelay = Math.min(index % 5, 4) * 45 + 'ms';
      observer.observe(item);
    });
  }

  function initActiveModuleNav() {
    var links = Array.prototype.slice.call(document.querySelectorAll('.rx-subnav a[href^="#"], .reception-module-nav a[href^="#"]'));
    if (!links.length) return;
    var sidebarLinks = Array.prototype.slice.call(document.querySelectorAll('.hms-nav a[href*="#"]'));
    var targets = links.map(function (link) { return document.querySelector(link.getAttribute('href')); }).filter(Boolean);
    function setActive() {
      var current = targets[0];
      targets.forEach(function (target) {
        if (target.getBoundingClientRect().top <= 135) current = target;
      });
      links.forEach(function (link) {
        link.classList.toggle('active', current && link.getAttribute('href') === '#' + current.id);
      });
      sidebarLinks.forEach(function (link) {
        var hash = link.getAttribute('href').split('#')[1];
        link.classList.toggle('is-reception-active', current && hash === current.id);
      });
    }
    setActive();
    window.addEventListener('scroll', setActive, { passive: true });
  }

  function initNewPatientModalAndLookup() {
    var form = document.getElementById('new-patient-form');
    var patientSearch = document.querySelector('[data-patient-lookup-input]');
    var dropdown = document.querySelector('[data-patient-lookup-results]');
    var hiddenInput = document.querySelector('[data-patient-id-input]');
    var source = document.querySelector('[data-patient-source]');
    var debounceTimer;
    var patients = source ? Array.prototype.slice.call(source.options).map(function (option) {
      return {
        id: option.value,
        label: option.textContent.trim(),
        search: option.textContent.trim().toLowerCase()
      };
    }).filter(function (patient) { return patient.id; }) : [];

    function openPatientModal(prefillName) {
      var modal = document.getElementById('new-patient-modal');
      if (!modal) return;
      modal.classList.add('is-open');
      modal.setAttribute('aria-hidden', 'false');
      if (form) form.reset();
      var firstInput = form ? form.querySelector('input[name="fullName"]') : null;
      if (firstInput && prefillName) firstInput.value = prefillName;
      if (firstInput) firstInput.focus();
    }

    document.querySelectorAll('[data-modal-open="new-patient-modal"]').forEach(function (button) {
      button.addEventListener('click', function () {
        openPatientModal('');
      });
    });

    function renderDropdown(results) {
      if (!dropdown) return;
      dropdown.classList.add('is-open');
      if (!results.length) {
        dropdown.innerHTML = '<div class="rx-patient-noresult"><span>No matching patient found.</span><button type="button" class="hms-btn hms-btn-ghost" data-patient-create-from-search>Add new</button></div>';
        return;
      }
      dropdown.innerHTML = results.map(function (patient) {
        return '<button type="button" class="rx-patient-hit" data-patient-id="' + patient.id + '" data-patient-label="' + patient.label.replace(/"/g, '&quot;') + '"><strong>' + patient.label + '</strong></button>';
      }).join('');
    }

    document.addEventListener('click', function (event) {
      var createButton = event.target.closest('[data-patient-create-from-search]');
      if (createButton) {
        openPatientModal(patientSearch ? patientSearch.value.trim() : '');
        return;
      }

      var hit = event.target.closest('.rx-patient-hit');
      if (hit && patientSearch && hiddenInput) {
        hiddenInput.value = hit.getAttribute('data-patient-id');
        patientSearch.value = hit.getAttribute('data-patient-label') || hit.textContent.trim();
        if (dropdown) {
          dropdown.innerHTML = '';
          dropdown.classList.remove('is-open');
        }
        showToast('Patient selected for booking.');
        return;
      }

      if (dropdown && !event.target.closest('[data-patient-lookup]')) {
        dropdown.classList.remove('is-open');
      }
    });

    if (patientSearch) {
      patientSearch.addEventListener('focus', function () {
        if (patientSearch.value.trim()) patientSearch.dispatchEvent(new Event('input'));
      });
      patientSearch.addEventListener('input', function () {
        clearTimeout(debounceTimer);
        var query = patientSearch.value.trim().toLowerCase();
        if (hiddenInput) hiddenInput.value = '';
        if (!query) {
          if (dropdown) {
            dropdown.innerHTML = '';
            dropdown.classList.remove('is-open');
          }
          return;
        }
        if (dropdown) {
          dropdown.classList.add('is-open');
          dropdown.innerHTML = '<div class="rx-patient-loading"><span class="rx-mini-spinner" aria-hidden="true"></span>Searching patient records</div>';
        }
        debounceTimer = setTimeout(function () {
          renderDropdown(patients.filter(function (patient) {
            return patient.search.indexOf(query) !== -1;
          }).slice(0, 8));
        }, 220);
      });
    }

    if (form) {
      form.addEventListener('submit', function (event) {
        event.preventDefault();
        if (!form.checkValidity()) {
          form.reportValidity();
          return;
        }
        var data = new FormData(form);
        var label = 'NEW-' + String(Date.now()).slice(-5) + ' - ' + data.get('fullName') + ' (' + data.get('mobile') + ')';
        var patient = {
          id: 'new-' + Date.now(),
          label: label,
          search: label.toLowerCase()
        };
        patients.unshift(patient);
        if (hiddenInput) hiddenInput.value = patient.id;
        if (patientSearch) patientSearch.value = patient.label;
        if (dropdown) {
          dropdown.innerHTML = '';
          dropdown.classList.remove('is-open');
        }
        closeModal(form);
        form.reset();
        showToast('Patient captured and selected for booking.');
      });
    }

    var bookingForm = document.querySelector('#book-appointment form');
    if (bookingForm && hiddenInput && patientSearch) {
      bookingForm.addEventListener('submit', function (event) {
        if (!hiddenInput.value) {
          event.preventDefault();
          patientSearch.focus();
          showToast('Select a patient from UHID lookup before booking.');
          return;
        }
        if (hiddenInput.value.indexOf('new-') === 0) {
          event.preventDefault();
          patientSearch.focus();
          showToast('Permanent MRN is required before appointment submission.');
        }
      });
    }
  }

  onReady(function () {
    initHeaderShell();
    initGlobalSearch();
    initTabs();
    initTableSearch();
    initColumnFilters();
    initAppointmentFilters();
    initAppointmentSorting();
    initPagination();
    initAppointmentCheckIn();
    initBookingGuard();
    initModals();
    initNewPatientModalAndLookup();
    initTokenGenerator();
    initQueueActions();
    initToastButtons();
    initScrollButtons();
    initAppointmentButtons();
    initRecordViews();
    initRowActions();
    initPrintButtons();
    initLocalForms();
    initRefundCalculator();
    initReportButtons();
    initViewToggle();
    initSettingToggles();
    initChecklistProgress();
    initDocumentVerification();
    initTaskBoard();
    initReveal();
    initActiveModuleNav();
  });
})();
