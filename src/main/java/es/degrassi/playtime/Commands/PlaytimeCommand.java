package es.degrassi.playtime.Commands;

import es.degrassi.playtime.Handlers.ConfigHandler;
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
        SendYourPlaytime(player);

      }
      case 1 -> {
        if(sender instanceof ProxiedPlayer player1)
          if(player1.getName().equalsIgnoreCase(args[0])) {
            SendYourPlaytime(player1);
            return;
          }
        if(configHandler.isVIEW_OTHERS_TIME() && !sender.hasPermission("wpt.getotherstime")) {
          sender.sendMessage(configHandler.getNO_PERMISSION());
          return;
        }
        long PlayTime = main.playtimeCache.containsKey(args[0]) ? main.GetPlayTime(args[0]) : configHandler.getPtFromConfig(args[0]);
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

  public void SendYourPlaytime(ProxiedPlayer player) {
    if (configHandler.isVIEW_OWN_TIME() && !player.hasPermission("wpt.getowntime")) {
      player.sendMessage(configHandler.getNO_PERMISSION());
      return;
    }
    long PlayTime = main.GetPlayTime(player.getName());
    String messageBegin = configHandler.getYOUR_PLAYTIME()
      .replace("%hours%", String.valueOf(main.calculatePlayTime(PlayTime, 'h')))
      .replace("%minutes%", String.valueOf(main.calculatePlayTime(PlayTime, 'm')))
      .replace("%seconds%", String.valueOf(main.calculatePlayTime(PlayTime, 's')));
    player.sendMessage(configHandler.decideNonComponent(messageBegin));
  }

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
