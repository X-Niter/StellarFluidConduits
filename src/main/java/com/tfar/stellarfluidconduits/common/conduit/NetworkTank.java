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

    final @Nonnull
    public StellarFluidConduit con;
    public final @Nonnull EnumFacing conDir;
    public final IFluidWrapper externalTank;
    public final @Nonnull EnumFacing tankDir;
    public final @Nonnull BlockPos conduitLoc;
    public final boolean acceptsOuput;
    public final DyeColor inputColor;
    public final DyeColor outputColor;
    public final int priority;
    public final boolean roundRobin;
    public final boolean selfFeed;
    public final boolean supportsMultipleTanks;

    public NetworkTank(@Nonnull StellarFluidConduit con, @Nonnull EnumFacing conDir) {
        this.con = con;
        this.conDir = conDir;
        conduitLoc = con.getBundle().getLocation();
        tankDir = conDir.getOpposite();
        externalTank = AbstractLiquidConduit.getExternalFluidHandler(con.getBundle().getBundleworld(), conduitLoc.offset(conDir), tankDir);
        acceptsOuput = con.getConnectionMode(conDir).acceptsOutput();
        inputColor = con.getOutputColor(conDir);
        outputColor = con.getInputColor(conDir);
        priority = con.getOutputPriority(conDir);
        roundRobin = con.isRoundRobinEnabled(conDir);
        selfFeed = con.isSelfFeedEnabled(conDir);
        supportsMultipleTanks = (externalTank != null) && externalTank.getTankInfoWrappers().size() > 1;
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
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NetworkTank other = (NetworkTank) obj;
        return conDir == other.conDir && conduitLoc.equals(other.conduitLoc);
    }

}