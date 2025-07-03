package wily.legacy.mixin.base;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.client.LegacyOptions;

@Mixin(ItemPickupParticle.class)
public abstract class ItemPickupParticleMixin extends Particle {
    @Shadow private int life;
    //? if >1.20.2 {
    @Shadow private double targetY;
    //?}
    @Shadow @Final private Entity target;
    @Unique
    private int lifetime = 3;

    protected ItemPickupParticleMixin(ClientLevel clientLevel, double d, double e, double f) {
        super(clientLevel, d, e, f);
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void init(CallbackInfo info) {
        if (LegacyOptions.legacyItemPickup.get()) lifetime += level.random.nextInt(8);
    }

    //? if <1.21.4 {
    @ModifyVariable(method = "render", at = @At(value = "STORE", ordinal = 0), index = 4)
    //?} else {
    /*@ModifyVariable(method = "renderCustom", at = @At("STORE"), index = 5)
    *///?}
    private float render(float original, @Local(ordinal = 0, argsOnly = true) float partialTick) {
        return LegacyOptions.legacyItemPickup.get() ? ((float) this.life + partialTick) / lifetime : original;
    }

    //? if <=1.20.2 {
    /*@ModifyArg(method = "render", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraft/util/Mth;lerp(DDD)D"), index = 2)
    private double render(double d) {
        return LegacyOptions.legacyItemPickup.get() ? target.getY() : d;
    }
    *///?} else {
    @Inject(method = "updatePosition", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ItemPickupParticle;targetY:D", shift = At.Shift.AFTER))
    private void updatePosition(CallbackInfo ci) {
        if (LegacyOptions.legacyItemPickup.get()) targetY = target.getY();
    }
    //?}
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(CallbackInfo ci) {
        if (LegacyOptions.legacyItemPickup.get()) {
            ci.cancel();
            ++this.life;
            if (this.life == lifetime) {
                this.remove();
            }
        }
    }

}
