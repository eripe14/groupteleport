package pl.kdronia.groupteleport;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import pl.kdronia.groupteleport.notice.NoticeService;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class GroupTeleportRunnable implements Runnable {

    private final String teleportId;
    private final Set<UUID> players;
    private final World world;
    private final CountdownTimer timer;
    private final GroupTeleportService groupTeleportService;
    private final NoticeService noticeService;
    private final Server server;

    private BukkitTask task;
    private boolean executed = false;

    public GroupTeleportRunnable(
            String teleportId,
            Set<UUID> players,
            World world,
            CountdownTimer timer,
            GroupTeleportService groupTeleportService,
            NoticeService noticeService,
            Server server
    ) {
        this.teleportId = teleportId;
        this.players = players;
        this.world = world;
        this.timer = timer;
        this.groupTeleportService = groupTeleportService;
        this.noticeService = noticeService;
        this.server = server;
    }

    public void setTask(BukkitTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        if (this.executed) {
            return;
        }

        if (this.timer.isFinished()) {
            this.executed = true;
            this.executeTeleportation();
            this.cancelTask();
            return;
        }

        if (this.timer.shouldDisplay()) {
            this.displayCountdown();
        }
    }

    private void displayCountdown() {
        long secondsLeft = this.timer.getRemainingSeconds();

        this.players.stream()
                .map(this.server::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .forEach(player -> this.noticeService.create()
                        .notice(messages -> messages.teleportCountdown)
                        .placeholder("{seconds}", String.valueOf(secondsLeft))
                        .player(player.getUniqueId())
                        .send()
                );
    }

    private void executeTeleportation() {
        this.groupTeleportService.teleportGroup(this.teleportId, this.world).whenComplete((success, ex) -> {
            if (!success || ex != null) {
                return;
            }

            this.players.stream()
                    .map(this.server::getPlayer)
                    .filter(Objects::nonNull)
                    .filter(Player::isOnline)
                    .forEach(player -> this.noticeService.create()
                            .notice(messages -> messages.teleported)
                            .player(player.getUniqueId())
                            .send()
                    );
        });
    }

    private void cancelTask() {
        if (this.task != null) {
            this.server.getScheduler().cancelTask(this.task.getTaskId());
        }
    }
}