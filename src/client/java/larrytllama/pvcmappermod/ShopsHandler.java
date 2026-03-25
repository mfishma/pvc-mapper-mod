package larrytllama.pvcmappermod;

import java.net.URI;
import java.util.Locale;

import com.google.gson.Gson;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Scanner;

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

    public static Shop[] shopsByItem(String item) {
        try (Scanner scanner = new Scanner(new URI("https://pvc.coolwebsite.uk/pvc-utils/tradesByItem/" + item).toURL().openStream(), "UTF-8")) {
            String out = scanner.useDelimiter("\\A").next();
            Gson gson = new Gson();
            Shop[] shops = gson.fromJson(out, Shop[].class);
            return shops;
        } catch(Exception e) {
            return new Shop[0];
        }
    }

    public static Shop[] shopsByPlayer(String username) {
        try {
        System.out.println(new URI("https://pvc.coolwebsite.uk/pvc-utils/tradesByPlayer/" + username).toURL().toString());
        } catch(Exception e) {
            // Whatevs
            System.out.println(e);
            e.printStackTrace();
        }
        try (Scanner scanner = new Scanner(new URI("https://pvc.coolwebsite.uk/pvc-utils/tradesByPlayer/" + username).toURL().openStream(), "UTF-8")) {
            String out = scanner.useDelimiter("\\A").next();
            Gson gson = new Gson();
            Shop[] shops = gson.fromJson(out, Shop[].class);
            System.out.println(shops.length);
            return shops;
        } catch(Exception e) {
            
            System.out.println(e);
            e.printStackTrace();
            return new Shop[0];
        }
    }

    // Possible use later. I'll see if I fancy it
    public static ShopsPlayerStats shopsStatsByPlayer(String username) {
        try (Scanner scanner = new Scanner(new URI("https://pvc.coolwebsite.uk/pvc-utils/playerStats/" + username).toURL().openStream(), "UTF-8")) {
            String out = scanner.useDelimiter("\\A").next();
            Gson gson = new Gson();
            ShopsPlayerStats shops = gson.fromJson(out, ShopsPlayerStats.class);
            return shops;
        } catch(Exception e) {
            return new ShopsPlayerStats();
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