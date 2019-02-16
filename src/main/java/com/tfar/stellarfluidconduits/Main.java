package com.tfar.stellarfluidconduits;

import com.tfar.stellarfluidconduits.common.CommonProxy;
import com.tfar.stellarfluidconduits.common.conduit.FluidConduitObject;
import com.tfar.stellarfluidconduits.common.config.Config;
import com.tfar.stellarfluidconduits.common.network.PacketHandler;
import crazypants.enderio.api.addon.IEnderIOAddon;
import crazypants.enderio.base.config.ConfigHandlerEIO;
import crazypants.enderio.base.init.RegisterModObject;
import info.loenwind.autoconfig.ConfigHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = ReferenceVariables.MOD_ID)

@Mod(modid = ReferenceVariables.MOD_ID, name = ReferenceVariables.MOD_NAME, version = ReferenceVariables.VERSION)
public class Main implements IEnderIOAddon
{    @SidedProxy(serverSide = ReferenceVariables.PROXY_COMMON_CLASS, clientSide = ReferenceVariables.PROXY_CLIENT_CLASS)
public static CommonProxy proxy;

    public static Logger logger;

    private static ConfigHandler configHandler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        configHandler = new ConfigHandlerEIO(event, Config.F);
        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.Init(event);
        PacketHandler.init(event);
    }
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        proxy.serverStart(event);
    }

    @SubscribeEvent
    public static void registerBlocksEarly(@Nonnull RegisterModObject event) {
        FluidConduitObject.registerBlocksEarly(event);
    }

    @Override
    @Nullable
    public Configuration getConfiguration() {
        return Config.F.getConfig();
    }
}
