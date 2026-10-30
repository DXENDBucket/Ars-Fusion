package dev.arsfusion;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.FusionPlatform;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.bus.api.IEventBus;
@Mod("ars_fusion")
public final class ArsFusion {
    public ArsFusion(IEventBus bus,ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER,FusionConfig.SPEC);
        FusionPlatform.register(bus);
    }
}
