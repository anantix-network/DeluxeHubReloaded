package dev.strafbefehl.deluxehubreloaded.module.modules.world;

import dev.strafbefehl.deluxehubreloaded.DeluxeHubPlugin;
import dev.strafbefehl.deluxehubreloaded.config.ConfigType;
import dev.strafbefehl.deluxehubreloaded.module.Module;
import dev.strafbefehl.deluxehubreloaded.module.ModuleType;
import dev.strafbefehl.deluxehubreloaded.utility.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

public class StaticTime extends Module {
	private int _time;

	public StaticTime(DeluxeHubPlugin plugin) {
		super(plugin, ModuleType.STATIC_TIME);
	}

	@Override
	public void onEnable() {
		FileConfiguration config = getConfig(ConfigType.SETTINGS);
		_time = config.getInt("static_time.time");
		runScheduler();
	}

	private void runScheduler() {
		final DeluxeHubPlugin plugin = DeluxeHubPlugin.getPlugin(DeluxeHubPlugin.class);
		FoliaScheduler.runTimer(plugin, () -> Bukkit.getWorlds().forEach(world -> {
			if (plugin.getModuleManager().getDisabledWorlds().contains(world.getName()))
				return;
			if (world.getTime() != _time)
				world.setTime(_time);
		}), 2L, 2L);
	}

	@Override
	public void onDisable() {
	}
}
