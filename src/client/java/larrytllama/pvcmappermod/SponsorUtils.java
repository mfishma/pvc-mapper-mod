package larrytllama.pvcmappermod;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

class SponsorBanner {
    String title;
    String link;
    String imgurl;
    String description;
}

public class SponsorUtils {
    public static CompletableFuture<SponsorBanner> getBannerAsync() {
        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI(NetworkUtils.API_V1 + "/sponsor-banner"))
                .GET().build();
            LogUtils.debug("Fetching sponsor banner from " + request.uri().toString() + "...");
            return NetworkUtils.HTTP_CLIENT.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        Gson gson = new Gson();
                        SponsorBanner banner = gson.fromJson(response.body(), SponsorBanner.class);
                        return banner;
                    }
                    return new SponsorBanner();
                })
                .exceptionally(e -> {
                    LogUtils.error("Failed to fetch sponsor banners", e);
                    return new SponsorBanner();
                });
        } catch(Exception e) {
            LogUtils.error("Failed to fetch sponsor banners", e);
            return CompletableFuture.completedFuture(new SponsorBanner());
        }
    }

    public static void bannerToTexture(String bannerDataURL, Consumer<ResourceLocation> callback) {
        String b64string = bannerDataURL.replace("data:image/png;base64,", "");
        if(b64string.startsWith("data:")) {
            callback.accept(null); // Wrong file format (not PNG)
            return;
        }
        Decoder decoder = Base64.getDecoder();
        byte[] bytes = decoder.decode(b64string);
        String idString = "sponsorbanner_" + Integer.toHexString(bannerDataURL.hashCode());
        try {
            NativeImage image;
            try (var input = new ByteArrayInputStream(bytes)) {
                image = NativeImage.read(input);
            }
            Minecraft.getInstance().execute(() -> {
                DynamicTexture texture = new DynamicTexture(() -> idString, image);
                ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("pvcmappermod", idString);
                Minecraft.getInstance().getTextureManager().register(resourceLocation, texture);
                callback.accept(resourceLocation);
            });
        } catch(Exception e) {
            LogUtils.error("Error converting data to image content:", e);
            callback.accept(null);
        }

    }
}
