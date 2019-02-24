

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class chatEvent {
		@Listener
	    public void onchat(MessageChannelEvent.Chat event, @First Player player) throws SQLException{
			Text newMsg = event.getRawMessage();
			FactionPlayer facP = new FactionPlayer();
			if (facP.getFacPlayerFromUUID(player.getUniqueId()) != null) {
				facP = facP.getFacPlayerFromUUID(player.getUniqueId());
				if (facP.getFacID() == 1 && facP.inFacChat()) {
					facP.leaveChat();
					player.sendMessage(Text.of(FactionsMain.defaultMessage,TextColors.GREEN, "You are not a member of any faction."));
					return;
				}
				String rank = "";
				if (facP.getFacID() != 1 && facP.inFacChat()) {
				Faction	faction = Faction.FactionFromID(facP.getFacID());
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
				if (facP.inFacChat()) {
				List<UUID> members = faction.getMembersNoQuery();
				event.setCancelled(true);
			 	for (UUID keyword : members){
		    		if (Sponge.getServer().getPlayer(keyword).isPresent()) {
		    			Sponge.getServer().getPlayer(keyword).get().sendMessage(Text.of(TextColors.DARK_GREEN, "[F] ", TextColors.YELLOW, rank, " ", TextColors.WHITE, player.getName(), " >> ", newMsg));
		    		}
		    	}
			}
				}
				else {
					return;
				}
			}
		
		}
	}

