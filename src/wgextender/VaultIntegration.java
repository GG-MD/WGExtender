package wgextender;

import lombok.Getter;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VaultIntegration implements Listener {
	private static final VaultIntegration instance = new VaultIntegration();

	public static @NotNull VaultIntegration getInstance() {
		return instance;
	}

	@Getter
	private @Nullable Permission permissions;

	public void initialize(@NotNull WGExtender plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		hook();
	}

	private void hook() {
		try {
			RegisteredServiceProvider<Permission> registration = Bukkit.getServicesManager().getRegistration(Permission.class);
			if (registration == null) {
				permissions = null;
				return;
			}
			Permission provider = registration.getProvider();
			if (!provider.hasGroupSupport()) {
				permissions = null;
				return;
			}
			permissions = provider;
		} catch (Exception e) {
			permissions = null;
		}
	}

	@EventHandler
	public void onPluginEnable(@NotNull PluginEnableEvent event) {
		hook();
	}

	@EventHandler
	public void onPluginDisable(@NotNull PluginDisableEvent event) {
		hook();
	}
}
