package larrytllama.pvcmappermod.mixin.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList.Entry;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public abstract class ServerListMixin extends Screen {

    private ServerListMixin(Component title) {
        super(title);
    }

    @Shadow private ServerList servers;
    @Shadow private ServerSelectionList serverSelectionList;
    StringWidget stringWidget;

    // Mixin into init to set widget size n stuff
    @Inject(method = "init", at = @At("TAIL"))
    private void initMore(CallbackInfo ci) {
        for (int i = 0; i < servers.size(); i++) {
            if(servers.get(i).ip.equals("mc.peacefulvanilla.club")) {
                Entry serverEntry = serverSelectionList.children().get(i);
                Minecraft mc = Minecraft.getInstance();
                MutableComponent timeString = Component.empty();
                try {
                    int actualPlayersOnline = servers.get(i).players.online() - Math.floorDiv(servers.get(i).players.online(), 10);
                    double queueTimeSeconds = (actualPlayersOnline * 2) * (0.75 + (Math.floorDiv(actualPlayersOnline, 10) * 0.25));
                    timeString = Component.literal(String.format("Queue: %d:%s%d", (int)Math.floor(queueTimeSeconds / 60), (queueTimeSeconds%60) < 10 ? "0" : "", (int)Math.floor(queueTimeSeconds % 60))).withStyle(ChatFormatting.GREEN);
                } catch(Exception e) {
                    // Oh well!
                    timeString = Component.literal("Queue: ?:??").withStyle(ChatFormatting.YELLOW);
                }
                stringWidget = new StringWidget(timeString, mc.font);
                stringWidget.setPosition(serverEntry.getContentX() + serverEntry.getContentWidth() - (mc.font.width(timeString) + 5), serverEntry.getContentY() + 4 + (mc.font.lineHeight * 2));
                addRenderableWidget(stringWidget);
            }
        };
    }

    // Update as needed
    
    @Inject(method = "tick", at = @At("TAIL"))
    private void tickMore(CallbackInfo ci) {
        ServerData server = servers.get("mc.peacefulvanilla.club");
        if(server == null || server.players == null) return;
        try {
            int actualPlayersOnline = server.players.online() - Math.floorDiv(server.players.online(), 10);
            double queueTimeSeconds = (actualPlayersOnline * 2) * (0.75 + (Math.floorDiv(actualPlayersOnline, 10) * 0.25));
            String timeString = String.format("Queue: %d:%s%d", (int)Math.floor(queueTimeSeconds / 60), (queueTimeSeconds%60) < 10 ? "0" : "", (int)Math.floor(queueTimeSeconds % 60));
            stringWidget.setMessage(Component.literal(timeString).withStyle(ChatFormatting.GREEN));
        } catch(Exception e) {
            // Who cares
        }
    }
}
