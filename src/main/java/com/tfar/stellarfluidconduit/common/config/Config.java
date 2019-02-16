package com.tfar.stellarfluidconduit.common.config;

import crazypants.enderio.base.config.factory.ValueFactoryEIO;
import com.tfar.stellarfluidconduit.ReferenceVariables;

    public final class Config {
        public static final ValueFactoryEIO F = new ValueFactoryEIO(ReferenceVariables.MOD_ID);

        static {
            // force sub-configs to be classloaded with the main config
            StellarFluidConduitConfig.F.getClass();
        }
}
