package larrytllama.pvcmappermod;

import larrytllama.pvcmappermod.utils.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import larrytllama.pvcmappermod.mixin.client.TabListMixin;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if <26.1 {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;*///?}
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.network.chat.Component;


public class PVCMapperModClient implements ClientModInitializer {
    public Category MOD_CATEGORY = Category.register(ResIdentifier.of("pvcmappermod", "category").get());
    public KeyMapping OPEN_MAP = new KeyMapping("pvcmappermod.open_map", GLFW.GLFW_KEY_M, MOD_CATEGORY);
    public KeyMapping OPEN_SHOPS = new KeyMapping("pvcmappermod.open_shops", GLFW.GLFW_KEY_COMMA, MOD_CATEGORY);
    public KeyMapping MINIMAP_ZOOM_IN = new KeyMapping("pvcmappermod.minimap_zoom_in", GLFW.GLFW_KEY_EQUAL,
            MOD_CATEGORY);
    public KeyMapping MINIMAP_ZOOM_OUT = new KeyMapping("pvcmappermod.minimap_zoom_out", GLFW.GLFW_KEY_MINUS,
            MOD_CATEGORY);

    public FullScreenMap fsm;
    public ShopsScreen shopsScreen = new ShopsScreen(Component.literal("PVC Mapper - Shops View"));
    public Minimap minimap;
    // Brigadier commands execute synchronously inside ChatScreen's text field handler.
    // Setting screen synchronously gets instantly closed/overwritten by the chat screen closing,
    // so we queue the screen/task to open on the very next client tick instead.
    private static net.minecraft.client.gui.screens.Screen NEXT_SCREEN = null;
    private static Runnable NEXT_TICK_TASK = null;

    public static void setScreenOnNextTick(net.minecraft.client.gui.screens.Screen screen) {
        NEXT_SCREEN = screen;
    }
    public static void setScreenOnNextTick(net.minecraft.client.gui.screens.Screen screen, Runnable callback) {
        NEXT_SCREEN = screen;
        NEXT_TICK_TASK = callback;
    }

    public boolean isInPVC = false;

    private static boolean seenMainMenu = false;

    private float inLevelTicks = 0;
    @Override
    public void onInitializeClient() {
        // Settings provider
        SettingsProvider sp = SettingsProvider.getInstance();
        sp.updateSettings();
        
        // Set up player fetchererer
        PlayerFetchUtils pfu = new PlayerFetchUtils();
        new MapperCmdHandler(pfu, this);
        

        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            if (!seenMainMenu && screen instanceof Screen) {
                seenMainMenu = true;
                // Check for updates
                if(sp.checkForUpdates) {
                    CompletableFuture.runAsync(() -> {
                        pfu.checkForUpdates();
                    });
                }
            }
        });

        this.minimap = Minimap.attach(pfu, sp);

        HttpClient http = HttpClient.newHttpClient();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            if(sp.collectData) {
                CompletableFuture.runAsync(() -> {
                    if(this.minimap.isInQueue || this.minimap.isInTerra2 || this.minimap.isLoadingIn || !this.isInPVC) return; // I love when borrowing old code just works <3
                    String jsonString = "[";
                    ClientPacketListener connection = Minecraft.getInstance().getConnection();
                    if(connection == null) return;
                    Collection<PlayerInfo> players = connection.getOnlinePlayers();
                    for (PlayerInfo player : players) {
                        // Get the player's username
                        String name = player.getProfile().name();
                        String tabListName = player.getTabListDisplayName().toString();
                        jsonString = jsonString + String.format("{\"n\": \"%s\", \"tln\": \"%s\"},", name, tabListName);
                    }
                    
                    // Trim trailing comma otherwise JSON formatting will have a fit
                    // (And add array enderer too)
                    jsonString = jsonString.substring(0, jsonString.length() - 1) + "]";

                    // Hey you! Yes you, the one trying to scan the source code to see if you can change your rank on the mapper.
                    // It's actually pretty impossible to hide this stuff. I researched, but it's pretty impossible.
                    // So you *could* just change your ranks, but if you do:
                    //   A) You'll annoy the heck out of me
                    //   B) Your entries will probably (hopefully) be updated by another client 5s later
                    //   C) You're mean :(
                    // So, eh, please don't. Thanks <3
                    HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(NetworkUtils.API_V2 + "/rank-upload/setPlayerRanks?me=" + Minecraft.getInstance().player.getGameProfile().name()))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                        .header("Content-Type", "application/json")
                        .build();
                    try {
                        http.send(req, HttpResponse.BodyHandlers.ofString());
                    } catch(Exception e) {
                        // No bother, we'll just log to console and ignore!
                        // Who actually cares about HTTP error codes? Nothing wrong with ignoring them! *foreshadowing*
                        LogUtils.error("[PVC Mapper Mod] Oh naur! Uploading data to mapper failed. Here's the error:", e);
                    }
                });
            }
        }, 0, 60000, TimeUnit.MILLISECONDS );

        OPEN_MAP = CompatUtils.registerKey(OPEN_MAP);
        OPEN_SHOPS = CompatUtils.registerKey(OPEN_SHOPS);
        MINIMAP_ZOOM_IN = CompatUtils.registerKey(MINIMAP_ZOOM_IN);
        MINIMAP_ZOOM_OUT = CompatUtils.registerKey(MINIMAP_ZOOM_OUT);
        fsm = FullScreenMap.createScreen(Component.literal("PVC Mapper - Map View"), pfu, sp);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_MAP.consumeClick()) {
                // Alt+M (or Alt+Full-screen-map-key) to hide minimap
                if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_RIGHT_ALT)) {
                    if(sp.miniMapEnabled) sp.miniMapEnabled = false;
                    else sp.miniMapEnabled = true;
                    sp.saveSettings();
                } else { 
                    CompatUtils.setScreen(fsm);
                }
            }

            while (OPEN_SHOPS.consumeClick()) {
                CompatUtils.setScreen(new ShopsScreen(Component.literal("PVC Mapper - Shops View")));
            }

            while (MINIMAP_ZOOM_IN.consumeClick()) {
                if (this.minimap.zoomlevel != 15) {
                    this.minimap.zoomlevel += 1;
                    this.minimap.resetTileImageCache();
                }
            }

            while (MINIMAP_ZOOM_OUT.consumeClick()) {
                if (this.minimap.zoomlevel != 1) {
                    this.minimap.zoomlevel -= 1;
                    this.minimap.resetTileImageCache();
                }
            }

            // Open queued screen or run queued task
            if (NEXT_SCREEN != null) {
                CompatUtils.setScreen(client, NEXT_SCREEN);
                NEXT_SCREEN = null;
                if (NEXT_TICK_TASK != null) {
                    NEXT_TICK_TASK.run();
                    NEXT_TICK_TASK = null;
                }
            }
        });


        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            if (client.level == null) return;
            inLevelTicks++;
            if(inLevelTicks == 40) {
                inLevelTicks = 0;
                // Janky AF Terra2 detector with the tab list (of all things)
                PlayerTabOverlay tabList = Minecraft.getInstance().gui.
                    //? if >=26.2 {
                    /*hud.*/
                    //?}
                    getTabList();
                // Check against IP if in server
                if( Minecraft.getInstance().getConnection().getServerData() != null && 
                    Minecraft.getInstance().getConnection().getServerData().ip.contains("peacefulvanilla.club")) { // Just in case they change the subdomain
                    // If null, we're in Terra2
                    if(((TabListMixin) tabList).getHeader() == null || ((TabListMixin) tabList).getFooter() == null) {
                        minimap.isInTerra2 = true;
                        minimap.isInQueue = false;
                    // Missing footer text, in Queue
                    } else if(!((TabListMixin) tabList).getFooter().getString().contains("Visit the website for more info")) {
                        minimap.isInQueue = true;
                        minimap.isInTerra2 = false;
                    // None of the above, in Mondo
                    } else {
                        minimap.isInQueue = false;
                        minimap.isInTerra2 = false;
                    }
                    isInPVC = true;
                } else {
                    // Just assume they want mondo, I don't care lol
                    minimap.isInQueue = false;
                    minimap.isInTerra2 = false;
                    isInPVC = false; // Prevent sending ranks when not in server
                }
                minimap.isLoadingIn = false;
            }
        });
        ClientPlayConnectionEvents.JOIN.register((a, b, c) -> {
            minimap.isInQueue = false;
            minimap.isInTerra2 = false;
            minimap.isLoadingIn = true;
            pfu.startUpdates();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            pfu.stopUpdates();
        });
    }
}
