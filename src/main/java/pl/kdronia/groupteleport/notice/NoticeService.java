package pl.kdronia.groupteleport.notice;

import com.eternalcode.multification.adventure.AudienceConverter;
import com.eternalcode.multification.bukkit.BukkitMultification;
import com.eternalcode.multification.translation.TranslationProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.kdronia.groupteleport.config.impl.MessageConfig;
import pl.kdronia.groupteleport.notice.adventure.MiniMessageHolder;

public class NoticeService extends BukkitMultification<MessageConfig> implements MiniMessageHolder {

    private final MessageConfig messageConfig;

    public NoticeService(MessageConfig messageConfig) {
        this.messageConfig = messageConfig;
    }

    @Override
    protected @NotNull TranslationProvider<MessageConfig> translationProvider() {
        return locale -> this.messageConfig;
    }

    @Override
    protected @NotNull ComponentSerializer<Component, Component, String> serializer() {
        return MINI_MESSAGE;
    }

    @Override
    protected @NotNull AudienceConverter<CommandSender> audienceConverter() {
        return commandSender -> {
            if (commandSender instanceof Player player) {
                return player;
            }

            return commandSender;
        };
    }

}