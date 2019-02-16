package com.tfar.stellarfluidconduits.common.network;

import com.tfar.stellarfluidconduits.common.conduit.stellar.StellarFluidConduit;
import crazypants.enderio.base.filter.fluid.IFluidFilter;
import crazypants.enderio.conduits.conduit.liquid.ILiquidConduit;
import crazypants.enderio.conduits.conduit.liquid.LiquidConduit;
import crazypants.enderio.conduits.network.AbstractConduitPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketFluidFilter extends AbstractConduitPacket<ILiquidConduit> {
    private EnumFacing dir;
    private boolean isInput;
    private ILiquidConduit filter;

    public PacketFluidFilter() {
    }

    public PacketFluidFilter(StellarFluidConduit eConduit, EnumFacing dir, ILiquidConduit filter, boolean isInput) {
        super(eConduit);
        this.dir = dir;
        this.filter = filter;
        this.isInput = isInput;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        if (dir != null) {
            buf.writeShort(dir.ordinal());
        } else {
            buf.writeShort(-1);
        }
        buf.writeBoolean(isInput);
        NBTTagCompound tag = new NBTTagCompound();
        filter.writeToNBT(tag);
        ByteBufUtils.writeTag(buf, tag);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        short ord = buf.readShort();
        dir = ord < 0 ? null : EnumFacing.values()[ord];
        isInput = buf.readBoolean();
        NBTTagCompound tag = ByteBufUtils.readTag(buf);
        filter = new LiquidConduit();
        if (tag != null) {
            filter.readFromNBT(tag);
        }
    }

    public static class Handler implements IMessageHandler<PacketFluidFilter, IMessage> {
        @Override
        public IMessage onMessage(PacketFluidFilter message, MessageContext ctx) {
            ILiquidConduit conduit = message.getConduit(ctx);
            if (!(conduit instanceof StellarFluidConduit)) {
                return null;
            }
            StellarFluidConduit eCon = (StellarFluidConduit) conduit;
            eCon.setFilter(message.dir, (IFluidFilter) message.filter, message.isInput);

            IBlockState bs = message.getWorld(ctx).getBlockState(message.getPos());
            message.getWorld(ctx).notifyBlockUpdate(message.getPos(), bs, bs, 3);
            return null;
        }
    }
}

