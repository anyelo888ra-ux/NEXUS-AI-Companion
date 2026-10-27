# NEXUS-AI-Companion

Mod cliente **Fabric para Minecraft Java**, conectado a una cuenta existente de NEXUS.AI. El backend de NEXUS.AI procesa el chat; la clave de Groq no debe distribuirse dentro del mod.

> **Estado honesto:** el compañero actual es un prototipo visual basado en un ArmorStand local. Esta rama de desarrollo está enfocada exclusivamente en **Singleplayer**. No afirmamos todavía que tenga una entidad autónoma real, inventario propio persistente ni recolección fiable de objetos.

## Estado actual

- [x] Comando de login con navegador y ticket temporal (requiere configurar el backend de tickets).
- [x] Chat y cierre de sesión locales.
- [x] Conexión existente con `/api/auth` y `/api/chat`.
- [x] Prototipo visual local con modos `follow`, `free` y `stay`.
- [x] Comando de estado y registro de una instrucción de tarea.
- [ ] Verificar compilación y ejecución dentro de Minecraft.
- [ ] Fase 1: compañero real controlado por el servidor integrado en Singleplayer.
- [ ] Fase 1: movimiento que respete el terreno y obstáculos.
- [ ] Fase 1: inventario propio y persistente del compañero.
- [ ] Fase 1: recoger objetos del mundo de forma fiable.
- [ ] Fase 1: comandos para seguir y quedarse quieto conectados a la entidad real.
- [ ] Fase posterior: recolección de recursos, fabricación y uso de herramientas.
- [ ] Fase posterior: voz de entrada y respuestas habladas.

## Alcance de esta fase

La primera fase se limita a **mundos de un jugador**. No se promete compatibilidad multijugador ni se debe asumir que una entidad creada en el cliente cambia el estado real del mundo.

Orden de implementación:
1. Crear y gestionar la entidad desde el servidor integrado de Singleplayer.
2. Añadir movimiento/seguimiento con comprobaciones de terreno y distancia.
3. Añadir un inventario dedicado a la entidad.
4. Transferir objetos cercanos al inventario del compañero sin duplicar ni perder objetos.
5. Conectar comandos y estado a esa entidad.
6. Compilar y probar en la versión objetivo antes de marcar las casillas como completas.

## Compatibilidad

La configuración Gradle del repositorio declara Minecraft **1.21.11**, Yarn y Fabric API. La documentación anterior mencionaba 1.21.1; se corrige aquí esa discrepancia documental. La compilación y la ejecución real todavía deben verificarse. No afirmamos compatibilidad con Forge o NeoForge.

## Requisitos

- JDK 21
- Gradle instalado
- Minecraft Java 1.21.11
- Fabric Loader y Fabric API compatibles con la configuración del proyecto

## Compilar

Desde la raíz del repositorio:

```bash
gradle build
```

El JAR normalmente se genera en `build/libs/`. El repositorio no incluye actualmente Gradle Wrapper; el resultado no debe considerarse probado hasta que el flujo de CI compile correctamente y se realice una prueba dentro del juego.

## Comandos existentes

- `/nexus login` (abre la página web de vinculación; ticket temporal de 5 minutos)
- `/nexus logout`
- `/nexus chat hola, ¿quién eres?`
- `/nexus credits`
- `/nexus logout`
- `/nexus spawn`
- `/nexus behavior follow`
- `/nexus behavior stay`
- `/nexus behavior free`
- `/nexus task <instrucción>`
- `/nexus status`

Los comandos de comportamiento existentes actúan sobre el prototipo visual. `/nexus task` solo guarda texto; no ejecuta minería, recolección ni fabricación todavía.

## Autenticación y seguridad

El mod ya no pide contraseñas dentro del chat de Minecraft. `/nexus login` solicita un ticket aleatorio temporal, abre la página web de vinculación y consulta el estado durante un máximo de cinco minutos. Al desconectarse del mundo, el mod intenta cancelar y eliminar el ticket pendiente en Supabase; si la conexión falla, el ticket igualmente caduca a los cinco minutos. La sesión local de NEXUS.AI se conserva al salir del mundo. Al autorizarlo, el mod guarda la sesión local en `nexus-companion-session.json`; no compartas ese archivo.

**Configuración obligatoria del backend antes de usar este flujo:** crea la tabla con `supabase/device-auth.sql` en Supabase y añade `SUPABASE_SERVICE_ROLE_KEY` como variable de entorno secreta de servidor en Vercel. Nunca uses un prefijo `NEXT_PUBLIC_` para esa clave. La cuenta web debe iniciar sesión en `/device-auth?ticket=...` y autorizar la vinculación. Hasta configurar y probar estos pasos, el login con ticket no está listo para producción.

Los créditos se controlan en el backend compartido; el mod no crea una cartera independiente ni garantiza créditos de bienvenida que el backend no haya configurado.

## Documentación

- [Roadmap de desarrollo](docs/ROADMAP.md)
- [Protocolo de API](docs/API-PROTOCOL.md)
