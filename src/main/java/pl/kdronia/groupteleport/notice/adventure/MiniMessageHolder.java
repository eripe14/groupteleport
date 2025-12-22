package pl.kdronia.groupteleport.notice.adventure;

import net.kyori.adventure.text.minimessage.MiniMessage;

public interface MiniMessageHolder {

    MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .preProcessor(new LegacyColorPreProcessor())
            .postProcessor(new LegacyColorProcessor())
            .build();

}