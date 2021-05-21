package com.williambl.playerlister;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

@Mod("playerlister")
public class PlayerLister
{
    private static final Logger LOGGER = LogManager.getLogger();

    private MinecraftServer server;
    private long ticks = 0L;

    public PlayerLister() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStart(FMLServerStartingEvent event) {
        server = event.getServer();
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
