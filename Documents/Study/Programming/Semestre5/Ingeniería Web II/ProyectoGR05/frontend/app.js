const qs = (id) => document.getElementById(id);

const state = {
  apiBaseUrl: 'http://localhost:4000/api',
  generos: [],
  directores: [],
  productoras: [],
  tipos: [],
  lastInfoType: 'generos',
  backendOnline: false
};

function setFieldMessage(fieldId, text = '', type = '') {
  const msgEl = qs(`${fieldId}Msg`);
  if (!msgEl) return;
  msgEl.textContent = text;
  msgEl.classList.remove('success', 'error');
  if (type) msgEl.classList.add(type);
}

function clearFieldMessages(fieldIds = []) {
  fieldIds.forEach((id) => setFieldMessage(id, '', ''));
}

function showFieldSuccess(fieldId, text) {
  setFieldMessage(fieldId, text, 'success');
}

function showFieldError(fieldId, text) {
  setFieldMessage(fieldId, text, 'error');
}

function logMessage(text, type = 'success') {
  console[type === 'error' ? 'error' : 'log'](`[${type}] ${text}`);
  const panel = qs('infoPanel');
  if (!panel) return;

  const line = document.createElement('div');
  line.className = `info-line ${type}`;
  line.textContent = text;
  panel.prepend(line);
}

function setCrudButtonsDisabled(disabled) {
  const ids = [
    'generoUpdateBtn', 'generoDeleteBtn', 'reloadGenerosBtn',
    'directorUpdateBtn', 'directorDeleteBtn', 'reloadDirectoresBtn',
    'productoraUpdateBtn', 'productoraDeleteBtn', 'reloadProductorasBtn',
    'tipoUpdateBtn', 'tipoDeleteBtn', 'reloadTiposBtn',
    'mediaUpdateBtn', 'mediaDeleteBtn', 'reloadMediasBtn',
    'infoPublishBtn', 'infoRefreshBtn'
  ];

  ids.forEach((id) => {
    const el = qs(id);
    if (el) el.disabled = disabled;
  });
}

async function checkBackendOnline() {
  try {
    await fetch(`${state.apiBaseUrl}/generos`, { method: 'GET' });
    state.backendOnline = true;
    setCrudButtonsDisabled(false);
    return true;
  } catch {
    state.backendOnline = false;
    setCrudButtonsDisabled(true);
    return false;
  }
}

function validateGeneroInput({ nombre, descripcion, estado }) {
  if (!nombre || !nombre.trim()) {
    return 'El nombre es obligatorio';
  }

  if (descripcion && descripcion.length > 300) {
    return 'La descripción no puede superar 300 caracteres';
  }

  if (estado && !['Activo', 'Inactivo'].includes(estado)) {
    return 'El estado debe ser Activo o Inactivo';
  }

  return '';
}

async function ensureBackendAvailable(fieldId = null) {
  const ok = await checkBackendOnline();
  if (ok) return true;

  const message = 'Backend no disponible en puerto 4000';
  if (fieldId) showFieldError(fieldId, message);
  logMessage(message, 'error');
  return false;
}

async function apiFetch(path, options = {}) {
  const url = `${state.apiBaseUrl}${path}`;

  let response;
  try {
    response = await fetch(url, {
      headers: { 'Content-Type': 'application/json' },
      ...options
    });
  } catch {
    throw new Error('No se pudo conectar con el servidor. Verifica que el backend esté encendido en el puerto 4000.');
  }

  let data = null;
  try {
    data = await response.json();
  } catch {
    data = null;
  }

  if (!response.ok) {
    const errorMessage = data?.message || `Error del servidor (${response.status})`;
    throw new Error(errorMessage);
  }

  return data;
}

function fillSelect(selectId, items, labelField, valueField = '_id', placeholder = 'Seleccione...') {
  const select = qs(selectId);
  if (!select) return;
  select.innerHTML = `<option value="">${placeholder}</option>`;
  items.forEach((item) => {
    const op = document.createElement('option');
    op.value = item[valueField];
    op.textContent = item[labelField];
    select.appendChild(op);
  });
}

async function loadGeneros() {
  state.generos = await apiFetch('/generos');
}

async function loadDirectores() {
  state.directores = await apiFetch('/directores');
}

async function loadProductoras() {
  state.productoras = await apiFetch('/productoras');
}

