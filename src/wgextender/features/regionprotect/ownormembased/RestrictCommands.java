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

package wgextender.features.regionprotect.ownormembased;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import wgextender.Config;
import wgextender.utils.ColorUtil;
import wgextender.utils.WGRegionUtils;

import java.util.Locale;

@RequiredArgsConstructor
public class RestrictCommands implements Listener {

	protected final Config config;

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		if (!config.restrictCommandsInRegionEnabled) {
			return;
		}
		Player player = event.getPlayer();
		if (WGRegionUtils.canBypassProtection(player)) {
			return;
		}
		if (WGRegionUtils.isInWGRegion(player.getLocation()) && !WGRegionUtils.canBuild(player, player.getLocation())) {
			String command = event.getMessage().substring(1).toLowerCase(Locale.ROOT);
			for (String rcommand : config.restrictedCommandsInRegion) {
				if (command.startsWith(rcommand.toLowerCase(Locale.ROOT)) && 
					(command.length() == rcommand.length() || command.charAt(rcommand.length()) == ' ')) {
					event.setCancelled(true);
					player.sendMessage(ColorUtil.deserialize("<dark_gray>[<red><b>!</b><dark_gray>] <gray>Вы не можете использовать эту команду в чужом регионе."));
					return;
				}
			}
		}
	}
}
