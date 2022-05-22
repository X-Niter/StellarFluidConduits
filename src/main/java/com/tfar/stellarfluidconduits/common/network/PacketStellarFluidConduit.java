package com.tfar.stellarfluidconduits.common.network;

import com.enderio.core.common.util.DyeColor;
import com.tfar.stellarfluidconduits.common.conduit.stellar.StellarFluidConduit;
import crazypants.enderio.base.conduit.IConduit;
import crazypants.enderio.base.filter.IFilter;
import crazypants.enderio.base.filter.capability.CapabilityFilterHolder;
import crazypants.enderio.base.filter.capability.IFilterHolder;
import crazypants.enderio.conduits.network.PacketConduitFilter;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nonnull;

public class PacketStellarFluidConduit extends PacketConduitFilter<StellarFluidConduit> {
    private DyeColor colIn;
    private DyeColor colOut;
    private int priority;
    private boolean roundRobin;
    private boolean selfFeed;

    public PacketStellarFluidConduit() {
    }

    public PacketStellarFluidConduit(@Nonnull StellarFluidConduit con, @Nonnull EnumFacing dir) {
        super(con, dir);
        colIn = con.getInputColor(dir);
        colOut = con.getOutputColor(dir);
        priority = con.getOutputPriority(dir);
        roundRobin = con.isRoundRobinEnabled(dir);
        selfFeed = con.isSelfFeedEnabled(dir);
    }

    @Override
    public void write(@Nonnull ByteBuf buf) {
        super.write(buf);
        buf.writeShort(colIn.ordinal());
        buf.writeShort(colOut.ordinal());
        buf.writeInt(priority);
        buf.writeBoolean(roundRobin);
        buf.writeBoolean(selfFeed);
    }

    @Override
    public void read(@Nonnull ByteBuf buf) {
        super.read(buf);
        colIn = DyeColor.values()[buf.readShort()];
        colOut = DyeColor.values()[buf.readShort()];
        priority = buf.readInt();
        roundRobin = buf.readBoolean();
        selfFeed = buf.readBoolean();
    }

    public static class Handler implements IMessageHandler<PacketStellarFluidConduit, IMessage> {

        @Override
        public IMessage onMessage(PacketStellarFluidConduit message, MessageContext ctx) {
            StellarFluidConduit conduit = message.getConduit(ctx);
            if (conduit != null) {
                conduit.setInputColor(message.dir, message.colIn);
                conduit.setOutputColor(message.dir, message.colOut);
                conduit.setOutputPriority(message.dir, message.priority);
                conduit.setRoundRobinEnabled(message.dir, message.roundRobin);
                conduit.setSelfFeedEnabled(message.dir, message.selfFeed);
                applyFilter(message.dir, conduit, message.inputFilter, true);
                applyFilter(message.dir, conduit, message.outputFilter, false);

                IBlockState bs = message.getWorld(ctx).getBlockState(message.getPos());
                message.getWorld(ctx).notifyBlockUpdate(message.getPos(), bs, bs, 3);
            }
            return null;
        }

        private void applyFilter(EnumFacing dir, IConduit conduit, IFilter filter, boolean isInput) {
            if (conduit.hasInternalCapability(CapabilityFilterHolder.FILTER_HOLDER_CAPABILITY, dir)) {
                IFilterHolder<IFilter> filterHolder = CapabilityFilterHolder.FILTER_HOLDER_CAPABILITY.cast(conduit.getInternalCapability(CapabilityFilterHolder.FILTER_HOLDER_CAPABILITY, dir));
                if (filterHolder != null) {
                    filterHolder.setFilter(isInput ? filterHolder.getInputFilterIndex() : filterHolder.getOutputFilterIndex(), dir, filter);
                }
            }
        }

    }

}
