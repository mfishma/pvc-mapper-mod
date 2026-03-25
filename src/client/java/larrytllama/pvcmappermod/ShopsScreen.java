package larrytllama.pvcmappermod;

import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.glfw.GLFW;

import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class ShopsScreen extends Screen {
    Shop[] shops;
    MyList list;
    EditBox itemSearch;
    ArrayList<String> itemsList;
    Button searchBtn;

    Shop displayedTrade;

    ResourceLocation sponsorBanner;
    List<MutableComponent> sponsorHoverText;
    String sponsorURLString;
    Boolean drawSponsorTooltip = false; 

    String currentSearchMode = "item";

    public ShopsScreen(Component title) {
        super(title);
        itemsList = ShopsHandler.getAllItems();
    }

    String[] filteredItems = new String[0];

    @Override
    protected void init() {
        // Get width
        int panelWidth = 100;
        Minecraft minecraft = Minecraft.getInstance();
        list = new MyList(minecraft, panelWidth + MyList.SCROLLBAR_WIDTH, this.height - 60, 0, 20);
        list.setPosition(10, 10);
        this.addRenderableWidget(list);

        itemSearch = new EditBox(minecraft.font, (this.width) - 200, this.height - 35, 160, 20,
                Component.literal("Search for items..."));
        itemSearch.setHint(Component.literal("E.G PLAYTIME_CERTIFICATE"));
        itemSearch.setMaxLength(64);
        itemSearch.setResponder((text) -> {
            // Get a list of filtered items, up to 5 of em
            filteredItems = new String[5];
            int filledIndex = 0;
            for (int i = 0; i < itemsList.size(); i++) {
                if (itemsList.get(i).toLowerCase().startsWith(text.toLowerCase())) {
                    filteredItems[filledIndex] = itemsList.get(i);
                    filledIndex++;
                    if (filledIndex == 5) break;
                }
            }
        });
        this.addRenderableWidget(itemSearch);

        searchBtn = new Button.Builder(Component.empty(), (b) -> {
            if(currentSearchMode.equals("item")) this.openWithItem(this.itemSearch.getValue());
            else if(currentSearchMode.equals("username")) this.openWithUsername(this.itemSearch.getValue());
        }).bounds( this.width - 35, this.height - 35, 20, 20).build();

        this.addRenderableWidget(searchBtn);

        CycleButton<String> searchMode = CycleButton.builder((String value) -> Component.literal(value))
            .withValues("item", "username")
            .create(
                this.width - 20 - minecraft.font.width("Search by: username"), 
                this.height - 50 - minecraft.font.lineHeight, 
                minecraft.font.width("Search by: username") + 10, minecraft.font.lineHeight + 10, 
                Component.literal("Search by"), (btn, value) -> {
                    currentSearchMode = value;
                    switch (value) {
                        case "item":
                            itemSearch.setHint(Component.literal("e.g PLAYTIME_CERTIFICATE"));
                            break;
                        case "username":
                            itemSearch.setHint(Component.literal("e.g .LarryTLlama"));
                            break;
                        default:
                            itemSearch.setHint(Component.literal("Search items or players"));
                            break;
                    }
                } 
            );
        this.addRenderableWidget(searchMode);

        CompletableFuture.runAsync(() -> {
            SponsorBanner banner = SponsorUtils.getBanner();
            SponsorUtils.bannerToTexture(banner.imgurl, (rl) -> {
                this.sponsorBanner = rl;
            });
            sponsorHoverText = List.of(
                    Component.literal(banner.title).withStyle(ChatFormatting.BOLD)
                            .append(Component.literal(" (Click to view)").withStyle(ChatFormatting.GRAY)),
                    Component.literal(banner.description));
            sponsorURLString = banner.link;
        });
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.key() == GLFW.GLFW_KEY_ESCAPE) {
            if(itemSearch.isFocused()) itemSearch.setFocused(false);
            else Minecraft.getInstance().setScreen(null);
        } else if(itemSearch.isFocused()) {
            if(keyEvent.key() == GLFW.GLFW_KEY_TAB) {
                itemSearch.setValue(filteredItems[0]);
            } else if(keyEvent.key() == GLFW.GLFW_KEY_ENTER) {
                if(currentSearchMode.equals("item")) this.openWithItem(this.itemSearch.getValue());
                else if(currentSearchMode.equals("username")) this.openWithUsername(this.itemSearch.getValue());
                itemSearch.setFocused(false);
            }
        }
        return super.keyPressed(keyEvent);
    }

    ResourceLocation SEARCH_ICON = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/icon/search.png");

    public void drawTooltip(GuiGraphics context, List<MutableComponent> content, int x, int y) {
        int lines = content.size();
        Font mcfont = Minecraft.getInstance().font;
        int maxSize = 0;
        for (int i = 0; i < lines; i++) {
            int w = mcfont.width(content.get(i));
            if (w > maxSize) {
                maxSize = w;
            }
        }
        TooltipRenderUtil.renderTooltipBackground(context, x, y, maxSize, mcfont.lineHeight * lines, null);
        for (int i = 0; i < lines; i++) {
            context.drawString(
                    mcfont,
                    content.get(i),
                    x,
                    y + (i * mcfont.lineHeight),
                    0xFFFFFFFF, false);
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (sponsorBanner != null) {
            if (mouseY > (this.height - 35) && mouseY < (this.height - 10) && mouseX > 10 && mouseX < 203) {
                drawSponsorTooltip = true;
            } else {
                drawSponsorTooltip = false;
            }
        } else {
            drawSponsorTooltip = false;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mbe, boolean bl) {
        if (mbe.y() > (this.height - 28) && mbe.y() < (this.height - 3) && mbe.x() > 3 && mbe.x() < 196) {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) {
                    Util.getPlatform().openUri(sponsorURLString);
                }
                mc.setScreen(null);
            }, sponsorURLString, true));
        }
        return super.mouseClicked(mbe, bl);
    }

    Map<String, ResourceLocation> tiles = new HashMap<String, ResourceLocation>();
    ResourceLocation blurredTile = ResourceLocation.fromNamespaceAndPath("pvcmappermod", "textures/gui/tileloading.png");
    ResourceLocation villagerImg = ResourceLocation.fromNamespaceAndPath("pvcmappermod", "textures/gui/villager.png");

    public void fetchTile(int tileX, int tileZ, String dimension) {
        tiles.put(String.format("%s/8/%d_%d", dimension, tileX, tileZ), blurredTile);
        // Make a request for the tile
        String url = String.format("%s%s/8/%d_%d.png",
            "https://pvc.coolwebsite.uk/maps/", dimension, tileX, tileZ);
        TextureUtils.fetchRemoteTexture(url, (id) -> {
            tiles.put(String.format("%s/8/%d_%d", dimension, tileX, tileZ), id);
        });
    }

    // Trace meeeee
    public void drawMap(GuiGraphics graphics, int x, int y, int width, int height, int coordX, int coordZ, String dimension) {
        graphics.enableScissor(x, y, x+width, y+height);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + (width / 2) - (coordX % 512) - 512, y + (height / 2) - (coordZ % 512) - 512);
        for (int iX = 0; iX < 3; iX++) {
            for (int iZ = 0; iZ < 3; iZ++) {

                int tileX = ((int)Math.floor(coordX / 512))+ iX -1;
                int tileZ = ((int)Math.floor(coordZ / 512))+ iZ -1;

                ResourceLocation tile = tiles.get(String.format("%s/8/%d_%d", dimension, tileX, tileZ));
                if(tile == null) {
                    fetchTile(tileX, tileZ, dimension);
                } else {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, tile, (512 * iX), (512 * iZ), 0, 0, 512, 512, 512, 512);
                }

            }
        }
        graphics.pose().popMatrix();
        graphics.blit(RenderPipelines.GUI_TEXTURED, villagerImg, x + (width/2) - 8, y + (height/2) - 8, 0, 0, 16, 16, 16, 16 );
        graphics.disableScissor();
        graphics.submitOutline(x, y, width, height, 0xAA333333);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (list.getSelected() != null) {
            Shop trade = list.getSelected().shopTrade;
        }


        if(displayedTrade != null) {
            int boxWidth = (this.width / 2) - 80; 
            // Left box
            graphics.fill(
                130, 
                30, 
                130 + boxWidth, 
                30 + (minecraft.font.lineHeight * 2) + 10, 0xAA333333); 
            graphics.drawCenteredString(minecraft.font,
                Component.literal("Shop Name: "), 
            130 + (boxWidth/2), 35, 0xFFFFFFFF);
            graphics.drawCenteredString(minecraft.font,
                Component.literal(displayedTrade.shopName).withStyle(ChatFormatting.BOLD), 
            130 + (boxWidth/2), 35 + minecraft.font.lineHeight, 0xFFFFFFFF);
            // Right box
            graphics.fill(
                150 + boxWidth, 
                30, 
                150 + (2 * boxWidth), 
                30 + (minecraft.font.lineHeight * 2) + 10, 0xAA333333); 
            graphics.drawCenteredString(minecraft.font,
                Component.literal("Stocked By: "), 
            150 + boxWidth + (boxWidth/2), 35, 0xFFFFFFFF);
            graphics.drawCenteredString(minecraft.font,
                Component.literal(displayedTrade.shopOwner).withStyle(ChatFormatting.BOLD), 
            150 + boxWidth + (boxWidth/2), 35 + minecraft.font.lineHeight, 0xFFFFFFFF);

            // Beneath that, the location
            graphics.fill(
                130, 
                30 + (minecraft.font.lineHeight * 2) + 20, 
                this.width - 10, 
                30 + (minecraft.font.lineHeight * 3) + 30, 0xAA333333); 

            graphics.drawCenteredString(minecraft.font, 
                Component.literal("Location: ")
                    .append(Component.literal(displayedTrade.location).withStyle(ChatFormatting.BOLD))
                    .append(Component.literal("   -   Stock: "))
                    .append(Component.literal(displayedTrade.stock.toString() + "x").withStyle(ChatFormatting.BOLD)), 130 + ((this.width - 140)/2), 30 + (minecraft.font.lineHeight * 2) + 25, 0xFFFFFFFF);

            String[] split = displayedTrade.location.split(", ");
            String dim = "minecraft_overworld";
            if(split[2].endsWith(" in Nether")) {
                split[2] = split[2].split(" ", 2)[0];
                dim = "minecraft_the_nether";
            }
            drawMap(graphics, 130, 30 + (minecraft.font.lineHeight * 3) + 40, this.width - 140, (this.height - 60 - minecraft.font.lineHeight) - (30 + (minecraft.font.lineHeight * 3) + 30), Integer.parseInt(split[0]), Integer.parseInt(split[2]), dim);
        }

        graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal("Shops Viewer").withStyle(ChatFormatting.BOLD), (this.width/2)+60, 10, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, SEARCH_ICON, this.width - 32, this.height - 32, 0, 0, 12, 12, 12, 12);

        if(itemSearch.isFocused() && currentSearchMode.equals("item")) {
            int y = this.height - 37;
            int maxItemLength = 0;
            for (int i = 0; i < filteredItems.length; i++) {
                if(filteredItems[i] == null) break;
                y -= (minecraft.font.lineHeight + 2);
                maxItemLength = Math.max(maxItemLength, minecraft.font.width(filteredItems[i]));
                graphics.drawString(
                    Minecraft.getInstance().font,
                    Component.literal(itemSearch.getValue()).withStyle(ChatFormatting.WHITE)
                        .append(Component.literal(filteredItems[i].substring(itemSearch.getValue().length()))
                            .withStyle(ChatFormatting.GRAY)), 
                    (this.width) - 198, y, 0xFFFFFFFF);
            }

            graphics.fill((this.width) - 200, y - 2, (this.width) - 200 + maxItemLength + 2, this.height-35, 0xAA000000);
        }

        if (sponsorBanner != null) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, sponsorBanner, 10, this.height - 35, 0, 0, 193, 25, 193, 25);
            if (drawSponsorTooltip) {
                drawTooltip(graphics, sponsorHoverText, mouseX, mouseY);
            }
        } else {
            graphics.drawString(Minecraft.getInstance().font, "Loading banner...", 10, this.height - 30, 0xFFFFFFFF);
        }

        
    }

    public void openWithItem(String itemID) {
        final String item = itemID;
        CompletableFuture
                .supplyAsync(() -> ShopsHandler.shopsByItem(item))
                .thenAccept(shops -> Minecraft.getInstance().execute(() -> {
                    list.removeAllWidgets();
                    for (int i = 0; i < shops.length; i++) {
                        final int index = i;
                        list.addWidget(new MyList.Entry(shops[i], i, 100 - MyList.SCROLLBAR_WIDTH, () -> {
                            displayedTrade = shops[index];
                        }));
                    }
                }));
    }

    public void openWithUsername(String username) {
        final String user = username;
        CompletableFuture
            .supplyAsync(() -> ShopsHandler.shopsByPlayer(user))
            .thenAccept((shops) -> Minecraft.getInstance().execute(() -> {
                list.removeAllWidgets();
                for (int i = 0; i < shops.length; i++) {
                    final int index = i;
                    list.addWidget(new MyList.Entry(shops[i], i, 100 - MyList.SCROLLBAR_WIDTH, () -> {
                        displayedTrade = shops[index];
                    }));
                }
            })
        );
    }
}

