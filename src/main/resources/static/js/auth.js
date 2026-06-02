document.addEventListener('DOMContentLoaded', function() {
  initPasswordToggles();
  initPasswordStrength();
  initAuthLoadingStates();
});

function initPasswordToggles() {
  document.querySelectorAll('[data-password-toggle]').forEach(button => {
    const wrapper = button.closest('.hms-auth-input-wrap');
    const input = wrapper ? wrapper.querySelector('input') : null;
    const icon = button.querySelector('.material-symbols-outlined');

    if (!input) return;

    button.addEventListener('click', () => {
      const isPassword = input.type === 'password';
      input.type = isPassword ? 'text' : 'password';
      button.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
      if (icon) {
        icon.textContent = isPassword ? 'visibility_off' : 'visibility';
      }
      input.focus();
    });
  });
}

function initPasswordStrength() {
  const passwordInput = document.querySelector('[data-password-strength]');
  const bar = document.querySelector('[data-strength-bar]');
  const label = document.querySelector('[data-strength-label]');

  if (!passwordInput || !bar || !label) return;

  passwordInput.addEventListener('input', () => {
    const value = passwordInput.value;
    const score = getPasswordScore(value);
    const states = [
      { width: '0%', color: '#94a3b8', text: 'Password strength' },
      { width: '25%', color: '#ef4444', text: 'Weak password' },
      { width: '50%', color: '#f59e0b', text: 'Fair password' },
      { width: '75%', color: '#0ea5e9', text: 'Good password' },
      { width: '100%', color: '#10b981', text: 'Strong password' }
    ];
    const state = states[score];

    bar.style.width = state.width;
    bar.style.background = state.color;
    label.textContent = state.text;
  });
}

function getPasswordScore(value) {
  if (!value) return 0;

  let score = 0;
  if (value.length >= 6) score += 1;
  if (value.length >= 10) score += 1;
  if (/[A-Z]/.test(value) && /[a-z]/.test(value)) score += 1;
  if (/\d|[^A-Za-z0-9]/.test(value)) score += 1;

  return Math.min(score, 4);
}

function initAuthLoadingStates() {
  document.querySelectorAll('[data-auth-form]').forEach(form => {
    form.addEventListener('submit', () => {
      if (!form.checkValidity()) return;

      const button = form.querySelector('[data-auth-submit]');
      if (!button) return;

      button.classList.add('is-loading');
      button.disabled = true;
    });
  });
}
