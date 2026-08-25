package dev.lepton.systems.modules;

import dev.lepton.systems.modules.combat.Criticals;
import dev.lepton.systems.modules.combat.KillAura;
import dev.lepton.systems.modules.combat.TriggerBot;
import dev.lepton.systems.modules.world.AutoMount;
import dev.lepton.systems.modules.world.Nuker;
import dev.lepton.systems.modules.world.VeinMiner;
import dev.lepton.systems.modules.player.AntiAFK;
import dev.lepton.systems.modules.player.AutoEat;
import dev.lepton.systems.modules.player.AutoReplenish;
import dev.lepton.systems.modules.player.AutoRespawn;
import dev.lepton.systems.modules.player.AutoTool;
import dev.lepton.systems.modules.player.FastUse;
import dev.lepton.systems.modules.render.ESP;
import dev.lepton.systems.modules.render.Fullbright;
import dev.lepton.systems.modules.render.StorageESP;
import dev.lepton.systems.modules.render.Tracers;
import dev.lepton.systems.modules.render.Xray;
import dev.lepton.systems.modules.render.Zoom;
import dev.lepton.systems.modules.movement.AntiVoid;
import dev.lepton.systems.modules.movement.AutoJump;
import dev.lepton.systems.modules.movement.AutoWalk;
import dev.lepton.systems.modules.movement.HighJump;
import dev.lepton.systems.modules.movement.Jesus;
import dev.lepton.systems.modules.movement.NoClip;
import dev.lepton.systems.modules.movement.Spider;
import dev.lepton.systems.modules.movement.Step;
import dev.lepton.systems.modules.movement.Flight;
import dev.lepton.systems.modules.movement.NoFall;
import dev.lepton.systems.modules.movement.Speed;
import dev.lepton.systems.modules.movement.Sprint;

/**
 * The one place every module gets wired in. Kept separate from {@link Modules} so the
 * registry logic stays readable as the list grows.
 */
public class ModuleRegistry {
    public static void registerAll(Modules modules) {
        registerCombat(modules);
        registerMovement(modules);
        registerPlayer(modules);
        registerRender(modules);
        registerWorld(modules);
        registerMisc(modules);
    }

    private static void registerCombat(Modules modules) {
        modules.add(new Criticals());
        modules.add(new KillAura());
        modules.add(new TriggerBot());
    }

    private static void registerMovement(Modules modules) {
        modules.add(new AntiVoid());
        modules.add(new AutoJump());
        modules.add(new AutoWalk());
        modules.add(new HighJump());
        modules.add(new Jesus());
        modules.add(new NoClip());
        modules.add(new Spider());
        modules.add(new Step());
        modules.add(new Flight());
        modules.add(new NoFall());
        modules.add(new Speed());
        modules.add(new Sprint());
    }

    private static void registerPlayer(Modules modules) {
        modules.add(new AntiAFK());
        modules.add(new AutoEat());
        modules.add(new AutoReplenish());
        modules.add(new AutoRespawn());
        modules.add(new AutoTool());
        modules.add(new FastUse());
    }

    private static void registerRender(Modules modules) {
        modules.add(new ESP());
        modules.add(new Fullbright());
        modules.add(new StorageESP());
        modules.add(new Tracers());
        modules.add(new Xray());
        modules.add(new Zoom());
    }

    private static void registerWorld(Modules modules) {
        modules.add(new AutoMount());
        modules.add(new Nuker());
        modules.add(new VeinMiner());
    }

    private static void registerMisc(Modules modules) {
    }
}
