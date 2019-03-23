package CrunchFactions;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.world.Location;

import com.flowpowered.math.vector.Vector3i;

public class UserCommands {
	public static class teleportToFactionHome implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (facP.getFacID() == 1) {
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				else {
					faction = Faction.FactionFromID(facP.getFacID());
					Location loc = faction.getFactionHome();
					player.setLocation(loc);

					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Sending you to your faction home."));
				}
			}

			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class sendRankInfo implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

			src.sendMessage(Text.of(TextColors.GREEN, "-=======-", TextColors.DARK_GREEN, "[Factions]", TextColors.GREEN, "-=======-"));
			src.sendMessage(Text.of(TextColors.GREEN, "/f ranks leader <name>"));
			src.sendMessage(Text.of(TextColors.GREEN, "/f ranks officer <name>"));
			src.sendMessage(Text.of(TextColors.GREEN, "/f ranks helper <name>"));
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class sendMap implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

			src.sendMessage(Text.of(TextColors.GREEN, "-====================-", TextColors.DARK_GREEN, "[Map]", TextColors.GREEN, "-====================-"));
			src.sendMessage(Text.of(TextColors.YELLOW, "Unclaimed ", TextColors.DARK_GREEN, "Claimed by your faction ", TextColors.RED, "Claimed by other faction"));
			src.sendMessage(Text.of(TextColors.WHITE, "+ Your position")); 
			if (src instanceof Player) {
				Player player = (Player) src;
				int playerX = player.getLocation().getBlockX(), playerZ = player.getLocation().getBlockZ();
				FactionPlayer facP = new FactionPlayer();
				try {
					facP = facP.getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//player.sendMessage(Text.of(loc.getChunkPosition()));
				Vector3i[][] chunks = new Vector3i[11][17];
				for (int x = playerX + (5 * 16); x > playerX + (-5 * 16); x -= 16) {
					for (int y = playerZ + (8 * 16); y > playerZ + (-8 * 16); y -= 16) {
						int localX = ((x - playerX) / 16) + 5;
						int localY = ((y - playerZ) / 16) + 8;

						Location loc = new Location(player.getWorld(), x, 0, y);
						Vector3i chunk = loc.getChunkPosition();
						chunks[localX][localY] = chunk;
					}
				}

				for (Vector3i[] row : chunks) {
					Text.Builder sendMessage = Text.builder();
					for (Vector3i chunk : row) {

						if (FactionsMain.allFactionClaims.containsKey(player.getWorld().getName() + ":" + chunk)) {
							if (player.getLocation().getChunkPosition().equals(chunk)) {
								if (FactionsMain.allFactionClaims.get(player.getWorld().getName() + ":" + chunk) != facP.getFacID()){
									sendMessage.append(Text.of(TextColors.RED, "+"));
								}
								else {
									sendMessage.append(Text.of(TextColors.DARK_GREEN, "+"));
								}
							}
							else {
								if (FactionsMain.allFactionClaims.get(player.getWorld().getName() + ":" + chunk) != facP.getFacID()){
									sendMessage.append(Text.of(TextColors.RED, "#"));
								}
								else {
									sendMessage.append(Text.of(TextColors.DARK_GREEN, "#"));
								}
							}
						}
						else {
							if (player.getLocation().getChunkPosition().equals(chunk)) {
								sendMessage.append(Text.of(TextColors.YELLOW, "+"));
							}
							else {
								sendMessage.append(Text.of(TextColors.YELLOW, "#"));
							}
						}


					}
					player.sendMessage(Text.of(sendMessage));
					sendMessage.removeAll();
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class unclaimLand implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();	
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 2) {
					Vector3i location = player.getLocation().getChunkPosition();
					String worldName = player.getWorld().getName();
					String combined = worldName + ":" + location;
					if (FactionsMain.allFactionClaims.containsKey(combined) && FactionsMain.allFactionClaims.get(combined) == facP.getFacID()) {
						try {
							faction.removeClaim(combined);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Unclaiming land."));
						FactionsMain.allFactionClaims.remove(combined);
						return CommandResult.success();
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "This land is not claimed by you."));
					}
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You do not have the required rank to do this."));
					return CommandResult.success();
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class claimLand implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();	
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 2) {
					Vector3i location = player.getLocation().getChunkPosition();
					String worldName = player.getWorld().getName();
					String combined = worldName + ":" + location;
					if (FactionsMain.allFactionClaims.containsKey(combined)) {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "This land is already claimed."));
						return CommandResult.success();
					}
					player.sendMessage(Text.of(faction.getClaimCount(), " ", faction.calcMaxClaims()));
					//do some checks for claim counts and if they have enough
					if (faction.getClaimCount() >= faction.calcMaxClaims()) {

						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You do not have enough faction power to claim more land. Invite more members or buy bonus claims."));
						return CommandResult.success();
					}
					FactionsMain.allFactionClaims.put(combined, faction.getFacID());
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Claiming chunk."));
					try {
						faction.addClaim(combined);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You do not have the required rank to do this."));
					return CommandResult.success();
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class disbandFaction implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (facP.getFacID() == 1) {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				else {
					faction = faction.FactionFromID(facP.getFacID());
					if (faction.getFacLeaderUUID().equals(player.getUniqueId())) {
						try {
							Sponge.getServer().getConsole().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.RED, "Disbanding this faction: " + faction.getFacName()));
							faction.disband();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}
			}

			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class setLeaderRankName implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();		
				String name = args.getOne("Name").get().toString();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId())) {
					if (name.matches("^[0-9a-zA-Z ]{2,20}$")){
						faction.setLeaderRank(name);
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Setting Leader rank name to ", TextColors.WHITE, name));
						try {
							faction.saveFactionRanks();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Only A-Z and 0-9 are allowed."));
						return CommandResult.success();
					}
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Only the faction leader can do this."));
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class setOfficerRankName implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();		
				String name = args.getOne("Name").get().toString();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId())) {
					if (name.matches("^[0-9a-zA-Z ]{2,20}$")){
						faction.setOfficerRank(name);
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Setting Officer rank name to ",TextColors.WHITE,  name));
						try {
							faction.saveFactionRanks();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Only A-Z and 0-9 are allowed."));
						return CommandResult.success();
					}
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Only the faction leader can do this."));
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class setTitle implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();	
				User user = (User) args.getOne("player").get();
				String name = args.getOne("Name").get().toString();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(user.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 2) {
					if (name.matches("^[0-9a-zA-Z ]{2,20}$")){
						facP.setTitle(name);
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Setting Title of ", TextColors.WHITE, user.getName(), TextColors.GREEN, " to ", TextColors.WHITE, name));
						try {
							facP.saveUser();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Only A-Z and 0-9 are allowed."));
						return CommandResult.success();
					}
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Only the faction leader can do this."));
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class setHelperRankName implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();		
				String name = args.getOne("Name").get().toString();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId())) {
					if (name.matches("^[0-9a-zA-Z ]{2,20}$")){
						faction.setHelperRank(name);
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Setting Helper rank name to ", TextColors.WHITE, name));
						try {
							faction.saveFactionRanks();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Only A-Z and 0-9 are allowed."));
						return CommandResult.success();
					}
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Only the faction leader can do this."));
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class promoteUser implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				prepMessages prep = new prepMessages();
				List<String> messages = new ArrayList<>();
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				FactionPlayer promotedFacP = new FactionPlayer();
				Faction faction = new Faction();		
				User promoted = (User) args.getOne("player").get();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					promotedFacP = new FactionPlayer().getFacPlayerFromUUID(promoted.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 2) {
					if (facP.getFacID() == promotedFacP.getFacID()) {
						if (promotedFacP.getFacRank() == 3) {
							player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "That player cannot be promoted normally, use /f menu <player> to promote them to leader"));
							return CommandResult.success();
						}
						if (!faction.getFacLeaderUUID().equals(player.getUniqueId()) && promotedFacP.getFacRank() == 2) {
							player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You do not have the required rank to promote users to ", faction.getOfficerRank(), "!"));
							return CommandResult.success();
						}
						try {
							promotedFacP.setFacRank(promotedFacP.getFacRank() + 1);

						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}


						String rank = "";
						if (promotedFacP.getFacRank() == 2) {
							rank = faction.getHelperRank();
						}
						if (promotedFacP.getFacRank() == 3) {
							rank = faction.getOfficerRank();
						}
						try {
							promotedFacP.saveUser();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (promoted.isOnline()) {
							promoted.getPlayer().get().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You were promoted to ", rank));
						}
						player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Promoted ", TextColors.WHITE, promoted.getName(), TextColors.GREEN, " to ", rank));
						return CommandResult.success();
					}
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You do not have the required rank to promote users!"));
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}

	public static class demoteUser implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				prepMessages prep = new prepMessages();
				List<String> messages = new ArrayList<>();
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				FactionPlayer demotedFacP = new FactionPlayer();
				Faction faction = new Faction();		
				User demoted = (User) args.getOne("player").get();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					demotedFacP = new FactionPlayer().getFacPlayerFromUUID(demoted.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 2) {
					if (facP.getFacID() == demotedFacP.getFacID()) {
						if (demotedFacP.getFacRank() == 1) {
							player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "That player cannot be demoted further"));
							return CommandResult.success();
						}
						if (!faction.getFacLeaderUUID().equals(player.getUniqueId()) && demotedFacP.getFacRank() == 3) {
							player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You do not have the required rank to demote users from ", faction.getOfficerRank(), "!"));
							return CommandResult.success();
						}
						try {
							demotedFacP.setFacRank(demotedFacP.getFacRank() - 1);
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						String rank = "";
						if (demotedFacP.getFacRank() == 2) {
							rank = faction.getHelperRank();
						}
						if (demotedFacP.getFacRank() == 1) {
							rank = "member";
						}
						try {
							demotedFacP.saveUser();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (demoted.isOnline()) {
							demoted.getPlayer().get().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You were demoted to ", rank));
						}
						player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Demoted ", TextColors.WHITE, demoted.getName(), TextColors.GREEN, " to ", rank));
						return CommandResult.success();
					}
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You do not have the required rank to demote users!"));
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class chatFaction implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();
				try {
					facP = facP.getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				String message = "";
				if (facP.getFacID() == 1 && facP.inFacChat()) {
					facP.leaveChat();
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				if (facP.getFacID() == 1) {
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				faction = Faction.FactionFromID(facP.getFacID());
				if(args.getOne("Message").isPresent()) {
					message = args.getOne("Message").get().toString();
					List<UUID> members = faction.getMembersNoQuery();
					String rank = "";
					if (facP.getFacRank() == 2) {
						rank = faction.getHelperRank();
					}


					if (facP.getFacRank() == 3) {
						rank = faction.getOfficerRank();
					}
					if (faction.getFacLeaderUUID().equals(player.getUniqueId())) {
						rank = faction.getLeaderRank();
					}
					if (facP.getTitle() != null && !facP.getTitle().equals("NoTitleSet")) {
						rank = facP.getTitle();
					}
					for (UUID keyword : members){
						if (Sponge.getServer().getPlayer(keyword).isPresent()) {
							Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(TextColors.DARK_GREEN, "[F] ", TextColors.YELLOW, rank, " ", TextColors.WHITE, player.getName(), " >> ", message));
						}
					}
				}
				else {
					if (facP.inFacChat()) {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.RED, "Leaving faction chat, use /f ch to join"));
						facP.leaveChat();
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Joining faction chat, use /f ch to leave"));
						facP.joinChat();
					}

				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class createFaction implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();
				String newFactionName = args.getOne("Faction Name").get().toString();
				System.out.println(FactionsMain.allFactions.keySet());
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (facP.getFacID() != 1) {
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, " You are already in a faction! You must leave first before creating one."));
					return CommandResult.success();
				}
				if (!FactionsMain.allFactions.containsKey(newFactionName.toLowerCase()) && newFactionName.matches("^[0-9a-zA-Z ]{3,25}$")) {
					Vector3i location = player.getLocation().getChunkPosition();
					String worldName = player.getWorld().getName();
					String combined = worldName + ":" + location;
					if (FactionsMain.allFactionClaims.containsKey(combined)) {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "This land is already claimed, you must be in unclaimed land to create a faction."));
						return CommandResult.success();
					}

					try {
						faction.createNewFaction(newFactionName, player);

						player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Successfully created faction"));
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

					}
				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "That Faction name is taken or disallowed. Only A-Z and 0-9 are allowed."));
					return CommandResult.success();
				}
			}

			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class listFac implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			int max = 10;
			if (args.getOne("Page Number").isPresent()) {
				max = (int) args.getOne("Page Number").get() * 10;
				if (FactionsMain.factionsInOrder.size() == 0) {
					src.sendMessage(Text.of("There are currently no factions."));
					return CommandResult.success();
				}
				if (FactionsMain.factionsInOrder.size() > max-10) {
					src.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Factions Page ", args.getOne("Page Number").get(), " of ", ((FactionsMain.factionsInOrder.size() / 10) + ((FactionsMain.factionsInOrder.size()%10) > 0 ? 1 : 0))));
					src.sendMessage(Text.of(TextColors.WHITE, "Neutral ", TextColors.RED, "Enemy ", TextColors.AQUA, "Ally ", TextColors.DARK_AQUA, "Vassal ", TextColors.YELLOW, "Member of"));
				}
				else {
					src.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.WHITE, "There are no more pages to display."));	
				}
				for (int i = 10 ; i > 0 ; i--) {
					if (FactionsMain.factionsInOrder.size() > max-i) {
						Text.Builder sendMessage = Text.builder();
						boolean neutral = true;

						if (src instanceof Player) {

							FactionPlayer facP = new FactionPlayer();
							Player player = (Player) src;
							try {
								facP = facP.getFacPlayerFromUUID(player.getUniqueId());
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (facP.getFacID() == 1) {
							
								
							}
							else {
							if (Faction.FactionFromID(facP.getFacID()).getFacID() == (FactionsMain.factionsInOrder.get(max-i).getFacID())) {

								sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.YELLOW, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.YELLOW, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
								neutral = false;
							}
							if (Faction.FactionFromID(facP.getFacID()).getAllies().contains(FactionsMain.factionsInOrder.get(max-i).getFacID())) {

								sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.AQUA, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.AQUA, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
								neutral = false;
							}
							if (Faction.FactionFromID(facP.getFacID()).getEnemies().contains(FactionsMain.factionsInOrder.get(max-i).getFacID())){

								sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.RED, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.RED, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
								neutral = false;
							}
							if (Faction.FactionFromID(facP.getFacID()).getVassals().contains(FactionsMain.factionsInOrder.get(max-i).getFacID())) {

								sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.DARK_AQUA, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.DARK_AQUA, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
								neutral = false;
							}
						}
					}
						if (neutral) {
							sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.WHITE, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.WHITE, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
						}

						src.sendMessage(Text.of(sendMessage));
					}
				}
			}
			else {
				max = 10;
				src.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Factions Page 1 of ", ((FactionsMain.factionsInOrder.size() / 10) + ((FactionsMain.factionsInOrder.size()%10) > 0 ? 1 : 0)) ));
				src.sendMessage(Text.of(TextColors.WHITE, "Neutral ", TextColors.RED, "Enemy ", TextColors.AQUA, "Ally ", TextColors.DARK_AQUA, "Vassal ", TextColors.YELLOW, "Member of"));
				for (int i = 10 ; i > 0 ; i--) {
					if (FactionsMain.factionsInOrder.size() > max-i) {
						Text.Builder sendMessage = Text.builder();
						boolean neutral = true;
						if (src instanceof Player) {

							FactionPlayer facP = new FactionPlayer();
							Player player = (Player) src;
							try {
								facP = facP.getFacPlayerFromUUID(player.getUniqueId());
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (facP.getFacID() == 1) {

								
							}
							else {
							if (Faction.FactionFromID(facP.getFacID()).getFacID() == (FactionsMain.factionsInOrder.get(max-i).getFacID())) {

								sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.YELLOW, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.YELLOW, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
								neutral = false;
							}
							if (Faction.FactionFromID(facP.getFacID()).getAllies().contains(FactionsMain.factionsInOrder.get(max-i).getFacID())) {

								sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.AQUA, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.AQUA, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
								neutral = false;
							}
							if (Faction.FactionFromID(facP.getFacID()).getEnemies().contains(FactionsMain.factionsInOrder.get(max-i).getFacID())){

								sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.RED, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.RED, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
								neutral = false;
							}
							if (Faction.FactionFromID(facP.getFacID()).getVassals().contains(FactionsMain.factionsInOrder.get(max-i).getFacID())) {

								sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.DARK_AQUA, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.DARK_AQUA, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
								neutral = false;
							}
						}
					}
						if (neutral) {
							sendMessage.append(Text.of(TextColors.GREEN, max-i + 1, ". ", TextColors.WHITE, FactionsMain.factionsInOrder.get(max-i).getFacName(), TextColors.WHITE, " - ", FactionsMain.factionsInOrder.get(max-i).getMembersNoQuery().size(), " Members")).onClick(TextActions.runCommand("/f show " + FactionsMain.factionsInOrder.get(max-i).getFacName())).onHover(TextActions.showText(Text.of("Click me to view faction info for ", FactionsMain.factionsInOrder.get(max-i).getFacName()))).build();
						}

						src.sendMessage(Text.of(sendMessage));
					}
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class showFac implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				prepMessages prep = new prepMessages();
				List<String> messages = new ArrayList<>();
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (facP.getFacID() == 1 && !args.getOne("Faction Name").isPresent()) {
					//do wilderness message
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				if (args.getOne("Faction Name").isPresent()) {
					String facName = args.getOne("Faction Name").get().toString();
					if (FactionsMain.allFactions.containsKey(facName.toLowerCase())) {
						faction = FactionsMain.allFactions.get(facName.toLowerCase());
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "That faction does not exist. Use /f list to see available factions."));
						return CommandResult.success();
					}
				}
				else if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				try {
					messages = prep.prepFaction(faction);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				boolean regMsg = true;
				for (String message: messages) 
				{ 
					if (message.contains("PREP-MEMBERS")) {
						regMsg = false;
						message = message.replaceAll("PREP-MEMBERS", "");
						String[] splitMessage = message.split(",");
						Text.Builder sendMembers = Text.builder();
						for (String clickableMessage : splitMessage) {
							if (clickableMessage.equals("")) {
								break;
							}		
							Text.Builder temp = Text.builder();
							temp.append(Text.of(TextColors.WHITE, clickableMessage, ", ")).onClick(TextActions.runCommand("/f menu" + clickableMessage)).onHover(TextActions.showText(Text.of("Click me to view player menu for ", clickableMessage)));
							sendMembers.append(Text.of(temp));
						}
						sendMembers.build();
						player.sendMessage(Text.of(TextColors.GREEN, "Members : ", sendMembers));
					}
					else if (message.contains("PREP-ALLIES")) {
						regMsg = false;
						message = message.replaceAll("PREP-ALLIES", "");
						String[] splitMessage = message.split(",");
						Text.Builder sendAllies = Text.builder();
						for (String clickableMessage : splitMessage) {
							if (clickableMessage.equals("")) {
								break;
							}
							Text.Builder temp = Text.builder();
							temp.append(Text.of(TextColors.AQUA, clickableMessage, ", ")).onClick(TextActions.runCommand("/f show " + clickableMessage)).onHover(TextActions.showText(Text.of("Click me to view faction info.")));
							sendAllies.append(Text.of(temp));
						}
						sendAllies.build();
						player.sendMessage(Text.of(TextColors.AQUA, "Allies : ", sendAllies));
					}
					else if (message.contains("PREP-VASSALS")) {

						regMsg = false;
						message = message.replaceAll("PREP-VASSALS", "");
						String[] splitMessage = message.split(",");
						Text.Builder sendVassals = Text.builder();
						for (String clickableMessage : splitMessage) {
							if (clickableMessage.equals("")) {
								break;
							}
							Text.Builder temp = Text.builder();
							temp.append(Text.of(TextColors.DARK_AQUA, clickableMessage, ", ")).onClick(TextActions.runCommand("/f show " + clickableMessage)).onHover(TextActions.showText(Text.of("Click me to view faction info.")));
							sendVassals.append(Text.of(temp));
						}
						sendVassals.build();
						player.sendMessage(Text.of(TextColors.DARK_AQUA, "Vassals : ", sendVassals));
					}
					else if (message.contains("PREP-ENEMIES")) {
						regMsg = false;
						message = message.replaceAll("PREP-ENEMIES", "");
						String[] splitMessage = message.split(",");
						Text.Builder sendEnemies = Text.builder();
						for (String clickableMessage : splitMessage) {
							if (clickableMessage.equals("")) {
								break;
							}
							Text.Builder temp = Text.builder();
							temp.append(Text.of(TextColors.RED, clickableMessage, ", ")).onClick(TextActions.runCommand("/f show " + clickableMessage)).onHover(TextActions.showText(Text.of("Click me to view faction info.")));
							sendEnemies.append(Text.of(temp));
						}
						sendEnemies.build();
						player.sendMessage(Text.of(TextColors.RED, "Enemies : ", sendEnemies));
					}
					if (regMsg) {
						src.sendMessage(Text.of(message.replaceAll("&", "§")));
					}
				}
			}

			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class inviteUser implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				prepMessages prep = new prepMessages();
				List<String> messages = new ArrayList<>();
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				FactionPlayer invitedFacP = new FactionPlayer();
				Faction faction = new Faction();		
				Player invited = (Player) args.getOne("player").get();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					invitedFacP = new FactionPlayer().getFacPlayerFromUUID(invited.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (facP.getFacID() == 1 || facP.getFacID() == 0) {
					//do wilderness message
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				if (Faction.FactionFromID(facP.getFacID()) != null) {
					faction = Faction.FactionFromID(facP.getFacID());
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 1) {
					if (facP.getFacID() == invitedFacP.getFacID()) {
						player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "That player is already in your faction"));
						return CommandResult.success();
					}
					faction.addInvite(invited.getName());

					invited.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You have been invited to ",TextColors.WHITE, faction.getFacName(),TextColors.GREEN, " by ",TextColors.WHITE, src.getName(), TextColors.GREEN, ". This invite will expire in 5 minutes."));
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Invite sent to ", TextColors.WHITE, invited.getName(), TextColors.GREEN, "."));
					Text.Builder sendInviteButton = Text.builder();
					sendInviteButton.append(Text.of(TextColors.AQUA, TextStyles.UNDERLINE, " [Accept Invite]")).onClick(TextActions.runCommand("/f join " + faction.getFacName())).onHover(TextActions.showText(Text.of("Click me to accept this invite"))).build();
					invited.sendMessage(Text.of(FactionsMain.defaultMessage, sendInviteButton));

				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You do not have the required rank to invite users!"));
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
	public static class reqAlly implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();


				String facName = args.getOne("Faction Name").get().toString();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (facP.getFacID() == 1) {
					//do wilderness message
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				else {
					faction = Faction.FactionFromID(facP.getFacID());
					if (FactionsMain.allFactions.containsKey(facName.toLowerCase())) {
						Faction facToAlly = FactionsMain.allFactions.get(facName.toLowerCase());
						if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 2 && facToAlly.getFacID() != faction.getFacID()) {
							if (faction.AllyReqs.containsKey(facToAlly.getFacID())  && !faction.isAlly(facToAlly.getFacID())) {								
								if (facToAlly.getFacID() == faction.getFacID()) {
									player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You cannot ally with yourself"));
									return CommandResult.success();
								}
								try {
									faction.addAlly(facToAlly.getFacID());
									facToAlly.addAlly(faction.getFacID());
									faction.saveAllies();
									facToAlly.saveAllies();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							else {
								if (facToAlly.getFacID() != faction.getFacID()) {
									if (faction.isEnemy(facToAlly.getFacID())) {
										player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You cannot ally with an enemy, make peace first."));
										return CommandResult.success();
									}
						    player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Sending ally request to ", facToAlly.getFacName()));
							facToAlly.addAllyReq(faction.getFacID());
								}else {
									player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You cannot ally with yourself"));
									return CommandResult.success();
								}
						}
					}
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.RED, "That faction does not exist."));
					}
				}
			}

			return CommandResult.success();
		}
	}
	public static class reqPeace implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();


				String facName = args.getOne("Faction Name").get().toString();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (facP.getFacID() == 1) {
					//do wilderness message
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				else {
					faction = faction.FactionFromID(facP.getFacID());
					if (FactionsMain.allFactions.containsKey(facName.toLowerCase())) {
						Faction facToPeace = FactionsMain.allFactions.get(facName.toLowerCase());
						if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 2 && facToPeace.getFacID() != faction.getFacID()) {
							if (faction.isAlly(facToPeace.getFacID())) {								
								try {
									faction.removeAlly(facToPeace.getFacID());
									facToPeace.removeAlly(faction.getFacID());
									faction.saveAllies();
									facToPeace.saveAllies();
									return CommandResult.success();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							else {
								if (facToPeace.getFacID() != faction.getFacID()) {
						    player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Sending peace request to ", facToPeace.getFacName()));
							facToPeace.addNeutralReq(faction.getFacID());
							return CommandResult.success();
								}
						}
					}
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.RED, "That faction does not exist."));
					}
					if (FactionsMain.allFactions.containsKey(facName.toLowerCase())) {
						Faction facToPeace = FactionsMain.allFactions.get(facName.toLowerCase());
						if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 2  && facToPeace.getFacID() != faction.getFacID()) {
							if (faction.NeutralReqs.containsKey(facToPeace.getFacID())) {								
								try {
									if (facToPeace.getFacID() == faction.getFacID()) {
										player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You cannot make peace with yourself"));
										return CommandResult.success();
									}
									faction.removeEnemy(facToPeace.getFacID());
									facToPeace.removeEnemy(faction.getFacID());
									faction.saveEnemies();
									facToPeace.saveEnemies();
								} catch (SQLException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
							}
							else {
								if (facToPeace.getFacID() != faction.getFacID()) {
						    player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Sending peace request to ", facToPeace.getFacName()));
							facToPeace.addNeutralReq(faction.getFacID());
								}
								else {
									player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You cannot make peace with yourself"));
									return CommandResult.success();
								}
						}
					}
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.RED, "That faction does not exist."));

					}


				}
			}

			return CommandResult.success();
		}
	}
	public static class declareEnemy implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();


				String facName = args.getOne("Faction Name").get().toString();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (facP.getFacID() == 1) {
					//do wilderness message
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				else {
					faction = faction.FactionFromID(facP.getFacID());
					Faction facToEnemy = new Faction();
					if (FactionsMain.allFactions.containsKey(facName.toLowerCase())) {
						 facToEnemy = FactionsMain.allFactions.get(facName.toLowerCase());
						if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() > 2 && facToEnemy.getFacID() != faction.getFacID() ) {	
							if (faction.isEnemy(facToEnemy.getFacID())) {
								return CommandResult.success();
							}
							if (facToEnemy.getFacID() == faction.getFacID()) {
								player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You cannot declare yourself an enemy"));
								return CommandResult.success();
							}
							try {
								faction.addEnemy(facToEnemy.getFacID());
								faction.saveEnemies();
								facToEnemy.addEnemy(faction.getFacID());
								facToEnemy.saveEnemies();
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else {
						if (facToEnemy.getFacID() != faction.getFacID()) {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.RED, "That faction does not exist."));
						}
						else {
							player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You cannot declare yourself an enemy"));
							return CommandResult.success();
						}
					}


				}
			}

			return CommandResult.success();
		}
	}

	public static class joinFaction implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();


				String facName = args.getOne("Faction Name").get().toString();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (facP.getFacID() != 1) {
					//do wilderness message
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are already in a faction, you must leave it first."));
					return CommandResult.success();
				}
				else {
					if (FactionsMain.allFactions.containsKey(facName.toLowerCase())) {
						Faction facToJoin = FactionsMain.allFactions.get(facName.toLowerCase());
						if (facToJoin.invites.containsKey(src.getName())) {
							try {
								facP.setFacID(facToJoin.getFacID());
								facToJoin.addMember(player.getUniqueId());
								facToJoin.removeInvite(player.getName());
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.RED, "That faction does not exist."));
					}
				}

			}
			return CommandResult.success();
		}
	}
	public static class facMenu implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();
				User user = (User) args.getOne("player").get();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (facP.getFacID() != 1) {
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					Text.Builder sendButton = Text.builder();
					player.sendMessage(Text.of(TextColors.GREEN, "-=======- ",FactionsMain.defaultMessage, TextColors.GREEN, "-=======-"));
					player.sendMessage(Text.of(""));
					player.sendMessage(Text.of(" ",user.getName()));
					player.sendMessage(Text.of(""));
					sendButton.append(Text.of(TextColors.GREEN, " [Promote to Leader]")).onClick(TextActions.runCommand("/f leader " + user.getName())).onHover(TextActions.showText(Text.of("Click me to promote this member to Faction leader."))).build();
					player.sendMessage(Text.of(sendButton));
					player.sendMessage(Text.of(""));
					sendButton.removeAll();
					sendButton.append(Text.of(TextColors.GREEN, " [Promote]")).onClick(TextActions.runCommand("/f promote " + user.getName())).onHover(TextActions.showText(Text.of("Click me to promote this member."))).build();
					player.sendMessage(Text.of(sendButton));
					player.sendMessage(Text.of(""));
					sendButton.removeAll();
					sendButton.append(Text.of(TextColors.RED, " [Demote]")).onClick(TextActions.runCommand("/f demote " + user.getName())).onHover(TextActions.showText(Text.of("Click me to demote this member."))).build();
					player.sendMessage(Text.of(sendButton));
					player.sendMessage(Text.of(""));
					sendButton.removeAll();
					sendButton.append(Text.of(TextColors.DARK_RED, " [Kick from faction]")).onClick(TextActions.runCommand("/f kick " + user.getName())).onHover(TextActions.showText(Text.of("Click me to promote this member."))).build();
					player.sendMessage(Text.of(sendButton));
					return CommandResult.success();
				}
				else {
					if (Faction.FactionFromID(facP.getFacID()) != null) {
						faction = Faction.FactionFromID(facP.getFacID());

					}
				}

			}
			return CommandResult.success();
		}
	}
	public static class leaveFaction implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (facP.getFacID() == 1) {
					//do wilderness message
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				else {
					if (faction.FactionFromID(facP.getFacID()) != null) {
						faction = faction.FactionFromID(facP.getFacID());
						try {
							if (faction.getFacLeaderUUID().equals(player.getUniqueId())) {
								player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are the leader, you cannot leave. You must promote someone first."));
								return CommandResult.success();
							}
							faction.removeMember(player.getUniqueId());

						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							facP.setFacID(1);
							facP.setFacRank(1);
							facP.setTitle("NoTitleSet");
							facP.saveUser();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
			return CommandResult.success();
		}
	}

	public static class kickUser implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				prepMessages prep = new prepMessages();
				List<String> messages = new ArrayList<>();
				Player player = (Player) src;
				FactionPlayer facP = new FactionPlayer();
				FactionPlayer invitedFacP = new FactionPlayer();
				Faction faction = new Faction();		
				User invited = (User) args.getOne("player").get();

				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					invitedFacP = new FactionPlayer().getFacPlayerFromUUID(invited.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (facP.getFacID() == 1 || facP.getFacID() == 0) {
					//do wilderness message
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return CommandResult.success();
				}
				if (faction.FactionFromID(facP.getFacID()) != null) {
					faction = faction.FactionFromID(facP.getFacID());
				}
				//implement the rank shit too
				if (faction.getFacLeaderUUID().equals(player.getUniqueId()) || facP.getFacRank() == 1) {
					if (facP.getPlayerUUID().equals(invitedFacP.getPlayerUUID()) || invitedFacP.getFacRank() > 1 || faction.getFacLeaderUUID().equals(invitedFacP.getPlayerUUID())) {
						player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You cannot kick this user"));
						return CommandResult.success();
					}
					if (facP.getFacID() == invitedFacP.getFacID()) {
						try {
							faction.kickMember(invited.getUniqueId());
							invitedFacP.setFacID(1);
							invitedFacP.setFacRank(1);
							invitedFacP.setTitle("NoTitleSet");
							invitedFacP.saveUser();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return CommandResult.success();
					}
					else {
						player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "That player is not in your faction"));
					}
					faction.addInvite(invited.getName());
					if (invited.isOnline()) {
						Player inv = Sponge.getServer().getPlayer(invited.getUniqueId()).get();
						inv.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You have been kicked from ",TextColors.WHITE, faction.getFacName(),TextColors.GREEN, " by ",TextColors.WHITE, src.getName(), TextColors.GREEN, "."));

					}
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "Kicked player: ", TextColors.WHITE, invited.getName(), TextColors.GREEN, " from faction."));

				}
				else {
					player.sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "You do not have the required rank to invite users!"));
				}
			}
			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
}