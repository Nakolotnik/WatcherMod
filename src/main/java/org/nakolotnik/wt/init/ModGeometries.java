package org.nakolotnik.wt.init;

import org.zeith.hammeranims.api.geometry.IGeometryContainer;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;

@SimplyRegister
public interface ModGeometries {
    @RegistryName("watcher")
    IGeometryContainer WATCHER = IGeometryContainer.createNoSuffix();
}
