package com.tfar.stellarfluidconduits.common.conduit;


import javax.annotation.Nonnull;

import com.enderio.core.common.fluid.IFluidWrapper;
import com.enderio.core.common.util.DyeColor;

import com.tfar.stellarfluidconduits.common.conduit.stellar.StellarFluidConduit;
import crazypants.enderio.base.conduit.ConnectionMode;
import crazypants.enderio.conduits.conduit.liquid.AbstractLiquidConduit;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class NetworkTank {

    @Nonnull
    private final StellarFluidConduit con;
    @Nonnull
    private final EnumFacing conDir;
    private final IFluidWrapper externalTank;
    @Nonnull
    private final EnumFacing tankDir;
    @Nonnull
    private final BlockPos conduitLoc;
    private final boolean acceptsOutput;
    private final DyeColor inputColor;
    private final DyeColor outputColor;
    private final int priority;
    private final boolean roundRobin;
    private final boolean selfFeed;

    public NetworkTank(@Nonnull StellarFluidConduit con, @Nonnull EnumFacing conDir) {
        this.con = con;
        this.conDir = conDir;
        conduitLoc = con.getBundle().getLocation();
        tankDir = conDir.getOpposite();
        externalTank = AbstractLiquidConduit.getExternalFluidHandler(con.getBundle().getBundleworld(), conduitLoc.offset(conDir), tankDir);
        acceptsOutput = con.getConnectionMode(conDir).acceptsOutput();
        inputColor = con.getOutputColor(conDir);
        outputColor = con.getInputColor(conDir);
        priority = con.getOutputPriority(conDir);
        roundRobin = con.isRoundRobinEnabled(conDir);
        selfFeed = con.isSelfFeedEnabled(conDir);
    }

    public boolean isValid() {
        return externalTank != null && con.getConnectionMode(conDir) != ConnectionMode.DISABLED;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + conDir.hashCode();
        result = prime * result + conduitLoc.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NetworkTank other = (NetworkTank) obj;
        if (conDir != other.conDir) {
            return false;
        }
        if (!conduitLoc.equals(other.conduitLoc)) {
            return false;
        }
        return true;
    }
    public boolean acceptsOutput() {
        return acceptsOutput;
    }

    public DyeColor getInputColor() {
        return inputColor;
    }

    public DyeColor getOutputColor() {
        return outputColor;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isRoundRobin() {
        return roundRobin;
    }

    public boolean isSelfFeed() {
        return selfFeed;
    }

    public IFluidWrapper getExternalTank() {
        return externalTank;
    }

    public EnumFacing getConduitDir() {
        return conDir;
    }

    public EnumFacing getTankDir() {
        return tankDir;
    }

    @Nonnull
    public BlockPos getConduitLocation() {
        return conduitLoc;
    }

    public StellarFluidConduit getConduit() {
        return con;
    }

}