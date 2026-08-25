# Lepton Client

A singleplayer-only utility client for Minecraft Java Edition, built on Fabric.

Lepton is a Meteor-style modular client — a ClickGUI, a HUD, and a set of toggleable
modules — with one deliberate constraint: **it does not run in multiplayer.** Every module
is gated behind a check that requires a plain singleplayer world, and a world opened to LAN
counts as multiplayer.

**Minecraft 1.21.11 · Fabric**

---

## The singleplayer constraint

This is the design decision the whole project is built around.

Three independent conditions must all hold before any module can activate:

- the client reports it is in singleplayer,
- there is no remote server entry (you did not join anything),
- the integrated server has not been opened to LAN.

If any of them fails, modules refuse to enable, and anything already running is switched
off on the next tick. Nothing in the mod touches the network layer — there are no packet
mixins, no movement spoofing, no anti-cheat evasion.

Some modules go further and are *structurally* singleplayer-only rather than merely gated.
NoFall, for example, works by injecting into `PlayerEntity.handleFallDamage` to reach the
integrated server's copy of the player. That only works because singleplayer runs its
server inside the same JVM; on a real server that calculation happens on another machine
where this code does not exist.

To be clear about what this is and isn't: the gate is a design constraint, not a security
boundary. It makes the shipped client refuse multiplayer. It is not DRM and does not
pretend to be.

---

## Modules

Currently **30 modules** across five categories.

**Combat** — KillAura · TriggerBot · Criticals

**Movement** — Flight · Speed · Sprint · NoFall · Step · Jesus · Spider · NoClip ·
HighJump · AutoJump · AutoWalk · AntiVoid

**Player** — AutoEat · AutoTool · AutoReplenish · AutoRespawn · AntiAFK · FastUse

**Render** — ESP · Tracers · StorageESP · Xray · Fullbright · Zoom

**World** — Nuker · VeinMiner · AutoMount

### Interface

- **ClickGUI** on `Right Shift` — draggable category panels, live search, per-module
  settings with sliders, dropdowns, colour pickers and keybind capture
- **HUD** — watermark, active-module list, coordinates / nether coordinates / facing / FPS
- **Themes** — six palettes (Lepton Blue by default) plus a user-defined accent colour
- Config saved as JSON in `.minecraft/lepton/`

---

## Installing

1. Install [Fabric Loader](https://fabricmc.net/use/) 0.19.3+ for Minecraft 1.21.11
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) in your `mods` folder
3. Drop `lepton-client-1.0.0.jar` into `.minecraft/mods`
4. Launch the Fabric profile, load a singleplayer world, press `Right Shift`

## Building from source

Requires JDK 21.

```bash
./gradlew build
```

The jar lands in `build/libs/`.

To run a development client:

```bash
./gradlew runClient
```

---

## Status

This is a work in progress. 30 modules are implemented and compiling; the original scope
was closer to 124. Still to come:

- the remaining modules across all categories, particularly World and Misc
- a `.` prefixed command system, macros, waypoints and config profiles
- widening StorageESP beyond the player's current chunk

`docs/api-notes-1.21.11.md` records the yarn mapping changes in 1.21.11 that this was built
against — `getPos()` becoming `getEntityPos()`, the `KeyInput`/`MouseInput`/`Click` input
refactor, `WorldRenderer.render` losing its MatrixStack, and Fabric API dropping
`WorldRenderEvents`. `docs/mcapi.sh` introspects the remapped Minecraft jar with `javap`,
which is considerably more reliable than guessing mappings.

---

## Licence

MIT — see [LICENSE](LICENSE).
