

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class TestFac implements CommandExecutor {

	@Override
	public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {

    	prepMessages prep = new prepMessages();
    	List<String> messages = new ArrayList<>();
    	Faction fac = new Faction();
    	System.out.println("Started creation.");
    	if (src instanceof Player) {
    	Player player = (Player) src;
    	FactionPlayer facP = FactionsMain.allFactionPlayers.get(player.getUniqueId());

    	List<String> taxMessages = null;
		try {
			taxMessages = facP.getTaxMessage();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String newFactionName = "Daves 302 Republic";
    	System.out.println("Try check if the faction exists");
			if (!FactionsMain.allFactions.containsKey(newFactionName.toLowerCase()) && newFactionName.matches("^[0-9a-zA-Z ]{3,25}$")) {
				try {
					fac.createNewFaction(newFactionName, player);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
				}
				System.out.println("Making the faction");
				System.out.println(fac.getFacName());
			}
			else {
				player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "That Faction name is taken or disallowed. Only A-Z and 0-9 are allowed."));
				return CommandResult.success();
			}
    	System.out.println(FactionsMain.allFactions);
    	player.sendMessage(Text.of(player.getLocation().getChunkPosition()));
    	}
    	else {
    		src.sendMessage(Text.of("I havent implemented this feature yet, currently you must create a faction as a player, alternatively sudo the player you wish to create the faction for."));
    	}
    	try {
			messages = prep.prepFaction(fac);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println(fac.getFacID());
    	for (String message: messages) 
    	{ 
    	    src.sendMessage(Text.of(message.replaceAll("&", "§")));
    	}
//		try {
//			fac.addEnemy(3);
//		} catch (SQLException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		try {
//			fac.addEnemy(7);
//		} catch (SQLException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		try {
//			fac.addEnemy(12);
//		} catch (SQLException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
//		try {
//			fac.saveEnemies();
//		} catch (SQLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		fac.addAlly(4);
//		fac.addAlly(8);
//		try {
//			fac.saveAllies();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		fac.addVassal(9);
//		try {
//			fac.saveVassals();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		//}
    	
		return CommandResult.success();
	}


}
