package dev.arsfusion.mixin;
import dev.arsfusion.bow.BowCasting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import java.util.function.Predicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Player.class)
public abstract class BowAmmoMixin {
    @Redirect(method="getProjectile",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ProjectileWeaponItem;getSupportedHeldProjectiles(Lnet/minecraft/world/item/ItemStack;)Ljava/util/function/Predicate;",remap=false))
    private Predicate<ItemStack> arsFusion$held(ProjectileWeaponItem item,ItemStack weapon) {return BowCasting.ammo(item.getSupportedHeldProjectiles(weapon),weapon);}
    @Redirect(method="getProjectile",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ProjectileWeaponItem;getAllSupportedProjectiles(Lnet/minecraft/world/item/ItemStack;)Ljava/util/function/Predicate;",remap=false))
    private Predicate<ItemStack> arsFusion$inventory(ProjectileWeaponItem item,ItemStack weapon) {return BowCasting.ammo(item.getAllSupportedProjectiles(weapon),weapon);}
}
