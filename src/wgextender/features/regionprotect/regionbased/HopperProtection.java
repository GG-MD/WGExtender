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
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import wgextender.Config;
import wgextender.utils.WGRegionUtils;

public class HopperProtection implements Listener {

	protected final Config config;
	public HopperProtection(Config config) {
		this.config = config;
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInventoryMoveItem(InventoryMoveItemEvent event) {
		Inventory source = event.getSource();
		Inventory destination = event.getDestination();

		Location sourceLocation = getInventoryLocation(source);
		Location destinationLocation = getInventoryLocation(destination);

		if (sourceLocation != null && destinationLocation != null) {
			if (!WGRegionUtils.isInTheSameRegionOrWild(sourceLocation, destinationLocation)) {
				event.setCancelled(true);
			}
		}
	}

	private Location getInventoryLocation(Inventory inventory) {
		InventoryHolder holder = inventory.getHolder();
		
		if (holder instanceof BlockState blockState) {
			return blockState.getLocation();
		}
		
		if (holder instanceof Entity entity) {
			return entity.getLocation();
		}
		
		if (holder instanceof Container container) {
			return container.getLocation();
		}
		
		return null;
	}

}
