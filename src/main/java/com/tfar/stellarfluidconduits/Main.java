package com.tfar.stellarfluidconduits;

import com.tfar.stellarfluidconduits.common.CommonProxy;
import crazypants.enderio.api.addon.IEnderIOAddon;
import info.loenwind.autoconfig.ConfigHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
@Mod.EventBusSubscriber(modid = ReferenceVariables.MOD_ID)

@Mod(modid = ReferenceVariables.MOD_ID, name = ReferenceVariables.MOD_NAME, version = ReferenceVariables.VERSION)
public class Main implements IEnderIOAddon
{    @SidedProxy(serverSide = ReferenceVariables.PROXY_COMMON_CLASS, clientSide = ReferenceVariables.PROXY_CLIENT_CLASS)
public static CommonProxy proxy;

    public static Logger logger;

    private static ConfigHandler configHandler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    }
}