async function loadTipos() {
  state.tipos = await apiFetch('/tipos');
}

function loadMediaDependencies() {
  const generosActivos = state.generos.filter((g) => g.estado === 'Activo');
  const directoresActivos = state.directores.filter((d) => d.estado === 'Activo');
  const productorasActivas = state.productoras.filter((p) => p.estado === 'Activo');

  fillSelect('mediaGenero', generosActivos, 'nombre', '_id', 'Seleccione género');
  fillSelect('mediaDirector', directoresActivos, 'nombres', '_id', 'Seleccione director');
  fillSelect('mediaProductora', productorasActivas, 'nombre', '_id', 'Seleccione productora');
  fillSelect('mediaTipo', state.tipos, 'nombre', '_id', 'Seleccione tipo');
}

async function reloadAll() {
  await loadGeneros();
  await loadDirectores();
  await loadProductoras();
  await loadTipos();
  loadMediaDependencies();
}

function initGeneroEvents() {
  const form = qs('generoForm');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFieldMessages(['generoNombre', 'generoDescripcion', 'generoEstado']);

    const nombre = qs('generoNombre').value.trim();
    const descripcion = qs('generoDescripcion').value.trim();
    const estado = qs('generoEstado').value;

    const validationMessage = validateGeneroInput({ nombre, descripcion, estado });
    if (validationMessage) return showFieldError('generoNombre', validationMessage);

    if (!(await ensureBackendAvailable('generoNombre'))) return;

    try {
      await apiFetch('/generos', {
        method: 'POST',
        body: JSON.stringify({ nombre, descripcion, estado })
      });
      showFieldSuccess('generoNombre', 'Creado');
      logMessage('Género creado correctamente', 'success');
      await reloadAll();
    } catch (err) {
      showFieldError('generoNombre', err.message);
      logMessage(`Géneros: ${err.message}`, 'error');
    }
  });

  qs('generoUpdateBtn').addEventListener('click', async () => {
    clearFieldMessages(['generoNombre', 'generoDescripcion', 'generoEstado']);

    const nombre = qs('generoNombre').value.trim();
    const descripcion = qs('generoDescripcion').value.trim();
    const estado = qs('generoEstado').value;

    if (!nombre) return showFieldError('generoNombre', 'Ingresa el nombre del género');

    const validationMessage = validateGeneroInput({ nombre, descripcion, estado });
    if (validationMessage) return showFieldError('generoNombre', validationMessage);

    if (!(await ensureBackendAvailable('generoNombre'))) return;

    try {
      await apiFetch(`/generos/${encodeURIComponent(nombre)}`, {
        method: 'PUT',
        body: JSON.stringify({ nombre, descripcion, estado })
      });
      showFieldSuccess('generoNombre', 'Actualizado');
      logMessage('Género actualizado correctamente', 'success');
      await reloadAll();
    } catch (err) {
      showFieldError('generoNombre', err.message);
      logMessage(`Géneros: ${err.message}`, 'error');
    }
  });

  qs('generoDeleteBtn').addEventListener('click', async () => {
    clearFieldMessages(['generoNombre', 'generoDescripcion', 'generoEstado']);

    const nombre = qs('generoNombre').value.trim();
    if (!nombre) return showFieldError('generoNombre', 'Ingresa el nombre del género');

    if (!(await ensureBackendAvailable('generoNombre'))) return;

    try {
      await apiFetch(`/generos/${encodeURIComponent(nombre)}`, { method: 'DELETE' });
      showFieldSuccess('generoNombre', 'Eliminado');
      logMessage('Género eliminado correctamente', 'success');
      form.reset();
      await reloadAll();
    } catch (err) {
      showFieldError('generoNombre', err.message);
      logMessage(`Géneros: ${err.message}`, 'error');
    }
  });

  qs('reloadGenerosBtn').addEventListener('click', async () => {
    try {
      await reloadAll();
      showFieldSuccess('generoNombre', 'Éxito');
      logMessage('Datos recargados correctamente', 'success');
    } catch (err) {
      showFieldError('generoNombre', 'Fallido');
      logMessage(`Recarga: ${err.message}`, 'error');
    }
  });
}

