# Fabric adapter

Status: planned, not built or launch-tested.

Implementation checklist:
- Confirm Fabric Loader and Fabric API releases for the chosen Minecraft version.
- Add `fabric.mod.json` and a Fabric entrypoint.
- Register the companion entity and client/server networking.
- Add a minimal command such as `/nexus stay` and `/nexus follow`.
- Build and test a development client before packaging.

Do not copy a Forge or NeoForge metadata file into this module; the loader APIs are different.
