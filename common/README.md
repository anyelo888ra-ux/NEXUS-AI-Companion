# Common layer

This directory describes loader-independent behavior. Keep it free of Minecraft loader imports.

Planned components:
- `CompanionCommand`: enum-like allowlist of safe commands.
- `CompanionState`: FOLLOW / STAY / ASSIST.
- `ConversationRequest`: user message plus minimal context.
- `ActionValidator`: rejects unknown or destructive commands by default.

The first implementation should use Minecraft's own entity navigation and server-side checks; the AI should suggest intent, not directly execute arbitrary code.
