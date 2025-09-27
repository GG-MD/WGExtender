package wgextender.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class ColorUtil {

    private static final MiniMessage miniMessage = MiniMessage.builder().build();

    public static Component deserialize(String string) {
        if (string == null) {
            return Component.empty();
        }
        return miniMessage.deserialize(string).decoration(TextDecoration.ITALIC, false);
    }

    public static List<Component> deserialize(List<String> strings) {
        if (strings == null) {
            return new ArrayList<>();
        }
        List<Component> components = new ArrayList<>();
        for (String s : strings) {
            components.add(deserialize(s));
        }
        return components;
    }

    public static String serialize(Component component) {
        if (component == null) {
            return "";
        }
        return miniMessage.serialize(component);
    }
}