// List for left side
class MyList extends ContainerObjectSelectionList<MyList.Entry> {

    public MyList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
        super(minecraft, width, height, y, itemHeight);
    }

    public void addWidget(Entry entry) {
        this.addEntry(entry);
    }

    public void removeWidget(Entry entry) {
        this.removeEntry(entry);
    }

    public void removeAllWidgets() {
        this.clearEntries();
    }
    
    @Override
    public int getRowWidth() {
        return 100 - (MyList.SCROLLBAR_WIDTH * 4);
    }

    

    // List buttons
    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        private Button button;
        public Shop shopTrade;
        // If any of the three items are unknown show...
        private ResourceLocation UNKNOWN_ITEM = ResourceLocation.fromNamespaceAndPath("minecraft",
                "textures/gui/sprites/icon/unseen_notification.png");
        // Here's the arrows too, so we can go full villager-trade mode
        private ResourceLocation TRADE_ARROW = ResourceLocation.fromNamespaceAndPath("minecraft",
                "textures/gui/sprites/container/villager/trade_arrow.png");
        private ResourceLocation TRADE_ARROW_OOS = ResourceLocation.fromNamespaceAndPath("minecraft",
                "textures/gui/sprites/container/villager/trade_arrow_out_of_stock.png");

        public Runnable runWhenActive;
        public int index;

        public Entry(Shop shopTrade, int index, int width, Runnable runWhenActive) {
            this.shopTrade = shopTrade;
            Builder btn = Button.builder(Component.empty(), b -> {
                runWhenActive.run();
            }).width(width);
            MutableComponent tooltiptext = Component.literal(shopTrade.price + "x " + ShopsHandler.IDToPrettyName(shopTrade.currency));
            if(shopTrade.currency2 != null) tooltiptext = tooltiptext.append(Component.literal(" + " + shopTrade.price2 + "x " + ShopsHandler.IDToPrettyName(shopTrade.currency2)));
            tooltiptext = tooltiptext.append(Component.literal(" ➡ "));
            tooltiptext = tooltiptext.append(shopTrade.tradeAmount + "x " + ShopsHandler.IDToPrettyName(shopTrade.item));
            btn.tooltip(
                Tooltip.create(tooltiptext)
            );

            this.button = btn.build();
            this.runWhenActive = runWhenActive;
            this.index = index;
        }

        @Override
        public List<GuiEventListener> children() {
            return List.of(button);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return List.of(); // accessibility narration
        }

        @Override
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            int y = getContentY();
            button.setPosition(13, y);
            button.render(graphics, mouseX, mouseY, delta);

            Optional<Holder.Reference<Item>> item;
            try {
                item = BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:" + shopTrade.currency.toLowerCase()));
                if (item.isEmpty()) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_ITEM, 17, y + 3, 0, 0, 12, 12, 12, 12);
                } else {
                    graphics.renderItem(new ItemStack(item.get(), shopTrade.price), 17, y + 1);
                }
            } catch(Exception e) {
                // If all else fails, I.E the $ in $LLAMACOIN just give the UNKNOWN_ITEM thingy
                graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_ITEM, 17, y + 3, 0, 0, 12, 12, 12, 12);
            }
            if(shopTrade.price > 1) graphics.drawCenteredString(Minecraft.getInstance().font, shopTrade.price.toString(), 30, y + 10, 0xFFFFFFFF);

            if (shopTrade.currency2 != null) {
                // Lets go round again!
                try {
                    Optional<Holder.Reference<Item>> item2 = BuiltInRegistries.ITEM
                            .get(ResourceLocation.parse("minecraft:" + shopTrade.currency2.toLowerCase()));
                    if (item2.isEmpty()) {
                        graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_ITEM, 37, y + 3, 0, 0, 12, 12, 12, 12);
                    } else {
                        graphics.renderItem(new ItemStack(item2.get(), shopTrade.price2), 37, y + 1);
                    }
                } catch(Exception e) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_ITEM, 37, y + 3, 0, 0, 12, 12, 12, 12);
                }
                if(shopTrade.price2 > 1) graphics.drawCenteredString(Minecraft.getInstance().font, shopTrade.price2.toString(), 50, y + 10, 0xFFFFFFFF);
            }

            // Arrow intermission
            if (shopTrade.stock == 0) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TRADE_ARROW_OOS, 59, y + 4, 0, 0, 10, 10, 10, 10);
            } else {
                graphics.blit(RenderPipelines.GUI_TEXTURED, TRADE_ARROW, 59, y + 4, 0, 0, 10, 10, 10, 10);
            }

            // (duh duh duh -duhhh) One More Time!
            try {
                Optional<Holder.Reference<Item>> item3 = BuiltInRegistries.ITEM
                        .get(ResourceLocation.parse("minecraft:" + shopTrade.item.toLowerCase()));
                if (item3.isEmpty()) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_ITEM, 77, y + 3, 0, 0, 12, 12, 12, 12);
                } else {
                    graphics.renderItem(new ItemStack(item3.get(), shopTrade.tradeAmount), 77, y + 1);
                }
            } catch(Exception e) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, UNKNOWN_ITEM, 77, y + 3, 0, 0, 12, 12, 12, 12);
            }
            
            if(shopTrade.tradeAmount > 1) graphics.drawCenteredString(Minecraft.getInstance().font, shopTrade.tradeAmount.toString(), 90, y + 10, 0xFFFFFFFF);
        }

    }
}
