package CrunchFactions;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class prepMessages {
	public List<String> prepFaction(Faction fac) throws SQLException {
	   List<String> facMessage = new ArrayList<>();
	   List<UUID> tempList = new ArrayList<>();
	   fac.clearMembers();
	   String members = "";
	   tempList = fac.getMembers();
	   	facMessage.add("&a-=======- &2[Factions] &a-=======-");
	    facMessage.add("&aFaction: &f" + fac.getFacName());
	    //get name from uuid, cba doing that yet
	    facMessage.add("&aLeader: &f" + fac.getFacLeader());
	    facMessage.add("&aDesc: &f" + fac.getFacDescription());
	    facMessage.add("&aClaims: &f" + fac.getClaimCount() + " out of " + fac.calcMaxClaims());
	    facMessage.add("&aBank Balance: &f" + fac.getBankBalance() + " &2|| &aTax Rate: &f" + fac.getTaxRate() +"%");
	    members = "PREP-MEMBERS";
    	for (UUID keyword : tempList){
    		if (FactionsMain.getUser(keyword).isPresent()) {
				String rank = "";
				FactionPlayer facP = new FactionPlayer();
						facP = facP.getFacPlayerFromUUID(keyword);
				Faction faction = Faction.FactionFromID(facP.getFacID());
				if (facP.getFacRank() == 2) {
					rank = faction.getHelperRank();
				}
			
		
				if (facP.getFacRank() == 3) {
					rank = faction.getOfficerRank();
				}
				if (faction.getFacLeaderUUID().equals(keyword)) {
					rank = faction.getLeaderRank();
				}
				if (facP.getTitle() != null && !facP.getTitle().equals("NoTitleSet")) {
					rank = facP.getTitle();
				}
    		members =  members + ", " + rank + " " + FactionsMain.getUser(keyword).get().getName();
    		}
    	}
    	members = members.replaceFirst(",", "");
	    facMessage.add(members);
	    List<Integer> enemies = fac.getEnemies();
	    String enemiesString = "PREP-ENEMIES";
    	for (int i : enemies){
    		if (fac.FactionFromID(i) != null) {
    			Faction tempFac = fac.FactionFromID(i);
    			enemiesString += ", " + tempFac.getFacName();
    		}
    		else {
    			fac.removeEnemy(i);
    			fac.saveEnemies();
    		}
    	}
    	enemiesString = enemiesString.replaceFirst(", ", "");
	    List<Integer> allies = fac.getAllies();
	    String alliesString = "PREP-ALLIES";
    	for (int i : allies){
    		if (fac.FactionFromID(i) != null) {
    			Faction tempFac = fac.FactionFromID(i);
    		    alliesString += ", " + tempFac.getFacName();
    		}
    		else {
    			fac.removeAlly(i);
    			fac.saveAllies();
    		}
    	}
    	alliesString = alliesString.replaceFirst(", ", "");
        facMessage.add(alliesString);
        
        
	    List<Integer> vassals = fac.getVassals();
	    String vassalsString = "PREP-VASSALS";
    	for (int i : vassals){
    		if (fac.FactionFromID(i) != null) {
    			Faction tempFac = fac.FactionFromID(i);
    		    vassalsString += ", " + tempFac.getFacName();
    		}
    		else {
    			fac.removeAlly(i);
    			fac.saveVassals();
    		}
    	}
    	vassalsString = vassalsString.replaceFirst(", ", "");
        facMessage.add(vassalsString);
	    facMessage.add(enemiesString);
	    
		return facMessage;	    
	}
}
