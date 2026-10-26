# NEXUS.AI Companion — Minecraft Java

Starter repository for a friendly AI companion, initially targeting Minecraft Java 1.21.11.

## Current status (honest)
This is a **project scaffold**, not yet a verified, installable mod release.
- Shared companion design and safe command protocol: included.
- Fabric / Forge / NeoForge adapter directories: included as implementation plans.
- Minecraft launch tests and loader-specific JAR builds: **not run**.
- Live NEXUS.AI / Groq connection: **not implemented**.
- Bedrock port: future work.

### Important compatibility note
Minecraft mod loaders do not share one universal API. A single JAR cannot work across Fabric, Forge and NeoForge. Each adapter needs its own metadata, entry point, dependencies and build configuration. Also verify that the exact loader has a release for the exact Minecraft version before selecting it. If Forge does not support 1.21.11, a Forge build must target a supported Minecraft version rather than pretending it works.

## Planned first milestones
1. Build and launch a minimal mod for one supported loader.
2. Add an in-world Nexus entity with a visible name and a basic follow/stay command.
3. Add a local chat screen and a configurable API bridge.
4. Add Fabric, NeoForge and Forge adapters where the chosen Minecraft versions are supported.
5. Test multiplayer/server permissions and performance.

## Safety and privacy
- Never put API keys in the mod JAR or commit them to Git.
- The eventual API bridge should run locally or on a server and keep secrets outside the client.
- Validate every requested action against an allowlist; ask for confirmation before destructive actions.
- Do not send world data or chat logs to an AI service without a clear user-controlled setting.

## Repository layout
- `common/`: loader-independent design and action protocol.
- `fabric/`, `forge/`, `neoforge/`: adapter plans and compatibility notes.
- `docs/`: development roadmap and API protocol.
- `scripts/`: helper script for initializing a Git repository and pushing to a newly created empty GitHub repository.

## Build status
No compiled `.jar` files are included. Do not put this folder into `mods` and expect it to launch yet.
