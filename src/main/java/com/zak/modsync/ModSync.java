package com.zak.modsync;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("modsync")
public class ModSync
{
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    private ModHostingServer modListServer;

    public ModSync()
    {
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        modListServer = new ModHostingServer();
        modListServer.run();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
    	LOGGER.info("Stopping mod list server");
    	modListServer.stop();
    	modListServer = null;
    }
}
