package com.williambl.playerlister;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
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
public class PlayerLister
{
    private static final Logger LOGGER = LogManager.getLogger();

    private MinecraftServer server;
    private long ticks = 0L;

    public PlayerLister() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
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
        if (ticks++ % 300 != 0) return;

        Path path = FileSystems.getDefault().getPath("./players.txt");
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
}
