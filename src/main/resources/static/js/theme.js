(() => {
  const mq = window.matchMedia('(prefers-color-scheme: dark)');
  const KEY = 'theme'; // Una sola clave global para todos los usuarios

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
      if (!localStorage.getItem(KEY)){
        apply(e.matches ? 'dark':'light');
      }
    });
  }

  document.addEventListener('DOMContentLoaded', init);
})();
