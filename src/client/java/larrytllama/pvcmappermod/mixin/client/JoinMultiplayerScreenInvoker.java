package larrytllama.pvcmappermod.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;

@Mixin (JoinMultiplayerScreen.class)
public interface JoinMultiplayerScreenInvoker {
    @Invoker ("join") // method name may differ by mappings; adjust if needed
    void invokeConnect(ServerData serverData);
}