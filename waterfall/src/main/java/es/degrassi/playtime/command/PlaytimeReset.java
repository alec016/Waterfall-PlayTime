package es.degrassi.playtime.command;

import es.degrassi.playtime.handler.cache.PlayerDataCacheHandler;
import es.degrassi.playtime.handler.ConfigHandler;
import es.degrassi.playtime.Main;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class PlaytimeReset extends Command implements TabExecutor {
  private final Main main;
  private final ConfigHandler configHandler;
  private final PlayerDataCacheHandler cacheHandler;

  public PlaytimeReset(Main main, ConfigHandler configHandler, PlayerDataCacheHandler cacheHandler) {
    super("playtimereset", "wpt.ptreset", "ptr", "ptreset");
    this.main = main;
    this.configHandler = configHandler;
    this.cacheHandler = cacheHandler;
  }
  @Override
  public void execute(CommandSender sender, String[] args) {
    if(!sender.hasPermission("wpt.ptreset")) {
      sender.sendMessage(configHandler.getNO_PERMISSION());
      return;
    }

    switch (args.length) {
      case 0 -> sender.sendMessage(configHandler.getPTRESET_HELP());
      case 1 -> {
        ProxiedPlayer player = Main.instance.getProxy().getPlayer(args[0]);
        if(main.getPlayTime(player) != null) {
          cacheHandler.upd2(player); //No idea what I'm doing here
          resetPT(player, sender);
          return;
        }
        if(main.playerDataCacheHandler.get(player.getUniqueId()) == null) {
          sender.sendMessage(configHandler.getNO_PLAYER());
          return;
        }
        resetPT(player, sender);
      }
      default -> sender.sendMessage(configHandler.getINVALID_ARGS());
    }
  }

  private void resetPT(ProxiedPlayer player, CommandSender sender) {
    cacheHandler.save(player);
    String message = configHandler.getPTRESET().replace("%player%", player.getName());
    sender.sendMessage(configHandler.decideNonComponent(message));
  }

  @Override
  public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
    List<String> tabargs = new ArrayList<>();
    if(!sender.hasPermission("wpt.ptreset")) {
      return tabargs;
    }
    try {
      for (ProxiedPlayer player : main.getProxy().getPlayers()) {
        if (!player.equals(sender) && player.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
          tabargs.add(player.getName());
        }
      }
    } catch (Exception ignored) {
      for (ProxiedPlayer player : main.getProxy().getPlayers()) {
        if (!player.equals(sender)) {
          tabargs.add(player.getName());
        }
      }
    }
    return tabargs;
  }
}
