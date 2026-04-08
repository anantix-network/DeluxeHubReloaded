package dev.strafbefehl.deluxehubreloaded.module.modules.world;

import dev.strafbefehl.deluxehubreloaded.DeluxeHubPlugin;
import dev.strafbefehl.deluxehubreloaded.config.ConfigType;
import dev.strafbefehl.deluxehubreloaded.module.Module;
import dev.strafbefehl.deluxehubreloaded.module.ModuleType;
import dev.strafbefehl.deluxehubreloaded.utility.FoliaScheduler;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class LobbySpawn extends Module {

	private boolean spawnJoin;
	private Location location = null;

	public LobbySpawn(DeluxeHubPlugin plugin) {
		super(plugin, ModuleType.LOBBY);
	}

	@Override
	public void onEnable() {
		FoliaScheduler.run(getPlugin(), () -> {
			FileConfiguration config = getConfig(ConfigType.DATA);
			if (config.contains("spawn"))
				location = (Location) config.get("spawn");
		});
		spawnJoin = getConfig(ConfigType.SETTINGS).getBoolean("join_settings.spawn_join", false);
	}

	@Override
	public void onDisable() {
		getConfig(ConfigType.DATA).set("spawn", location);
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPlayedBefore()) {
			FoliaScheduler.runLaterAtEntity(player, getPlugin(), () -> {
				if (spawnJoin && location != null)
					FoliaScheduler.teleport(player, location);
			}, 2L);
		} else {
			if (spawnJoin && location != null)
				FoliaScheduler.teleport(player, location);
		}

	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (location != null && !inDisabledWorld(player.getLocation()))
			event.setRespawnLocation(location);
	}
}
