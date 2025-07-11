package wily.legacy.mixin.base.client;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wily.legacy.Legacy4J;
import wily.legacy.Legacy4JClient;
import wily.legacy.client.GlobalPacks;
import wily.legacy.client.LegacyOptions;
import wily.legacy.client.PackAlbum;

import java.util.function.BooleanSupplier;

@Mixin(Options.class)
public abstract class OptionsMixin {

    @Shadow protected Minecraft minecraft;

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;<init>(Ljava/lang/String;ILjava/lang/String;)V", ordinal = 5),index = 0)
    protected String initKeyCraftingName(String string) {
        return "legacy.key.inventory";
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;<init>(Ljava/lang/String;ILjava/lang/String;)V", ordinal = 5),index = 1)
    protected int initKeyCrafting(int i) {
        return 73;
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;<init>(Ljava/lang/String;Lnet/minecraft/client/OptionInstance$TooltipSupplier;Lnet/minecraft/client/OptionInstance$CaptionBasedToString;Lnet/minecraft/client/OptionInstance$ValueSet;Ljava/lang/Object;Ljava/util/function/Consumer;)V", ordinal = 8),index = 4)
    protected Object initChatSpacingOption(Object object) {
        return 1.0d;
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ToggleKeyMapping;<init>(Ljava/lang/String;ILjava/lang/String;Ljava/util/function/BooleanSupplier;)V", ordinal = 0),index = 3)
    protected BooleanSupplier initKeyShift(BooleanSupplier booleanSupplier) {
        return ()-> (minecraft == null || minecraft.player == null || (!minecraft.player.getAbilities().flying && minecraft.player.getVehicle() == null && (!minecraft.player.isInWater() || minecraft.player.onGround()) && !minecraft.player./*? if >=1.21 {*/getInBlockState/*?} else {*//*getFeetBlockState*//*?}*/().is(Blocks.SCAFFOLDING))) && (booleanSupplier.getAsBoolean() && !Legacy4JClient.controllerManager.isControllerTheLastInput() || LegacyOptions.controllerToggleCrouch.get() && Legacy4JClient.controllerManager.isControllerTheLastInput());
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ToggleKeyMapping;<init>(Ljava/lang/String;ILjava/lang/String;Ljava/util/function/BooleanSupplier;)V", ordinal = 1),index = 3)
    protected BooleanSupplier initKeySprint(BooleanSupplier booleanSupplier) {
        return ()-> booleanSupplier.getAsBoolean() && !Legacy4JClient.controllerManager.isControllerTheLastInput() || LegacyOptions.controllerToggleSprint.get() && Legacy4JClient.controllerManager.isControllerTheLastInput();
    }

    @Inject(method = "loadSelectedResourcePacks",at = @At("HEAD"), cancellable = true)
    private void loadSelectedResourcePacks(PackRepository packRepository, CallbackInfo ci){
        PackAlbum.init();
        GlobalPacks.STORAGE.load();
        if (Legacy4J.isNewerVersion("1.8.0.2518.5", LegacyOptions.lastLoadedVersion.get()) && !LegacyOptions.lastLoadedVersion.isEmpty() && !GlobalPacks.globalResources.get().list().contains("legacy:legacy_resources"))
            GlobalPacks.globalResources.set(GlobalPacks.globalResources.get().withPacks(ImmutableList.<String>builder().addAll(GlobalPacks.globalResources.get().list()).add("legacy:legacy_resources").build()));
        GlobalPacks.globalResources.get().applyPacks(packRepository, PackAlbum.getDefaultResourceAlbum().packs());
        PackAlbum.updateSavedResourcePacks();
        ci.cancel();
    }

}
