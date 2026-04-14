/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package wgextender;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wgextender.commands.Commands;
import wgextender.features.claimcommand.WGRegionCommandWrapper;
import wgextender.features.extendedwand.WEWandCommandWrapper;
import wgextender.features.extendedwand.WEWandListener;
import wgextender.features.flags.ChorusFruitFlagHandler;
import wgextender.features.flags.OldPVPFlagsHandler;
import wgextender.features.flags.WGExtenderFlags;
import wgextender.features.regionprotect.ownormembased.PvPHandlingListener;
import wgextender.features.regionprotect.ownormembased.RestrictCommands;
import wgextender.features.regionprotect.regionbased.BlockBurn;
import wgextender.features.regionprotect.regionbased.Explode;
import wgextender.features.regionprotect.regionbased.FireSpread;
import wgextender.features.regionprotect.regionbased.LiquidFlow;

import java.util.Objects;
import java.util.logging.Level;

public class WGExtender extends JavaPlugin {

	private static WGExtender instance;

	public static @NotNull WGExtender getInstance() {
		return Objects.requireNonNull(instance, "WGExtender is not initialized");
	}

	public WGExtender() {
		instance = this;
	}

	private @Nullable PvPHandlingListener pvplistener;
	private @Nullable OldPVPFlagsHandler oldpvphandler;
	private boolean wgRegionInjected;
	private boolean weWandInjected;

	@Override
	public void onLoad() {
		WGExtenderFlags.registerFlags(getLogger());
	}

	@Override
	public void onEnable() {
		VaultIntegration.getInstance().initialize(this);
		Config config = new Config(this);
		config.loadConfig();
		Objects.requireNonNull(getCommand("wgex"), "command 'wgex' missing in plugin.yml").setExecutor(new Commands(config));
		PluginManager pluginManager = getServer().getPluginManager();
		pluginManager.registerEvents(new RestrictCommands(config), this);
		pluginManager.registerEvents(new LiquidFlow(config), this);
		pluginManager.registerEvents(new FireSpread(config), this);
		pluginManager.registerEvents(new BlockBurn(config), this);
		pluginManager.registerEvents(new Explode(config), this);
		pluginManager.registerEvents(new WEWandListener(), this);
		pluginManager.registerEvents(new ChorusFruitFlagHandler(), this);
		try {
			WGRegionCommandWrapper.inject(config);
			wgRegionInjected = true;
			WEWandCommandWrapper.inject(config);
			weWandInjected = true;
			pvplistener = new PvPHandlingListener(config);
			pvplistener.inject(this);
			oldpvphandler = new OldPVPFlagsHandler();
			if (config.miscOldPvpFlags) {
				getLogger().warning(
						"Enabling the old-PvP flags. Do note that they're not supported, " +
						"as they're very out of scope of extending WG capabilities. " +
						"Consider turning them off by setting 'misc.old-pvp-flags' to 'false'"
				);
				oldpvphandler.start(this);
			}
		} catch (Throwable t) {
			getLogger().log(Level.SEVERE, "Unable to inject, rolling back and shutting down", t);
			safeUninjectAll();
			Bukkit.shutdown();
		}
	}

	@Override
	public void onDisable() {
		try {
			safeUninjectAll();
		} catch (Throwable t) {
			getLogger().log(Level.SEVERE, "Unable to uninject cleanly, shutting down", t);
			Bukkit.shutdown();
		}
	}

	private void safeUninjectAll() {
		if (oldpvphandler != null) {
			try {
				oldpvphandler.stop(this);
			} catch (Throwable t) {
				getLogger().log(Level.SEVERE, "Failed to stop OldPVPFlagsHandler", t);
			}
			oldpvphandler = null;
		}
		if (pvplistener != null) {
			try {
				pvplistener.uninject();
			} catch (Throwable t) {
				getLogger().log(Level.SEVERE, "Failed to uninject PvPHandlingListener", t);
			}
			pvplistener = null;
		}
		if (weWandInjected) {
			try {
				WEWandCommandWrapper.uninject();
			} catch (Throwable t) {
				getLogger().log(Level.SEVERE, "Failed to uninject WEWandCommandWrapper", t);
			}
			weWandInjected = false;
		}
		if (wgRegionInjected) {
			try {
				WGRegionCommandWrapper.uninject();
			} catch (Throwable t) {
				getLogger().log(Level.SEVERE, "Failed to uninject WGRegionCommandWrapper", t);
			}
			wgRegionInjected = false;
		}
	}

}
