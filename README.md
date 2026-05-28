# SGRC — Backend (Spring Boot)

Sistema de Gestión de Recursos del Centro de Cómputo — UACH.  
Cubre reserva de cubículos, renta de equipo y notificaciones en tiempo real.

---

## Requisitos previos

| Herramienta | Versión mínima | Notas |
|-------------|---------------|-------|
| Java (JDK) | 21 | [Descargar](https://adoptium.net/) |
| Docker Desktop | Cualquier versión reciente | [Descargar](https://www.docker.com/products/docker-desktop/) |
| Maven | — | Incluido en el repo (`./mvnw`), no hace falta instalar |

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

## Scripts disponibles

| Script | Qué hace |
|--------|---------|
| `./restart.sh` | Reinicia Docker **y** Spring Boot (limpia la BD cada vez) |
| `./start.sh` | Levanta Docker y Spring Boot sin borrar datos existentes |

---

## Acceder a la aplicación

Abre **`https://localhost:3000`** en el navegador.  
El certificado es autofirmado, el browser pedirá aceptarlo una vez — haz clic en *"Continuar de todas formas"*.

---

## Usuarios de prueba

Todos usan la contraseña `password123`.

| Matrícula | Rol | Notas |
|-----------|-----|-------|
| `ADM001` | ADMIN | Panel de administración completo |
| `367886` | STUDENT | Alumno activo sin restricciones |
| `374357` | STUDENT | Alumno activo |
| `EMP001` | TEACHER | Docente (tiempo extendido, sin sanciones automáticas) |
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

## Tecnologías principales

- **Spring Boot 4** — framework base
- **Spring Security + JWT** — autenticación stateless
- **Spring Data JPA + Hibernate** — ORM con MySQL
- **Apache Camel 4** — 2 rutas de integración (equipment + reservation)
- **Spring AMQP + RabbitMQ** — mensajería asíncrona
- **Spring WebSocket (STOMP)** — notificaciones en tiempo real
- **Docker Compose** — MySQL 8 + RabbitMQ 3

---