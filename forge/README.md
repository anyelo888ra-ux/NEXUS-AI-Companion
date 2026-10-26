# Forge adapter

Status: planned, not built or launch-tested.

Before implementation, verify that Forge itself has a compatible release for the intended Minecraft version. Forge and NeoForge are separate projects. If Forge does not support Minecraft 1.21.11, choose a supported Forge version and document that version split; do not claim the same target works.

Implementation checklist:
- Configure the exact supported Forge MDK and Gradle wrapper.
- Add `META-INF/mods.toml`.
- Register the entity, commands, networking, and lifecycle hooks.
- Build and launch-test in a clean Forge profile.