function initDirectorEvents() {
  const form = qs('directorForm');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFieldMessages(['directorNombres', 'directorEstado']);

    const nombres = qs('directorNombres').value.trim();
    const estado = qs('directorEstado').value;

    if (!nombres) return showFieldError('directorNombres', 'El nombre es obligatorio');

    try {
      await apiFetch('/directores', {
        method: 'POST',
        body: JSON.stringify({ nombres, estado })
      });
      showFieldSuccess('directorNombres', 'Éxito');
      logMessage('Director creado correctamente', 'success');
      await reloadAll();
    } catch (err) {
      showFieldError('directorNombres', 'Fallido');
      logMessage(`Directores: ${err.message}`, 'error');
    }
  });

  qs('directorUpdateBtn').addEventListener('click', async () => {
    clearFieldMessages(['directorNombres', 'directorEstado']);

    const nombres = qs('directorNombres').value.trim();
    const estado = qs('directorEstado').value;

    if (!nombres) return showFieldError('directorNombres', 'Ingresa el nombre del director');

    try {
      await apiFetch(`/directores/${encodeURIComponent(nombres)}`, {
        method: 'PUT',
        body: JSON.stringify({ estado })
      });
      showFieldSuccess('directorNombres', 'Éxito');
      logMessage('Director actualizado correctamente', 'success');
      await reloadAll();
    } catch (err) {
      showFieldError('directorNombres', 'Fallido');
      logMessage(`Directores: ${err.message}`, 'error');
    }
  });

  qs('directorDeleteBtn').addEventListener('click', async () => {
    clearFieldMessages(['directorNombres', 'directorEstado']);

    const nombres = qs('directorNombres').value.trim();
    if (!nombres) return showFieldError('directorNombres', 'Ingresa el nombre del director');

    try {
      await apiFetch(`/directores/${encodeURIComponent(nombres)}`, { method: 'DELETE' });
      showFieldSuccess('directorNombres', 'Éxito');
      logMessage('Director eliminado correctamente', 'success');
      form.reset();
      await reloadAll();
    } catch (err) {
      showFieldError('directorNombres', 'Fallido');
      logMessage(`Directores: ${err.message}`, 'error');
    }
  });

  qs('reloadDirectoresBtn').addEventListener('click', async () => {
    try {
      await reloadAll();
      showFieldSuccess('directorNombres', 'Éxito');
      logMessage('Datos recargados correctamente', 'success');
    } catch (err) {
      showFieldError('directorNombres', 'Fallido');
      logMessage(`Recarga: ${err.message}`, 'error');
    }
  });
}

function initProductoraEvents() {
  const form = qs('productoraForm');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFieldMessages(['productoraNombre', 'productoraSlogan', 'productoraDescripcion', 'productoraEstado']);

    const nombre = qs('productoraNombre').value.trim();
    const slogan = qs('productoraSlogan').value.trim();
    const descripcion = qs('productoraDescripcion').value.trim();
    const estado = qs('productoraEstado').value;

    if (!nombre) return showFieldError('productoraNombre', 'El nombre es obligatorio');

    try {
      await apiFetch('/productoras', {
        method: 'POST',
        body: JSON.stringify({ nombre, slogan, descripcion, estado })
      });
      showFieldSuccess('productoraNombre', 'Éxito');
      logMessage('Productora creada correctamente', 'success');
      await reloadAll();
    } catch (err) {
      showFieldError('productoraNombre', 'Fallido');
      logMessage(`Productoras: ${err.message}`, 'error');
    }
  });

  qs('productoraUpdateBtn').addEventListener('click', async () => {
    clearFieldMessages(['productoraNombre', 'productoraSlogan', 'productoraDescripcion', 'productoraEstado']);

    const nombre = qs('productoraNombre').value.trim();
    const slogan = qs('productoraSlogan').value.trim();
    const descripcion = qs('productoraDescripcion').value.trim();
    const estado = qs('productoraEstado').value;

    if (!nombre) return showFieldError('productoraNombre', 'Ingresa el nombre de la productora');

    try {
      await apiFetch(`/productoras/${encodeURIComponent(nombre)}`, {
        method: 'PUT',
        body: JSON.stringify({ slogan, descripcion, estado })
      });
      showFieldSuccess('productoraNombre', 'Éxito');
      logMessage('Productora actualizada correctamente', 'success');
      await reloadAll();
    } catch (err) {
      showFieldError('productoraNombre', 'Fallido');
      logMessage(`Productoras: ${err.message}`, 'error');
    }
  });

  qs('productoraDeleteBtn').addEventListener('click', async () => {
    clearFieldMessages(['productoraNombre', 'productoraSlogan', 'productoraDescripcion', 'productoraEstado']);

    const nombre = qs('productoraNombre').value.trim();
    if (!nombre) return showFieldError('productoraNombre', 'Ingresa el nombre de la productora');

    try {
      await apiFetch(`/productoras/${encodeURIComponent(nombre)}`, { method: 'DELETE' });
      showFieldSuccess('productoraNombre', 'Éxito');
      logMessage('Productora eliminada correctamente', 'success');
      form.reset();
      await reloadAll();
    } catch (err) {
      showFieldError('productoraNombre', 'Fallido');
      logMessage(`Productoras: ${err.message}`, 'error');
    }
  });

  qs('reloadProductorasBtn').addEventListener('click', async () => {
    try {
      await reloadAll();
      showFieldSuccess('productoraNombre', 'Éxito');
      logMessage('Datos recargados correctamente', 'success');
    } catch (err) {
      showFieldError('productoraNombre', 'Fallido');
      logMessage(`Recarga: ${err.message}`, 'error');
    }
  });
}

