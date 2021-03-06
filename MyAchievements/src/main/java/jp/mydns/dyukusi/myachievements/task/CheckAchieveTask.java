package jp.mydns.dyukusi.myachievements.task;

import java.util.LinkedList;

import jp.mydns.dyukusi.myachievements.AchieveInterface;
import jp.mydns.dyukusi.myachievements.MyAchievements;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import com.avaje.ebeaninternal.server.autofetch.Statistics;
import com.wolvencraft.yasp.StatisticsAPI;
import com.wolvencraft.yasp.events.session.SessionCreateEvent;
import com.wolvencraft.yasp.session.OnlineSession;
import com.wolvencraft.yasp.util.VariableManager.PlayerVariable;

public class CheckAchieveTask extends BukkitRunnable {

	MyAchievements plugin;
	Player player;
	LinkedList<AchieveInterface> alist;
	OnlineSession session;

	public CheckAchieveTask(MyAchievements plugin, Player player,
			OnlineSession session) {
		this.plugin = plugin;
		this.player = player;
		this.alist = plugin.get_achievements_list();
		this.session = session;
	}

	@Override
	public synchronized void run() {

		if (player.isOnline()) {

			// check achieve or not
			for (AchieveInterface ach : alist) {

				// already has?
				if (!ach.hasAchievement(player)) {

					// to avoid error messasge
					if (session == null
							|| session.getPlayerTotals() == null
							|| session.getPlayerTotals().getValue(
									PlayerVariable.TOTAL_PLAYTIME) == null) {
						continue;
					}

					// achieve?
					if (ach.isAchieved(player, session)) {
				
						//register ach_total meta
						if (!player.hasMetadata("ach_total") || player.getMetadata("ach_total").size() == 0) {
							player.setMetadata("ach_total",
									new FixedMetadataValue(plugin, 0));
						}

						// increment ach got num
						player.setMetadata("ach_total", new FixedMetadataValue(
								plugin, player.getMetadata("ach_total").get(0)
										.asInt() + 1));

						// play sound effect
						player.playSound(player.getLocation(), Sound.LEVEL_UP,
								1, 1);

						// heal the player
						player.setHealth(player.getMaxHealth());

						// heal food level
						player.setExhaustion(20);

						// give achievement
						ach.giveAchievement(player);

						// give reward
						ach.getReward(player);

					}
				}

			}

		}
		// player is offline
		else {

			if (session != null)
				session.finalize();

			this.cancel();
		}

	}
}
