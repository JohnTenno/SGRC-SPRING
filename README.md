# SGRC — Backend (Spring Boot)

Sistema de Gestión de Recursos del Centro de Cómputo — UACH.  
Cubre reserva de cubículos, renta de equipo y notificaciones en tiempo real.

---

## Requisitos previos

| Herramienta    | Versión mínima             | Notas                                                        |
| -------------- | -------------------------- | ------------------------------------------------------------ |
| Java (JDK)     | 21                         | [Descargar](https://adoptium.net/)                           |
| Docker Desktop | Cualquier versión reciente | [Descargar](https://www.docker.com/products/docker-desktop/) |
| Maven          | —                          | Incluido en el repo (`./mvnw`), no hace falta instalar       |

> **El frontend ya viene compilado** dentro de `src/main/resources/static/` y Spring Boot lo sirve automáticamente. No necesitas Node.js para correr el proyecto.

---

## Clonar y correr

```bash
git clone <url-del-repositorio>
cd SGRC-SPRING

# Primera vez o cuando quieras reiniciar todo desde cero
./restart.sh
```

Eso es todo. El script hace en orden:

1. Baja y elimina los contenedores Docker existentes (con sus volúmenes)
2. Levanta MySQL y RabbitMQ con Docker y espera a que estén listos
3. Aplica el `init.sql` que crea las tablas y carga los datos de demo
4. Compila y arranca Spring Boot en `https://localhost:3000`

> La primera ejecución puede tardar ~2–3 min porque Maven descarga las dependencias.

---

**Importante — usar HTTPS obligatoriamente**

El servidor **solo acepta HTTPS**, incluso en desarrollo local. Esto es intencional: iOS no permite acceso a la cámara desde contextos no seguros (`http://`), y el módulo de check-in por QR requiere la cámara del dispositivo.

- Correcto: `https://localhost:3000`
- Incorrecto: `http://localhost:3000` (el servidor no responderá)

El certificado es autofirmado (`keystore.p12`). En iOS, además de aceptarlo en Safari, puede ser necesario instalarlo como perfil de confianza en **Ajustes → General → VPN y gestión del dispositivo**.

---

## Scripts disponibles

| Script         | Qué hace                                                  |
| -------------- | --------------------------------------------------------- |
| `./restart.sh` | Reinicia Docker **y** Spring Boot (limpia la BD cada vez) |
| `./start.sh`   | Levanta Docker y Spring Boot sin borrar datos existentes  |

---

## Acceder a la aplicación

Abre **`https://localhost:3000`** en el navegador.  
El certificado es autofirmado, el browser pedirá aceptarlo una vez — haz clic en _"Continuar de todas formas"_.

## Usuarios de prueba

Todos usan la contraseña `password123`.

| Matrícula | Rol     | Notas                                                 |
| --------- | ------- | ----------------------------------------------------- |
| `ADM001`  | ADMIN   | Panel de administración completo                      |
| `367886`  | STUDENT | Alumno activo sin restricciones                       |
| `374357`  | STUDENT | Alumno activo                                         |
| `EMP001`  | TEACHER | Docente (tiempo extendido, sin sanciones automáticas) |

---

## Estructura del proyecto

```
SGRC-SPRING/
├── src/main/java/com/app/
│   └── modules/
│       ├── auth/          JWT, login, seguridad
│       ├── cubicle/       Gestión de cubículos
│       ├── reservation/   Reservaciones (ciclo de vida completo)
│       ├── equipment/     Renta de equipo
│       ├── checkin/       Check-in por QR
│       ├── notification/  Notificaciones push (WebSocket)
│       ├── messaging/     Rutas Apache Camel + RabbitMQ
│       ├── scheduler/     Jobs automáticos (no-show, expiración)
│       ├── penalty/       Sanciones
│       ├── user/          Usuarios
│       └── spa/           Controlador fallback para React Router
├── src/main/resources/
│   ├── application.properties
│   ├── keystore.p12       Certificado SSL autofirmado
│   └── static/            Frontend React compilado (generado por pnpm build)
├── Db/
│   ├── docker-compose.yaml
│   └── init.sql           Esquema + datos demo
├── restart.sh
└── start.sh
```

---

## Dónde vive el frontend

El frontend (React + Vite) está en el repositorio hermano **`SGRC-REACT`**.  
Al correr `pnpm build` desde ese proyecto, Vite compila y deposita los archivos directamente en `src/main/resources/static/` de este proyecto.  
Spring Boot los sirve como recursos estáticos desde la raíz `/`.

Si necesitas modificar el frontend:

```bash
cd ../SGRC-REACT
pnpm install
pnpm build        # actualiza src/main/resources/static/
```

Luego reinicia Spring Boot (sin necesidad de volver a correr restart.sh si solo cambió el frontend):

```bash
# En la carpeta SGRC-SPRING
pkill -f SGRCApplication   # mata el proceso actual
./mvnw spring-boot:run     # vuelve a arrancar
```

---

## Apache Camel — Rutas implementadas

### `EquipmentCamelRoute`

Procesa el ciclo de vida de solicitudes de renta de equipo.

| Endpoint interno           | Disparado cuando                  | Acción                                                            |
| -------------------------- | --------------------------------- | ----------------------------------------------------------------- |
| `direct:equipment.created` | Se crea una solicitud de renta    | Notifica al admin en tiempo real vía WebSocket                    |
| `direct:equipment.updated` | Cambia el estado de una solicitud | Guarda notificación en BD y envía mensaje al alumno vía WebSocket |

Estados manejados: `READY_FOR_PICKUP`, `AWAITING_RETURN`, `COMPLETED`, `CANCELLED`.

### `ReservationCamelRoute`

Procesa el ciclo de vida de reservaciones de cubículos.

| Endpoint interno             | Disparado cuando                    | Acción                                                                   |
| ---------------------------- | ----------------------------------- | ------------------------------------------------------------------------ |
| `direct:reservation.created` | Se crea una reservación             | Guarda notificación en BD y envía confirmación al usuario vía WebSocket  |
| `direct:reservation.updated` | Cambia el estado de una reservación | Guarda notificación en BD y envía actualización al usuario vía WebSocket |

Estados manejados: `APPROVED`, `ACTIVE`, `COMPLETED`, `CANCELLED`, `NO_SHOW`.

---

## RabbitMQ — Exchanges y colas

| Exchange               | Cola                        | Routing Key                 | Uso                                 |
| ---------------------- | --------------------------- | --------------------------- | ----------------------------------- |
| `reservation.exchange` | `reservation.created`       | `reservation.created`       | Evento de nueva reservación         |
| `reservation.exchange` | `reservation.updated`       | `reservation.updated`       | Evento de cambio de estado          |
| `equipment.exchange`   | `equipment.request.created` | `equipment.request.created` | Evento de nueva solicitud de equipo |
| `equipment.exchange`   | `equipment.request.updated` | `equipment.request.updated` | Evento de cambio de estado          |

Todas las colas son **durables** (sobreviven reinicios de RabbitMQ).

---

## Tecnologías principales

- **Spring Boot 4** — framework base
- **Spring Security + JWT** — autenticación stateless
- **Spring Data JPA + Hibernate** — ORM con MySQL
- **Apache Camel 4** — 2 rutas de integración (equipment + reservation)
- **Spring AMQP + RabbitMQ** — mensajería asíncrona
- **Spring WebSocket (STOMP)** — notificaciones en tiempo real
- **Docker Compose** — MySQL 8 + RabbitMQ 3

---
