package pl.kdronia.groupteleport;

import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import pl.kdronia.groupteleport.config.ConfigService;
import pl.kdronia.groupteleport.config.impl.MessageConfig;
import pl.kdronia.groupteleport.config.impl.PluginConfig;
import pl.kdronia.groupteleport.notice.NoticeService;
import pl.kdronia.groupteleport.random.RandomTeleportService;

public class GroupTeleportPlugin extends JavaPlugin {

    private NoticeService noticeService;

    private ConfigService configService;
    private PluginConfig pluginConfig;
    private MessageConfig messageConfig;

    private GroupTeleportService groupTeleportService;

    @Override
    public void onEnable() {
        Server server = this.getServer();

        this.messageConfig = new MessageConfig();
        this.noticeService = new NoticeService(this.messageConfig);

        this.configService = new ConfigService(this.noticeService.getNoticeRegistry());
        this.pluginConfig = this.configService.load(PluginConfig.class, this.getDataFolder(), "config.yml");
        this.messageConfig = this.configService.load(MessageConfig.class, this.getDataFolder(), "messages.yml");

        this.groupTeleportService = new GroupTeleportService(
                this,
                server,
                this.pluginConfig,
                new RandomTeleportService(this.pluginConfig),
                this.noticeService
        );

        server.getPluginManager().registerEvents(
                new GroupTeleportController(this.pluginConfig, this.groupTeleportService),
                this
        );
    }

    @Override
    public void onDisable() {
        if (this.groupTeleportService != null) {
            this.groupTeleportService.shutdown();
        }
    }
}