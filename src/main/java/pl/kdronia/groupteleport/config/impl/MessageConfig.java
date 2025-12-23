package pl.kdronia.groupteleport.config.impl;

import com.eternalcode.multification.notice.Notice;
import eu.okaeri.configs.OkaeriConfig;

public class MessageConfig extends OkaeriConfig {

    public Notice teleported = Notice.chat(
            "&aPrzeteleportowano!"
    );

    public Notice teleportCountdown = Notice.title(
            "&6Teleportacja za &c&l{seconds}s&7!"
    );

    public Notice teleportCancelled = Notice.chat(
            "&cTeleportacja anulowana!"
    );

    public Notice teleportationFailed = Notice.chat(
            "&cNie udało się znaleźć bezpiecznej lokalizacji do teleportacji!"
    );

}