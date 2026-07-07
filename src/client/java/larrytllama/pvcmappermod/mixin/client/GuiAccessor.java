package larrytllama.pvcmappermod.mixin.client;


import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * In 26.1+, the `tabList` field in the `Gui` class became private.
 * This Mixin Accessor grants the mod permission to retrieve the 
 * `PlayerTabOverlay` to check the current player list (used for the Terra2 detector).
 * 
 * Note: This file is wrapped in a Stonecutter conditional block, so it only
 * compiles the Mixin interface when building for 26.1 and newer versions. 
 * For older versions, the file remains essentially empty and is ignored by the compiler.
 */
//? if >=26.1 {
/*@Mixin(Gui.class)
public interface GuiAccessor {
    @Accessor("tabList")
    PlayerTabOverlay getTabList();
}*///?}
