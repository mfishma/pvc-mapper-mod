package larrytllama.pvcmappermod.utils;

//? if <1.21.11 {
import net.minecraft.resources.ResourceLocation;
//?} else {
/*import net.minecraft.resources.Identifier;*/
//?}

public class ResIdentifier {
    //? if <1.21.11 {
    private final ResourceLocation value;
    public ResIdentifier(ResourceLocation value) { this.value = value; }
    public ResourceLocation get() { return value; }
    public static ResIdentifier of(ResourceLocation value) {
        return new ResIdentifier(value);
    }
    public static ResIdentifier of(String namespace, String path) {
        return new ResIdentifier(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
    public static ResIdentifier parse(String string) {
        return new ResIdentifier(ResourceLocation.parse(string));
    }
    //?} else {
    /*private final Identifier value;
    public ResIdentifier(Identifier value) { this.value = value; }
    public Identifier get() { return value; }
    public static ResIdentifier of(Identifier value) {
        return new ResIdentifier(value);
    }
    public static ResIdentifier of(String namespace, String path) {
        return new ResIdentifier(Identifier.fromNamespaceAndPath(namespace, path));
    }
    public static ResIdentifier parse(String string) {
        return new ResIdentifier(Identifier.parse(string));
    }*///?}

    public String getPath() {
        return value.getPath();
    }

    public String getNamespace() {
        return value.getNamespace();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ResIdentifier) {
            return this.value.equals(((ResIdentifier) obj).value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
