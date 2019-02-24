

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;

import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.flowpowered.math.vector.Vector3i;

public class Faction {
	private int FacID;

	public void setFacID(int ID) {
		this.FacID = ID;
	}
	public int getFacID() {
		return FacID;
	}
	private Location factionHome;
	
	public Location getFactionHome() {
		return factionHome;
	}
	
	public void setFactionHome(Location loc) {
		factionHome = loc;
	}
	private double claimCount;
	private double maxClaims;
	private double maxBonus = 50;
	private double purchasedBonus;
	public double calcMaxClaims() {
		maxClaims = (getMembersNoQuery().size() * 10) + purchasedBonus;
		return maxClaims;
	}
	public double getClaimCount() {
		return claimCount;
	}
	public void addToClaimCount() {
		claimCount += 1;
	}
	public void addBonus(int num) {
		if (purchasedBonus + num > maxBonus) {
			return;
		}
		else {
			purchasedBonus += num;
		}
	}
	public void setBonusOnLoad(int num) {
		purchasedBonus = num;
	}
	public void removeClaim(String claimLocation) throws SQLException {
		claimCount -= 1;
		//do the query to add the claim
    	String uri = "jdbc:sqlite:" + FactionsMain.staticRoot + "/Factions.db";
    	ArrayList <String> queries = new ArrayList<>();
    	queries.add("DELETE FROM LandClaims where WorldNameChunkID = '" + claimLocation + "'");
            Connection conn = FactionsMain.getDataSource(uri).getConnection();
 			Statement statement = conn.createStatement();
 			for (String query : queries) {
 				statement.addBatch(query);
 			}
 			statement.executeBatch();
 			statement.close();
 			conn.close();
	}
	public void addClaim(String claimLocation) throws SQLException {
		claimCount += 1;
		Calendar cal = Calendar.getInstance();
		//do the query to add the claim
    	String uri = "jdbc:sqlite:" + FactionsMain.staticRoot + "/Factions.db";
    	ArrayList <String> queries = new ArrayList<>();
    	queries.add("INSERT INTO LandClaims (WorldNameChunkID, FacID, DateClaimed) values ('"+ claimLocation + "', '" + this.FacID + "', '" + cal.getTime().toString() + "')");
            Connection conn = FactionsMain.getDataSource(uri).getConnection();
 			Statement statement = conn.createStatement();
 			for (String query : queries) {
 				statement.addBatch(query);
 			}
 			statement.executeBatch();
 			statement.close();
 			conn.close();
	}
	public void disband() throws SQLException {
		Members.clear();
		getMembers();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.RED, "The faction has been disbanded."));
    			FactionPlayer facP = new FactionPlayer();
    			facP = facP.getFacPlayerFromUUID(keyword);
    			facP.setFacID(1);
				facP.setFacRank(1);
				facP.setTitle("NoTitleSet");
				facP.saveUser();
				ServerBossBar bossBarB;
				if (facP.getBar() != null && facP.getLastTerritory() == this.FacID) {
					System.out.println("Removing bar");
					bossBarB = facP.getBar();
					bossBarB.removePlayer(Sponge.getServer().getPlayer(keyword).get());
					}
    		}
    	}
    	FactionsMain.allFactions.remove(FacName.toLowerCase());
    	if (!FactionsMain.allFactionClaims.isEmpty()) {
		for (Entry<String, Integer> entry : FactionsMain.allFactionClaims.entrySet()) {
			String key = entry.getKey();
			int checkFac = entry.getValue();
			if (checkFac == this.FacID) {
				 FactionsMain.allFactionClaims.remove(key);
			}
			// ...
		}
    	}
    	String uri = "jdbc:sqlite:" + FactionsMain.staticRoot + "/Factions.db";
    	ArrayList <String> queries = new ArrayList<>();
    	queries.add("UPDATE PlayerData set Faction = 1 where Faction = '" + FacID + "'");
    	 queries.add("DELETE from FactionData where FacID = '" + FacID + "'");
    	 queries.add("DELETE from LandClaims where FacID = '" + FacID + "'");
            Connection conn = FactionsMain.getDataSource(uri).getConnection();
 			Statement statement = conn.createStatement();
 			for (String query : queries) {
 				statement.addBatch(query);
 			}
 			statement.executeBatch();
 			statement.close();
 			conn.close();
	}
	public HashMap<String, Long> invites = new HashMap<>();
	
	public void addInvite(String string) {
		invites.put(string, System.currentTimeMillis() / 1000 );
	}
	public void removeInvite(String string) {
		invites.remove(string);
	}
	public static Faction loadWilderness() {
		Faction wilderness = new Faction();
		wilderness.FacName = "Wilderness";
		wilderness.setBankBalance(0.0);
		wilderness.setTaxRate(0.0);
		wilderness.setFacDescription("The wilderness, dangerous place, some say Diggy lives here");
		return wilderness;

	}
	public HashMap<Integer, Long> NeutralReqs = new HashMap<>();
	
	public void addNeutralReq(int ID) {
		NeutralReqs.put(ID, System.currentTimeMillis() / 1000 );
		getMembersNoQuery();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, Faction.FactionFromID(ID).getFacName(), " wishes to declare peace with your faction."));
    		}
    	}
	}
	public HashMap<Integer, Long> AllyReqs = new HashMap<>();
	
	public void addAllyReq(int ID) {
		AllyReqs.put(ID, System.currentTimeMillis() / 1000 );
		getMembersNoQuery();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, Faction.FactionFromID(ID).getFacName(), " wishes to form an alliance with your faction."));
    		}
    	}
	}
	public void removeNeutralReq(int ID) {
		NeutralReqs.remove(ID);
	}
	private String LeaderRank;
	
	public void setLeaderRank(String string) {
		LeaderRank = string;
	}
	public String getLeaderRank() {
		return LeaderRank;
	}
	private String OfficerRank;
	public void setOfficerRank(String string) {
		OfficerRank = string;
	}
	public String getOfficerRank() {
		return OfficerRank;
	}
	private String HelperRank;
	public void setHelperRank(String string) {
		HelperRank = string;
	}
	public String getHelperRank() {
		return HelperRank;
	}
	
	public boolean isEnemy(int ID) {
		if (Enemies.contains(ID)) {
			return true;
		}
		return false;
	}
	public boolean isAlly(int ID) {
		if (Allies.contains(ID)) {
			return true;
		}
		return false;
	}
	public boolean isVassal(int ID) {
		if (Vassals.contains(ID)) {
			return true;
		}
		return false;
	}
	//create new faction
	public boolean createNewFaction (String name, Player player) throws SQLException {
		Sponge.getServer().getConsole().sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "Attempting to create Faction: ", name));
		setFacName(name);
		setFacDescription("Default");
		setBankBalance(0.0);
		FacLeader = player.getUniqueId();
		//setFacLeader(player.getUniqueId());
		addMember(player.getUniqueId());
		setTaxRate(0.0);
		setFactionHome(player.getLocation());
		FactionsMain.allFactions.put(name.toLowerCase(), this);
		ArrayList <String> queries = new ArrayList<>();
		String uri = "jdbc:sqlite:" + FactionsMain.staticRoot + "/Factions.db";
		String insertQuery = "INSERT INTO FactionData (FacName, FacLeader, FacDescription, FacTax, FacBankBalance, Allies, Vassals, Enemies, LeaderRankName, OfficerRankName, HelperRankName, FactionHomeWorld, FactionHomeX, FactionHomeY, FactionHomeZ) values (?, ?, ?, 0.0, 0.0, NULL, NULL, NULL, 'Leader', 'Officer', 'Helper', ?, ?, ?, ?)";
		String selectQuery = "SELECT FacID from FactionData where FacName = '" + this.FacName + "'";
		World world;
		Vector3i location = player.getLocation().getChunkPosition();
		String worldName = player.getWorld().getName();
		String combined = worldName + ":" + location;
		this.setLeaderRank("Leader");
		this.setHelperRank("Helper");
		this.setOfficerRank("Officer");
		world = (World) this.factionHome.getExtent();
		try (Connection conn2 = FactionsMain.getDataSource(uri).getConnection()) {
			PreparedStatement stmt = conn2.prepareStatement(insertQuery); {
				stmt.setString(1, this.FacName);
				stmt.setString(2, this.FacLeader.toString());
				stmt.setString(3, this.FacDescription);
				stmt.setString(4, world.getName());
				stmt.setDouble(5, this.factionHome.getBlockX());
				stmt.setDouble(6, this.factionHome.getBlockY());
				stmt.setDouble(7, this.factionHome.getBlockZ());
				stmt.execute();
				conn2.close();
			}
		}
		try (Connection conn3 = FactionsMain.getDataSource(uri).getConnection()) {
			PreparedStatement stmt = conn3.prepareStatement(selectQuery); {
				ResultSet results = stmt.executeQuery(); {
					while (results.next()) {
						this.FacID = results.getInt("FacID");

						
						conn3.close();
						stmt.close();

					}
				}
			}
		}
		FactionPlayer facP = FactionsMain.allFactionPlayers.get(player.getUniqueId());
		facP.setFacID(this.FacID);
		FactionsMain.allFactionPlayers.remove(player.getUniqueId());
		FactionsMain.allFactionPlayers.put(player.getUniqueId(), facP);
		addClaim(combined);
		addToClaimCount();
		FactionsMain.allFactionClaims.put(combined, this.getFacID());
		Sponge.getServer().getConsole().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.GREEN, "successfully created Faction: ", name));
		return true;
	}

	public void saveFactionRanks() throws SQLException {
		ArrayList <String> queries = new ArrayList<>();
		queries.add("UPDATE FactionData set LeaderRankName = '" + this.getLeaderRank() + "' where FacID = '" + this.getFacID() + "'");
		queries.add("UPDATE FactionData set OfficerRankName = '" + this.getOfficerRank() + "' where FacID = '" + this.getFacID() + "'");
		queries.add("UPDATE FactionData set HelperRankName = '" + this.getHelperRank() + "' where FacID = '" + this.getFacID() + "'");
		String uri = "jdbc:sqlite:" + FactionsMain.staticRoot + "/Factions.db";
		try (Connection conn2 = FactionsMain.getDataSource(uri).getConnection()) {
			Statement stmt = conn2.createStatement(); {{
				for (String query : queries) {
					stmt.addBatch(query);
				}
				stmt.executeBatch();
				stmt.close();
			}
			}
		}
	}
	//create a faction from the provided ID, probably wont use this much since i can get all faction data from a resultset and do a loop to create factions
	public static Faction FactionFromID (int ID) {
		for (Entry<String, Faction> entry : FactionsMain.allFactions.entrySet()) {
			String key = entry.getKey();
			Faction checkFac = entry.getValue();
			if (checkFac.getFacID() == ID) {
				return checkFac;
			}
			// ...
		}

		return null;
	}
	private String FacName;

	public String getFacName() {
		return FacName;
	}

	public boolean setFacName(String name) {
		if (this.FacName != null) {
		if (FactionsMain.allFactions.containsKey(this.FacName.toLowerCase())) {
			//do an error message
			return false;
		}
		}
		this.FacName = name;
		//Also implement saving to file
		FactionsMain.allFactions.remove(this.FacName);
		FactionsMain.allFactions.put(name.toLowerCase(), this);
		return true;
	}
	private UUID FacLeader;
	public UUID getFacLeaderUUID() {
		return FacLeader;
	}
	public String getFacLeader() {
		String FacLeaderName = FactionsMain.getUser(this.FacLeader).get().getName();

		return FacLeaderName;
	}

	public void setFacLeader(UUID leader) {
		//do stuff to prevent duplicate names
		this.FacLeader = leader;
		//Also implement saving to file
	}
	private String FacDescription;

	public String getFacDescription() {
		return FacDescription;
	}

	public void setFacDescription(String desc) {
		this.FacDescription = desc;
		//Also implement saving to file
	}

	private List<UUID> Members = new ArrayList<>();

	public void setMembers(List<UUID> members) {
		this.Members = members;
	}

	public void clearMembers() {
		Members.clear();
	}
	public void addMember(UUID member) throws SQLException {
		Members.clear();
		getMembers();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.WHITE, Sponge.getServer().getPlayer(member).get().getName(),TextColors.GREEN, " has joined the faction."));
    		}
    	}
		Members.add(member);
	}
	public void kickMember(UUID member) throws SQLException {
		Members.clear();
		getMembers();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.WHITE, FactionsMain.getUser(member).get().getName(),TextColors.RED, " was kicked from the faction."));
    		}
    	}
		Members.remove(member);
	}
	public void removeMember(UUID member) throws SQLException {
		Members.clear();
		getMembers();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.WHITE, Sponge.getServer().getPlayer(member).get().getName(),TextColors.RED, " has left the faction."));
    		}
    	}
		Members.remove(member);
	}
	public List<UUID> getMembers() throws SQLException {
		String selectQuery = "SELECT * from PlayerData where Faction = '" + this.FacID + "'";
		String uri = "jdbc:sqlite:" + FactionsMain.staticRoot + "/Factions.db";
		try (Connection conn3 = FactionsMain.getDataSource(uri).getConnection()) {
			PreparedStatement stmt = conn3.prepareStatement(selectQuery); {
				ResultSet results = stmt.executeQuery(); {
					while (results.next()) {
						Members.add(UUID.fromString(results.getString("UUID")));

					}
				}
			}
		}
		return Members;
	}
	
	public List<UUID> getMembersNoQuery(){
		return Members;
	}
	private Double FacBankBalance;

	public Double getBankBalance() {
		return FacBankBalance;
	}

	public void setBankBalance(Double amt) {
		//Do this for admin command or initial creation bank balance
		this.FacBankBalance = amt;
	}
	public void addToBalance(Double amt) {
		//check if player has money first
		this.FacBankBalance += amt;
	}

	public void subtractFromBalance(Double amt) {
		//check bank has money first and player has perms
		//then give to players balance
		this.FacBankBalance -= amt;
	}
	private Double FacTaxRate;

	public Double getTaxRate() {
		return FacTaxRate;
	}

	public void setTaxRate(Double taxRate) {
		//Do some stuff for calculating tax by the faction set tax percent, then take the money and add it to the tax message
		this.FacTaxRate = taxRate;
	}

	//enemies
	private List<Integer> Enemies = new ArrayList<>();

	public void setEnemies(List<Integer> enemies) {
		this.Enemies = enemies;
	}
	public void saveEnemies() throws SQLException {
		String temp = "";
		for (Integer temporary : Enemies) {
			temp += ":" + temporary;
		}
		ArrayList <String> queries = new ArrayList<>();
		if (temp == "") {
			queries.add("UPDATE FactionData set Enemies = NULL where FacID = '" + this.getFacID() + "'");
		}
		else {
			queries.add("UPDATE FactionData set Enemies = '" + temp + "' where FacID = '" + this.getFacID() + "'");
		}
		temp = temp.replaceFirst(":", "");
		
		String uri = "jdbc:sqlite:" + FactionsMain.staticRoot + "/Factions.db";
		try (Connection conn2 = FactionsMain.getDataSource(uri).getConnection()) {
			Statement stmt = conn2.createStatement(); {{
				for (String query : queries) {
					stmt.addBatch(query);
				}
				stmt.executeBatch();
				stmt.close();
			}
			}
		}
	}
	public void addEnemy(Integer enemy) throws SQLException {
		if (isEnemy(enemy)) {
			return;
		}
		Enemies.add(enemy);
		
		getMembersNoQuery();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.RED, Faction.FactionFromID(enemy).getFacName(), " has declared war upon your faction."));
    		}
    	}
	}
	public void removeEnemy(Integer enemy) throws SQLException {
		Enemies.remove(enemy);
		saveEnemies();
		getMembersNoQuery();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.RED, Faction.FactionFromID(enemy).getFacName(), " is no longer at war with you."));
    		}
    	}
	}
	public List<Integer> getEnemies() {
		return Enemies;
	}


	//allies
	private List<Integer> Allies = new ArrayList<>();

	public void setAllies(List<Integer> Allies) {
		this.Allies = Allies;
	}
	public void saveAllies() throws SQLException {
		String temp = "";
		for (Integer temporary : Allies) {
			temp += ":" + temporary;
		}
		ArrayList <String> queries = new ArrayList<>();
		if (temp == "") {
			queries.add("UPDATE FactionData set Allies = NULL where FacID = '" + this.getFacID() + "'");
		}
		else {
			queries.add("UPDATE FactionData set Allies = '" + temp + "' where FacID = '" + this.getFacID() + "'");
		}
		temp = temp.replaceFirst(":", "");
		String uri = "jdbc:sqlite:" + FactionsMain.staticRoot + "/Factions.db";
		try (Connection conn2 = FactionsMain.getDataSource(uri).getConnection()) {
			Statement stmt = conn2.createStatement(); {{
				for (String query : queries) {
					stmt.addBatch(query);
				}
				stmt.executeBatch();
				stmt.close();
			}
			}
		}
	}
	public void addAlly(Integer ally) throws SQLException {
		if (isAlly(ally)) {
			return;
		}
		Allies.add(ally);
		getMembersNoQuery();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.AQUA, Faction.FactionFromID(ally).getFacName(), " is now allied with your faction."));
    		}
    	}
	}
	public void removeAlly(Integer ally) throws SQLException {
		Allies.remove(ally);
		saveAllies();
		getMembersNoQuery();
    	for (UUID keyword : Members){
    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(FactionsMain.defaultMessage, TextColors.AQUA, Faction.FactionFromID(ally).getFacName(), " is no longer allied with your faction."));
    		}
    	}
	}
	public List<Integer> getAllies() {
		return Allies;
	}

	//vassals
	private List<Integer> Vassals = new ArrayList<>();

	public void setVassals(List<Integer> Vassals) {
		this.Vassals = Vassals;
	}
	public void saveVassals() throws SQLException {
		String temp = "";
		for (Integer temporary : Vassals) {
			temp += ":" + temporary;
		}
		System.out.println("Saving Vassals");
		temp = temp.replaceFirst(":", "");
		ArrayList <String> queries = new ArrayList<>();
		queries.add("UPDATE FactionData set Vassals = '" + temp + "' where FacID = '" + this.getFacID() + "'");
		String uri = "jdbc:sqlite:" + FactionsMain.staticRoot + "/Factions.db";
		try (Connection conn2 = FactionsMain.getDataSource(uri).getConnection()) {
			Statement stmt = conn2.createStatement(); {{
				for (String query : queries) {
					stmt.addBatch(query);
				}
				stmt.executeBatch();
				stmt.close();
			}
			}
		}
	}
	public void addVassal(Integer vassal) {
		Vassals.add(vassal);
	}
	public void removeVassal(Integer vassal) {
		Vassals.remove(vassal);
	}
	public List<Integer> getVassals() {
		return Vassals;
	}
}
