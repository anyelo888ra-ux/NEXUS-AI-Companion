# AI bridge protocol (proposal)

The game client/server should send only the minimum required context.

Example request:
```json
{
  "protocolVersion": 1,
  "message": "Nexus, sígueme",
  "worldContext": {
    "dimension": "minecraft:overworld",
    "nearbyBlocks": [],
    "nearbyEntities": []
  }
}
```

Example response:
```json
{
  "reply": "¡Voy contigo!",
  "actions": [
    { "type": "FOLLOW_PLAYER" }
  ]
}
```

The mod must validate response shape, allowlisted action types, distances, cooldowns, permissions and server-side conditions. Never interpret model output as Java code, shell commands, or arbitrary game commands.
