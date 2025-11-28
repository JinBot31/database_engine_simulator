// Detect if the page is being served by Live Server (port 5500) or similar.
// If so, point API calls to the Spring Boot backend at :8081 for local testing.
// If the page is opened via Live Server (127.0.0.1:5500) use the backend port where Spring Boot runs.
// The application is configured to run on port 3000 in `application.properties`.
// If page served by Live Server (127.0.0.1:5500), try backend on 3000 (or change to whichever port Spring Boot uses)
const API_ROOT = (window.location.hostname === '127.0.0.1' || window.location.hostname === 'localhost') && window.location.port === '5500'
  ? 'http://localhost:3000/api'
  : '/api';

const api = {
  tables: API_ROOT + '/tables'
}

let currentTable = null;
let stagedFields = {};
let editingId = null;
let currentFields = [];

async function fetchJSON(url, opts) {
  const res = await fetch(url, opts);
  if (!res.ok) {
    const txt = await res.text();
    throw new Error(txt || res.statusText);
  }
  return res.json();
}

async function loadTables() {
  const list = document.getElementById('tablesList');
  list.innerHTML = '';
  try {
    const tables = await fetchJSON(api.tables);
    for (const t of tables) {
      const li = document.createElement('li');
      li.className = 'list-group-item list-group-item-action';
      li.textContent = t;
      li.onclick = () => selectTable(t);
      // context menu: click with ctrl to delete
      li.onauxclick = (e) => {
        if (confirm(`Eliminar tabla '${t}'? Esta acción no se puede deshacer.`)) {
          fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(t)}`, {method:'DELETE'}).then(()=>loadTables());
        }
      };
      list.appendChild(li);
    }
  } catch (e) {
    console.error(e);
  }
}

async function createTable() {
  const name = document.getElementById('newTableName').value.trim();
  if (!name) return alert('Nombre requerido');
  await fetchJSON(api.tables, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({name})});
  document.getElementById('newTableName').value = '';
  loadTables();
}

async function clearTable() {
  if (!currentTable) return alert('Seleccione una tabla');
  if (!confirm(`Eliminar tabla '${currentTable}' por completo?`)) return;
  await fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(currentTable)}`, {method:'DELETE'});
  currentTable = null;
  document.getElementById('tableTitle').textContent = 'Selecciona una tabla';
  loadTables();
}

async function populateExample() {
  // create table usuarios and insert sample rows like the Main
  const table = 'usuarios';
  try {
    await fetchJSON(api.tables, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({name: table})});
  } catch(e) { console.warn('create table may have failed (exists)'); }
  await fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(table)}/indexes`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({field:'nombre'})});
  await fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(table)}/indexes`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({field:'edad'})});
  const rows = [
    {nombre:'Juan', edad:25, email:'juan@email.com'},
    {nombre:'María', edad:30, email:'maria@email.com'},
    {nombre:'Juan', edad:35, email:'juan2@email.com'}
  ];
  for (const r of rows) {
    await fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(table)}/records`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(r)});
  }
  loadTables();
  selectTable(table);
}

async function selectTable(name) {
  currentTable = name;
  document.getElementById('tableTitle').textContent = `Tabla: ${name}`;
  document.getElementById('noTableMsg').style.display = 'none';
  document.getElementById('recordsTable').style.display = '';
  loadRecords();
}

async function loadRecords() {
  const head = document.getElementById('recordsHead');
  const body = document.getElementById('recordsBody');
  head.innerHTML = '';
  body.innerHTML = '';
  if (!currentTable) return;
  const recs = await fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(currentTable)}/records`);
  if (!recs || recs.length === 0) {
    head.innerHTML = '<tr><th>ID</th><th>Campos</th><th></th></tr>';
    return;
  }
  // build header fields union
  const fields = new Set();
  recs.forEach(r => Object.keys(r).forEach(k => fields.add(k)));
  // remove id from fields list if present (we show ID separately)
  fields.delete('id');
  currentFields = Array.from(fields);
  updateFieldSelect();
  const tr = document.createElement('tr');
  for (const f of fields) {
    const th = document.createElement('th'); th.textContent = f; tr.appendChild(th);
  }
  tr.appendChild(document.createElement('th'));
  head.appendChild(tr);

  for (const r of recs) {
    const tr = document.createElement('tr');
    for (const f of fields) {
      const td = document.createElement('td'); td.textContent = r[f] ?? '';
      tr.appendChild(td);
    }
    const ops = document.createElement('td');
    const del = document.createElement('button'); del.className='btn btn-sm btn-danger me-1'; del.textContent='Eliminar';
    del.onclick = async ()=>{ if(confirm('Eliminar registro?')) { await fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(currentTable)}/records/${r.id}`, {method:'DELETE'}); loadRecords(); }};
    const edit = document.createElement('button'); edit.className='btn btn-sm btn-outline-primary'; edit.textContent='Editar';
    edit.onclick = ()=>{ startEditing(r); };
    ops.appendChild(del);
    ops.appendChild(edit);
    tr.appendChild(ops);
    body.appendChild(tr);
  }
}

function updateFieldSelect() {
  const sel = document.getElementById('fieldSelect');
  sel.innerHTML = '';
  const placeholder = document.createElement('option');
  placeholder.value = '';
  placeholder.textContent = currentFields.length ? '-- Seleccionar campo --' : '-- No hay campos --';
  sel.appendChild(placeholder);
  if (currentFields.length === 0) {
    sel.disabled = true;
  } else {
    sel.disabled = false;
    currentFields.forEach(f => {
      const o = document.createElement('option'); o.value = f; o.textContent = f; sel.appendChild(o);
    });
  }
}

function addFieldToPreview() {
  const select = document.getElementById('fieldSelect');
  const manual = document.getElementById('fieldNameManual');
  const value = document.getElementById('fieldValue').value;
  let name = '';
  if (manual.style.display !== 'none' && manual.value.trim()) {
    name = manual.value.trim();
  } else if (select && select.value) {
    name = select.value;
  }
  if (!name) return alert('Seleccione o escriba el nombre del campo');
  stagedFields[name] = (isNaN(Number(value)) ? value : Number(value));
  // clear manual name and value
  if (manual) manual.value = '';
  if (select) select.value = '';
  document.getElementById('fieldValue').value = '';
  renderPreview();
}

function renderPreview() {
  const node = document.getElementById('fieldsPreview');
  node.innerHTML = '';
  // Render each staged field as a labeled input so user edits values directly
  const keys = Object.keys(stagedFields);
  for (const k of keys) {
    const wrap = document.createElement('div'); wrap.className = 'input-group mb-2';
    const span = document.createElement('span'); span.className = 'input-group-text'; span.textContent = k;
    const input = document.createElement('input'); input.className = 'form-control'; input.value = stagedFields[k];
    input.oninput = (e) => {
      const v = e.target.value;
      stagedFields[k] = (v === '' ? '' : (isNaN(Number(v)) ? v : Number(v)));
    };
    const delBtn = document.createElement('button'); delBtn.className = 'btn btn-outline-danger'; delBtn.type='button'; delBtn.textContent='Eliminar';
    delBtn.onclick = () => { delete stagedFields[k]; renderPreview(); };
    wrap.appendChild(span);
    wrap.appendChild(input);
    wrap.appendChild(delBtn);
    node.appendChild(wrap);
  }
}

async function insertRecord() {
  if (!currentTable) return alert('Seleccione una tabla primero');
  await fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(currentTable)}/records`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(stagedFields)});
  stagedFields = {};
  renderPreview();
  loadRecords();
}

