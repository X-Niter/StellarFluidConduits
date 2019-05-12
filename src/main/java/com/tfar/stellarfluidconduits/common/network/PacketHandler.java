package com.tfar.stellarfluidconduits.common.network;

import com.enderio.core.common.network.ThreadedNetworkWrapper;
import com.tfar.stellarfluidconduits.ReferenceVariables;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;

public class PacketHandler {
    @Nonnull
    public static final ThreadedNetworkWrapper INSTANCE = new ThreadedNetworkWrapper(ReferenceVariables.MOD_ID);
    public static int ID;

    public static void init(FMLInitializationEvent event) {
        INSTANCE.registerMessage(PacketStellarFluidConduit.Handler.class, PacketStellarFluidConduit.class, ID++, Side.SERVER);
    }
}