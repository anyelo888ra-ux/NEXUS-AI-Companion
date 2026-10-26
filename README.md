# NEXUS-AI-Companion

Primer prototipo **Fabric cliente** que conecta el chat de Minecraft con una cuenta existente de NEXUS.AI. La IA se ejecuta en el backend de NEXUS.AI; la clave de Groq nunca se incluye en el mod.

## Estado

- [x] Proyecto inicial Gradle/Fabric para Minecraft Java 1.21.1.
- [x] Comandos cliente para login, registro, chat y cierre de sesión.
- [x] Llamadas a `/api/auth` y `/api/chat` del backend existente.
- [ ] Compilación y prueba dentro de Minecraft real.
- [ ] Pantalla de login segura (el comando actual puede quedar en logs).
- [ ] Consulta del saldo exacto y renovación automática de sesión.
- [ ] Entidad visible que siga al jugador.
- [ ] Adaptadores Forge y NeoForge.

**Compatibilidad:** empezamos con Minecraft 1.21.1 + Fabric para tener una base concreta. No afirmamos compatibilidad con 1.21.11 ni con Forge/NeoForge hasta crear y probar esos módulos por separado.

## Requisitos

- JDK 21
- Gradle instalado
- Minecraft Java 1.21.1, Fabric Loader y Fabric API

## Compilar

Desde la raíz del repo: `gradle build`. El JAR debería aparecer en `build/libs/`. Este repositorio aún no incluye Gradle Wrapper y el build no se ha verificado en una máquina de compilación; no consideres el JAR listo hasta ejecutar y corregir el build.

## Comandos

- `/nexus register correo@example.com contraseña`
- `/nexus login correo@example.com contraseña`
- `/nexus chat hola, ¿quién eres?`
- `/nexus credits`
- `/nexus logout`

**Seguridad:** el prototipo guarda tokens localmente en `nexus-companion-session.json`, no la contraseña. Los comandos con contraseña pueden aparecer en el historial/logs: úsalo solo en tu propio equipo y no compartas logs ni el archivo de sesión. Antes de distribuirlo, reemplazaremos el comando por una pantalla de login. Los créditos los controla el backend compartido; no se crea una cartera independiente ni se promete un bono de registro que el backend no haya configurado.
