package com.tfar.stellarfluidconduits.common.conduit.stellar;

import crazypants.enderio.base.conduit.item.FunctionUpgrade;
import crazypants.enderio.base.conduit.item.ItemFunctionUpgrade;

import com.enderio.core.common.fluid.IFluidWrapper;
import com.enderio.core.common.util.RoundRobinIterator;
import crazypants.enderio.base.filter.fluid.IFluidFilter;
import crazypants.enderio.conduits.conduit.AbstractConduitNetwork;
import crazypants.enderio.conduits.conduit.liquid.EnderLiquidConduitNetwork;
import crazypants.enderio.conduits.conduit.liquid.ILiquidConduit;

import com.tfar.stellarfluidconduits.common.conduit.NetworkTank;
import com.tfar.stellarfluidconduits.common.config.StellarFluidConduitConfig;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import java.util.*;

public class StellarFluidConduitNetwork extends EnderLiquidConduitNetwork{
    List<NetworkTank> tanks = new ArrayList<>();
    Map<NetworkTankKey, NetworkTank> tankMap = new HashMap<>();

    Map<NetworkTank, RoundRobinIterator<NetworkTank>> iterators;

    boolean filling;

    public StellarFluidConduitNetwork() {
      //  super(StellarFluidConduit.class, ILiquidConduit.class);
    }

    public void connectionChanged(@Nonnull StellarFluidConduit con, @Nonnull EnumFacing conDir) {
        NetworkTankKey key = new NetworkTankKey(con, conDir);
        NetworkTank tank = new NetworkTank(con, conDir);

        // Check for later
        boolean sort = false;
        NetworkTank oldTank = tankMap.get(key);
        if (oldTank != null && oldTank.getPriority() != tank.getPriority()) {
            sort = true;
        }

        tanks.remove(tank); // remove old tank, NB: =/hash is only calced on location and dir
        tankMap.remove(key);
        tanks.add(tank);
        tankMap.put(key, tank);

        // If the priority has been changed, then sort the list to match
        if (sort) {
            tanks.sort(new Comparator<NetworkTank>() {

                @Override
                public int compare(NetworkTank arg0, NetworkTank arg1) {
                    return arg1.getPriority() - arg0.getPriority();
                }

            });
        }
    }

    public boolean extractFrom(@Nonnull StellarFluidConduit con, @Nonnull EnumFacing conDir) {
        NetworkTank tank = getTank(con, conDir);
        if (!tank.isValid()) {
            return false;
        }
        FluidStack drained = tank.getExternalTank().getAvailableFluid();
        if (drained == null || drained.amount <= 0 || !matchedFilter(drained, con, conDir, true)) {
            return false;
        }

        drained = drained.copy();
        drained.amount = Math.min(drained.amount, StellarFluidConduitConfig.extractRate.get() * getExtractSpeedMultiplier(tank) / 2);
        int amountAccepted = fillFrom(tank, drained.copy(), true);
        if (amountAccepted <= 0) {
            return false;
        }
        drained.amount = amountAccepted;
        drained = tank.getExternalTank().drain(drained);
        if (drained == null || drained.amount <= 0) {
            return false;
        }
        // if(drained.amount != amountAccepted) {
        // Log.warn("StellarFluidConduit.extractFrom: Extracted fluid volume is not equal to inserted volume. Drained=" + drained.amount + " filled="
        // + amountAccepted + " Fluid: " + drained + " Accepted=" + amountAccepted);
        // }
        return true;
    }

    @Nonnull
    private NetworkTank getTank(@Nonnull StellarFluidConduit con, @Nonnull EnumFacing conDir) {
        return tankMap.get(new NetworkTankKey(con, conDir));
    }

    public int fillFrom(@Nonnull StellarFluidConduit con, @Nonnull EnumFacing conDir, FluidStack resource, boolean doFill) {
        return fillFrom(getTank(con, conDir), resource, doFill);
    }

