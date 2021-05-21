package com.williambl.playerlister;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

@Mod("playerlister")
public class PlayerLister {
    private static final Logger LOGGER = LogManager.getLogger();

    private MinecraftServer server;
    private long ticks = 0L;

    public PlayerLister() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        if (event.getServer().isDedicatedServer()) {
            server = event.getServer();
        }
    }

    @SubscribeEvent
    public void onServerStop(FMLServerStoppingEvent event) {
        server = null;
    }

    @SubscribeEvent
    public void onServerStarting(TickEvent.ServerTickEvent event) throws IOException {
        if (ticks++ % Config.CONFIG.refreshTime.get() != 0) return;

        Path path = FileSystems.getDefault().getPath(Config.CONFIG.outputPath.get());
        Files.deleteIfExists(path);
        Files.createFile(path);
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            server.getPlayerList()
                    .getPlayers()
                    .stream()
                    .map(it -> it.getGameProfile().getName())
                    .forEach(it -> {
                        try {
                            writer.write(it);
                            writer.newLine();
                        } catch (IOException e) {
                            LOGGER.error("Failure writing PlayerList: ", e);
                        }
                    });
        }
    }

    static class Config {
        static final ForgeConfigSpec SPEC;
        public static final Config CONFIG;
        static {
            final Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Config::new);
            SPEC = specPair.getRight();
            CONFIG = specPair.getLeft();
        }

        public final ForgeConfigSpec.LongValue refreshTime;
        public final ForgeConfigSpec.ConfigValue<String> outputPath;

        Config(ForgeConfigSpec.Builder builder) {
            refreshTime = builder
                    .comment("Time in seconds between list refreshes.")
                    .defineInRange("refreshTime", 15L, 0L, Long.MAX_VALUE);

            outputPath = builder
                    .comment("Path to list file.")
                    .define("outputPath", "./players.txt");
        }
    }
}
