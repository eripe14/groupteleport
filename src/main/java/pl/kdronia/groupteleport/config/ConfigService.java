package pl.kdronia.groupteleport.config;

import com.eternalcode.multification.notice.resolver.NoticeResolverRegistry;
import com.eternalcode.multification.okaeri.MultificationSerdesPack;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.serdes.commons.SerdesCommons;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;

import java.io.File;

public class ConfigService {

    private final NoticeResolverRegistry noticeResolverRegistry;

    public ConfigService(NoticeResolverRegistry noticeResolverRegistry) {
        this.noticeResolverRegistry = noticeResolverRegistry;
    }

    public <T extends OkaeriConfig> T load(Class<T> configClass, File path, String fileName) {
        return ConfigManager.create(configClass, (config) -> {
            config.withConfigurer(
                    new YamlBukkitConfigurer(),
                    new SerdesBukkit(),
                    new SerdesCommons(),
                    new MultificationSerdesPack(this.noticeResolverRegistry)
            );
            config.withBindFile(new File(path, fileName));
            config.withRemoveOrphans(true);
            config.saveDefaults();
            config.load(true);
        });
    }

}