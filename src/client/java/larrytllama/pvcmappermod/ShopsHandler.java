package larrytllama.pvcmappermod;

import java.net.URI;
import java.util.Locale;

import com.google.gson.Gson;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.concurrent.CompletableFuture;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ShopsHandler {
    public static SuggestionProvider<FabricClientCommandSource> ITEM_SUGGESTIONS =
    (context, builder) -> {
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
                String suggestion = id.getPath().toUpperCase(Locale.ROOT);
                builder.suggest(suggestion);
            }
        }

        List<String> specialItems = List.of("PLAYTIME_CERTIFICATE", "TERRA2_PLAYTIME_CERT", "INACTIVITY_CERTIFICATE", "VOTE_DIAMOND", "DONOR_VOUCHER",
            "SKIP_THE_QUEUE_TICKET", "BULK_TOTEM_VOUCHER", "INACTIVITY_TICKET", "NICKNAME_VOUCHER","HARD_SURVIVAL_TOKEN", "WOMP_WOMP", "SPONSOR_TOKEN", 
            "WRITER_QUILL", "EVENT_COIN", "TRICK_OR_TREAT_COIN", "GCU_COIN", "NERCHIUS_POOP", "THE_BIG_CLAIM", "SLOT_MACHINE_TOKEN", "VOUCHER_CREDITS_TOKEN",
            "LAPIS_CLAIM", "COAL_CLAIM", "REDSTONE_CLAIM", "EMERALD_CLAIM", "DIAMOND_CLAIM", "CHUNK_CLAIM", "HARD_SURVIVAL_CLAIM", "NOTCH_TEMPLE_APPLE", 
            "QUARRY_EXTRACTOR_FUEL", "NEPERO", "BANK_NOTE", "CADDOZZO", "$LLAMACOIN");

        for (String itemString : specialItems) {
            builder.suggest(itemString);
        }
        return builder.buildFuture();
    };

    public static ArrayList<String> getAllItems() {
        ArrayList<String> items = new ArrayList<String>();
        items.addAll(List.of("PLAYTIME_CERTIFICATE", "TERRA2_PLAYTIME_CERT", "INACTIVITY_CERTIFICATE", "VOTE_DIAMOND", "DONOR_VOUCHER",
            "SKIP_THE_QUEUE_TICKET", "BULK_TOTEM_VOUCHER", "INACTIVITY_TICKET", "NICKNAME_VOUCHER","HARD_SURVIVAL_TOKEN", "WOMP_WOMP", "SPONSOR_TOKEN", 
            "WRITER_QUILL", "EVENT_COIN", "TRICK_OR_TREAT_COIN", "GCU_COIN", "NERCHIUS_POOP", "THE_BIG_CLAIM", "SLOT_MACHINE_TOKEN", "VOUCHER_CREDITS_TOKEN",
            "LAPIS_CLAIM", "COAL_CLAIM", "REDSTONE_CLAIM", "EMERALD_CLAIM", "DIAMOND_CLAIM", "CHUNK_CLAIM", "HARD_SURVIVAL_CLAIM", "NOTCH_TEMPLE_APPLE", 
            "QUARRY_EXTRACTOR_FUEL", "NEPERO", "BANK_NOTE", "CADDOZZO", "$LLAMACOIN"));

        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
                String itemm = id.getPath().toUpperCase(Locale.ROOT);
                items.add(itemm);
            }
        }
        Collections.sort(items);
        return items;
    }

    public static CompletableFuture<Shop[]> shopsByItemAsync(String item) {
        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("https://pvc.coolwebsite.uk/pvc-utils/tradesByItem/" + item))
                .GET().build();
            return NetworkUtils.HTTP_CLIENT.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        Gson gson = new Gson();
                        Shop[] shops = gson.fromJson(response.body(), Shop[].class);
                        return shops;
                    }
                    return new Shop[0];
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return new Shop[0];
                });
        } catch(Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(new Shop[0]);
        }
    }

    public static CompletableFuture<Shop[]> shopsByPlayerAsync(String username) {
        try {
            System.out.println("Fetching shops for " + username);
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("https://pvc.coolwebsite.uk/pvc-utils/tradesByPlayer/" + username))
                .GET().build();
            return NetworkUtils.HTTP_CLIENT.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        Gson gson = new Gson();
                        Shop[] shops = gson.fromJson(response.body(), Shop[].class);
                        System.out.println(shops.length);
                        return shops;
                    }
                    return new Shop[0];
                })
                .exceptionally(e -> {
                    System.out.println(e);
                    e.printStackTrace();
                    return new Shop[0];
                });
        } catch(Exception e) {
            System.out.println(e);
            e.printStackTrace();
            return CompletableFuture.completedFuture(new Shop[0]);
        }
    }

    // Possible use later. I'll see if I fancy it
    public static CompletableFuture<ShopsPlayerStats> shopsStatsByPlayerAsync(String username) {
        try {
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(new URI("https://pvc.coolwebsite.uk/pvc-utils/playerStats/" + username))
                .GET().build();
            return NetworkUtils.HTTP_CLIENT.sendAsync(request, java.net.http.HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        Gson gson = new Gson();
                        ShopsPlayerStats stats = gson.fromJson(response.body(), ShopsPlayerStats.class);
                        return stats;
                    }
                    return new ShopsPlayerStats();
                })
                .exceptionally(e -> {
                    e.printStackTrace();
                    return new ShopsPlayerStats();
                });
        } catch(Exception e) {
            e.printStackTrace();
            return CompletableFuture.completedFuture(new ShopsPlayerStats());
        }
    }

    public static String IDToPrettyName(String itemID) {
        String allLowerCase = itemID.replaceAll("_", " ").toLowerCase();
        return allLowerCase.substring(0, 1).toUpperCase() + allLowerCase.substring(1);
    }
}

class Shop {
    String currency;
    String currency2;
    String customName;
    Boolean inShulker;
    String item;
    String location;
    double normalisedPrice;
    Integer price;
    Integer price2;
    String shopHash;
    String shopName;
    String shopOwner;
    Integer stock;
    Integer tradeAmount;
}

class ShopsPlayerStats {
    double inStockRatio;
    Integer shops;
    Integer trades;
    Integer uniqueItemsOnSale;
}