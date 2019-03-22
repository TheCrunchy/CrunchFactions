

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.google.inject.Inject;

import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Relational;
import me.rojo8399.placeholderapi.Source;
import me.rojo8399.placeholderapi.Token;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

@Plugin(id = "crunchfactions", name = "Factions by Crunch", version = "1.0", description = "Factions plugin, land protection, raiding, etc", dependencies = { @Dependency(id = "placeholderapi") })
public class FactionsMain {
	@Inject
	@ConfigDir(sharedRoot = false)
	public Path root;
	
	public static Path staticRoot;
	public static ArrayList <Faction> factionsInOrder = new ArrayList<Faction>();
	public static  HashMap<String, Faction> allFactions = new HashMap<>();
	public static  HashMap<UUID, FactionPlayer> allFactionPlayers = new HashMap<>();
	public static  HashMap<String, Integer> allFactionClaims = new HashMap<>();
	public HashMap<String, Long> everyInvite = new HashMap<>();
	
	public static Text defaultMessage = Text.of(TextColors.DARK_GREEN, "[Factions] ", TextColors.AQUA);
	PlaceholderService s;
	ArrayList <Faction> sortedFactions = new ArrayList<>();
	@Listener
    public void onServerFinishLoad(GameStartingServerEvent event) throws SQLException {
    	Sponge.getCommandManager().register(this, testFac, "factest");
    	Sponge.getCommandManager().register(this, facAdmin, "facadmin", "fadmin");
     	Sponge.getCommandManager().register(this, facMain, "f", "faction");
    	Sponge.getEventManager().registerListeners(this, new login());
    	Sponge.getEventManager().registerListeners(this, new chatEvent());
       	Sponge.getEventManager().registerListeners(this, new moveEvent());
       	Sponge.getEventManager().registerListeners(this, new protectEvents());
    	//Sponge.getEventManager().registerListeners(this, new protectEvents.fireSpread());
    	s = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
    	staticRoot = root;
		Task updateFactionsOrder = Task.builder().execute(new sorterTask())
	            .delayTicks(1)
	            .interval(1, TimeUnit.MINUTES)
	            .name("Update factions order task").submit(this);
		Task task = Task.builder().execute(new inviteTask())
	            .delayTicks(1)
	            .interval(1, TimeUnit.MINUTES)
	            .name("Update invites task").submit(this);
    	allFactions.put("Wilderness", Faction.loadWilderness());
    	DatabaseCreation dbCreate = new DatabaseCreation(sql, root);
    	String query = "SELECT * from FactionData";
    	String query2 = "SELECT * from LandClaims";
		String uri = "jdbc:sqlite:" + root + "/Factions.db";
		try (Connection conn2 = FactionsMain.getDataSource(uri).getConnection()) {
			PreparedStatement stmt = conn2.prepareStatement(query); {
				ResultSet results = stmt.executeQuery(); {
					while(results.next()) {
						Faction initialLoad = new Faction();
						if (initialLoad.FactionFromID(results.getInt("FacID")) != null){
						allFactions.put(initialLoad.getFacName(), initialLoad);
						}
						else {
							if (results.getInt("FacID") == 1) {
							}
							else {

							initialLoad.setFacDescription(results.getString("FacDescription"));
							initialLoad.setFacName(results.getString("FacName"));
							initialLoad.setFacLeader(UUID.fromString(results.getString("FacLeader")));
							initialLoad.setBankBalance(results.getDouble("FacBankBalance"));
							
							initialLoad.setFacID(results.getInt("FacID"));
							initialLoad.setTaxRate(results.getDouble("FacTax"));
							initialLoad.setLeaderRank(results.getString("LeaderRankName"));
							initialLoad.setOfficerRank(results.getString("OfficerRankName"));
							initialLoad.setHelperRank(results.getString("HelperRankName"));
							if (results.getString("Enemies") != null) {
							String enemies = results.getString("Enemies");
							if (enemies.startsWith(":")) {
								enemies = enemies.replaceFirst(":", "");
							}
							String[] enemySplit = enemies.split(":");
						
							for (String enemy : enemySplit) {
								initialLoad.addEnemy(Integer.valueOf(enemy));
							 }
							}
							if (results.getString("Allies") != null) {
							String allies = results.getString("Allies");
							if (allies.startsWith(":")) {
								allies = allies.replaceFirst(":", "");
							}
							String[] allySplit = allies.split(":");
							
							for (String ally : allySplit) {
								initialLoad.addAlly(Integer.valueOf(ally));
							 }
							}
							if (results.getString("Vassals") != null) {
							String vassals = results.getString("Vassals");
							if (vassals.startsWith(":")) {
								vassals = vassals.replaceFirst(":", "");
							}
							String[] vassalSplit = vassals.split(":");
							for (String vassal : vassalSplit) {
								initialLoad.addVassal(Integer.valueOf(vassal));
							 }
							}
							if (results.getDouble("PurchasedBonus") != 0) {
								initialLoad.setBonusOnLoad((int) results.getDouble("PurchasedBonus"));
								
							}
							double X = 0, Y = 0, Z = 0;
							World world = null;
			
							if (results.getDouble("FactionHomeX") != 0) {
								X = results.getDouble("FactionHomeX");
								
							}
		
							if (results.getDouble("FactionHomeY") != 0) {
								Y = results.getDouble("FactionHomeY");
								
							}
	
							if (results.getDouble("FactionHomeZ") != 0) {
								Z = results.getDouble("FactionHomeZ");
								
							}
							if (results.getString("FactionHomeWorld") != null) {
								world = Sponge.getServer().getWorld(results.getString("FactionHomeWorld").toString()).get();
							}
							@SuppressWarnings("unchecked")
							Location loc = new Location(world, X, Y, Z);
							initialLoad.setFactionHome(loc);
							allFactions.put(initialLoad.getFacName().toLowerCase(), initialLoad);
							}
						
							}
						
						}
					
				}
			}
		}
		try (Connection conn3 = FactionsMain.getDataSource(uri).getConnection()) {
			PreparedStatement stmt2 = conn3.prepareStatement(query2); {
				ResultSet results = stmt2.executeQuery(); {
					while(results.next()) {
						String claim = results.getString("WorldNameChunkID");
						int facID = results.getInt("FacID");
						allFactionClaims.put(claim, facID);
						Faction.FactionFromID(facID).addToClaimCount();
					}
				}
			}
		}
		for (Entry<String, Faction> fac : allFactions.entrySet()) {
			fac.getValue().getMembers();
		}
        s.loadAll(this, this).forEach(builder -> {
            if (builder.getId().startsWith("fac-")) {
                builder.author("Crunch");
                builder.version("1");
                try {
                    builder.buildAndRegister();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        s.loadAll(this, this).forEach(builder -> {
        	   System.out.println(builder.getId());
           
       });
    }
	@Placeholder(id = "fac-name")
	public String facName(@Source CommandSource src) throws SQLException {
		if (src instanceof Player) {
			FactionPlayer facP = new FactionPlayer().getFacPlayerFromUUID(((Player) src).getUniqueId());
			if (facP.getFacID() != 1) {
			return " " + Faction.FactionFromID(facP.getFacID()).getFacName();
		}
		}
		return null;
	}
	@Placeholder(id = "fac-rank")
	public String facRank(@Source CommandSource src) throws SQLException {
		if (src instanceof Player) {
			FactionPlayer facP = new FactionPlayer().getFacPlayerFromUUID(((Player) src).getUniqueId());
		
			if (facP.getFacID() != 1) {
				Faction faction = Faction.FactionFromID(facP.getFacID());
				if (faction.getFacLeaderUUID().equals(((Player) src).getUniqueId())) {
					return faction.getLeaderRank();
				}
			if (facP.getFacRank() == 2) {
				return faction.getHelperRank();
			}
			if (facP.getFacRank() == 3) {
				return faction.getLeaderRank();
			}
		}
		}
		return null;
	}
	public static DataSource getDataSource(String jdbcUrl) throws SQLException {
		if (sql == null) {
			sql = Sponge.getServiceManager().provide(SqlService.class).get();
		}
		return sql.getDataSource(jdbcUrl);
	}
	
	public ArrayList <Faction> sortFactions() throws SQLException{
		ArrayList <Faction> sortedFacs = new ArrayList<Faction>();
		for (Entry<String, Faction> fac : allFactions.entrySet()) {
			sortedFacs.add(fac.getValue());
			fac.getValue().clearMembers();
			fac.getValue().getMembers();
		}
	    for (int i = 0 ; i < sortedFacs.size() ; i++) {
	        for (int j = i+1; j < sortedFacs.size() ; j++) {
	            if (sortedFacs.get(i).getMembersNoQuery().size() < sortedFacs.get(j).getMembersNoQuery().size()) {
	                Collections.swap(sortedFacs, i, j);
	            }
	        }
	    }
		return sortedFacs;
	}
	public class sorterTask implements Runnable {
	public void run() {
		try {
			factionsInOrder = sortFactions();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	}
	public class inviteTask implements Runnable {
	public void run() {
		for (Entry<String, Faction> entry : allFactions.entrySet()) {
			String key = entry.getKey();
			Faction checkFac = entry.getValue();
			for (Entry<String, Long> playerEntry : checkFac.invites.entrySet()) {
				String playerName = playerEntry.getKey();
				long time = playerEntry.getValue();
				if ((time + 300) <= (System.currentTimeMillis() / 1000)) {
					Sponge.getServer().getConsole().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.RED, " expired invite " + playerName));
					checkFac.removeInvite(playerName);
				}
				// ...
			}
			// ...
		}
	}
	}
	public static Optional<User> getUser(UUID owner) {
		Optional<UserStorageService> userStorage = Sponge.getServiceManager().provide(UserStorageService.class);
		return userStorage.get().get(owner);
	}
	//this shite for getting all FacIDs select FacID from FactionData
    @Inject
	@DefaultConfig(sharedRoot = false)
	private ConfigurationLoader<CommentedConfigurationNode> configManager;
	public static SqlService sql;
	
	//                GenericArguments.optional(
	//   GenericArguments.remainingJoinedStrings(Texts.of("args"))
	//)
	
    CommandSpec testFac = CommandSpec.builder()
    	    .description(Text.of("Tests the faction command"))
    	    .permission("Test.Fac")
    	    .executor(new TestFac())
    	    .build();
    CommandSpec facShow = CommandSpec.builder()
    	    .description(Text.of("Show the players faction information"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("Faction Name"))))
    	    .executor(new UserCommands.showFac())
    	    .build();
    CommandSpec facChat = CommandSpec.builder()
    	    .description(Text.of("Join the faction chat channel or send a channel message"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.optional(GenericArguments.remainingJoinedStrings(Text.of("Message"))))
    	    .executor(new UserCommands.chatFaction())
    	    .build();
    CommandSpec facList = CommandSpec.builder()
    	    .description(Text.of("Show the players a list of all factions"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.optional(GenericArguments.integer(Text.of("Page Number"))))
    	    .executor(new UserCommands.listFac())
    	    .build();
    CommandSpec facLeave = CommandSpec.builder()
    	    .description(Text.of("Leave a faction"))
    	    .permission("Factions.user")
    	    .executor(new UserCommands.leaveFaction())
    	    .build();
    CommandSpec facDisband = CommandSpec.builder()
    	    .description(Text.of("Disband a faction"))
    	    .permission("Factions.user")
    	    .executor(new UserCommands.disbandFaction())
    	    .build();
    CommandSpec facKick = CommandSpec.builder()
    	    .description(Text.of("Invite player to faction"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.user(Text.of("player")))
    	    .executor(new UserCommands.kickUser())
    	    .build();
    CommandSpec facInvite = CommandSpec.builder()
    	    .description(Text.of("Invite player to faction"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.player(Text.of("player")))
    	    .executor(new UserCommands.inviteUser())
    	    .build();
    CommandSpec facPromote = CommandSpec.builder()
    	    .description(Text.of("Promote a player in the faction"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.user(Text.of("player")))
    	    .executor(new UserCommands.promoteUser())
    	    .build();
    CommandSpec facDemote = CommandSpec.builder()
    	    .description(Text.of("Demote a player in the faction"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.user(Text.of("player")))
    	    .executor(new UserCommands.demoteUser())
    	    .build();
    CommandSpec facMenu = CommandSpec.builder()
    	    .description(Text.of("Invite player to faction"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.user(Text.of("player")))
    	    .executor(new UserCommands.facMenu())
    	    .build();
    CommandSpec facJoin = CommandSpec.builder()
    	    .description(Text.of("Join a faction"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.remainingJoinedStrings(Text.of("Faction Name")))
    	    .executor(new UserCommands.joinFaction())
    	    .build();
     CommandSpec facEnemy = CommandSpec.builder()
    	    .description(Text.of("Declare a faction an enemy"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.remainingJoinedStrings(Text.of("Faction Name")))
    	    .executor(new UserCommands.declareEnemy())
    	    .build();
     CommandSpec facAlly = CommandSpec.builder()
     	    .description(Text.of("Request an alliance with a faction"))
     	    .permission("Factions.user")
     	    .arguments(GenericArguments.remainingJoinedStrings(Text.of("Faction Name")))
     	    .executor(new UserCommands.reqAlly())
     	    .build();
     CommandSpec facPeace = CommandSpec.builder()
     	    .description(Text.of("Make peace with an enemy faction"))
     	    .permission("Factions.user")
     	    .arguments(GenericArguments.remainingJoinedStrings(Text.of("Faction Name")))
     	    .executor(new UserCommands.reqPeace())
     	    .build();
    CommandSpec facCreate = CommandSpec.builder()
    	    .description(Text.of("Join a faction"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.remainingJoinedStrings(Text.of("Faction Name")))
    	    .executor(new UserCommands.createFaction())
    	    .build();
    CommandSpec facHome = CommandSpec.builder()
    	    .description(Text.of("Teleport to faction"))
    	    .permission("Factions.user")
    	    .executor(new UserCommands.teleportToFactionHome())
    	    .build();
    CommandSpec facClaim = CommandSpec.builder()
    	    .description(Text.of("Claim land"))
    	    .permission("Factions.user")
    	    .executor(new UserCommands.claimLand())
    	    .build();
    CommandSpec facUnclaim = CommandSpec.builder()
    	    .description(Text.of("Unclaim land"))
    	    .permission("Factions.user")
    	    .executor(new UserCommands.unclaimLand())
    	    .build();
    CommandSpec facRankOfficer = CommandSpec.builder()
    	    .description(Text.of("Set rank name for officer"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.remainingJoinedStrings(Text.of("Name")))
    	    .executor(new UserCommands.setOfficerRankName())
    	    .build();
    CommandSpec facRankHelper = CommandSpec.builder()
    	    .description(Text.of("Set rank name for helper"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.remainingJoinedStrings(Text.of("Name")))
    	    .executor(new UserCommands.setHelperRankName())
    	    .build();
    CommandSpec facRankLeader = CommandSpec.builder()
    	    .description(Text.of("Set rank name for helper"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.remainingJoinedStrings(Text.of("Name")))
    	    .executor(new UserCommands.setLeaderRankName())
    	    .build();
    CommandSpec facTitle = CommandSpec.builder()
    	    .description(Text.of("Set title for a user"))
    	    .permission("Factions.user")
    	    .arguments(GenericArguments.user(Text.of("player")),GenericArguments.remainingJoinedStrings(Text.of("Name")))
    	    .executor(new UserCommands.setTitle())
    	    .build();
    CommandSpec facRanks = CommandSpec.builder()
    	    .description(Text.of("Set the rank names"))
    	    .permission("Factions.user")
    	    .child(facRankOfficer, "officer")
    	    .child(facRankHelper, "helper")
    	    .child(facRankLeader, "leader")
    	    .executor(new UserCommands.sendRankInfo())
    	    .build();
    CommandSpec facMap = CommandSpec.builder()
    	    .description(Text.of("Teleport to faction"))
    	    .permission("Factions.user")
    	    .executor(new UserCommands.sendMap())
    	    .build();
	CommandSpec facMain = CommandSpec.builder()
			.description(Text.of("Factions main command"))
			.permission("Factions.main")
			.child(facShow, "show")   	  
			.child(facInvite, "invite")
			.child(facKick, "kick")
			.child(facJoin, "join")
			.child(facLeave, "leave")
			.child(facMenu, "menu")
			.child(facCreate, "create")
			.child(facDisband, "disband")
			.child(facList, "list")
			.child(facChat, "chat")
			.child(facChat, "ch")
			.child(facPromote, "promote")
			.child(facDemote, "demote")
			.child(facRanks, "ranks")
			.child(facTitle, "title")
			.child(facHome, "home")
			.child(facClaim, "claim")
			.child(facUnclaim, "unclaim")
			.child(facMap, "map")
			.child(facEnemy, "enemy")
			.child(facPeace, "neutral")
			.child(facAlly, "ally")
			.build();
	
    CommandSpec facTestMul = CommandSpec.builder()
    	    .description(Text.of("Test multiple faction creation."))
    	    .permission("Factions.admin")
    	    .executor(new AdminCommands.testMultipleFac())
    	    .build();
	CommandSpec facAdmin = CommandSpec.builder()
			.description(Text.of("Factions main command"))
			.permission("Factions.admin")
			.child(facTestMul, "test")   	  
			.build();
    public class login {

        @Listener
        public void onSomeEvent(ClientConnectionEvent.Join event, @First Player player) throws SQLException {
            // Do something with the event
        	FactionPlayer facP;
        	if (!allFactionPlayers.containsKey(player.getUniqueId())) {
        	facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
        	}
        	else {
        		facP = allFactionPlayers.get(player.getUniqueId());
        		facP.setLastTerritory(0);
        	}
        	//facP.saveUser();
        	allFactionPlayers.put(player.getUniqueId(), facP);
        	List<String> taxMessages = null;
    		try {
    			taxMessages = facP.getTaxMessage();
    		} catch (SQLException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		if (taxMessages != null) {
        	for (String keyword : taxMessages){
        		player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, " ", keyword));
        	}
        }
    }
    }
//block break
//block place
//block interact
//fire
//lava
//water
//crop trample
//animal interaction
//attack
//arrows
//teleport
//pistons
//explosion
}