    public int fillFrom(@Nonnull NetworkTank tank, FluidStack resource, boolean doFill) {

        if (filling) {
            return 0;
        }

        try {

            filling = true;

            if (resource == null || !matchedFilter(resource, tank.getConduit(), tank.getConduitDir(), true)) {
                return 0;
            }

            resource = resource.copy();
            resource.amount = Math.min(resource.amount, StellarFluidConduitConfig.maxIO.get() * getExtractSpeedMultiplier(tank) / 2);
            int filled = 0;
            int remaining = resource.amount;
            // TODO: Only change starting pos of iterator is doFill is true so a false then true returns the same

            for (NetworkTank target : getIteratorForTank(tank)) {
                if ((!target.equals(tank) || tank.isSelfFeed()) && target.acceptsOutput() && target.isValid() && target.getInputColor() == tank.getOutputColor()
                        && matchedFilter(resource, target.getConduit(), target.getConduitDir(), false)) {
                    int vol = doFill ? target.getExternalTank().fill(resource.copy()) : target.getExternalTank().offer(resource.copy());
                    remaining -= vol;
                    filled += vol;
                    if (remaining <= 0) {
                        return filled;
                    }
                    resource.amount = remaining;
                }
            }
            return filled;

        } finally {
            if (!tank.isRoundRobin()) {
                getIteratorForTank(tank).reset();
            }
            filling = false;
        }
    }

    private int getExtractSpeedMultiplier(NetworkTank tank) {
        int extractSpeedMultiplier = 2;

        ItemStack upgradeStack = tank.getConduit().getFunctionUpgrade(tank.getConduitDir());
        if (!upgradeStack.isEmpty()) {
            FunctionUpgrade upgrade = ItemFunctionUpgrade.getFunctionUpgrade(upgradeStack);
            if (upgrade == FunctionUpgrade.EXTRACT_SPEED_UPGRADE) {
                extractSpeedMultiplier += FunctionUpgrade.LIQUID_MAX_EXTRACTED_SCALER * Math.min(upgrade.maxStackSize, upgradeStack.getCount());
            } else if (upgrade == FunctionUpgrade.EXTRACT_SPEED_DOWNGRADE) {
                extractSpeedMultiplier = 1;
            }
        }

        return extractSpeedMultiplier;
    }
    private boolean matchedFilter(FluidStack drained, @Nonnull StellarFluidConduit con, @Nonnull EnumFacing conDir, boolean isInput) {
        if (drained == null) {
            return false;
        }
        IFluidFilter filter = con.getFilter(conDir, isInput);
        if (filter == null || filter.isEmpty()) {
            return true;
        }
        return filter.matchesFilter(drained);
    }

    private RoundRobinIterator<NetworkTank> getIteratorForTank(@Nonnull NetworkTank tank) {
        if (iterators == null) {
            iterators = new HashMap<NetworkTank, RoundRobinIterator<NetworkTank>>();
        }
        RoundRobinIterator<NetworkTank> res = iterators.get(tank);
        if (res == null) {
            res = new RoundRobinIterator<NetworkTank>(tanks);
            iterators.put(tank, res);
        }
        return res;
    }

    public IFluidTankProperties[] getTankProperties(@Nonnull StellarFluidConduit con, @Nonnull EnumFacing conDir) {
        List<IFluidTankProperties> res = new ArrayList<IFluidTankProperties>(tanks.size());
        NetworkTank tank = getTank(con, conDir);
        for (NetworkTank target : tanks) {
            if (!target.equals(tank) && target.isValid()) {
                for (IFluidWrapper.ITankInfoWrapper info : target.getExternalTank().getTankInfoWrappers()) {
                    res.add(info.getIFluidTankProperties());
                }
            }
        }
        return res.toArray(new IFluidTankProperties[res.size()]);
    }

    static class NetworkTankKey {

        EnumFacing conDir;
        BlockPos conduitLoc;

        public NetworkTankKey(@Nonnull StellarFluidConduit con, @Nonnull EnumFacing conDir) {
            this(con.getBundle().getLocation(), conDir);
        }

        public NetworkTankKey(@Nonnull BlockPos conduitLoc, @Nonnull EnumFacing conDir) {
            this.conDir = conDir;
            this.conduitLoc = conduitLoc;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((conDir == null) ? 0 : conDir.hashCode());
            result = prime * result + ((conduitLoc == null) ? 0 : conduitLoc.hashCode());
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
            NetworkTankKey other = (NetworkTankKey) obj;
            if (conDir != other.conDir) {
                return false;
            }
            if (conduitLoc == null) {
                if (other.conduitLoc != null) {
                    return false;
                }
            } else if (!conduitLoc.equals(other.conduitLoc)) {
                return false;
            }
            return true;
        }

    }

}
