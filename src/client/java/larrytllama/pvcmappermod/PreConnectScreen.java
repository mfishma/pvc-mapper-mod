package larrytllama.pvcmappermod;

import java.net.URI;
import java.util.List;

import larrytllama.pvcmappermod.utils.CompatUtils;
import net.minecraft.ChatFormatting;
//? if <1.21.11 {
import net.minecraft.Util;
//?} else {
/*import net.minecraft.util.Util;*///?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class PreConnectScreen extends Screen {
    private final Runnable onConfirm;
    private final Screen parent;

    public PreConnectScreen(Screen parent, Runnable onConfirm) {
        super(Component.literal("About to connect"));
        this.parent = parent;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        // Title
        StringWidget title = new StringWidget(Component.literal("Welcome to the PVC Mapper!").withStyle(ChatFormatting.BOLD), Minecraft.getInstance().font);
        title.setPosition((this.width/2) - (Minecraft.getInstance().font.width("Welcome to the PVC Mapper!")/2), 20);
        this.addRenderableWidget(title);

        // Main message
        URI dataUploadsLink;
        try {
            dataUploadsLink = new URI("https://pvc.coolwebsite.uk/help/data-uploading");
        } catch(Exception e) {
            dataUploadsLink = null;
            System.out.println(e);
        }

        MutableComponent messageString = Component.empty().withStyle(Style.EMPTY)
            .append(Component.literal("Hello!").withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)))
            .append(Component.literal(" Thanks for trying the unofficial PVC Mapper Mod! Once in game:\n"))
            .append(Component.literal("- Press M to open the map\n"))
            .append(Component.literal("- Press comma , to open the shops viewer!\n"))
            .append(Component.literal("(You can rebind these keys in the settings).\n"))
            .append(Component.literal("- Use /mapper help to see the mod commands!\n\n"))
            .append(Component.literal("The mod also auto-uploads things like player ranks (taken from the Tab list), nether portal locations, and Terra2 map data (coming soon!). This stuff isn't available from the PVC website, so is contributed by mod users like you!\n"))
            .append(Component.literal("To find out more, Click 'More Info'. Use the checkbox below to enable or disable it.\n\n"))
            .append(Component.literal("Happy Mappering - Larry :)").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
        
        MultiLineTextWidget message = new MultiLineTextWidget(
            messageString, 
            Minecraft.getInstance().font
        );
        message.setMaxWidth(this.width - 40);
        message.setPosition(20, 20 + (Minecraft.getInstance().font.lineHeight*2));
        this.addRenderableWidget(message);

        // Bottom checkboxes and buttons
        Checkbox checkbox = Checkbox.builder(Component.literal("Enable Mapper Uploads"), Minecraft.getInstance().font).selected(true).onValueChange((cb, bl) -> {
            PVCMapperModClient.INSTANCE.sp.collectData = bl;
        }).pos(20, this.height-65).build();
        this.addRenderableWidget(checkbox);

        this.addRenderableWidget(Button.builder(Component.literal("More Info"), btn -> {
            CompatUtils.setScreen(Minecraft.getInstance(), new ConfirmLinkScreen(confirmed -> {
                if (confirmed) {
                    Util.getPlatform().openUri("https://pvc.coolwebsite.uk/help/data-uploading/");
                }
                CompatUtils.setScreen(Minecraft.getInstance(), this);
            }, "https://pvc.coolwebsite.uk/help/data-uploading/", true));
        }).bounds(this.width - 110, this.height-65, 100, 20).build());


        this.addRenderableWidget(Button.builder(Component.literal("Connect to PVC"), btn -> {
            PVCMapperModClient.INSTANCE.sp.shownDataNotice = true;
            PVCMapperModClient.INSTANCE.sp.saveSettings();
            onConfirm.run();
        }).bounds(this.width/2 - 110, this.height-40, 100, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> {
            CompatUtils.setScreen(parent);
        }).bounds(this.width/2 + 10, this.height-40, 100, 20).build());
    }
}