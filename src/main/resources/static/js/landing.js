/**
 * HMS Landing Page JavaScript
 * Handles interactivity, animations, and browser metadata collection.
 */

document.addEventListener('DOMContentLoaded', function() {
  initMobileMenu();
  initSmoothScrolling();
  initHeroSlider();
  initAnimations();
  collectBrowserMetadata();
  initCounterAnimation();
  initHeaderScrollEffect();
});

function initMobileMenu() {
  const hamburgerBtn = document.getElementById('hamburgerBtn');
  const mobileNav = document.getElementById('mobileNav');

  if (!hamburgerBtn || !mobileNav) return;

  hamburgerBtn.addEventListener('click', () => {
    hamburgerBtn.classList.toggle('active');
    mobileNav.classList.toggle('active');
  });

  mobileNav.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', () => {
      hamburgerBtn.classList.remove('active');
      mobileNav.classList.remove('active');
    });
  });

  document.addEventListener('click', (event) => {
    if (!hamburgerBtn.contains(event.target) && !mobileNav.contains(event.target)) {
      hamburgerBtn.classList.remove('active');
      mobileNav.classList.remove('active');
    }
  });
}

function initSmoothScrolling() {
  document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function(event) {
      const targetSelector = this.getAttribute('href');
      if (!targetSelector || targetSelector === '#') return;

      const target = document.querySelector(targetSelector);
      if (target) {
        event.preventDefault();
        target.scrollIntoView({
          behavior: 'smooth',
          block: 'start'
        });
      }
    });
  });

  const explorBtn = document.getElementById('explorBtn');
  if (explorBtn) {
    explorBtn.addEventListener('click', () => {
      const servicesSection = document.getElementById('services');
      if (servicesSection) {
        servicesSection.scrollIntoView({ behavior: 'smooth' });
      }
    });
  }
}

function initHeroSlider() {
  const slider = document.querySelector('.hms-landing-hero-slider');
  if (!slider) return;

  const slides = Array.from(slider.querySelectorAll('.hms-landing-slide'));
  const dotsContainer = slider.querySelector('.hms-landing-slider-dots');
  const prevButton = slider.querySelector('.hms-landing-slider-prev');
  const nextButton = slider.querySelector('.hms-landing-slider-next');

  if (slides.length <= 1 || !dotsContainer) return;

  let activeIndex = slides.findIndex(slide => slide.classList.contains('is-active'));
  activeIndex = activeIndex >= 0 ? activeIndex : 0;
  let autoplayTimer;

  const dots = slides.map((slide, index) => {
    const dot = document.createElement('button');
    dot.type = 'button';
    dot.className = 'hms-landing-slider-dot';
    dot.setAttribute('aria-label', `Go to slide ${index + 1}`);
    dot.addEventListener('click', () => {
      showSlide(index);
      restartAutoplay();
    });
    dotsContainer.appendChild(dot);
    return dot;
  });

  function showSlide(index) {
    activeIndex = (index + slides.length) % slides.length;

    slides.forEach((slide, slideIndex) => {
      slide.classList.toggle('is-active', slideIndex === activeIndex);
    });

    dots.forEach((dot, dotIndex) => {
      dot.classList.toggle('is-active', dotIndex === activeIndex);
      dot.setAttribute('aria-current', dotIndex === activeIndex ? 'true' : 'false');
    });
  }

  function nextSlide() {
    showSlide(activeIndex + 1);
  }

  function restartAutoplay() {
    window.clearInterval(autoplayTimer);
    autoplayTimer = window.setInterval(nextSlide, 5200);
  }

  if (prevButton) {
    prevButton.addEventListener('click', () => {
      showSlide(activeIndex - 1);
      restartAutoplay();
    });
  }

  if (nextButton) {
    nextButton.addEventListener('click', () => {
      nextSlide();
      restartAutoplay();
    });
  }

  slider.addEventListener('mouseenter', () => window.clearInterval(autoplayTimer));
  slider.addEventListener('mouseleave', restartAutoplay);

  showSlide(activeIndex);
  restartAutoplay();
}

function initAnimations() {
  const revealElements = document.querySelectorAll('.reveal');

  if (!('IntersectionObserver' in window)) {
    revealElements.forEach(element => element.classList.add('is-visible'));
    return;
  }

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('is-visible');
        observer.unobserve(entry.target);
      }
    });
  }, {
    threshold: 0.12,
    rootMargin: '0px 0px -70px 0px'
  });

  revealElements.forEach((element, index) => {
    element.style.transitionDelay = `${Math.min(index % 6, 5) * 70}ms`;
    observer.observe(element);
  });
}

function initCounterAnimation() {
  const counters = document.querySelectorAll('[data-target]');
  if (!counters.length) return;

  if (!('IntersectionObserver' in window)) {
    animateCounters(counters);
    return;
  }

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting && !entry.target.dataset.animated) {
        entry.target.dataset.animated = 'true';
        animateCounter(entry.target);
        observer.unobserve(entry.target);
      }
    });
  }, {
    threshold: 0.6
  });

  counters.forEach(counter => observer.observe(counter));
}

function animateCounters(counters) {
  counters.forEach(counter => animateCounter(counter));
}

function animateCounter(counter) {
  const target = parseInt(counter.dataset.target, 10);
  if (Number.isNaN(target)) return;

  const duration = 1700;
  const startTime = performance.now();

  function update(currentTime) {
    const elapsed = currentTime - startTime;
    const progress = Math.min(elapsed / duration, 1);
    const easedProgress = 1 - Math.pow(1 - progress, 3);
    const value = Math.floor(target * easedProgress);

    counter.textContent = value.toLocaleString();

    if (progress < 1) {
      requestAnimationFrame(update);
    } else {
      counter.textContent = target.toLocaleString();
    }
  }

  requestAnimationFrame(update);
}

