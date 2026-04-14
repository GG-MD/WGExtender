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

package wgextender.features.claimcommand;

import com.sk89q.minecraft.util.commands.CommandException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wgextender.Config;
import wgextender.utils.ColorUtil;
import wgextender.utils.CommandUtils;
import wgextender.utils.WEUtils;
import wgextender.utils.WGRegionUtils;

public class WGRegionCommandWrapper extends Command {

	public static void inject(@NotNull Config config) {
		Command original = CommandUtils.getCommands().get("region");
		if (original == null) {
			throw new IllegalStateException("WorldGuard command 'region' not found — is WorldGuard loaded?");
		}
		WGRegionCommandWrapper wrapper = new WGRegionCommandWrapper(config, original);
		CommandUtils.replaceCommand(original, wrapper);
	}

	public static void uninject() {
		Command current = CommandUtils.getCommands().get("region");
		if (current instanceof WGRegionCommandWrapper wrapper) {
			CommandUtils.replaceCommand(wrapper, wrapper.originalCmd);
		}
	}

	protected final Config config;
	protected final Command originalCmd;

	protected WGRegionCommandWrapper(@NotNull Config config, @NotNull Command originalCmd) {
		super(originalCmd.getName(), originalCmd.getDescription(), originalCmd.getUsage(), originalCmd.getAliases());
		this.config = config;
		this.originalCmd = originalCmd;
	}

	private final BlockLimits blockLimits = new BlockLimits();

	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
		if ((sender instanceof Player player) && (args.length >= 2) && args[0].equalsIgnoreCase("claim")) {
            String regionName = args[1];
			if (config.claimExpandSelectionVertical) {
				boolean result = WEUtils.expandVert(player);
				if (result) {
					player.sendMessage(ColorUtil.deserialize("<yellow>Регион автоматически расширен по вертикали"));
				}
			}
			if (!process(player)) {
				return true;
			}
			boolean hasRegion = AutoFlags.hasRegion(player.getWorld(), regionName);
			try {
				WGClaimCommand.claim(regionName, sender);
				if (!hasRegion && config.claimAutoFlagsEnabled) {
					AutoFlags.setFlagsForRegion(WGRegionUtils.wrapAsPrivileged(player, config.showAutoFlagMessages), player.getWorld(), config, regionName);
				}
			} catch (CommandException ex) {
				sender.sendMessage(ColorUtil.deserialize("<red>" + ex.getMessage()));
			}
			return true;
		} else {
			return originalCmd.execute(sender, label, args);
		}
	}

	private boolean process(Player player) {
		BlockLimits.ProcessedClaimInfo info = blockLimits.processClaimInfo(config, player);
		return switch (info.result()) {
            case ALLOW -> true;
			case DENY_MAX_VOLUME -> {
				player.sendMessage(ColorUtil.deserialize("<red>Вы не можете заприватить такой большой регион"));
				player.sendMessage(ColorUtil.deserialize("<red>Ваш лимит: " + info.assignedLimit() + ", вы попытались заприватить: " + info.assignedSize()));
				yield false;
			}
			case DENY_MIN_VOLUME -> {
				player.sendMessage(ColorUtil.deserialize("<red>Вы не можете заприватить такой маленький регион"));
				player.sendMessage(ColorUtil.deserialize("<red>Минимальный объем: " + info.assignedLimit() + ", вы попытались заприватить: " + info.assignedSize()));
				yield false;
			}
			case DENY_HORIZONTAL -> {
				player.sendMessage(ColorUtil.deserialize("<red>Вы не можете заприватить такой узкий регион"));
				player.sendMessage(ColorUtil.deserialize("<red>Минимальная ширина: " + info.assignedLimit() + ", вы попытались заприватить: " + info.assignedSize()));
				yield false;
			}
			case DENY_VERTICAL -> {
				player.sendMessage(ColorUtil.deserialize("<red>Вы не можете заприватить такой низкий регион"));
				player.sendMessage(ColorUtil.deserialize("<red>Минимальная высота: " + info.assignedLimit() + ", вы попытались заприватить: " + info.assignedSize()));
				yield false;
			}
		};
	}
}
