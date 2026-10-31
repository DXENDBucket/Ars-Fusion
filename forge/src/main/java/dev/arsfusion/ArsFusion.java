package dev.arsfusion;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.FusionPlatform;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
@Mod("ars_fusion")
public final class ArsFusion {
    public ArsFusion() {
        var context=FMLJavaModLoadingContext.get();
        context.registerConfig(ModConfig.Type.SERVER,FusionConfig.SPEC);
        dev.arsfusion.platform.HighArmorPlatform.register(context.getModEventBus());
        FusionPlatform.register(context.getModEventBus());
    }
}
