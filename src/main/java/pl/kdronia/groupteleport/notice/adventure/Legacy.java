package pl.kdronia.groupteleport.notice.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;
import java.util.stream.Collectors;

public class Legacy {

    public static final LegacyComponentSerializer AMPERSAND_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    public final static Component RESET_ITALIC = Component.text()
            .decoration(TextDecoration.ITALIC, false)
            .build();

    public static Component component(String text) {
        return RESET_ITALIC.append(AMPERSAND_SERIALIZER.deserialize(text));
    }

    public static List<Component> component(List<String> texts) {
        return texts.stream()
                .map(Legacy::component)
                .collect(Collectors.toList());
    }

}