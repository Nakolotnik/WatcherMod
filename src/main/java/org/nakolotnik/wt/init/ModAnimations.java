package org.nakolotnik.wt.init;

import org.zeith.hammeranims.api.animation.*;
import org.zeith.hammerlib.annotations.*;

@SimplyRegister
public interface ModAnimations {
    @RegistryName("watcher")
    IAnimationContainer WATCHER_ANIM = IAnimationContainer.create();
    AnimationHolder WATCHER_IDLE = new AnimationHolder(WATCHER_ANIM, "idle");
}
