package CrunchFactions;


import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.boss.ServerBossBar;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.service.user.UserStorageService;

public class FactionPlayer {

	//get a datasource


	private Path root;

	private SqlService sql;
	public FactionPlayer getFacPlayerFromUUID(UUID playerUUID) throws SQLException {
		this.sql = FactionsMain.sql;
		this.root = FactionsMain.staticRoot;
		this.PlayerUUID = playerUUID;
		if (FactionsMain.allFactionPlayers.containsKey(playerUUID)) {
			return FactionsMain.allFactionPlayers.get(playerUUID);
		}
	createUser();
	return this;
	}
	public boolean facChat;
	public void joinChat() {
		facChat = true;
	}
	public void leaveChat() {
		facChat = false;
	}
	public boolean inFacChat() {
		return facChat;
	}
	public Player getPlayer() {
		if (Sponge.getServer().getPlayer(this.PlayerUUID).isPresent()) {
			return Sponge.getServer().getPlayer(this.PlayerUUID).get();
		}
		return null;
	}
	public List<String> splitTaxMessage() throws SQLException{
		String query = "SELECT * from TaxMessages where PlayerUUID = ?";
		String uri = "jdbc:sqlite:" + root + "/Factions.db";
		try (Connection conn2 = FactionsMain.getDataSource(uri).getConnection()) {
			PreparedStatement stmt = conn2.prepareStatement(query); {
				stmt.setString(1, this.PlayerUUID.toString());
				ResultSet results = stmt.executeQuery(); {
					while(results.next()) {
						TaxMessage.add("You were taxed : " + results.getString("Amount") + " on " + results.getString("Date"));
					}
				}
			}
		}

		return TaxMessage;	
	}
	
	private String Title = "NoTitleSet";
	public void setTitle(String string) {
		Title = string;
	}
	public String getTitle() {
		return Title;
	}
	private int LastTerritory;
	public void setLastTerritory(int last) {
		LastTerritory = last;
	}
	public int getLastTerritory() {
		return LastTerritory;
	}
	private ServerBossBar BossBar;
	
	public ServerBossBar getBar() {
		return BossBar;
	}
	public void setBar(ServerBossBar bar) {
		BossBar = bar;
	}
	private void createUser() throws SQLException {
		String uri = "jdbc:sqlite:" + root + "/Factions.db";
		Boolean exists = false;
		String query = "SELECT * from PlayerData where UUID = '" + this.PlayerUUID + "'";
		try (Connection conn2 = FactionsMain.getDataSource(uri).getConnection()) {
			PreparedStatement stmt = conn2.prepareStatement(query); {
				ResultSet results = stmt.executeQuery(); {
					while (results.next()) {
						this.FacID = results.getInt("Faction");
						this.TaxMessage = splitTaxMessage();
						this.setPlayerName();
						this.setFacRank(results.getInt("Rank"));
						this.setTitle(results.getString("Title"));
						exists = true;
					}
				}
			}
		}
		setPlayerName();
		if (!exists) {
			String Insertquery = "INSERT INTO PlayerData (UUID, Faction, LastKnownName, Title, Rank) values (?, ?, ?, ?, ?)";
			try (Connection conn2 = FactionsMain.getDataSource(uri).getConnection()) {
				PreparedStatement stmt = conn2.prepareStatement(Insertquery); {
					stmt.setString(1, this.PlayerUUID.toString());
					stmt.setInt(2, 1);
					stmt.setString(4, this.getTitle());
					setFacRank(1);
					stmt.setInt(5, this.getFacRank());
					setFacID(1);
					stmt.setString(3, this.PlayerName);
					stmt.execute();
				}		}
		}
	}
	public void deleteUser() {
		ArrayList <String> queries = new ArrayList<>();
		queries.add("");
	}
	public void saveUser() throws SQLException {
		String uri = "jdbc:sqlite:" + root + "/Factions.db";
		ArrayList <String> queries = new ArrayList<>();
		setPlayerName();
		queries.add("UPDATE PlayerData set LastKnownName = '" + this.getPlayerName() + "' where UUID = '" + this.getPlayerUUID() + "'");
		queries.add("UPDATE PlayerData set Faction = '" + this.getFacID() + "' where UUID = '" + this.getPlayerUUID() + "'");
		queries.add("UPDATE PlayerData set Rank = '" + this.getFacRank() + "' where UUID = '" + this.getPlayerUUID() + "'");
		queries.add("UPDATE PlayerData set Title = '" + this.getTitle() + "' where UUID = '" + this.getPlayerUUID() + "'");
		for (String query : NewTaxMessage) {
			String[] split = query.split("<>");
			queries.add("INSERT INTO TaxMessages (PlayerUUID, Amount, Date) values ('" + this.getPlayerUUID() + "','" + split[0] + "','" + split[1] + "')");
		}
		NewTaxMessage.clear();
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
		FactionsMain.allFactionPlayers.put(this.PlayerUUID, this);
	}

	private int FacID;

	public int getFacID() {
		return FacID;
	}

	public void setFacID(int ID) throws SQLException {
		this.FacID = ID;
		saveUser();
		//Also implement saving to file
	}
	
	private int FacRank;

	public int getFacRank() {
		return FacRank;
	}

	public void setFacRank(int rank) throws SQLException {
		this.FacRank = rank;
		//Also implement saving to file
	}
	private String PlayerName;

	public String getPlayerName() {
		return PlayerName;
	}

	public void setPlayerName() {
		this.PlayerName = FactionsMain.getUser(this.PlayerUUID).get().getName();
	}
	private UUID PlayerUUID;

	public UUID getPlayerUUID() {
		return PlayerUUID;
	}

	public void setPlayerUUID(UUID player) {
		//do stuff to prevent duplicate names
		this.PlayerUUID = player;
		//Also implement saving to file
	}
	public void doTax(Double TaxRate) {
		//Do some stuff for calculating tax by the faction set tax percent, then take the money and add it to the tax message
		//get taxrate from faction
		//Tax = the calculations
		Double Tax = 5.0;

		//fix that to do the actual taxrate, will probably move into faction object as its easier to do. 
		//make it say the amount they were taxed also, not the taxrate. 

		setTaxMessage(Tax);
	}
	//
	private List<String> NewTaxMessage = new ArrayList<>();
	private List<String> TaxMessage = new ArrayList<>();

	public List<String> getTaxMessage() throws SQLException {
		TaxMessage.clear();
		splitTaxMessage();
		return TaxMessage;
	}
	public void setTaxMessage(Double amount) {
		Calendar cal = Calendar.getInstance();
		NewTaxMessage.add(amount + "<>" + cal.getTime().toString());// + " at" + cal.getTime().getHours() + ";" + cal.getTime().getMinutes());
		//Also implement saving to file
	}
}