function initTipoEvents() {
  const form = qs('tipoForm');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFieldMessages(['tipoNombre', 'tipoDescripcion']);

    const nombre = qs('tipoNombre').value.trim();
    const descripcion = qs('tipoDescripcion').value.trim();

    if (!nombre) return showFieldError('tipoNombre', 'El nombre es obligatorio');

    try {
      await apiFetch('/tipos', {
        method: 'POST',
        body: JSON.stringify({ nombre, descripcion })
      });
      showFieldSuccess('tipoNombre', 'Éxito');
      logMessage('Tipo creado correctamente', 'success');
      await reloadAll();
    } catch (err) {
      showFieldError('tipoNombre', 'Fallido');
      logMessage(`Tipos: ${err.message}`, 'error');
    }
  });

  qs('tipoUpdateBtn').addEventListener('click', async () => {
    clearFieldMessages(['tipoNombre', 'tipoDescripcion']);

    const nombre = qs('tipoNombre').value.trim();
    const descripcion = qs('tipoDescripcion').value.trim();

    if (!nombre) return showFieldError('tipoNombre', 'Ingresa el nombre del tipo');

    try {
      await apiFetch(`/tipos/${encodeURIComponent(nombre)}`, {
        method: 'PUT',
        body: JSON.stringify({ descripcion })
      });
      showFieldSuccess('tipoNombre', 'Éxito');
      logMessage('Tipo actualizado correctamente', 'success');
      await reloadAll();
    } catch (err) {
      showFieldError('tipoNombre', 'Fallido');
      logMessage(`Tipos: ${err.message}`, 'error');
    }
  });

  qs('tipoDeleteBtn').addEventListener('click', async () => {
    clearFieldMessages(['tipoNombre', 'tipoDescripcion']);

    const nombre = qs('tipoNombre').value.trim();
    if (!nombre) return showFieldError('tipoNombre', 'Ingresa el nombre del tipo');

    try {
      await apiFetch(`/tipos/${encodeURIComponent(nombre)}`, { method: 'DELETE' });
      showFieldSuccess('tipoNombre', 'Éxito');
      logMessage('Tipo eliminado correctamente', 'success');
      form.reset();
      await reloadAll();
    } catch (err) {
      showFieldError('tipoNombre', 'Fallido');
      logMessage(`Tipos: ${err.message}`, 'error');
    }
  });

  qs('reloadTiposBtn').addEventListener('click', async () => {
    try {
      await reloadAll();
      showFieldSuccess('tipoNombre', 'Éxito');
      logMessage('Datos recargados correctamente', 'success');
    } catch (err) {
      showFieldError('tipoNombre', 'Fallido');
      logMessage(`Recarga: ${err.message}`, 'error');
    }
  });
}

