# Roadmap de NEXUS.AI Companion

## Alcance acordado

El desarrollo actual está dirigido a **Minecraft Java 1.21.11 + Fabric en Singleplayer**. No se implementará compatibilidad multijugador en esta fase. El inicio de sesión y la conexión existente de `NexusSession` con NEXUS.AI deben mantenerse.

## Fase 1 — Compañero real

### Estado inicial revisado
- [x] Revisar el prototipo actual y confirmar sus limitaciones.
- [x] Documentar el alcance Singleplayer y corregir la descripción de README.
- [ ] Entidad real gestionada por el servidor integrado; sustituir el ArmorStand puramente cliente como fuente de comportamiento.
- [ ] Seguimiento real del jugador, sin atravesar bloques ni ignorar el terreno.
- [ ] Comportamiento de quedarse quieto y volver a seguir al jugador.
- [ ] Inventario propio del compañero.
- [ ] Recoger objetos cercanos sin duplicarlos ni eliminarlos por error.
- [ ] Comandos conectados a la entidad real.
- [ ] Guardado/carga del inventario y estado cuando corresponda.
- [ ] Compilación CI y prueba dentro de un mundo Singleplayer.

### Comandos objetivo
- `/nexus spawn`: invocar al compañero.
- `/nexus behavior follow`: seguir al jugador.
- `/nexus behavior stay`: quedarse en su sitio.
- `/nexus status`: mostrar el estado real del compañero.
- Un comando de inventario se añadirá cuando el inventario exista y se pueda consultar de forma fiable.

Los comandos actuales `/nexus behavior free` y `/nexus task` siguen siendo prototipos: el modo libre mueve el ArmorStand local y la tarea solo guarda una cadena de texto. No deben describirse como autonomía real.

## Fase 2 — Supervivencia y tareas
- [ ] Recoger recursos bajo reglas explícitas.
- [ ] Cortar madera y minar con límites de seguridad.
- [ ] Usar mesas de trabajo y fabricar herramientas.
- [ ] Gestionar comida, herramientas, armadura y durabilidad.
- [ ] Pedir confirmación para tareas destructivas o que puedan poner en riesgo objetos valiosos.
- [ ] Mostrar qué acción está realizando y por qué.

## Fase 3 — Voz e IA
- [ ] Comprobar integración opcional con Simple Voice Chat.
- [ ] Ofrecer un botón alternativo de micrófono cuando no exista compatibilidad.
- [ ] Solicitar permiso antes de usar el micrófono.
- [ ] Convertir voz a texto mediante un servicio configurado.
- [ ] Enviar mensajes mediante la sesión existente de NEXUS.AI.
- [ ] Reproducir respuestas con voz sintética si hay un servicio disponible.
- [ ] Manejar errores de red, límites y servicios no configurados.

## Requisitos de calidad
- [ ] No incluir claves privadas en el JAR.
- [ ] Mantener intacto el flujo de autenticación existente.
- [ ] No afirmar que una función está terminada sin pruebas.
- [ ] Mantener README y esta hoja de ruta sincronizados con el código.
- [ ] No declarar soporte multijugador hasta diseñar y probar sus permisos y sincronización.
