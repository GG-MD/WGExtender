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

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import wgextender.Config;
import wgextender.utils.WGRegionUtils;

@RequiredArgsConstructor
public class LiquidFlow implements Listener {

	protected final Config config;

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onLiquidFlow(BlockFromToEvent event) {
		if (event.getBlock().isLiquid()) {
			check(event.getBlock(), event.getToBlock(), event, true);
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onDispense(BlockDispenseEvent event) {
		Block block = event.getBlock();
		BlockData blockData = block.getBlockData();
		if (blockData instanceof Directional directional) {
			Block relative = block.getRelative(directional.getFacing());
			
			if (relative.isLiquid()) {
				check(block, relative, event, false);
			}
			
			ItemStack item = event.getItem();
			if (isLiquidBucket(item.getType())) {
				Material liquidType = getLiquidFromBucket(item.getType());
				if (shouldCheckLiquid(liquidType)) {
					if (!WGRegionUtils.isInTheSameRegionOrWild(block.getLocation(), relative.getLocation())) {
						event.setCancelled(true);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		Block block = event.getBlock();
		BlockData blockData = block.getBlockData();
		
		if (blockData instanceof Waterlogged waterlogged && waterlogged.isWaterlogged()) {
			if (config.checkWaterFlow) {
				Block sourceBlock = event.getBlockAgainst();
				if (!WGRegionUtils.isInTheSameRegionOrWild(sourceBlock.getLocation(), block.getLocation())) {
					event.setCancelled(true);
				}
			}
		}
	}

	private void check(Block source, Block to, Cancellable event, boolean checkSource) {
		if (switch (checkSource ? source.getType() : to.getType()) {
			case LAVA -> config.checkLavaFlow;
			case WATER -> config.checkWaterFlow;
			default -> config.checkOtherLiquidFlow;
		}) {
			if (!WGRegionUtils.isInTheSameRegionOrWild(source.getLocation(), to.getLocation())) {
				event.setCancelled(true);
			}
		}
	}

	private boolean isLiquidBucket(Material material) {
		return material.name().endsWith("_BUCKET") && material != Material.BUCKET;
	}

	private Material getLiquidFromBucket(Material bucketType) {
		String bucketName = bucketType.name();
		if (bucketName.endsWith("_BUCKET") && !bucketName.equals("BUCKET")) {
			String liquidName = bucketName.replace("_BUCKET", "");
			try {
				return Material.valueOf(liquidName);
			} catch (IllegalArgumentException e) {
				return Material.AIR;
			}
		}
		return Material.AIR;
	}

	private boolean shouldCheckLiquid(Material liquidType) {
		return switch (liquidType) {
			case LAVA -> config.checkLavaFlow;
			case WATER -> config.checkWaterFlow;
			default -> config.checkOtherLiquidFlow;
		};
	}

}
