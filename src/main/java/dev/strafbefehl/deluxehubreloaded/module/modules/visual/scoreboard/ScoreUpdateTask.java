package dev.strafbefehl.deluxehubreloaded.module.modules.visual.scoreboard;

import dev.strafbefehl.deluxehubreloaded.utility.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScoreUpdateTask implements Runnable {

	private final ScoreboardManager scoreboardManager;

	public ScoreUpdateTask(ScoreboardManager scoreboardManager) {
		this.scoreboardManager = scoreboardManager;
	}

	@Override
	public void run() {
		List<UUID> toRemove = new ArrayList<>();
		scoreboardManager.getPlayers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				toRemove.add(uuid);
				return;
			}
			FoliaScheduler.runAtEntity(player, scoreboardManager.getPlugin(),
					() -> scoreboardManager.updateScoreboard(uuid));
		});
		toRemove.forEach(scoreboardManager::removeScoreboard);
	}

}
