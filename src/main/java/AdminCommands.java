import java.sql.SQLException;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;

public class AdminCommands {
	public static class testMultipleFac implements CommandExecutor{

		@Override
		public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
			if (src instanceof Player) {
				Player player = (Player) src;
				Player player2 = Sponge.getServer().getPlayer("DiggyNevs").get();
				FactionPlayer facP = new FactionPlayer();
				Faction faction = new Faction();
				Faction faction2 = new Faction();
				try {
					facP = new FactionPlayer().getFacPlayerFromUUID(player.getUniqueId());
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					faction.createNewFaction(player.getName(), player);
					faction2.createNewFaction(player2.getName(), player2);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// TODO Auto-generated method stub
			return CommandResult.success();
		}
	}
}
