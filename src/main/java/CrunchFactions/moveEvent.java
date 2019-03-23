package CrunchFactions;


import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.BossBarColors;
import org.spongepowered.api.boss.BossBarOverlays;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import com.flowpowered.math.vector.Vector3i;

public class moveEvent {
		@Listener
	    public void onMove(MoveEntityEvent event, @First Player player) throws SQLException{
			FactionPlayer facP = new FactionPlayer();
			if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
				facP = facP.getFacPlayerFromUUID(player.getUniqueId());
				if (facP.getFacID() != 1) {
				Faction	faction = Faction.FactionFromID(facP.getFacID());
				}
				Vector3i location = player.getLocation().getChunkPosition();
				String worldName = player.getWorld().getName();
				String combined = worldName + ":" + location;
				boolean hasRelation = false, otherFaction = false;
				int otherFacID = 0;
				if (FactionsMain.allFactionClaims.containsKey(combined)) {
			    otherFacID = Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID();
			    otherFaction = true;
				}
				if (FactionsMain.allFactionClaims.containsKey(combined) && facP.getLastTerritory() != otherFacID && otherFaction) {
					ServerBossBar bossBarA;
					ServerBossBar bossBarB;
					if (facP.getFacID() == otherFacID) {
						hasRelation = true;
						
						bossBarA = ServerBossBar.builder()
			    	             .name(Text.of(
			    	                   TextColors.YELLOW, Faction.FactionFromID(facP.getFacID()).getFacName()
			    	              ))
			    	               .percent(1f)
			    	               .color(BossBarColors.YELLOW)
			    	               .overlay(BossBarOverlays.PROGRESS)
			    	               .build();
						if (facP.getBar() != null) {
						bossBarB = facP.getBar();
						bossBarB.removePlayer(player);
						}
						facP.setBar(bossBarA);
						bossBarA.addPlayer(player);
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.YELLOW, "Entering your factions territory."));
					}
					if (facP.getFacID() != 1 && Faction.FactionFromID(facP.getFacID()).isEnemy(otherFacID) && otherFaction) {
						hasRelation = true;
						bossBarA = ServerBossBar.builder()
			    	             .name(Text.of(
			    	                   TextColors.RED, "Enemy: ", Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacName()
			    	              ))
			    	               .percent(1f)
			    	               .color(BossBarColors.RED)
			    	               .overlay(BossBarOverlays.PROGRESS)
			    	               .build();
						if (facP.getBar() != null) {
							bossBarB = facP.getBar();
							bossBarB.removePlayer(player);
							}
						facP.setBar(bossBarA);
						bossBarA.addPlayer(player);
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.RED, "Entering Enemy territory of ", Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacName(), "."));
					}
					if (facP.getFacID() != 1 && Faction.FactionFromID(facP.getFacID()).isAlly(otherFacID) && otherFaction) {
						hasRelation = true;
						bossBarA = ServerBossBar.builder()
			    	             .name(Text.of(
			    	                   TextColors.AQUA, "Ally: ", Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacName()
			    	              ))
			    	               .percent(1f)
			    	               .color(BossBarColors.BLUE)
			    	               .overlay(BossBarOverlays.PROGRESS)
			    	               .build();
						if (facP.getBar() != null) {
							bossBarB = facP.getBar();
							bossBarB.removePlayer(player);
							}
						facP.setBar(bossBarA);
						bossBarA.addPlayer(player);
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.AQUA, "Entering Allied territory of ", Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacName(), "."));
					}
					if (facP.getFacID() != 1 && Faction.FactionFromID(facP.getFacID()).isVassal(otherFacID) && otherFaction) {
						hasRelation = true;
						bossBarA = ServerBossBar.builder()
			    	             .name(Text.of(
			    	                   TextColors.DARK_AQUA, "Vassal: ",Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacName()
			    	              ))
			    	               .percent(1f)
			    	               .color(BossBarColors.BLUE)
			    	               .overlay(BossBarOverlays.PROGRESS)
			    	               .build();
						if (facP.getBar() != null) {
							bossBarB = facP.getBar();
							bossBarB.removePlayer(player);
							}
						facP.setBar(bossBarA);
						bossBarA.addPlayer(player);
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.DARK_AQUA, "Entering Vassal territory of ", Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacName(), "."));
					}
					if (!hasRelation) {
						bossBarA = ServerBossBar.builder()
			    	             .name(Text.of(
			    	                   TextColors.WHITE,"Neutral: ", Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacName()
			    	              ))
			    	               .percent(1f)
			    	               .color(BossBarColors.WHITE)
			    	               .overlay(BossBarOverlays.PROGRESS)
			    	               .build();
						if (facP.getBar() != null) {
							bossBarB = facP.getBar();
							bossBarB.removePlayer(player);
							}
						facP.setBar(bossBarA);
						bossBarA.addPlayer(player);
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.WHITE, "Entering Neutral territory of ", Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacName(), "."));
					}
					facP.setLastTerritory(Faction.FactionFromID(FactionsMain.allFactionClaims.get(combined)).getFacID());
				}
				else {
					if (!FactionsMain.allFactionClaims.containsKey(combined) && facP.getLastTerritory() != 1){
						ServerBossBar bossBarA;
						ServerBossBar bossBarB;
						bossBarA = ServerBossBar.builder()
			    	             .name(Text.of(
			    	                   TextColors.GREEN, "Wilderness"
			    	              ))
			    	               .percent(1f)
			    	               .color(BossBarColors.GREEN)
			    	               .overlay(BossBarOverlays.PROGRESS)
			    	               .build();
						if (facP.getBar() != null) {
							bossBarB = facP.getBar();
							bossBarB.removePlayer(player);
							}
						facP.setBar(bossBarA);
						bossBarA.addPlayer(player);
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Entering the Wilderness.", TextColors.RED, " PvP is enabled here."));
						facP.setLastTerritory(1);
					}
				}
			}
		
		}
	}

