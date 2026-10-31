package dev.arsfusion.mixin;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.objectweb.asm.tree.ClassNode;
import java.util.List;
import java.util.Set;
public final class FusionMixinPlugin implements IMixinConfigPlugin {
    @Override public boolean shouldApplyMixin(String target,String mixin) {
        if(!mixin.endsWith(".HighArmorJeiMixin"))return true;
        var mods=net.minecraftforge.fml.loading.FMLLoader.getLoadingModList();return mods!=null&&mods.getModFileById("jei")!=null;
    }
    @Override public void onLoad(String packageName) { }
    @Override public String getRefMapperConfig() {return null;}
    @Override public void acceptTargets(Set<String> own,Set<String> others) { }
    @Override public List<String> getMixins() {return null;}
    @Override public void preApply(String target,ClassNode node,String mixin,IMixinInfo info) { }
    @Override public void postApply(String target,ClassNode node,String mixin,IMixinInfo info) { }
}
