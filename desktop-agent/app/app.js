const invoke = window.__TAURI__?.core?.invoke ?? (async () => {
  throw new Error("API desktop Tauri indisponivel.");
});

const screens = {
  welcome: document.querySelector("#welcomeScreen"),
  pairing: document.querySelector("#pairingScreen"),
  unpairing: document.querySelector("#unpairingScreen"),
};

const state = {
  status: null,
  unpairingTimer: null,
  unpairingCheckRunning: false,
};

const elements = {
  instanceUrlInput: document.querySelector("#instanceUrlInput"),
  pairingCodeInput: document.querySelector("#pairingCodeInput"),
  computerNameValue: document.querySelector("#computerNameValue"),
  userOsValue: document.querySelector("#userOsValue"),
  pairingBadge: document.querySelector("#pairingBadge"),
  pairingStateValue: document.querySelector("#pairingStateValue"),
  pairingResultValue: document.querySelector("#pairingResultValue"),
  pairingFailureValue: document.querySelector("#pairingFailureValue"),
  currentInstanceValue: document.querySelector("#currentInstanceValue"),
  currentDeviceValue: document.querySelector("#currentDeviceValue"),
  unpairingBadge: document.querySelector("#unpairingBadge"),
  unpairingResultValue: document.querySelector("#unpairingResultValue"),
  startPairingButton: document.querySelector("#startPairingButton"),
  pairButton: document.querySelector("#pairButton"),
  cancelUnpairingButton: document.querySelector("#cancelUnpairingButton"),
  unpairButton: document.querySelector("#unpairButton"),
};

function show(screen) {
  Object.values(screens).forEach((element) => element.classList.remove("active"));
  screens[screen].classList.add("active");
}

function setPairingStatus(label, message, failure = "-") {
  elements.pairingBadge.innerHTML = `<span class="dot"></span> ${label}`;
  setText(elements.pairingStateValue, message);
  setText(elements.pairingResultValue, message);
  setText(elements.pairingFailureValue, failure);
}

function setUnpairingStatus(label, message, variant = "danger") {
  elements.unpairingBadge.className = `badge ${variant}`;
  elements.unpairingBadge.innerHTML = `<span class="dot"></span> ${label}`;
  elements.unpairingResultValue.className = variant === "active" ? "warning" : variant;
  setText(elements.unpairingResultValue, message);
}

function stopUnpairingPolling() {
  if (state.unpairingTimer !== null) {
    window.clearTimeout(state.unpairingTimer);
    state.unpairingTimer = null;
  }
}

function scheduleUnpairingPolling() {
  stopUnpairingPolling();
  state.unpairingTimer = window.setTimeout(checkUnpairingStatus, 3000);
}

function renderPendingUnpairing() {
  setUnpairingStatus("DESPAREAMENTO SOLICITADO", "Aguardando aprovacao do administrador", "active");
  elements.cancelUnpairingButton.disabled = true;
  elements.unpairButton.disabled = true;
  setText(elements.unpairButton, "Aguardando aprovacao");
}

function renderRejectedUnpairing() {
  setUnpairingStatus("SOLICITACAO REJEITADA", "Vinculo mantido", "danger");
  elements.cancelUnpairingButton.disabled = false;
  elements.unpairButton.disabled = false;
  setText(elements.unpairButton, "Desparear dispositivo");
}

async function checkUnpairingStatus() {
  if (state.unpairingCheckRunning || !state.status?.has_pairing) {
    return;
  }

  state.unpairingCheckRunning = true;
  try {
    const remote = await invoke("sync_pairing_status");
    if (remote.pairing_status === "unpaired") {
      stopUnpairingPolling();
      const status = await invoke("agent_status");
      renderStatus(status);
      setUnpairingStatus("DESPAREADO", "Vinculo removido", "success");
      elements.cancelUnpairingButton.disabled = true;
      elements.unpairButton.disabled = false;
      setText(elements.unpairButton, "Iniciar novo pareamento");
      show("unpairing");
      return;
    }

    if (remote.unpairing_status === "rejected") {
      stopUnpairingPolling();
      renderRejectedUnpairing();
      return;
    }

    renderPendingUnpairing();
    scheduleUnpairingPolling();
  } catch (error) {
    setUnpairingStatus("SYNC PENDENTE", String(error), "danger");
    scheduleUnpairingPolling();
  } finally {
    state.unpairingCheckRunning = false;
  }
}

