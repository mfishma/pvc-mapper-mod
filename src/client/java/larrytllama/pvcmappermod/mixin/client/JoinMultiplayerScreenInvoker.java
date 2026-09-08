package larrytllama.pvcmappermod.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;

@Mixin (JoinMultiplayerScreen.class)
public interface JoinMultiplayerScreenInvoker {
    // Join the thing
    @Invoker ("join")
    void invokeConnect(ServerData serverData);
}