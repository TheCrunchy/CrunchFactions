

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

public class protectEvents {

	@Listener
	public void onBoom(ExplosionEvent.Detonate event)  throws SQLException{
		FactionPlayer facP = new FactionPlayer();
		Optional<Player> cause = event.getCause().first(Player.class);
		if (cause.isPresent()) {
			Player player = cause.get();
			if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
				facP = facP.getFacPlayerFromUUID(player.getUniqueId());
				Faction	faction = new Faction();
				if (facP.getFacID() != 1) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				Vector3i location = event.getExplosion().getLocation().getChunkPosition();
				List<Location<World>> locs = event.getAffectedLocations();
				int otherFacID = 0;
				for (Location loc : locs) {
					String worldName = event.getTargetWorld().getName();
					Vector3i location2 = loc.getChunkPosition();
					String combined = worldName + ":" + location2;

					if (FactionsMain.allFactionClaims.containsKey(combined) && !faction.isEnemy(facP.getFacID())) {
						otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
						if (facP.getFacID() != otherFacID) {
							event.setCancelled(true);
							player.sendMessage(Text.of(FactionsMain.defaultMessage, "This is not your land, you cannot damage them without being at war!"));
							return;
						}
					}
				}
				String worldName = player.getWorld().getName();
				String combined = worldName + ":" + location;
				if (FactionsMain.allFactionClaims.containsKey(combined) && !faction.isEnemy(facP.getFacID())) {
					otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
					if (facP.getFacID() != otherFacID) {
						event.setCancelled(true);
						player.sendMessage(Text.of(FactionsMain.defaultMessage, "This is not your land, you cannot damage them without being at war!"));
					}
				}
			}
		}
		else {
			List<Location<World>> locs = event.getAffectedLocations();
			for (Location loc : locs) {
				String worldName = event.getTargetWorld().getName();
				Vector3i location2 = loc.getChunkPosition();
				String combined = worldName + ":" + location2;

				if (FactionsMain.allFactionClaims.containsKey(combined)) {
					event.setCancelled(true);
					return;
				}

			}
			Vector3i location = event.getExplosion().getLocation().getChunkPosition();
			String worldName = event.getTargetWorld().getName();
			String combined = worldName + ":" + location;
			if (FactionsMain.allFactionClaims.containsKey(combined)) {
				event.setCancelled(true);

			}
		}



	}
	@Listener
	public void onDamage(DamageEntityEvent event)  throws SQLException{
		FactionPlayer facP = new FactionPlayer();

		Optional<Player> cause = event.getCause().first(Player.class);
		if (cause.isPresent()) {
			Player player = cause.get();
			if (event.getTargetEntity() == cause.get()) {
				player.sendMessage(Text.of("dumbass"));
				return;
			}
			if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
				facP = facP.getFacPlayerFromUUID(player.getUniqueId());
				Faction	faction = new Faction();
				if (facP.getFacID() != 1) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				Vector3i location = event.getTargetEntity().getLocation().getChunkPosition();
				String worldName = player.getWorld().getName();
				String combined = worldName + ":" + location;
				int otherFacID = 0;
				if (FactionsMain.allFactionClaims.containsKey(combined) && !faction.isEnemy(FactionsMain.allFactionClaims.get(combined))) {
					otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
					if (facP.getFacID() != otherFacID) {
						if (event.getTargetEntity().getType() == EntityTypes.PLAYER) {
							FactionPlayer otherFacPlayer = new FactionPlayer();
							Player otherPlayer = (Player) event.getTargetEntity();
							otherFacPlayer = otherFacPlayer.getFacPlayerFromUUID(otherPlayer.getUniqueId());
							if (Faction.FactionFromID(facP.getFacID()).isAlly(otherFacPlayer.getFacID()) || facP.getFacID() == otherFacPlayer.getFacID()) {
								event.setCancelled(true);
								return;
							}
							Double damage = event.getBaseDamage() - (event.getBaseDamage() * 0.2);
							player.sendMessage(Text.of(FactionsMain.defaultMessage, "Damage reduced by 20% as you are attacking in neutral territory."));
							event.setBaseDamage(damage);
							return;
						}
						else {
							event.setCancelled(true);
						}
						player.sendMessage(Text.of(FactionsMain.defaultMessage, "This is not your land, you cannot damage them without being at war!"));
					}
				}
			}
		}
		else {
			Vector3i location = event.getTargetEntity().getLocation().getChunkPosition();
			String worldName = event.getTargetEntity().getWorld().getName();
			String combined = worldName + ":" + location;
			if (FactionsMain.allFactionClaims.containsKey(combined) && event.getTargetEntity().getType() != EntityTypes.PLAYER) {
				event.setCancelled(true);
			}
		}

	}

	@Listener
	public void onBlockBreak(ChangeBlockEvent.Break event, @Root Player player)  throws SQLException{
		FactionPlayer facP = new FactionPlayer();
		if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
			facP = facP.getFacPlayerFromUUID(player.getUniqueId());
			if (facP.getFacID() != 1) {
				Faction	faction = Faction.FactionFromID(facP.getFacID());
			}
			Vector3i location = null;
			for(Transaction<BlockSnapshot> blockSnapshotTransaction : event.getTransactions()) {
				location = blockSnapshotTransaction.getOriginal().getLocation().get().getChunkPosition();
			}
			String worldName = player.getWorld().getName();
			String combined = worldName + ":" + location;

			int otherFacID = 0;
			if (FactionsMain.allFactionClaims.containsKey(combined)) {
				otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
				if (facP.getFacID() != otherFacID && !player.hasPermission("factions.admin")) {
					event.setCancelled(true);
					player.sendMessage(Text.of(FactionsMain.defaultMessage, "This is not your land!"));
				}
			}
		}
	}


	@Listener
	public void onBlockPlace(ChangeBlockEvent.Place event) throws SQLException{
		Optional<Player> cause = event.getCause().first(Player.class);
		Player player = null;
		if (cause.isPresent()) {
			player = cause.get();
			FactionPlayer facP = new FactionPlayer();
			if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
				facP = facP.getFacPlayerFromUUID(player.getUniqueId());
				if (facP.getFacID() != 1) {
					Faction	faction = Faction.FactionFromID(facP.getFacID());
				}
				Vector3i location = null;
				for(Transaction<BlockSnapshot> blockSnapshotTransaction : event.getTransactions()) {
					location = blockSnapshotTransaction.getOriginal().getLocation().get().getChunkPosition();
				}
				String worldName = player.getWorld().getName();
				String combined = worldName + ":" + location;

				int otherFacID = 0;
				if (FactionsMain.allFactionClaims.containsKey(combined)) {
					otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
					if (facP.getFacID() != otherFacID && !player.hasPermission("factions.admin")) {
						event.setCancelled(true);
						player.sendMessage(Text.of(FactionsMain.defaultMessage, "This is not your land!"));
					}
				}
			}
		}
		//			else {
		//			List<Transaction<BlockSnapshot>> locs = event.getTransactions();
		//			for (Transaction<BlockSnapshot> loc : locs) {
		//				String worldName = loc.getOriginal().getLocation().get().getExtent().getName();
		//				Vector3i location2 = loc.getOriginal().getLocation().get().getChunkPosition();
		//				String combined = worldName + ":" + location2;
		//				if (FactionsMain.allFactionClaims.containsKey(combined)) {
		//						event.setCancelled(true);
		//						return;
		//					}
		//				}
		//		}
	}

	@Listener
	public void onBlockBreak(ChangeBlockEvent.Modify event, @Root Player player)  throws SQLException{
		FactionPlayer facP = new FactionPlayer();
		if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
			facP = facP.getFacPlayerFromUUID(player.getUniqueId());
			if (facP.getFacID() != 1) {
				Faction	faction = Faction.FactionFromID(facP.getFacID());
			}
			Vector3i location = null;
			for(Transaction<BlockSnapshot> blockSnapshotTransaction : event.getTransactions()) {
				location = blockSnapshotTransaction.getOriginal().getLocation().get().getChunkPosition();
			}
			String worldName = player.getWorld().getName();
			String combined = worldName + ":" + location;

			int otherFacID = 0;
			if (FactionsMain.allFactionClaims.containsKey(combined)) {
				otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
				if (facP.getFacID() != otherFacID && !player.hasPermission("factions.admin")) {
					event.setCancelled(true);
					player.sendMessage(Text.of(FactionsMain.defaultMessage, "This is not your land!"));
				}
			}
		}
	}


	@Listener
	public void onBlockBreak(InteractBlockEvent event, @Root Player player)  throws SQLException{
		FactionPlayer facP = new FactionPlayer();
		if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
			facP = facP.getFacPlayerFromUUID(player.getUniqueId());
			if (facP.getFacID() != 1) {
				Faction	faction = Faction.FactionFromID(facP.getFacID());
			}
			if (event.getInteractionPoint().isPresent()) {
				Location loc = new Location(player.getWorld(), event.getInteractionPoint().get().getX(),  0, event.getInteractionPoint().get().getZ());
				Vector3i location = loc.getChunkPosition();
				String worldName = player.getWorld().getName();
				String combined = worldName + ":" + location;

				int otherFacID = 0;
				if (FactionsMain.allFactionClaims.containsKey(combined)) {
					otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
					if (facP.getFacID() != otherFacID && !player.hasPermission("factions.admin")) {
						event.setCancelled(true);
						player.sendMessage(Text.of(FactionsMain.defaultMessage, "This is not your land!"));
					}
				}
			}
			else {
				return;
			}
		}
	}

	//	public static class fireSpread{	
	//		@Listener
	//		public void onBlockBreak(ChangeBlockEvent event)  throws SQLException{
	//			for(Transaction<BlockSnapshot> blockSnapshotTransaction : event.getTransactions()) {
	//				if (blockSnapshotTransaction.getFinal().getState().getType() == BlockTypes.FIRE) {
	//					//do fire check for faction flags
	//					String worldName = blockSnapshotTransaction.getFinal().getLocation().get().getExtent().getName();
	//					Vector3i location = blockSnapshotTransaction.getOriginal().getLocation().get().getChunkPosition();
	//					String combined = worldName + ":" + location;
	//					if (FactionsMain.allFactionClaims.containsKey(combined)) {
	//							event.setCancelled(true);
	//					}
	//				}
	//			}
	//		}
	//
	//	}

	@Listener
	public void onBlockBreak(ChangeBlockEvent.Post event, @Root Player player)  throws SQLException{
		FactionPlayer facP = new FactionPlayer();
		if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
			facP = facP.getFacPlayerFromUUID(player.getUniqueId());
			if (facP.getFacID() != 1) {
				Faction	faction = Faction.FactionFromID(facP.getFacID());
			}
			Vector3i location = null;
			for(Transaction<BlockSnapshot> blockSnapshotTransaction : event.getTransactions()) {
				location = blockSnapshotTransaction.getOriginal().getLocation().get().getChunkPosition();
			}
			String worldName = player.getWorld().getName();
			String combined = worldName + ":" + location;

			int otherFacID = 0;
			if (FactionsMain.allFactionClaims.containsKey(combined)) {
				otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
				if (facP.getFacID() != otherFacID && !player.hasPermission("factions.admin")) {
					event.setCancelled(true);
					player.sendMessage(Text.of(FactionsMain.defaultMessage, "This is not your land!"));
				}
			}
		}
	}



	@Listener
	public void onBlockBreak(ChangeBlockEvent.Pre event)  throws SQLException{
		boolean pistonExtend = event.getContext().containsKey(EventContextKeys.PISTON_EXTEND);
		boolean pistonRetract = event.getContext().containsKey(EventContextKeys.PISTON_RETRACT);

		Optional<Player> cause = event.getCause().first(Player.class);
		Player player = null;
		if (cause.isPresent()) {

			player = cause.get();
			if (pistonExtend || pistonRetract) {
				LocatableBlock locatableBlock = event.getCause().first(LocatableBlock.class).orElse(null);
				TileEntity tileEntity = event.getCause().first(TileEntity.class).orElse(null);
				Location<World> sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;
				String tempName = sourceLocation.getExtent().getName();
				Vector3i tempLoc = sourceLocation.getChunkPosition();
				String tempCombined = tempName + ":" + tempLoc;
				if (FactionsMain.allFactionClaims.containsKey(tempCombined)) {
					return;
				}
				List<Location<World>> locs = event.getLocations();
				for (Location loc : locs) {

					String worldName = event.getLocations().get(0).getExtent().getName();
					Vector3i location2 = loc.getChunkPosition();
					String combined = worldName + ":" + location2;
					if (FactionsMain.allFactionClaims.containsKey(combined)) {
						event.setCancelled(true);
						return;
					}
				}
			}
			FactionPlayer facP = new FactionPlayer();
			if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
				facP = facP.getFacPlayerFromUUID(player.getUniqueId());
				if (facP.getFacID() != 1) {
					Faction	faction = Faction.FactionFromID(facP.getFacID());
				}
				Vector3i location = player.getLocation().getChunkPosition();
				String worldName = player.getWorld().getName();
				String combined = worldName + ":" + location;

				int otherFacID = 0;
				if (FactionsMain.allFactionClaims.containsKey(combined)) {
					otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
					if (facP.getFacID() != otherFacID && !player.hasPermission("factions.admin")) {
						event.setCancelled(true);
						player.sendMessage(Text.of(FactionsMain.defaultMessage, "This is not your land!"));
					}
				}
			}
			return;
		}
		if (pistonExtend || pistonRetract) {
			LocatableBlock locatableBlock = event.getCause().first(LocatableBlock.class).orElse(null);
			TileEntity tileEntity = event.getCause().first(TileEntity.class).orElse(null);
			Location<World> sourceLocation = locatableBlock != null ? locatableBlock.getLocation() : tileEntity != null ? tileEntity.getLocation() : null;

			String tempName = sourceLocation.getExtent().getName();
			Vector3i tempLoc = sourceLocation.getChunkPosition();
			String tempCombined = tempName + ":" + tempLoc;
			if (FactionsMain.allFactionClaims.containsKey(tempCombined)) {
				return;
			}
			List<Location<World>> locs = event.getLocations();
			for (Location loc : locs) {

				String worldName = event.getLocations().get(0).getExtent().getName();
				Vector3i location2 = loc.getChunkPosition();
				String combined = worldName + ":" + location2;
				if (FactionsMain.allFactionClaims.containsKey(combined)) {
					event.setCancelled(true);
					return;
				}
			}
		}
	}
}


