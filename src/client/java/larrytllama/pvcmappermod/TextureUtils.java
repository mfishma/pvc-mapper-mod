package larrytllama.pvcmappermod;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ConcurrentHashMap;

public class TextureUtils {
    // Limits background tile downloads to prevent Cloudflare rate-limiting.
    // Increased to 16 from 7-8 because the Stagger logic below prevents bursts. Could try higher.
    private static final int MAX_CONCURRENT_DOWNLOADS = 16;
    
    // Global stagger to prevent Cloudflare/Origin challenges during initialization's fetches.
    private static final java.util.concurrent.atomic.AtomicLong lastFetchTime = new java.util.concurrent.atomic.AtomicLong(0);
    private static final int FETCH_STAGGER_MS = 50;

    // LIFO ThreadPool: Prioritizes the most recently requested tiles over older, off-screen tiles.
    private static final ExecutorService downloadExecutor = new ThreadPoolExecutor(
            MAX_CONCURRENT_DOWNLOADS, MAX_CONCURRENT_DOWNLOADS, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<Runnable>() {
                @Override
                public boolean offer(Runnable e) {
                    return super.offerFirst(e);
                }
            }
    );
    private static final ConcurrentHashMap<String, Long> failedFetches = new ConcurrentHashMap<>();
    
    static ResourceLocation blurredTile = ResourceLocation.fromNamespaceAndPath("pvcmappermod","textures/gui/tileloading.png");

    /**
     * Fetches an image from a URL, converts it to a DynamicTexture, registers it,
     * and returns the ResourceLocation via a callback. Uses the bounded download pool.
     *
     * @param urlString The URL of the image.
     * @param callback  The callback to run when the texture is ready.
     */
    public static void fetchRemoteTexture(String urlString, Consumer<ResourceLocation> callback) {
        Long failedTime = failedFetches.get(urlString);
        if (failedTime != null && (System.currentTimeMillis() - failedTime < 10000)) {
            // Already failed recently. Use blurred tile silently.
            callback.accept(blurredTile);
            return;
        }

        System.out.println("Fetching image from source: " + urlString);
        downloadExecutor.submit(() -> {
            executeFetch(urlString, callback);
        });
    }

    /**
     * Instantly fetches an image, bypassing the background tile queue.
     * Intended exclusively for UI elements like POI overlays and banners.
     */
    public static void fetchImmediateRemoteTexture(String urlString, Consumer<ResourceLocation> callback) {
        System.out.println("Fetching immediate UI image from source: " + urlString);
        CompletableFuture.runAsync(() -> {
            executeFetch(urlString, callback);
        });
    }

    private static void executeFetch(String urlString, Consumer<ResourceLocation> callback) {
        // Apply Global Stagger to avoid overwhelming the server/Cloudflare with bursts from initialization
        try {
            long now = System.currentTimeMillis();
            long nextTime = lastFetchTime.updateAndGet(last -> Math.max(last + FETCH_STAGGER_MS, now));
            long sleepTime = nextTime - now;
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException ignored) {}

        try {
            String targetUrl = urlString.startsWith("https://") ? urlString : "https://pvc.coolwebsite.uk" + urlString;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(targetUrl))
                    .GET()
                    .build();

            // Use InputStream handler for memory efficiency (pipes directly to NativeImage)
            HttpResponse<InputStream> response = NetworkUtils.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 404) {
                Minecraft.getInstance().execute(() -> callback.accept(blurredTile));
                return;
            }
            
            if (response.statusCode() != 200) {
                System.out.println("[TextureUtils Error] Failed to fetch tile: " + urlString + " (HTTP " + response.statusCode() + ")");
                failedFetches.put(urlString, System.currentTimeMillis());
                Minecraft.getInstance().execute(() -> callback.accept(blurredTile)); // using blurredTile so we don't return null
                return;
            }

            // Check if Cloudflare served an explicit HTML challenge instead of an image
            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (contentType.contains("text/html")) {
                handleSoftRateLimit(urlString, "Cloudflare Challenge", callback);
                return;
            }

            NativeImage image;
            // Download and read the image off-thread
            try (InputStream input = response.body()) {
                image = NativeImage.read(input);
            }

            // Schedule registration on the main render thread
            NativeImage finalImage = image;
            Minecraft.getInstance().execute(() -> {
                String idStr = "remote_" + Integer.toHexString(urlString.hashCode());
                DynamicTexture dynamicTexture = new DynamicTexture(() -> idStr, finalImage);
                //dynamicTexture
                ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath("pvcmappermod", idStr);
                Minecraft.getInstance().getTextureManager().register(resourceLocation, dynamicTexture);
                callback.accept(resourceLocation);
                failedFetches.remove(urlString); // clear cache on success
            });

        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Bad PNG Signature")) {
                handleSoftRateLimit(urlString, "Fake Image Header", callback);
                return;
            }

            System.err.println("[TextureUtils Error] Exception while fetching URL: " + urlString);
            Minecraft.getInstance().execute(() -> callback.accept(null));
            e.printStackTrace();
        }
    }

    /**
     * Handles Cloudflare "Soft Rate Limits" by applying a backoff and serving a placeholder.
     */
    private static void handleSoftRateLimit(String urlString, String reason, Consumer<ResourceLocation> callback) {
        System.err.println("[TextureUtils] Soft Rate Limit Triggered (" + reason + "): " + urlString);
        // Back off for 20 seconds for this specific URL
        failedFetches.put(urlString, System.currentTimeMillis() + 20000);
        Minecraft.getInstance().execute(() -> callback.accept(blurredTile));
    }
}
