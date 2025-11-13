// contador.js
let contadorTimer = null;

// ---- Utilidades ----
function parseFechaISO(value) {
  if (!value) return null;
  if (value instanceof Date) return value;
  if (typeof value === "number") return new Date(value);
  if (typeof value === "string") {
    const iso = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}(:\d{2})?$/.test(value)
      ? (value.length === 16 ? value + ":00" : value)
      : value;
    const d = new Date(iso);
    return isNaN(d.getTime()) ? null : d;
  }
  return null;
}

function formatHHMMSS(ms) {
  const s = Math.max(0, Math.floor(ms / 1000));
  const h = Math.floor(s / 3600);
  const m = Math.floor((s % 3600) / 60);
  const ss = s % 60;
  return `${h}h ${String(m).padStart(2, "0")}m ${String(ss).padStart(2, "0")}s`;
}

// ---- API principal ----
function iniciarContador(deliveredAt, requestedHours, status) {
  const contadorEl   = document.getElementById("contador");
  const devolucionEl = document.getElementById("horaDevolucion");
  if (!contadorEl) return;

  if (contadorTimer) {
    clearInterval(contadorTimer);
    contadorTimer = null;
  }

  const st = (status || "").toLowerCase();
  window.loanStatus = st;

  if (st === "pending")   { contadorEl.textContent = "⏳ Esperando aprobación"; if (devolucionEl) devolucionEl.textContent = "—"; return; }
  if (st === "approved")  { contadorEl.textContent = "📦 Aprobado, retira el equipo"; if (devolucionEl) devolucionEl.textContent = "—"; return; }
  if (st === "rejected")  { contadorEl.textContent = "❌ Préstamo rechazado"; if (devolucionEl) devolucionEl.textContent = "—"; return; }
  if (st === "completed") { contadorEl.textContent = "✅ Préstamo finalizado"; if (devolucionEl) devolucionEl.textContent = "—"; return; }

  if (st !== "active") {
    contadorEl.textContent = "⚠️ Estado desconocido";
    if (devolucionEl) devolucionEl.textContent = "—";
    return;
  }

  const inicioDate = parseFechaISO(deliveredAt);
  const horas = Number(requestedHours || 0);
  if (!inicioDate || !horas) {
    contadorEl.textContent = "⚠️ Datos incompletos";
    if (devolucionEl) devolucionEl.textContent = "—";
    return;
  }

  const inicioMs = inicioDate.getTime();
  const finMs = inicioMs + horas * 60 * 60 * 1000;

  if (devolucionEl) {
    const fechaDev = new Date(finMs);
    devolucionEl.textContent = fechaDev.toLocaleString();
  }

  window.alertaMostrada = false;

  const tick = () => {
    if (window.loanStatus === "completed") {
      contadorEl.textContent = "✅ Préstamo finalizado";
      clearInterval(contadorTimer);
      return;
    }
    const ahora = Date.now();
    const diff = finMs - ahora;

    if (diff <= 0) {
      contadorEl.textContent = "⏰ Tiempo finalizado";
      clearInterval(contadorTimer);
      return;
    }

    contadorEl.textContent = formatHHMMSS(diff);

    if (diff <= 10 * 60 * 1000 && !window.alertaMostrada) {
      window.alertaMostrada = true;
      alert("⚠️ Te quedan menos de 10 minutos para devolver el equipo.");
    }
  };

  tick();
  contadorTimer = setInterval(tick, 1000);
}

window.iniciarContador = iniciarContador;

window.addEventListener("load", () => {
  const el = document.getElementById("contador");
  if (!el) return;
  const delivered = el.dataset.delivered || "";
  const hours     = el.dataset.hours || "0";
  const status    = el.dataset.status || "";
  if (status) iniciarContador(delivered, hours, status);
});