function renderPairingAction(hasPairing) {
  elements.pairButton.textContent = hasPairing
    ? "Desparear dispositivo"
    : "Validar codigo";
  elements.pairButton.classList.toggle("primary", !hasPairing);
  elements.pairButton.classList.toggle("destructive", hasPairing);
  elements.instanceUrlInput.disabled = hasPairing;
  elements.pairingCodeInput.disabled = hasPairing;
}

function renderStatus(status) {
  state.status = status;
  renderPairingAction(status.has_pairing);
  setText(elements.computerNameValue, status.computer_name || "Detectando");
  setText(elements.userOsValue, status.user_os || "Detectando");
  setText(elements.currentInstanceValue, status.instance_url || "-");
  setText(elements.currentDeviceValue, status.device_name || "-");

  if (status.instance_url && !elements.instanceUrlInput.value) {
    elements.instanceUrlInput.value = status.instance_url;
  }
}

async function refresh(navigate = true) {
  let status = await invoke("agent_status");
  let remote = null;

  if (status.has_pairing) {
    try {
      remote = await invoke("sync_pairing_status");
      if (remote.pairing_status === "unpaired") {
        status = await invoke("agent_status");
      }
    } catch (_) {
      remote = null;
    }
  }

  renderStatus(status);
  if (!navigate) {
    return status;
  }
  if (status.has_pairing) {
    show("unpairing");
    if (remote?.unpairing_status === "pending") {
      renderPendingUnpairing();
      scheduleUnpairingPolling();
    } else if (remote?.unpairing_status === "rejected") {
      renderRejectedUnpairing();
    }
  } else {
    show("welcome");
  }
}

async function pair() {
  const instanceUrl = elements.instanceUrlInput.value.trim();
  const pairingCode = elements.pairingCodeInput.value.trim();
  elements.pairButton.disabled = true;
  setPairingStatus("VALIDANDO", "Validando contrato local");

  try {
    if (!instanceUrl || !pairingCode) {
      throw new Error("Informe URL da instancia e codigo de pareamento.");
    }
    setPairingStatus("CONECTANDO API", "Conectando API");
    const response = await invoke("complete_pairing", {
      request: {
        instanceUrl,
        pairingCode,
      },
    });
    setPairingStatus("PAREADO", `Pareado como ${response.device_name}`);
    await refresh(false);
    setPairingStatus("PAREADO", `Pareado como ${response.device_name}`);
    show("pairing");
  } catch (error) {
    setPairingStatus("FALHA", "Falha", String(error));
  } finally {
    elements.pairButton.disabled = false;
  }
}

async function unpair() {
  let submitted = false;
  elements.unpairButton.disabled = true;
  elements.cancelUnpairingButton.disabled = true;
  setText(elements.unpairingResultValue, "Enviando solicitacao");
  elements.unpairingBadge.innerHTML = '<span class="dot"></span> DESPAREAMENTO SOLICITADO';

  try {
    const response = await invoke("request_unpairing");
    submitted = response.status === "pending";
    renderPendingUnpairing();
    show("unpairing");
    scheduleUnpairingPolling();
  } catch (error) {
    setUnpairingStatus("FALHA API", String(error), "danger");
    elements.cancelUnpairingButton.disabled = false;
  } finally {
    if (!submitted) {
      elements.unpairButton.disabled = false;
    }
  }
}

elements.startPairingButton.addEventListener("click", () => show("pairing"));
elements.pairButton.addEventListener("click", () => {
  if (state.status?.has_pairing) {
    show("unpairing");
    return;
  }

  pair();
});
elements.cancelUnpairingButton.addEventListener("click", () => {
  setUnpairingStatus("CONFIRMACAO EXIGIDA", "Nenhuma alteracao aplicada", "danger");
  show("pairing");
});
elements.unpairButton.addEventListener("click", () => {
  if (!state.status?.has_pairing) {
    setText(elements.unpairButton, "Desparear dispositivo");
    elements.cancelUnpairingButton.disabled = false;
    show("welcome");
    return;
  }

  unpair();
});

refresh().catch((error) => {
  setPairingStatus("FALHA", "Falha", String(error));
  show("welcome");
});

function setText(element, value) {
  if (element) {
    element.textContent = value;
  }
}
