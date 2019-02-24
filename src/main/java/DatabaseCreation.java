

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.sql.SqlService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class DatabaseCreation {
	private static Path root;
	 
    private static SqlService sql;
    DatabaseCreation(SqlService sqlP, Path rootP) throws SQLException{
    	sql = sqlP;
    	root = rootP;
    	create();
    	
    }
	public DataSource getDataSource(String jdbcUrl) throws SQLException {
        if (sql == null) {
            sql = Sponge.getServiceManager().provide(SqlService.class).get();
        }
        return sql.getDataSource(jdbcUrl);
    }
    
    public void create() throws SQLException{
    	
        String uri = "jdbc:sqlite:" + root + "/Factions.db";
        ArrayList <String> queries = new ArrayList<>();
        queries.add("CREATE TABLE IF NOT EXISTS LandClaims (`WorldNameChunkID` TEXT, `FacID` INTEGER, `DateClaimed` TEXT, PRIMARY KEY(`WorldNameChunkID`))");
        queries.add("CREATE TABLE IF NOT EXISTS PlayerData ( `UUID` TEXT, `Faction` INTEGER, `LastKnownName` TEXT, 'Title' TEXT, 'Rank' INTEGER, PRIMARY KEY(`UUID`) )");
        queries.add("CREATE TABLE IF NOT EXISTS FactionData ( `FacID` INTEGER NOT NULL, `FacName` TEXT NOT NULL, `FacLeader` TEXT NOT NULL, `FacDescription` TEXT, `FacTax` NUMERIC NOT NULL, `FacBankBalance` NUMERIC NOT NULL, `Allies` TEXT, `Vassals` TEXT, `Enemies` TEXT,  `PurchasedBonus` NUMERIC, `LeaderRankName` TEXT, `OfficerRankName` TEXT, `HelperRankName` TEXT, 'FactionHomeWorld' TEXT, 'FactionHomeX' NUMERIC, 'FactionHomeY' NUMERIC, 'FactionHomeZ' NUMERIC, PRIMARY KEY(`FacID`) )");
        queries.add("CREATE TABLE IF NOT EXISTS TaxMessages ( `MessageID` INTEGER, `PlayerUUID` NUMERIC, `Amount` NUMERIC, `Date` TEXT, PRIMARY KEY(`MessageID`) )");
        queries.add("INSERT INTO FactionData (FacID,FacName,FacLeader,FacDescription,FacTax,FacBankBalance,Allies,Vassals,Enemies) VALUES (1,'Wilderness','The Planet','The wilderness, dangerous to go alone',0,0,NULL,NULL,NULL)");
        File file = new File(root + "/Factions.db");
        if (!file.exists()) {
        	try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            Connection conn = getDataSource(uri).getConnection();
			Statement statement = conn.createStatement();

			for (String query : queries) {
				statement.addBatch(query);
			}
			statement.executeBatch();
			statement.close();
			conn.close();
			}
        
    }
}