function initHeaderScrollEffect() {
  const header = document.querySelector('.hms-landing-header');
  if (!header) return;

  const updateHeader = () => {
    header.classList.toggle('is-scrolled', window.scrollY > 20);
  };

  updateHeader();
  window.addEventListener('scroll', updateHeader, { passive: true });
}

function collectBrowserMetadata() {
  const metadata = {
    timestamp: new Date().toISOString(),
    timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
    language: navigator.language || navigator.userLanguage,
    screenResolution: {
      width: window.screen.width,
      height: window.screen.height,
      colorDepth: window.screen.colorDepth
    },
    deviceType: detectDeviceType(),
    os: detectOS(),
    browser: detectBrowser(),
    userAgent: navigator.userAgent,
    cookieEnabled: navigator.cookieEnabled,
    doNotTrack: navigator.doNotTrack,
    onLine: navigator.onLine,
    memory: navigator.deviceMemory ? navigator.deviceMemory + ' GB' : 'unknown',
    hardwareConcurrency: navigator.hardwareConcurrency || 'unknown'
  };

  try {
    localStorage.setItem('hms_visitor_metadata', JSON.stringify(metadata));
  } catch (error) {
    console.warn('localStorage not available:', error);
  }

  try {
    sessionStorage.setItem('hms_visitor_session_metadata', JSON.stringify(metadata));
  } catch (error) {
    console.warn('sessionStorage not available:', error);
  }

  requestGeolocation(metadata);
  fetchPublicIP(metadata);

  return metadata;
}

function detectDeviceType() {
  const userAgent = navigator.userAgent;

  if (/mobile|android|webos|iphone|ipad|ipod|blackberry|windows phone/i.test(userAgent.toLowerCase())) {
    if (/ipad/i.test(userAgent)) return 'tablet';
    return 'mobile';
  }

  return 'desktop';
}

function detectOS() {
  const userAgent = navigator.userAgent;

  if (/windows/i.test(userAgent)) return 'Windows';
  if (/macintosh|mac os/i.test(userAgent)) return 'macOS';
  if (/linux/i.test(userAgent) && !/android/i.test(userAgent)) return 'Linux';
  if (/android/i.test(userAgent)) return 'Android';
  if (/iphone|ipad|ipod/i.test(userAgent)) return 'iOS';

  return 'Unknown';
}

function detectBrowser() {
  const userAgent = navigator.userAgent;

  if (/chrome|chromium|crios/i.test(userAgent)) {
    const match = userAgent.match(/chrome\/(\d+)/i);
    return { name: 'Chrome', version: match ? match[1] : 'unknown' };
  }

  if (/safari/i.test(userAgent) && !/chrome/i.test(userAgent)) {
    const match = userAgent.match(/version\/(\d+)/i);
    return { name: 'Safari', version: match ? match[1] : 'unknown' };
  }

  if (/firefox/i.test(userAgent)) {
    const match = userAgent.match(/firefox\/(\d+)/i);
    return { name: 'Firefox', version: match ? match[1] : 'unknown' };
  }

  if (/edge|edg/i.test(userAgent)) {
    const match = userAgent.match(/edg[e\/]?(\d+)/i);
    return { name: 'Edge', version: match ? match[1] : 'unknown' };
  }

  return { name: 'Unknown', version: 'unknown' };
}

function requestGeolocation(metadata) {
  if (!navigator.geolocation) {
    console.warn('Geolocation not supported');
    metadata.geolocation = { status: 'not_supported' };
    updateMetadataStorage(metadata);
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      metadata.geolocation = {
        status: 'granted',
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        accuracy: position.coords.accuracy,
        altitude: position.coords.altitude,
        altitudeAccuracy: position.coords.altitudeAccuracy,
        heading: position.coords.heading,
        speed: position.coords.speed,
        timestamp: position.timestamp
      };
      updateMetadataStorage(metadata);
      console.log('Geolocation captured:', metadata.geolocation);
    },
    (error) => {
      metadata.geolocation = {
        status: 'denied',
        error: error.message,
        code: error.code
      };
      updateMetadataStorage(metadata);
      console.warn('Geolocation denied:', error.message);
    },
    {
      enableHighAccuracy: false,
      timeout: 5000,
      maximumAge: 0
    }
  );
}

function fetchPublicIP(metadata) {
  fetch('https://api.ipify.org?format=json')
    .then(response => response.json())
    .then(data => {
      metadata.publicIP = data.ip;
      updateMetadataStorage(metadata);
      console.log('Public IP:', data.ip);
    })
    .catch(error => {
      console.warn('Could not fetch public IP:', error);
      metadata.publicIP = { error: 'Could not fetch' };
      updateMetadataStorage(metadata);
    });
}

function updateMetadataStorage(metadata) {
  try {
    localStorage.setItem('hms_visitor_metadata', JSON.stringify(metadata));
  } catch (error) {
    console.warn('Could not update localStorage:', error);
  }

  try {
    sessionStorage.setItem('hms_visitor_session_metadata', JSON.stringify(metadata));
  } catch (error) {
    console.warn('Could not update sessionStorage:', error);
  }
}

function getStoredMetadata() {
  try {
    const metadata = localStorage.getItem('hms_visitor_metadata');
    return metadata ? JSON.parse(metadata) : null;
  } catch (error) {
    console.warn('Could not retrieve metadata:', error);
    return null;
  }
}

window.getHmsMetadata = getStoredMetadata;
