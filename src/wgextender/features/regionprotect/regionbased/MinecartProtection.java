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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;

import wgextender.utils.ColorUtil;
import wgextender.utils.WGRegionUtils;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.LocalPlayer;

public class MinecartProtection implements Listener {
	
	private boolean isNearRegion(Location location) {
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -2; z <= 2; z++) {
					if (x == 0 && y == 0 && z == 0) {
						continue;
					}
					
					Location checkLocation = location.clone().add(x, y, z);
					if (WGRegionUtils.isInWGRegion(checkLocation)) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private Location findNearestRegionCenter(Location location) {
		Location nearestCenter = null;
		double nearestDistance = Double.MAX_VALUE;
		
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				for (int z = -2; z <= 2; z++) {
					if (x == 0 && y == 0 && z == 0) {
						continue;
					}
					
					Location checkLocation = location.clone().add(x, y, z);
					if (WGRegionUtils.isInWGRegion(checkLocation)) {
						double distance = location.distance(checkLocation);
						if (distance < nearestDistance) {
							nearestDistance = distance;
							nearestCenter = checkLocation;
						}
					}
				}
			}
		}
		
		return nearestCenter;
	}
	
	private boolean isPlayerOwnerOrMember(Player player, Location location) {
		if (WGRegionUtils.canBypassProtection(player)) {
			return true;
		}
		
		ApplicableRegionSet regions = WGRegionUtils.getRegionsAt(location);
		if (regions.size() == 0) {
			for (int x = -2; x <= 2; x++) {
				for (int y = -2; y <= 2; y++) {
					for (int z = -2; z <= 2; z++) {
						if (x == 0 && y == 0 && z == 0) {
							continue;
						}
						
						Location checkLocation = location.clone().add(x, y, z);
						ApplicableRegionSet nearbyRegions = WGRegionUtils.getRegionsAt(checkLocation);
						if (nearbyRegions.size() > 0) {
							LocalPlayer localPlayer = WGRegionUtils.wrapPlayer(player);
							return nearbyRegions.isOwnerOfAll(localPlayer) || nearbyRegions.isMemberOfAll(localPlayer);
						}
					}
				}
			}
			return true;
		}
		
		LocalPlayer localPlayer = WGRegionUtils.wrapPlayer(player);
		return regions.isOwnerOfAll(localPlayer) || regions.isMemberOfAll(localPlayer);
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		
		ItemStack item = event.getItem();
		if (item == null || !isMinecartItem(item.getType())) {
			return;
		}
		
		Player player = event.getPlayer();
		Location clickedLocation = event.getClickedBlock().getLocation();
		
		for (int y = 0; y >= -2; y--) {
			Location checkLocation = clickedLocation.clone().add(0, y, 0);
			boolean inRegion = WGRegionUtils.isInWGRegion(checkLocation);
			boolean nearRegion = isNearRegion(checkLocation);
			
			if (inRegion || nearRegion) {
				if (!isPlayerOwnerOrMember(player, checkLocation)) {
					event.setCancelled(true);
					player.sendMessage(ColorUtil.deserialize("<dark_gray>[<red><b>!</b><dark_gray>] <gray>Запрещено размещать вагонетки рядом с чужим регионом!"));
					return;
				}
			}
		}
	}
	
	private boolean isMinecartItem(Material material) {
		return material.name().endsWith("_MINECART");
	}

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

		boolean fromInRegion = WGRegionUtils.isInWGRegion(from);
		boolean toInRegion = WGRegionUtils.isInWGRegion(to);
		boolean fromNearRegion = isNearRegion(from);
		boolean toNearRegion = isNearRegion(to);

		if ((!fromInRegion && !fromNearRegion) && (toInRegion || toNearRegion)) {
			if (!minecart.isEmpty()) {
				Entity passenger = minecart.getPassengers().get(0);
				if (passenger instanceof Player player) {
					if (WGRegionUtils.canBypassProtection(player)) {
						return;
					}
				}
			}
			
			Location regionCenter = findNearestRegionCenter(from);
			if (regionCenter != null) {
				Vector direction = from.toVector().subtract(regionCenter.toVector()).normalize();
				minecart.setVelocity(direction.multiply(2.0));
			} else {
				minecart.setVelocity(minecart.getVelocity().multiply(-2));
			}
		} else if (fromNearRegion && !fromInRegion) {
			if (!minecart.isEmpty()) {
				Entity passenger = minecart.getPassengers().get(0);
				if (passenger instanceof Player player) {
					if (WGRegionUtils.canBypassProtection(player)) {
						return;
					}
				}
			}
			
			Location regionCenter = findNearestRegionCenter(from);
			if (regionCenter != null) {
				Vector direction = from.toVector().subtract(regionCenter.toVector()).normalize();
				minecart.setVelocity(direction.multiply(2.0));
			} else {
				minecart.setVelocity(minecart.getVelocity().multiply(-2));
			}
		}
	}

}
