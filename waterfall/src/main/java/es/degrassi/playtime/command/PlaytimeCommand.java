package es.degrassi.playtime.command;

import es.degrassi.common.data.PlayerData;
import es.degrassi.playtime.handler.ConfigHandler;
import es.degrassi.playtime.Main;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class PlaytimeCommand extends Command implements TabExecutor {
  private final Main main;
  private final ConfigHandler configHandler;

  public PlaytimeCommand(Main main, ConfigHandler configHandler) {
    super("playtime", "wpt.getowntime", "pt");
    this.main = main;
    this.configHandler = configHandler;
  }

  public boolean hasPermission(CommandSender sender) {
    return super.hasPermission(sender) || sender.hasPermission("wpt.getotherstime");
  }

  @Override
  public void execute(CommandSender sender, String[] args) {
    switch (args.length) {
      case 0 -> {
        if(!(sender instanceof ProxiedPlayer player)) {
          sender.sendMessage(configHandler.getNO_CONSOLE_USE());
          return;
        }
        sendYourPlaytime(player);

      }
      case 1 -> {
        if(sender instanceof ProxiedPlayer player1)
          if(player1.getName().equalsIgnoreCase(args[0])) {
            sendYourPlaytime(player1);
            return;
          }
        if(configHandler.isVIEW_OTHERS_TIME() && !sender.hasPermission("wpt.getotherstime")) {
          sender.sendMessage(configHandler.getNO_PERMISSION());
          return;
        }
        ProxiedPlayer player = Main.instance.getProxy().getPlayer(args[0]);
        long PlayTime = main.getPlayTime(player) != null ? main.getPlayTime(player).getTime() : main.playerDataCacheHandler.get(player.getUniqueId()).getTime();
        if (PlayTime == 0) {
          sender.sendMessage(configHandler.getNO_PLAYER());
        } else {
          String message = configHandler.getOTHER_PLAYTIME()
            .replace("%hours%", String.valueOf(main.calculatePlayTime(PlayTime, 'h')))
            .replace("%minutes%", String.valueOf(main.calculatePlayTime(PlayTime, 'm')))
            .replace("%seconds%", String.valueOf(main.calculatePlayTime(PlayTime, 's')))
            .replace("%player%", args[0]);
          sender.sendMessage(configHandler.decideNonComponent(message));
        }
      }
      default -> sender.sendMessage(configHandler.getINVALID_ARGS());
    }
  }

  public void sendYourPlaytime(ProxiedPlayer player) {
    if (configHandler.isVIEW_OWN_TIME() && !player.hasPermission("wpt.getowntime")) {
      player.sendMessage(configHandler.getNO_PERMISSION());
      return;
    }
    PlayerData PlayTime = main.getPlayTime(player);
    String messageBegin = configHandler.getYOUR_PLAYTIME()
      .replace("%hours%", String.valueOf(main.calculatePlayTime(PlayTime.getTime(), 'h')))
      .replace("%minutes%", String.valueOf(main.calculatePlayTime(PlayTime.getTime(), 'm')))
      .replace("%seconds%", String.valueOf(main.calculatePlayTime(PlayTime.getTime(), 's')));
    player.sendMessage(configHandler.decideNonComponent(messageBegin));
  }

  @Override
  public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
    List<String> tabargs = new ArrayList<>();
    if(configHandler.isVIEW_OTHERS_TIME() && !sender.hasPermission("wpt.getotherstime")) {
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
