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

package wgextender.features.regionprotect.regionbased;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import wgextender.utils.WGRegionUtils;

public class MinecartProtection implements Listener {

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onVehicleMove(VehicleMoveEvent event) {
		if (!(event.getVehicle() instanceof Minecart minecart)) {
			return;
		}

		Location from = event.getFrom();
		Location to = event.getTo();

		if (from == null || to == null) {
			return;
		}

		if (!WGRegionUtils.isInWGRegion(from) && WGRegionUtils.isInWGRegion(to)) {
			if (!minecart.isEmpty()) {
				Entity passenger = minecart.getPassengers().get(0);
				if (passenger instanceof Player player) {
					if (WGRegionUtils.canBypassProtection(player)) {
						return;
					}
				}
			}
			
			minecart.setVelocity(minecart.getVelocity().multiply(-1));
		}
	}

}
