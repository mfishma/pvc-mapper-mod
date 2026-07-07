package larrytllama.pvcmappermod.utils;

import larrytllama.pvcmappermod.*;

import larrytllama.pvcmappermod.utils.ResIdentifier;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class PlayerSkinHelper {

    public static final ResIdentifier DEFAULT_SKIN =
            ResIdentifier.of("minecraft", "textures/entity/player/wide/steve.png");

    public static void fetchSkin(String uuidStr, String source, Consumer<ResIdentifier> callback) {
        if (SettingsProvider.getInstance().debugMode) {
            LogUtils.debug("[%s] Fetching skin for UUID: %s", source, uuidStr);
        }

        Minecraft mc = Minecraft.getInstance();

        try {
            UUID dashed = UUID.fromString(
                    uuidStr.substring(0, 8) + "-" +
                            uuidStr.substring(8, 12) + "-" +
                            uuidStr.substring(12, 16) + "-" +
                            uuidStr.substring(16, 20) + "-" +
                            uuidStr.substring(20));
            ResolvableProfile resolvable = ResolvableProfile.createUnresolved(dashed);
            resolvable.resolveProfile(Minecraft.getInstance().services().profileResolver()).thenAccept((resolvedProfile) -> {
                CompletableFuture<Optional<PlayerSkin>> skin = mc.getSkinManager().get(resolvedProfile);
                skin.thenAccept(playerSkin -> {
                    // Fallback to steeeeeeeeeve
                    if (playerSkin.isEmpty()) callback.accept(DEFAULT_SKIN);
                    else callback.accept(ResIdentifier.of(playerSkin.get().body().texturePath()));
                });
            });
        } catch (Exception e) {
            LogUtils.error("[%s] Failed to parse UUID for skin fetch: %s", source, uuidStr);
            callback.accept(DEFAULT_SKIN);
        }
    }
}
