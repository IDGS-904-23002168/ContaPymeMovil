# Documentación técnica de APIs — ContaPymeWeb

> Documento técnico para desarrolladores que consumen la API REST de ContaPymeWeb.  
> Basado en el análisis del código fuente actual (`server/*`, `src/services/api.js`, rutas/controladores/servicios/repositorios/validadores).  
> Fecha de generación: 2026-11-25.

## Índice

- [1. Visión general](#1-visión-general)
- [2. Configuración y ejecución](#2-configuración-y-ejecución)
- [3. Autenticación y autorización](#3-autenticación-y-autorización)
- [4. Convenciones de respuesta y errores](#4-convenciones-de-respuesta-y-errores)
- [5. Multiempresa, bitácora y reglas transversales](#5-multiempresa-bitácora-y-reglas-transversales)
- [6. Módulos y endpoints](#6-módulos-y-endpoints)
  - [6.1 Auth](#61-auth-apiauth)
  - [6.2 Usuarios](#62-usuarios-apiusers)
  - [6.3 Roles](#63-roles-apiroles)
  - [6.4 Clientes](#64-clientes-apiclientes-alias-apiclients)
  - [6.5 Proveedores](#65-proveedores-apiproveedores-alias-apisuppliers)
  - [6.6 Categorías](#66-categorías-apicategorias)
  - [6.7 Productos](#67-productos-apiproductos)
  - [6.8 Inventario](#68-inventario-apiinventario)
  - [6.9 Ingresos](#69-ingresos-apiingresos)
  - [6.10 Gastos](#610-gastos-apigastos)
  - [6.11 Caja](#611-caja-apicaja)
  - [6.12 Cuentas por Cobrar (CxC)](#612-cuentas-por-cobrar-cxc-apicxc)
  - [6.13 Cuentas por Pagar (CxP)](#613-cuentas-por-pagar-cxp-apicxp)
  - [6.14 Ventas](#614-ventas-apiventas)
  - [6.15 Compras](#615-compras-apicompras)
  - [6.16 Reportes](#616-reportes-apireportes)
  - [6.17 Dashboard](#617-dashboard-apidashboard)
  - [6.18 Notificaciones](#618-notificaciones-apinotificaciones)
  - [6.19 Bitácora](#619-bitácora-apibitacora)
- [7. Consumo desde el frontend actual](#7-consumo-desde-el-frontend-actual)
- [8. Inconsistencias y riesgos detectados](#8-inconsistencias-y-riesgos-detectados)

---

## 1. Visión general

| Elemento | Valor |
|---|---|
| Stack backend | Node.js + Express 5, PostgreSQL (`pg`), JWT (`jsonwebtoken`), bcrypt (`bcryptjs`), nodemailer, express-rate-limit |
| Stack frontend | React 19 + Vite 7, cliente HTTP propio con `fetch` (`src/services/api.js`) |
| Base API | `/api` |
| Puerto backend por defecto | `process.env.PORT || 5000` (`server/index.js`). `.env.example` muestra `PORT=5000`: **verificar el `.env` real**. |
| Health/doc rápida | `GET /api` devuelve metadata y un mapa estático parcial de endpoints. No es contrato completo; la fuente de verdad son rutas/controladores. |
| Formato | JSON (`Content-Type: application/json`) |
| DB | PostgreSQL. `DATABASE_URL` obligatoria. SSL activo salvo `DB_SSL=false`. |

## 2. Configuración y ejecución

### Variables de entorno (`.env.example`)

```env
PORT=5000
NODE_ENV=development
DATABASE_URL=postgresql://...
DB_SSL=true
DB_POOL_MAX=15
JWT_SECRET=...
JWT_EXPIRES_IN=8h
JWT_2FA_SECRET=...
SMTP_HOST=
SMTP_PORT=587
SMTP_SECURE=false
SMTP_USER=
SMTP_PASS=
EMAIL_FROM="ContaPyme Seguridad" <no-reply@contapyme.com>
INACTIVITY_TIMEOUT_MINUTES=15
```

**Notas:**
- `JWT_SECRET` y `JWT_2FA_SECRET` tienen fallbacks hardcodeados en código si faltan; configurar secretos reales en producción.
- `INACTIVITY_TIMEOUT_MINUTES` está en `.env.example`; el timeout de inactividad también se maneja en frontend.
- SMTP opcional: si no está configurado, el envío 2FA/recuperación usa fallback/log en consola (ver Auth).

### Scripts (`package.json`)

| Script | Comando | Uso |
|---|---|---|
| `dev` | `vite` | Frontend dev server (puerto 5173 por `vite.config.js`) |
| `server` | `node server/index.js` | API backend |
| `db:check` | `node server/test_db.js` | Diagnóstico de conexión BD |
| `build` | `vite build` | Build frontend |

### CORS, parsers, logging, 404 y errores

- **CORS**: `origin: true`, `credentials: true`, métodos `GET,POST,PUT,PATCH,DELETE,OPTIONS`, headers permitidos `Content-Type, Authorization, X-Requested-With`.
- **Body parsers**: `express.json({ limit: '2mb' })`, `urlencoded({ extended: true, limit: '2mb' })`.
- **Logging**: para rutas `/api`, loguea `[METHOD] URL - STATUS (ms)`.
- **404 API**: `{ "success": false, "message": "Ruta API no encontrada: [METHOD] /api/..." }`.
- **Error global**: `server/utils/http.js#errorHandler`.
  - `AppError` → `status` con `{ success:false, message, errors? }`.
  - Otro error → `500 { success:false, message:"Error interno del servidor." }`.

## 3. Autenticación y autorización

### 3.1 Flujo de autenticación (2FA por correo)

1. **Login**: `POST /api/auth/login` con credenciales. Si son válidas y cuenta/rol/empresa activos, genera código 2FA de 6 dígitos, lo guarda con expiración de 10 minutos, envía correo y responde `tempToken` (JWT temporal).
2. **Verificación 2FA**: `POST /api/auth/verify-2fa` con `tempToken + codigo`. Si es válido, responde `token` (JWT de sesión) y `user`.
3. **Uso de API**: enviar `Authorization: Bearer <token>` en endpoints protegidos.

### 3.2 JWT

| Token | Secret | Expiración | Payload |
|---|---|---:|---|
| Temporal 2FA | `JWT_2FA_SECRET` o fallback hardcodeado | `10m` | `{ id_usuario, nombre_usuario, empresa_id, step: '2fa_pending' }` |
| Sesión | `JWT_SECRET` o fallback hardcodeado | `JWT_EXPIRES_IN` o `8h` | `{ id_usuario, nombre_usuario, id_rol, rol_nombre, empresa_id }` |

> El JWT de sesión se firma con payload mínimo; `verifyToken` reconstruye `req.user` desde BD en cada request (estado actual de usuario/rol/empresa/permisos).

### 3.3 Header de autorización

```http
Authorization: Bearer <jwt>
```

### 3.4 `req.user` inyectado por `verifyToken`

```json
{
  "id_usuario": 1,
  "nombre": "...",
  "apellido_paterno": "...",
  "apellido_materno": "...",
  "nombre_completo": "Nombre Apellido",
  "nombre_usuario": "...",
  "correo": "...",
  "id_rol": 1,
  "rol_nombre": "Administrador",
  "empresa_id": 1,
  "empresa_nombre": "Mi Empresa",
  "permisos": ["ventas", "compras", "..."]
}
```

### 3.5 Middlewares de autorización

| Middleware | Archivo | Comportamiento |
|---|---|---|
| `verifyToken` | `server/middleware/auth.js` | Valida Bearer JWT, carga usuario/rol/empresa desde BD, valida estados, inyecta `req.user`, elimina `req.body.empresa_id`. |
| `requireAdmin` | `auth.js` | Solo `rol_nombre === 'Administrador'`. |
| `requirePermission(permiso)` | `auth.js` | Admin bypass; otros requieren `req.user.permisos.includes(permiso)`. |
| `requireEmpresa` | `auth.js` | Exige `req.user.empresa_id`. Definido, no usado en rutas analizadas. |

### 3.6 Errores de autenticación/autorización comunes

| Status | Código/condición | Mensaje/respuesta típica |
|---:|---|---|
| 401 | Sin Bearer | `No se proporcionó token de autorización` |
| 401 | Usuario no encontrado | `Usuario no encontrado o sesión inválida` |
| 401 | `TOKEN_EXPIRED` | `Tu sesión ha expirado. Por favor, inicia sesión nuevamente.` |
| 401 | Token inválido | `Token inválido o manipulado` |
| 403 | `USER_INACTIVE` | Cuenta desactivada por Administrador |
| 403 | `ROLE_INACTIVE` | Rol asignado inactivo |
| 403 | `NO_EMPRESA_ASSIGNED` | Sin empresa asignada |
| 403 | `EMPRESA_INACTIVE` | Empresa inactiva |
| 403 | `requireAdmin` | `Acceso denegado: se requieren privilegios de Administrador` |
| 403 | `requirePermission` | `Acceso denegado: se requiere el permiso '<permiso>'` |

### 3.7 Rate limiting

| Limiter | Ventana | Máx | Aplica a |
|---|---:|---:|---|
| `authLimiter` | 10 min | 30 | `POST /api/auth/login`, `/resend-2fa`, `/forgot-password`, `/reset-password` |
| `twoFactorLimiter` | 5 min | 10 | `POST /api/auth/verify-2fa` |

Respuesta al exceder límite: `429` por `express-rate-limit` con `{ success:false, message: ... }`.

## 4. Convenciones de respuesta y errores

### Éxito

Forma habitual (no 100% uniforme entre módulos):

```json
{ "success": true, "message": "Opcional", "data": ... }
```

Algunos endpoints agregan campos extra: `requires2FA`, `tempToken`, `emailMasked`, `token`, `user`, `estatus`, `usuariosAfectados`, `paginacion`, `total`, etc.

### Error

```json
{ "success": false, "message": "...", "code": "OPCIONAL", "errors": { "...": "..." } }
```

- `AppError` (inventario/ingresos/reportes/ventas/compras/categorías/productos/proveedores/clientes en buena parte): usa `status`, `message`, `errors` opcional.
- Algunos módulos (gastos/caja, partes de bitácora) lanzan `Error` plano o manejan `try/catch` propio; pueden terminar en `500 Error interno del servidor` aunque sea error de validación/no encontrado. **Ver sección 8**.

### Paginación

No hay formato único global. Casos con paginación:

| Endpoint | Query | Respuesta |
|---|---|---|
| `GET /api/ventas` | `limite` (1-100, default 50), `pagina` (>=1) | `data: { total, items }` |
| `GET /api/compras` | `limite` (1-100, default 50), `pagina` (>=1) | `data: { items, paginacion: { total, pagina, limite, total_paginas } }` |
| `GET /api/inventario/movimientos` | `limite` (1-200, default 50), `pagina` (>=1) | `data: { total, pagina, limite, items }` |
| `GET /api/bitacora` | `limit` (default 50), `offset` (default 0) | `{ success, total, data }` (offset, no página) |

## 5. Multiempresa, bitácora y reglas transversales

### Multiempresa / tenant

- El tenant principal es `empresa_id` del usuario autenticado (`req.user.empresa_id`).
- `verifyToken` elimina `req.body.empresa_id` para evitar suplantación.
- La mayoría de consultas/repos filtran por `empresa_id`. Hay excepciones/inconsistencias puntuales; ver sección 8.
- Login soporta `empresa_id` opcional para desambiguar usuario/correo en varias empresas.

### Bitácora

- Utilidad: `server/utils/bitacora.js#registrarBitacora(req, { id_usuario, nombre_usuario, accion, detalle, empresa_id })`.
- Inserta en `bitacora_accesos`.
- IP: `x-forwarded-for` (primer valor) / `socket.remoteAddress` / `req.ip`; normaliza `::1` a `127.0.0.1`.
- `empresa_id` efectivo: argumento || `req.user.empresa_id` || `1`.
- Si falla, solo loguea error; no rompe la operación principal.
- Acciones comunes observadas: `INICIO_SESION_*`, `CIERRE_SESION_*`, `CREACION_*`, `MODIFICACION_*`, `ACTIVACION_*`, `DESACTIVACION_*`, `CANCELACION_*`, `DEVOLUCION_*`, `ENTRADA_INVENTARIO`, etc.

### IDs en path

Varios validadores aceptan ID numérico o código visible:

| Módulo | Ejemplos aceptados |
|---|---|
| Ventas | `123`, `V-123`, `VEN-123` |
| Compras | `123`, `COM-123`, `C-123` |
| Clientes | `12`, `C-0012` |
| Productos | `12`, `PRD-12` |
| Categorías | `12`, `CAT-0012` |
| Proveedores | `12`, `P-0012` |
| Inventario/Ingresos | `PRD-n` / `ING-n` según endpoint |

---

## 6. Módulos y endpoints

> **Leyenda**: Auth = middleware de autenticación/autorización. “Body/Query/Params” lista campos observados; salvo indicación, los no marcados como requeridos son opcionales.

## 6.1 Auth (`/api/auth`)

| Método | Path | Auth/Rate limit | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | `authLimiter` | Paso 1: valida credenciales, genera 2FA, envía correo. |
| POST | `/api/auth/verify-2fa` | `twoFactorLimiter` | Paso 2: valida código 2FA y emite JWT de sesión. |
| POST | `/api/auth/resend-2fa` | `authLimiter` | Reenvía código 2FA. |
| POST | `/api/auth/logout` | Ninguno | Cierra sesión lógica y registra bitácora. |
| POST | `/api/auth/forgot-password` | `authLimiter` | Solicita recuperación de contraseña. |
| POST | `/api/auth/reset-password` | `authLimiter` | Restablece contraseña con token. |
| GET | `/api/auth/me` | `verifyToken` | Retorna usuario autenticado actual. |
| POST | `/api/auth/change-password` | `verifyToken` | Cambia contraseña del usuario autenticado. |

### POST `/api/auth/login`
- **Body**: `nombre_usuario` (string, requerido; busca por nombre_usuario o correo), `contrasena` (string, requerido), `empresa_id` (number, opcional, desambiguación multiempresa).
- **200**: `{ success, requires2FA:true, message, tempToken, emailMasked }`.
- **Errores relevantes**: `400` campos faltantes; `401` credenciales inválidas (genérico); `400 requiresEmpresaSelection:true, empresas:[{id_empresa,nombre}]` si hay ambigüedad; `403 USER_INACTIVE/EMPRESA_INACTIVE/ROLE_INACTIVE`.
- **Efectos**: invalida códigos 2FA previos no usados, crea código 6 dígitos (+10 min), envía email, bitácora `INICIO_SESION_PASO1_EXITOSO` o fallidos/denegados.

### POST `/api/auth/verify-2fa`
- **Body**: `tempToken` (req), `codigo` (req).
- **200**: `{ success, message, token, user }`. `user` incluye datos del usuario, rol, empresa y permisos.
- **Errores**: `400/401/403/404` por token temporal inválido, código incorrecto/usado/expirado, usuario/rol/empresa inactivos.
- **Efectos**: marca código usado, bitácora `INICIO_SESION_2FA_EXITOSO` o fallido.

### POST `/api/auth/resend-2fa`
- **Body**: `tempToken` (req). **200**: `{success,message}`. Invalida códigos anteriores, genera nuevo, envía correo, bitácora `REENVIO_CODIGO_2FA`.

### POST `/api/auth/logout`
- **Body opcional**: `motivo` (`manual`|`inactividad` u otro).
- **200**: `{success,message}`.
- **Efectos**: bitácora `CIERRE_SESION_MANUAL` o `CIERRE_SESION_INACTIVIDAD`. No invalida JWT server-side; el cliente debe borrar token.

### POST `/api/auth/forgot-password`
- **Body**: `correo_o_usuario` (req).
- **200**: respuesta anti-enumeración si no existe; si existe y activo, envía enlace/código. Incluye `devToken` solo si `NODE_ENV !== 'production'`.
- **Efectos**: token `crypto.randomBytes(32).hex`, guarda en `tokens_recuperacion` (+1h), invalida tokens previos, envía email con `/?reset_token=...`, bitácora.

### POST `/api/auth/reset-password`
- **Body**: `token` (req), `nueva_contrasena` (req, min 6).
- **200**: éxito. **400** si token inválido/expirado/usado o contraseña corta.
- **Efectos**: bcrypt hash, actualiza contraseña, marca token usado, bitácora.

### GET `/api/auth/me`
- **Auth**: `verifyToken`. **200**: `{ success:true, user: req.user }`.

### POST `/api/auth/change-password`
- **Auth**: `verifyToken`. **Body**: `contrasena_actual`, `nueva_contrasena` (min 6).
- **200**: éxito. **400** contraseña actual incorrecta/corta. **Efectos**: hash bcrypt, actualiza, bitácora éxito/fallo.

## 6.2 Usuarios (`/api/users`)

**Protección**: `router.use(verifyToken, requireAdmin)` — todos los endpoints requieren Admin.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/users` | Listar usuarios de la empresa. |
| GET | `/api/users/:id` | Obtener usuario por ID. |
| POST | `/api/users` | Crear usuario. |
| PUT | `/api/users/:id` | Actualizar usuario. |
| PATCH | `/api/users/:id/status` | Activar/desactivar usuario. |
| POST | `/api/users/:id/reset-password-admin` | Restablecer contraseña por admin. |

### GET `/api/users`
- **Query**: `search`, `id_rol`, `estatus` (`1|0`).
- **200**: `{ success:true, data:[{id_usuario,nombre,apellido_paterno,apellido_materno,nombre_completo,nombre_usuario,correo,id_rol,rol_nombre,rol_estatus,estatus,fecha_creacion}] }`.
- **Notas**: filtra por `req.user.empresa_id`; sin paginación.

### POST `/api/users`
- **Body req**: `nombre`, `apellido_paterno`, `nombre_usuario`, `contrasena` (min 6), `correo`, `id_rol`. Opcional: `apellido_materno`.
- **201**: usuario creado con `estatus=1`.
- **Validaciones**: unicidad `nombre_usuario` y `correo` por empresa; rol existente y activo.
- **Efectos**: bcrypt hash, bitácora `CREACION_USUARIO`.

### PUT `/api/users/:id`
- **Body opcional**: `nombre, apellido_paterno, apellido_materno, correo, id_rol, estatus, nueva_contrasena` (min 6).
- **200**: `{success,message}`. No modifica `id_usuario` ni `nombre_usuario`.
- **Efectos**: si cambia contraseña, hash bcrypt; bitácora `MODIFICACION_USUARIO`.

### PATCH `/api/users/:id/status`
- **Body**: `estatus` (normalizado a 1/0). **400** si se intenta desactivar a sí mismo.
- **200**: `{success,message,estatus}`. Bitácora activación/desactivación.

### POST `/api/users/:id/reset-password-admin`
- **Body**: `nueva_contrasena` (min 6). **200** éxito. **Efectos**: hash, invalida tokens recuperación pendientes, bitácora.

## 6.3 Roles (`/api/roles`)

**Protección**: `verifyToken, requireAdmin`.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/roles` | Listar roles. |
| GET | `/api/roles/:id` | Obtener rol. |
| POST | `/api/roles` | Crear rol. |
| PUT | `/api/roles/:id` | Actualizar rol. |
| PATCH | `/api/roles/:id/status` | Activar/desactivar rol. |
| PUT | `/api/roles/assign-user/:id_usuario` | Asignar rol a usuario. |

### GET `/api/roles`
- **Query**: `nombre` (ILIKE). **200**: `data[]` con `id_rol,nombre,descripcion,estatus,permisos,fecha_creacion,total_usuarios,usuarios_activos,es_predefinido`.

### POST `/api/roles`
- **Body**: `nombre` (req), `descripcion` opcional, `permisos` array opcional (default `[]`).
- **201**: rol con `estatus=1`. **409/400** por nombre duplicado según implementación.

### PUT `/api/roles/:id`
- **Body**: `nombre` req, `descripcion`, `permisos` array. No cambia `id_rol`.

### PATCH `/api/roles/:id/status`
- **Body**: `estatus`. **400** si intenta desactivar rol Admin principal (`id_rol==1`).
- **200**: incluye `usuariosAfectados`. Efecto real: usuarios con rol inactivo quedan bloqueados por `verifyToken`.

### PUT `/api/roles/assign-user/:id_usuario`
- **Body**: `id_rol` req. Valida rol existe y está activo. **200** éxito; bitácora `CAMBIO_ROL_USUARIO`.

## 6.4 Clientes (`/api/clientes`, alias `/api/clients`)

**Protección**: `verifyToken` en todas; **no** hay `requirePermission`/`requireAdmin` en rutas.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/clientes` | Listar clientes con filtros. |
| GET | `/api/clientes/stats` | Resumen de tarjetas. |
| GET | `/api/clientes/opciones` | Opciones para filtros. |
| GET | `/api/clientes/:id` | Detalle de cliente. |
| GET | `/api/clientes/:id/historial` | Ventas, cobros y resumen financiero. |
| POST | `/api/clientes` | Crear cliente. |
| PUT | `/api/clientes/:id` | Actualizar cliente. |
| PATCH | `/api/clientes/:id/status` | Activar/desactivar. |

### GET `/api/clientes`
- **Query**: `q`, `estatus` (`1|0`), `ciudad`, `estado`, `colonia`, `adeudo` (`con|sin`).
- **200** `data[]`: campos cliente + `nombre_completo`, `saldo_pendiente`.

### GET `/api/clientes/:id/historial`
- **200**: `{ ventas:[{id_venta,codigo,fecha,total,monto_pagado,estatus,productos}], cobros:[{id_cobro,fecha,monto,forma_pago,id_venta,venta_codigo}], resumen:{total_ventas,total_cobrado,saldo_pendiente} }`.

### POST `/api/clientes`
- **Body req**: `nombre_cliente`, `apellido_paterno`, `calle`, `num_exterior`, `colonia`, `codigo_postal` (5 dígitos), `ciudad`, `estado`, `telefono` (10 dígitos), `correo` (válido). Opcional: `apellido_materno`.
- **201**: cliente con `estatus=1`, `empresa_id` del usuario. Bitácora `CREACION_CLIENTE`.

### PATCH `/api/clientes/:id/status`
- **Body**: `estatus` o `activo`. **200**: `{success,message,estatus}`. Bitácora activación/desactivación.

## 6.5 Proveedores (`/api/proveedores`, alias `/api/suppliers`)

**Protección**: `verifyToken` en todas; sin `requirePermission`/`requireAdmin`.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/proveedores` | Listar proveedores. |
| GET | `/api/proveedores/stats` | Resumen. |
| GET | `/api/proveedores/:id` | Detalle. |
| POST | `/api/proveedores` | Crear. |
| PUT | `/api/proveedores/:id` | Actualizar. |
| PATCH | `/api/proveedores/:id/status` | Activar/desactivar. |

- **GET query**: `q`, `estatus`.
- **POST/PUT body req**: `nombre_empresa`, `nombre_contacto`, `calle`, `colonia`, `ciudad`, `estado`, `telefono` (10 dígitos), `correo`.
- **Status toggle**: al desactivar, si tiene compras asociadas puede devolver `409`.
- **Bitácora**: creación/modificación/estatus.

## 6.6 Categorías (`/api/categorias`)

**Protección**: `verifyToken` global; crear/editar/cambiar estatus requieren `requireAdmin`.

| Método | Path | Auth | Descripción |
|---|---|---|---|
| GET | `/api/categorias` | `verifyToken` | Listar categorías. |
| GET | `/api/categorias/stats` | `verifyToken` | Resumen. |
| GET | `/api/categorias/:id` | `verifyToken` | Detalle. |
| POST | `/api/categorias` | `verifyToken, requireAdmin` | Crear. |
| PUT | `/api/categorias/:id` | `verifyToken, requireAdmin` | Actualizar. |
| PATCH | `/api/categorias/:id/status` | `verifyToken, requireAdmin` | Activar/desactivar. |

- **GET query**: `q/search`, `estatus`. **200**: `data[]` con `id_categoria,nombre,descripcion,estatus,asociados`.
- **POST body**: `nombre` req (<=80), `descripcion` opcional. Nombre único case-insensitive por empresa.
- **PATCH status**: si desactiva y tiene productos asociados, `409` con `errors.code='CATEGORIA_EN_USO', asociados`.

## 6.7 Productos (`/api/productos`)

**Protección**: `verifyToken` global; escrituras `requireAdmin`.

| Método | Path | Auth | Descripción |
|---|---|---|---|
| GET | `/api/productos` | `verifyToken` | Listar productos/servicios. |
| GET | `/api/productos/stats` | `verifyToken` | Estadísticas. |
| GET | `/api/productos/categorias` | `verifyToken` | Categorías activas. |
| GET | `/api/productos/:id` | `verifyToken` | Detalle. |
| GET | `/api/productos/:id/operaciones` | `verifyToken` | Operaciones relacionadas. |
| POST | `/api/productos` | `verifyToken, requireAdmin` | Crear. |
| PUT | `/api/productos/:id` | `verifyToken, requireAdmin` | Actualizar. |
| PATCH | `/api/productos/:id/status` | `verifyToken, requireAdmin` | Activar/desactivar. |
| DELETE | `/api/productos/:id` | `verifyToken, requireAdmin` | Eliminar si no tiene operaciones. |

### GET `/api/productos`
- **Query**: `q`, `estatus` (`0|1`), `id_categoria`, `tipo` (`producto|servicio`).
- **200** `data[]`: `id_producto,codigo,nombre,descripcion,tipo,id_categoria,nombre_categoria,unidad_medida,precio_compra,precio_venta,stock,stock_minimo,estatus`.

### POST/PUT body
- **Req**: `codigo`, `nombre`, `tipo` (`producto|servicio`), `unidad_medida`, `id_categoria`, `precio_compra>=0`, `precio_venta>=0`.
- **Producto físico**: `stock` y `stock_minimo` enteros >=0 requeridos. **Servicio**: stock/stock_minimo se fuerzan a null.
- **Reglas**: código único por empresa (`409`); categoría debe existir y estar activa.

### DELETE `/api/productos/:id`
- Si tiene operaciones por FK: `409` con `errors.code='PRODUCTO_CON_OPERACIONES'`.

## 6.8 Inventario (`/api/inventario`)

**Protección**: `verifyToken` global. Movimientos manuales requieren `requireAdminOEncargado` local (rol Administrador o Encargado).

| Método | Path | Auth | Descripción |
|---|---|---|---|
| GET | `/api/inventario` | `verifyToken` | Listar existencias. |
| GET | `/api/inventario/stats` | `verifyToken` | Estadísticas. |
| GET | `/api/inventario/categorias` | `verifyToken` | Categorías activas. |
| GET | `/api/inventario/stock-bajo` | `verifyToken` | Productos bajo mínimo/sin stock. |
| GET | `/api/inventario/movimientos` | `verifyToken` | Historial paginado. |
| GET | `/api/inventario/:id` | `verifyToken` | Detalle + últimos 20 movimientos. |
| POST | `/api/inventario/entradas` | `verifyToken, Admin/Encargado` | Entrada manual. |
| POST | `/api/inventario/salidas` | `verifyToken, Admin/Encargado` | Salida manual. |
| POST | `/api/inventario/ajustes` | `verifyToken, Admin/Encargado` | Ajuste por conteo físico. |

### GET `/api/inventario`
- **Query**: `q`, `estatus` (`0|1`), `id_categoria`, `estado` (`disponible|bajo|sin_stock`).
- **200**: existencias con `valor_inventario` y `estado_stock`.

### GET `/api/inventario/movimientos`
- **Query**: `q`, `id_producto`, `tipo_movimiento` (`entrada|salida|ajuste`), `origen`, `desde`, `hasta` (`YYYY-MM-DD`), `limite` (1-200), `pagina`.
- **200**: `{ total, pagina, limite, items:[...] }`.

### POST entradas/salidas
- **Body**: `id_producto` (int o `PRD-n`), `cantidad` (entero >0, <=999999999), `motivo` (req, <=255).
- **Salida**: `409 STOCK_INSUFICIENTE` con `errors.code` y `disponible`.
- **Efectos**: transacción, bloqueo producto, actualiza stock, inserta movimiento inventario (`origen='manual'`), bitácora.

### POST ajustes
- **Body**: `id_producto`, `existencia_real` (entero >=0), `motivo`.
- **Regla**: si no hay diferencia, `400`. `origen='ajuste'`. Bitácora `AJUSTE_INVENTARIO`.

## 6.9 Ingresos (`/api/ingresos`)

**Protección**: `verifyToken` + `requireAdminOEncargado` a nivel router. Denegado intenta bitácora `ACCESO_DENEGADO`.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/ingresos` | Listar ingresos. |
| GET | `/api/ingresos/stats` | Estadísticas. |
| GET | `/api/ingresos/categorias` | Categorías (incluye inactivas). |
| GET | `/api/ingresos/:id` | Detalle con venta/caja relacionada. |
| POST | `/api/ingresos` | Crear ingreso manual. |
| PUT | `/api/ingresos/:id` | Modificar ingreso manual activo. |
| PATCH | `/api/ingresos/:id/cancelar` | Cancelar ingreso manual. |

### GET `/api/ingresos`
- **Query**: `q`, `desde`, `hasta` (`YYYY-MM-DD`), `id_categoria`, `origen` (`venta|cobro|otro`), `forma_pago` (`efectivo|tarjeta|transfer`), `estatus` (`activo|cancelado`).

### POST `/api/ingresos`
- **Body**: `concepto` (req, <=255), `id_categoria` (req), `monto` (>0, máx 2 decimales), `forma_pago` (`efectivo|tarjeta|transfer`), `fecha` opcional (`YYYY-MM-DD`/ISO, no futura), `origen` opcional (si se envía, solo `otro`).
- **Efectos**: inserta ingreso activo `origen='otro'`; si efectivo, crea movimiento caja entrada `origen='ingreso'`; bitácora `CREACION_INGRESO`.

### PUT `/api/ingresos/:id`
- Solo ingresos manuales activos (`origen='otro', estatus='activo'`). Si cambia monto/forma de pago, revierte/recrea efecto en caja.
- Bitácora `MODIFICACION_INGRESO`.

### PATCH `/api/ingresos/:id/cancelar`
- **Body**: `motivo` opcional. Solo manuales; cancela y revierte caja si efectivo. Bitácora `CANCELACION_INGRESO`.

## 6.10 Gastos (`/api/gastos`)

**Protección**: `verifyToken, requirePermission('gastos')`.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/gastos/resumen` | Resumen de gastos. |
| GET | `/api/gastos/categorias` | Categorías activas. |
| GET | `/api/gastos` | Listar gastos. |
| GET | `/api/gastos/:id` | Detalle. |
| POST | `/api/gastos` | Crear gasto. |
| PUT | `/api/gastos/:id` | Actualizar gasto activo. |
| PATCH | `/api/gastos/:id/cancelar` | Cancelar gasto activo. |

### GET `/api/gastos`
- **Query**: `fecha_inicio`, `fecha_fin`, `id_categoria`, `concepto`.
- **200** `data[]`: `id_gasto,fecha,concepto,id_categoria,categoria,monto,forma_pago,observaciones,id_usuario,estatus,nombre_usuario,apellido_paterno`.

### POST/PUT body
- `concepto` req (<=255), `id_categoria` entero, `monto` >0, `forma_pago` (`efectivo|tarjeta|transfer`), `fecha` opcional parseable, `observaciones` opcional.
- **POST efectos**: crea gasto; si efectivo, crea movimiento caja salida `origen='gasto'`.
- **PUT efectos**: solo activo; si era efectivo revierte caja, si nuevo es efectivo crea salida. Transacción.
- **PATCH cancelar**: marca cancelado; si efectivo revierte caja con entrada.
- **Nota**: sin bitácora en controlador; validaciones/no encontrado pueden llegar como 500 por `Error` plano.

## 6.11 Caja (`/api/caja`)

**Protección**: `verifyToken, requirePermission('caja')`.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/caja/resumen` | Entradas, salidas, saldo. |
| GET | `/api/caja` | Listar movimientos. |
| GET | `/api/caja/:id` | Detalle movimiento. |
| POST | `/api/caja` | Registrar movimiento manual. |

### GET `/api/caja/resumen`
- **Query**: `fecha_inicio`, `fecha_fin`. **200**: `{ total_entradas, total_salidas, saldo }`.

### GET `/api/caja`
- **Query**: `fecha_inicio`, `fecha_fin`, `tipo` (`entrada|salida`), `concepto`.
- **200**: movimientos con `id_movimiento,fecha,tipo,concepto,monto,origen,id_referencia,id_usuario,nombre_usuario,apellido_paterno`.

### POST `/api/caja`
- **Body**: `concepto` req (<=255), `tipo` (`entrada|salida`), `monto` >0, `fecha` opcional.
- **Efecto**: inserta movimiento manual (`origen='manual'`, `id_referencia=NULL`).
- **Nota**: sin bitácora; validaciones pueden devolver 500 por `Error` plano.

## 6.12 Cuentas por Cobrar (CxC) (`/api/cxc`)

**Protección**: `verifyToken, requirePermission('cxc')`.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/cxc/resumen` | Indicadores CxC. |
| GET | `/api/cxc` | Listar cuentas. |
| GET | `/api/cxc/:id` | Detalle cuenta. |
| GET | `/api/cxc/:id/pagos` | Historial cobros. |
| POST | `/api/cxc/:id/pagos` | Registrar cobro. |

### GET `/api/cxc`
- **Query**: `estatus` (`todas|vencida|pendiente|parcial|saldada` u otros), `busqueda` (cliente, código venta, id_cxc).
- **200**: cuentas con `estado_calculado` (`Cancelada|Saldada|Vencida|Parcial|Pendiente`).

### POST `/api/cxc/:id/pagos`
- **Body**: `monto` (>0, máx 2 decimales), `forma_pago` (`efectivo|tarjeta|transfer`), `observaciones` opcional.
- **201**: `{ cobro, id_ingreso, saldo_pendiente, estatus }`.
- **Efectos transaccionales**: bloquea CxC; inserta `cxc_cobros`; actualiza saldo/estatus CxC; actualiza `ventas.monto_pagado` y estatus (`Pagada/Parcial`); registra ingreso automático `origen='cobro'` (si efectivo, mueve caja).
- **Errores**: 400 monto/forma inválida o excede saldo; 404 cuenta; 409 cancelada/saldada/pagada/sin saldo.

## 6.13 Cuentas por Pagar (CxP) (`/api/cxp`)

**Protección**: `verifyToken, requirePermission('cxp')`.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/cxp/resumen` | Indicadores CxP. |
| GET | `/api/cxp` | Listar cuentas. |
| GET | `/api/cxp/:id` | Detalle cuenta. |
| GET | `/api/cxp/:id/pagos` | Historial pagos. |
| POST | `/api/cxp/:id/pagos` | Registrar pago. |

### GET `/api/cxp`
- **Query**: `estatus`, `busqueda` (proveedor, código compra, id_cxp).
- **200**: cuentas con `estado_calculado`, `proveedor`, `codigo_compra`.

### POST `/api/cxp/:id/pagos`
- **Body**: `monto` (>0, máx 2 decimales), `forma_pago` (`efectivo|tarjeta|transfer`), `fecha` opcional, `observaciones` opcional.
- **201**: `{ pago, cuenta, gasto, movimiento_caja }`.
- **Efectos transaccionales**: bloquea CxP; inserta `cxp_pagos`; actualiza saldo/estatus CxP; si tiene compra, actualiza `compras.estatus`; crea gasto activo por pago; si efectivo, crea salida de caja `origen='cxp'`.
- **Importante**: cada pago CxP genera gasto (impacto contable/reporting).

## 6.14 Ventas (`/api/ventas`)

**Protección**: `verifyToken, requirePermission('ventas')`.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/ventas/stats` | Estadísticas globales. |
| GET | `/api/ventas/siguiente-folio` | Siguiente folio. |
| GET | `/api/ventas` | Listado paginado. |
| GET | `/api/ventas/:id` | Detalle con ítems/devoluciones. |
| POST | `/api/ventas` | Crear venta. |
| POST/PATCH | `/api/ventas/:id/cancelar` | Cancelar venta. |
| POST | `/api/ventas/:id/devoluciones` | Registrar devolución. |

### GET `/api/ventas`
- **Query**: `q/search`, `estatus` (`pendiente_pago|pago_parcial|pagada|cancelada`, alias aceptados), `condiciones_pago` (`contado|credito`), `forma_pago` (`efectivo|tarjeta`), `desde/fecha_inicio`, `hasta/fecha_fin` (`YYYY-MM-DD`), `id_cliente`, `id_usuario`, `limite`, `pagina`.
- **200**: `{ data:{ total, items:[...] } }`.

### POST `/api/ventas`
- **Body**: `id_cliente` (req en crédito; opcional contado pero saldo>0 exige cliente), `forma_pago` (`efectivo|tarjeta`), `condiciones_pago` (`contado|credito`), `fecha_vencimiento` (crédito, `YYYY-MM-DD`, no pasada), `monto_pagado`, `observaciones`, `items` o `detalles`: `[{id_producto,cantidad}]`.
- **Validaciones**: items>=1; precio cliente se ignora (usa catálogo); consolida renglones repetidos; valida stock de productos físicos.
- **Efectos**: transacción; descuenta stock y crea movimientos inventario; crea venta/detalles; crea CxC si hay saldo; crea ingreso automático si hay monto pagado (y caja si efectivo); bitácora `CREACION_VENTA`.
- **201**: venta completa con items/devoluciones.

### POST/PATCH `/api/ventas/:id/cancelar`
- **Body**: `motivo` opcional.
- **Reglas**: no cancelar si ya cancelada, si tiene devoluciones, o si CxC tiene cobros.
- **Efectos**: revierte inventario, cancela CxC, cancela ingresos por origen, marca venta cancelada, bitácora.

### POST `/api/ventas/:id/devoluciones`
- **Body**: `tipo` (`parcial|total`, default parcial), `observaciones/motivo`, `items` (parcial req): `[{id_producto,cantidad}]`.
- **Efectos**: reingresa stock; calcula neto/reembolso; ajusta venta y CxC; intenta registrar reembolso de ingreso.
- **Riesgo actual**: si hay reembolso, el servicio espera `ingresoService.registrarReembolsoVenta`; si no existe, puede responder `501` (ver sección 8).

## 6.15 Compras (`/api/compras`)

**Protección**: `verifyToken, requirePermission('compras')`.

| Método | Path | Descripción |
|---|---|---|
| GET | `/api/compras/stats` | Estadísticas. |
| GET | `/api/compras/siguiente-folio` | Siguiente folio COM. |
| GET | `/api/compras` | Listado paginado. |
| GET | `/api/compras/:id` | Detalle. |
| POST | `/api/compras` | Crear compra. |
| POST | `/api/compras/:id/cancelar` | Cancelar compra. |
| POST | `/api/compras/:id/devoluciones` | Registrar devolución. |
| GET | `/api/compras/:id/devoluciones` | Listar devoluciones. |

### GET `/api/compras`
- **Query**: `q/search`, `estatus` (`pagada|parcial|pendiente_pago|cancelada`), `condiciones_pago` (`contado|credito`), `desde/fecha_inicio`, `hasta/fecha_fin`, `id_proveedor`, `limite`, `pagina`.
- **200**: `{ data:{ items, paginacion } }`.

### POST `/api/compras`
- **Body**: `id_proveedor` req, `condiciones_pago` (`contado|credito`), `forma_pago` (`efectivo|tarjeta|transfer`), `fecha_vencimiento` crédito, `monto_pagado`, `observaciones`, `items/detalles`: `[{id_producto,cantidad,costo_unitario|precio_unitario}]`.
- **Efectos**: transacción; valida proveedor/productos; incrementa stock de productos físicos, actualiza `precio_compra`, crea movimiento inventario entrada; crea compra/detalles; crea CxP si hay saldo; crea gasto si hay monto pagado y movimiento caja si efectivo.
- **201**: `{id_compra,codigo,cxp_creada,id_cxp,saldo_pendiente,id_gasto}`.

### POST `/api/compras/:id/cancelar`
- **Body**: `motivo` opcional.
- **Reglas**: no si cancelada, tiene devoluciones, o CxP tiene pagos.
- **Efectos**: revierte stock, cancela CxP, revierte caja/gasto asociado, marca compra cancelada, bitácora.

### POST `/api/compras/:id/devoluciones`
- **Body**: `resolucion` (`cxp|ingreso`, default `cxp`), `observaciones/motivo`, `items`: `[{id_producto,cantidad}]`.
- **Efectos**: valida disponible/stock; descuenta inventario; crea devolución; ajusta CxP o genera ingreso/caja por reembolso/excedente; actualiza total/estatus compra; bitácora.

## 6.16 Reportes (`/api/reportes`)

**Protección**: `verifyToken, requirePermission('reportes')`.

| Método | Path | Query | Descripción |
|---|---|---|---|
| GET | `/api/reportes/resumen` | `fecha_inicio,fecha_fin` | Resumen consolidado. |
| GET | `/api/reportes/ventas/detalle` | `fecha_inicio,fecha_fin` | Detalle ventas. |
| GET | `/api/reportes/compras/detalle` | `fecha_inicio,fecha_fin` | Detalle compras. |
| GET | `/api/reportes/ingresos/detalle` | `fecha_inicio,fecha_fin` | Detalle ingresos activos. |
| GET | `/api/reportes/gastos/detalle` | `fecha_inicio,fecha_fin` | Detalle gastos activos. |
| GET | `/api/reportes/inventario/detalle` | - | Detalle inventario actual. |
| GET | `/api/reportes/cxc/detalle` | - | Detalle CxC actual. |
| GET | `/api/reportes/cxp/detalle` | - | Detalle CxP actual. |

**Reglas comunes**:
- Fechas `YYYY-MM-DD`, válidas; `fecha_inicio <= fecha_fin`.
- Filtro por `empresa_id`.
- CxC/CxP/Inventario en resumen/detalle son estado actual (sin filtro de fechas).
- Ingresos/gastos detalle fuerzan estatus activo.

## 6.17 Dashboard (`/api/dashboard`)

| Método | Path | Auth | Descripción |
|---|---|---|---|
| GET | `/api/dashboard` | `verifyToken` | Resumen operativo para dashboard. |

- **Respuesta**: `metricas`, `grafica_semestral` (6 meses), `inventario`, `ventas_hoy` (incluye últimos 7 días), `cuentas_por_cobrar`, `cuentas_por_pagar`, `alertas_inventario`.
- **Notas**: no recibe filtros de periodo; usa fechas actuales de BD. No usa `requirePermission`.

## 6.18 Notificaciones (`/api/notificaciones`)

| Método | Path | Auth | Descripción |
|---|---|---|---|
| GET | `/api/notificaciones` | `verifyToken` | Notificaciones calculadas on-the-fly. |

- **200**: `{ data:{ total, resumen:{inventario,cxc,cxp,alta,media}, notificaciones:[...] } }`.
- **Lógica**: genera alertas por permisos del usuario: inventario (stock bajo/sin existencias), CxC/CxP vencidas o próximas a vencer (`DIAS_ANTICIPACION=7`).
- **Sin persistencia de lectura**: no hay endpoint para marcar leída/ocultar.

## 6.19 Bitácora (`/api/bitacora`)

| Método | Path | Auth | Descripción |
|---|---|---|---|
| GET | `/api/bitacora` | `verifyToken, requireAdmin` | Consulta bitácora de accesos/acciones. |

- **Query**: `accion` (ILIKE), `nombre_usuario` (ILIKE), `limit` (default 50), `offset` (default 0).
- **200**: `{ success:true, total, data:[{id_bitacora,id_usuario,nombre_usuario,accion,ip_origen,fecha,detalle}] }`.
- **Observación**: `total` se calcula solo por `empresa_id`, sin aplicar filtros `accion/nombre_usuario`; puede desalinear paginación.

---

## 7. Consumo desde el frontend actual

Esta sección describe el contrato **real usado por el frontend** (`src/services/api.js`), útil para mantener compatibilidad.

### Base URL y transporte

- `API_BASE = '/api'` fijo en `src/services/api.js`.
- Vite dev proxy: `/api -> http://localhost:5000` (`vite.config.js`).
- No hay uso de variables `VITE_*` para la URL.
- Cliente propio con `fetch`, sin axios/interceptores.

### Token y sesión

- Token: `localStorage['contapyme_token']`.
- Usuario: `localStorage['contapyme_user']`.
- Header: `Authorization: Bearer <token>`.
- En `401`, o `403` con `code` en `USER_INACTIVE|EMPRESA_INACTIVE|NO_EMPRESA_ASSIGNED`, el cliente limpia sesión y emite `window` event `contapyme_auth_expired` (excepto en login/verify-2fa).
- `logout` hace `POST /auth/logout` y siempre limpia sesión local en `finally`.

### Ejemplo de consumo (JS)

```js
const API_BASE = '/api';
const token = localStorage.getItem('contapyme_token');

async function api(endpoint, options = {}) {
  const res = await fetch(`${API_BASE}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
  });
  const data = await res.json().catch(() => ({ success:false, message:'Respuesta no válida del servidor' }));
  if (!res.ok) throw new Error(data.message || 'Error en la petición');
  return data;
}

// Ejemplo: crear venta
await api('/ventas', {
  method: 'POST',
  body: JSON.stringify({
    id_cliente: 10,
    forma_pago: 'efectivo',
    condiciones_pago: 'contado',
    items: [{ id_producto: 5, cantidad: 2 }]
  })
});
```

### Normalizaciones defensivas en frontend

El frontend tolera varias formas de respuesta (`data`, `data.items`, `data.pagos`, array directo, alias de campos). Al consumir la API desde un nuevo cliente, preferir el contrato documentado por backend y validar `success`, `data` y códigos HTTP.

## 8. Inconsistencias y riesgos detectados

Estas observaciones son relevantes para quien consuma o extienda la API:

1. **Contrato de error no uniforme**. Gastos/Caja y partes de bitácora lanzan `Error` plano; `errorHandler` puede devolver `500` para validaciones/no encontrado en lugar de `400/404`.
2. **Aislamiento multiempresa inconsistente en algunos repos**. Se observaron consultas por ID sin filtro `empresa_id` en módulos como cliente/proveedor/categoría, inventario `findExistencia`, gastos/caja por ID, CxC/CxP detalle/pagos. Revisar antes de exponer a múltiples empresas.
3. **Autorización desigual**. Ventas/Compras/Reportes/Gastos/Caja/CxC/CxP/Ingresos/Inventario movimientos usan permisos/roles; Clientes/Proveedores solo `verifyToken`; Dashboard y Notificaciones solo `verifyToken` (notificaciones filtran por permisos internamente).
4. **Devolución de venta con reembolso**. `ventaService.devolver` puede requerir `ingresoService.registrarReembolsoVenta`; si falta implementación, devoluciones con reembolso pueden fallar con `501`.
5. **CxP pago crea gasto**. Cada pago a CxP genera gasto activo y, si efectivo, salida de caja con origen `cxp`. Evitar duplicar registros al integrar.
6. **CxC cobro crea ingreso automático**. El cobro usa `ingresoService.registrarAutomatico` (`origen='cobro'`), que también puede mover caja.
7. **Bitácora `total` sin filtros**. `GET /api/bitacora` calcula `total` solo por empresa; la lista sí aplica `accion/nombre_usuario`.
8. **Paginación/shape heterogéneo**. Ventas: `{total,items}`; Compras: `{items,paginacion}`; Inventario movimientos: `{total,pagina,limite,items}`; Bitácora: `{total,data}` con offset.
9. **Formas de pago**. Ventas acepta `efectivo|tarjeta`; compras/gastos/ingresos/caja/cxc/cxp aceptan `transfer` en varios puntos. Confirmar por módulo antes de enviar.
10. **Docs embebidas en `/api` parciales**. `GET /api` no incluye todos los endpoints actuales (ventas, compras, reportes, dashboard, caja, cxc, notificaciones parcialmente ausentes o incompletos).
11. **Frontend tolera alias**. El frontend normaliza muchos alias de campos por compatibilidad histórica; para integraciones nuevas, usar nombres canónicos del backend y validar respuestas.

---

## Apéndice A — Resumen rápido de autenticación para integradores

1. `POST /api/auth/login` → recibir `tempToken`.
2. `POST /api/auth/verify-2fa` → recibir `token`.
3. Enviar `Authorization: Bearer <token>` en cada request protegida.
4. Manejar `401 TOKEN_EXPIRED` y códigos `403 USER_INACTIVE|ROLE_INACTIVE|EMPRESA_INACTIVE|NO_EMPRESA_ASSIGNED` forzando reautenticación.
5. Para cerrar sesión, llamar `POST /api/auth/logout` y descartar token localmente.

## Apéndice B — Checklist para consumidor nuevo

- Confirmar `PORT` real y proxy/base URL.
- Confirmar que el usuario tiene rol/permisos adecuados para el módulo.
- No enviar `empresa_id` en body: el backend lo ignora/sobrescribe por seguridad.
- Usar formatos de fecha `YYYY-MM-DD` cuando el endpoint lo indique.
- Manejar `success`, `message`, `data`, `errors`, `code`.
- Considerar efectos secundarios contables/inventario/caja antes de automatizar escrituras.