function startEditing(record) {
  editingId = record.id;
  document.getElementById('formTitle').textContent = `Editar registro ${editingId}`;
  document.getElementById('insertRecordBtn').style.display = 'none';
  document.getElementById('saveEditBtn').style.display = '';
  document.getElementById('cancelEditBtn').style.display = '';
  // load record into stagedFields
  stagedFields = {};
  for (const k of Object.keys(record)) {
    if (k === 'id') continue;
    stagedFields[k] = record[k];
  }
  renderPreview();
}

async function saveEdit() {
  if (!currentTable || editingId == null) return alert('Nada para guardar');
  await fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(currentTable)}/records/${editingId}`, {method:'PUT', headers:{'Content-Type':'application/json'}, body: JSON.stringify(stagedFields)});
  editingId = null;
  document.getElementById('formTitle').textContent = 'Insertar registro';
  document.getElementById('insertRecordBtn').style.display = '';
  document.getElementById('saveEditBtn').style.display = 'none';
  document.getElementById('cancelEditBtn').style.display = 'none';
  stagedFields = {};
  renderPreview();
  loadRecords();
}

function cancelEdit() {
  editingId = null;
  document.getElementById('formTitle').textContent = 'Insertar registro';
  document.getElementById('insertRecordBtn').style.display = '';
  document.getElementById('saveEditBtn').style.display = 'none';
  document.getElementById('cancelEditBtn').style.display = 'none';
  stagedFields = {};
  renderPreview();
}

async function createIndex() {
  if (!currentTable) return alert('Seleccione una tabla');
  const f = document.getElementById('indexField').value.trim();
  if (!f) return alert('Nombre de campo requerido');
  await fetchJSON(`${API_ROOT}/tables/${encodeURIComponent(currentTable)}/indexes`, {method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({field: f})});
  alert('Índice creado (si el campo existe)');
}

document.addEventListener('DOMContentLoaded', ()=>{
  loadTables();
  updateFieldSelect();
  document.getElementById('createTableBtn').onclick = createTable;
  document.getElementById('addFieldBtn').onclick = addFieldToPreview;
  document.getElementById('insertRecordBtn').onclick = insertRecord;
  document.getElementById('createIndexBtn').onclick = createIndex;
  document.getElementById('populateBtn').onclick = populateExample;
  document.getElementById('clearBtn').onclick = clearTable;
  document.getElementById('saveEditBtn').onclick = saveEdit;
  document.getElementById('cancelEditBtn').onclick = cancelEdit;
  const toggle = document.getElementById('toggleManualBtn');
  const manual = document.getElementById('fieldNameManual');
  const select = document.getElementById('fieldSelect');
  toggle.onclick = () => {
    if (manual.style.display === 'none') {
      manual.style.display = '';
      select.style.display = 'none';
      toggle.textContent = 'Seleccionar';
    } else {
      manual.style.display = 'none';
      select.style.display = '';
      toggle.textContent = 'Nuevo';
    }
  };
});
