(() => {
  const mq = window.matchMedia('(prefers-color-scheme: dark)');

  // Obtener el rol del usuario desde el atributo data-user-role del body o desde el DOM
  function getUserRole() {
    // Intentar obtener desde atributo data
    let role = document.body.getAttribute('data-user-role');
    if (role) return role.toLowerCase();

    // Intentar desde el badge de rol en el header
    const roleBadge = document.querySelector('.role-badge');
    if (roleBadge) {
      role = roleBadge.textContent.trim().toLowerCase();
      return role;
    }

    // Verificar si estamos en rutas de estudiante
    const path = window.location.pathname;
    if (path.includes('/estudiante/')) return 'student';
    if (path.includes('/administrador/') || path.includes('/admin/')) return 'admin';
    if (path.includes('/superadmin/')) return 'superadmin';
    
    // Verificar por clases o elementos específicos
    if (document.querySelector('[data-role="student"]')) return 'student';
    if (document.querySelector('[data-role="admin"]')) return 'admin';
    if (document.querySelector('[data-role="superadmin"]')) return 'superadmin';

    return 'default'; // Fallback para usuarios sin sesión o públicos
  }

  function getStorageKey() {
    const role = getUserRole();
    return `theme_${role}`; // Ej: theme_admin, theme_student, theme_superadmin
  }

  function apply(theme){
    document.documentElement.setAttribute('data-theme', theme);
    document.documentElement.style.colorScheme = theme;
    const btn = document.getElementById('themeToggle');
    if (btn){
      const sun  = btn.querySelector('.sun');
      const moon = btn.querySelector('.moon');
      btn.setAttribute('aria-pressed', theme === 'dark');
      if (sun && moon){
        sun.style.display  = theme === 'dark' ? 'none':'inline';
        moon.style.display = theme === 'dark' ? 'inline':'none';
      }
    }
  }

  function init(){
    const KEY = getStorageKey();
    const saved = localStorage.getItem(KEY);
    apply(saved ?? (mq.matches ? 'dark':'light'));

    const btn = document.getElementById('themeToggle');
    if (btn){
      btn.addEventListener('click', () => {
        const current = document.documentElement.getAttribute('data-theme') || 'light';
        const next = current === 'dark' ? 'light' : 'dark';
        localStorage.setItem(KEY, next);
        apply(next);
      });
    }

    mq.addEventListener?.('change', e => {
      // Solo si el usuario no forzó tema manualmente
      if (!localStorage.getItem(KEY)){
        apply(e.matches ? 'dark':'light');
      }
    });
  }

  document.addEventListener('DOMContentLoaded', init);
})();