function initMediaEvents() {
  const form = qs('mediaForm');

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    clearFieldMessages([
      'mediaSerial',
      'mediaTitulo',
      'mediaSinopsis',
      'mediaUrl',
      'mediaImagenPortada',
      'mediaAnioEstreno',
      'mediaGenero',
      'mediaDirector',
      'mediaProductora',
      'mediaTipo'
    ]);

    const payload = {
      serial: qs('mediaSerial').value.trim(),
      titulo: qs('mediaTitulo').value.trim(),
      sinopsis: qs('mediaSinopsis').value.trim(),
      url: qs('mediaUrl').value.trim(),
      imagenPortada: qs('mediaImagenPortada').value.trim(),
      anioEstreno: Number(qs('mediaAnioEstreno').value),
      genero: qs('mediaGenero').value,
      director: qs('mediaDirector').value,
      productora: qs('mediaProductora').value,
      tipo: qs('mediaTipo').value
    };

    if (!payload.serial) return showFieldError('mediaSerial', 'El serial es obligatorio');
    if (!payload.titulo) return showFieldError('mediaTitulo', 'El título es obligatorio');
    if (!payload.url) return showFieldError('mediaUrl', 'La URL es obligatoria');
    if (!payload.anioEstreno) return showFieldError('mediaAnioEstreno', 'El año de estreno es obligatorio');
    if (!payload.genero) return showFieldError('mediaGenero', 'Selecciona un género');
    if (!payload.director) return showFieldError('mediaDirector', 'Selecciona un director');
    if (!payload.productora) return showFieldError('mediaProductora', 'Selecciona una productora');
    if (!payload.tipo) return showFieldError('mediaTipo', 'Selecciona un tipo');

    try {
      await apiFetch('/medias', { method: 'POST', body: JSON.stringify(payload) });
      showFieldSuccess('mediaSerial', 'Éxito');
      logMessage('Media creada correctamente', 'success');
      form.reset();
    } catch (err) {
      showFieldError('mediaSerial', 'Fallido');
      logMessage(`Media: ${err.message}`, 'error');
    }
  });

  qs('mediaUpdateBtn').addEventListener('click', async () => {
    clearFieldMessages([
      'mediaSerial',
      'mediaTitulo',
      'mediaSinopsis',
      'mediaUrl',
      'mediaImagenPortada',
      'mediaAnioEstreno',
      'mediaGenero',
      'mediaDirector',
      'mediaProductora',
      'mediaTipo'
    ]);

    const serial = qs('mediaSerial').value.trim();
    if (!serial) return showFieldError('mediaSerial', 'Ingresa el serial de la media');

    const payload = {
      titulo: qs('mediaTitulo').value.trim(),
      sinopsis: qs('mediaSinopsis').value.trim(),
      url: qs('mediaUrl').value.trim(),
      imagenPortada: qs('mediaImagenPortada').value.trim(),
      anioEstreno: Number(qs('mediaAnioEstreno').value),
      genero: qs('mediaGenero').value,
      director: qs('mediaDirector').value,
      productora: qs('mediaProductora').value,
      tipo: qs('mediaTipo').value
    };

    try {
      await apiFetch(`/medias/${encodeURIComponent(serial)}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      showFieldSuccess('mediaSerial', 'Éxito');
      logMessage('Media actualizada correctamente', 'success');
      form.reset();
    } catch (err) {
      showFieldError('mediaSerial', 'Fallido');
      logMessage(`Media: ${err.message}`, 'error');
    }
  });

  qs('mediaDeleteBtn').addEventListener('click', async () => {
    clearFieldMessages([
      'mediaSerial',
      'mediaTitulo',
      'mediaSinopsis',
      'mediaUrl',
      'mediaImagenPortada',
      'mediaAnioEstreno',
      'mediaGenero',
      'mediaDirector',
      'mediaProductora',
      'mediaTipo'
    ]);

    const serial = qs('mediaSerial').value.trim();
    if (!serial) return showFieldError('mediaSerial', 'Ingresa el serial de la media');

    try {
      await apiFetch(`/medias/${encodeURIComponent(serial)}`, { method: 'DELETE' });
      showFieldSuccess('mediaSerial', 'Éxito');
      logMessage('Media eliminada correctamente', 'success');
      form.reset();
    } catch (err) {
      showFieldError('mediaSerial', 'Fallido');
      logMessage(`Media: ${err.message}`, 'error');
    }
  });

  qs('reloadMediasBtn').addEventListener('click', async () => {
    try {
      await reloadAll();
      showFieldSuccess('mediaSerial', 'Éxito');
      logMessage('Datos recargados correctamente', 'success');
    } catch (err) {
      showFieldError('mediaSerial', 'Fallido');
      logMessage(`Recarga: ${err.message}`, 'error');
    }
  });
}

async function initApp() {
  initGeneroEvents();
  initDirectorEvents();
  initProductoraEvents();
  initTipoEvents();
  initMediaEvents();

  try {
    const online = await checkBackendOnline();
    if (!online) {
      logMessage('No se pudo conectar con el servidor. Verifica que el backend esté encendido en el puerto 4000.', 'error');
      return;
    }

    await reloadAll();
    logMessage('Aplicación lista', 'success');
  } catch (e) {
    logMessage(`Carga inicial fallida: ${e.message}`, 'error');
  }
}

function printInfoLines(lines = []) {
  const panel = qs('infoPanel');
  if (!panel) return;
  panel.innerHTML = '';

  if (!lines.length) {
    const empty = document.createElement('div');
    empty.className = 'info-line';
    empty.textContent = 'No hay datos para mostrar.';
    panel.appendChild(empty);
    return;
  }

  lines.forEach((text) => {
    const row = document.createElement('div');
    row.className = 'info-line';
    row.textContent = text;
    panel.appendChild(row);
  });
}

function formatInfoRows(type, data) {
  switch (type) {
    case 'generos':
      return data.map((g) => `Género: ${g.nombre} | Estado: ${g.estado || '-'} | Descripción: ${g.descripcion || '-'}`);
    case 'directores':
      return data.map((d) => `Director: ${d.nombres} | Estado: ${d.estado || '-'}`);
    case 'productoras':
      return data.map((p) => `Productora: ${p.nombre} | Estado: ${p.estado || '-'} | Slogan: ${p.slogan || '-'}`);
    case 'tipos':
      return data.map((t) => `Tipo: ${t.nombre} | Descripción: ${t.descripcion || '-'}`);
    case 'medias':
      return data.map((m) => `Media: ${m.titulo} | Serial: ${m.serial} | Año: ${m.anioEstreno} | Género: ${m.genero?.nombre || '-'} | Director: ${m.director?.nombres || '-'} | Productora: ${m.productora?.nombre || '-'} | Tipo: ${m.tipo?.nombre || '-'}`);
    default:
      return [];
  }
}

async function getInfoData(type) {
  if (type === 'todo') {
    const [generos, directores, productoras, tipos, medias] = await Promise.all([
      apiFetch('/generos'),
      apiFetch('/directores'),
      apiFetch('/productoras'),
      apiFetch('/tipos'),
      apiFetch('/medias')
    ]);

    return [
      '=== GÉNEROS ===',
      ...formatInfoRows('generos', generos),
      '=== DIRECTORES ===',
      ...formatInfoRows('directores', directores),
      '=== PRODUCTORAS ===',
      ...formatInfoRows('productoras', productoras),
      '=== TIPOS ===',
      ...formatInfoRows('tipos', tipos),
      '=== MEDIAS ===',
      ...formatInfoRows('medias', medias)
    ];
  }

  const endpointByType = {
    generos: '/generos',
    directores: '/directores',
    productoras: '/productoras',
    tipos: '/tipos',
    medias: '/medias'
  };

  const endpoint = endpointByType[type];
  if (!endpoint) return [];

  const data = await apiFetch(endpoint);
  return formatInfoRows(type, data);
}

function initInfoEvents() {
  const typeSelect = qs('infoType');
  const publishBtn = qs('infoPublishBtn');
  const refreshBtn = qs('infoRefreshBtn');

  if (!typeSelect || !publishBtn || !refreshBtn) return;

  publishBtn.addEventListener('click', async () => {
    const selectedType = typeSelect.value;
    state.lastInfoType = selectedType;

    try {
      const lines = await getInfoData(selectedType);
      printInfoLines(lines);
      logMessage('Información publicada desde base de datos', 'success');
    } catch (err) {
      logMessage(`Información: ${err.message}`, 'error');
    }
  });

  refreshBtn.addEventListener('click', async () => {
    try {
      const lines = await getInfoData(state.lastInfoType || typeSelect.value);
      printInfoLines(lines);
      logMessage('Información refrescada', 'success');
    } catch (err) {
      logMessage(`Información: ${err.message}`, 'error');
    }
  });
}

async function initApp() {
  initGeneroEvents();
  initDirectorEvents();
  initProductoraEvents();
  initTipoEvents();
  initMediaEvents();
  initInfoEvents();

  try {
    await reloadAll();
    logMessage('Aplicación lista', 'success');
  } catch (e) {
    logMessage(`Carga inicial fallida: ${e.message}`, 'error');
  }
}

initApp();
