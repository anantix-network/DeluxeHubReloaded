package dev.strafbefehl.deluxehubreloaded.module.modules.visual.tablist;

import dev.strafbefehl.deluxehubreloaded.utility.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TablistUpdateTask implements Runnable {

	private final TablistManager tablistManager;

	public TablistUpdateTask(TablistManager tablistManager) {
		this.tablistManager = tablistManager;
	}

	@Override
	public void run() {
		List<UUID> toRemove = new ArrayList<>();
		tablistManager.getPlayers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				toRemove.add(uuid);
				return;
			}
			FoliaScheduler.runAtEntity(player, tablistManager.getPlugin(), () -> tablistManager.updateTablist(uuid));
		});
		toRemove.forEach(tablistManager::removeTablist);
	}

}
