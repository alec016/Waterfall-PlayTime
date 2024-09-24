package es.degrassi.playtime.Commands;

import es.degrassi.playtime.Handlers.ConfigHandler;
import es.degrassi.playtime.Main;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class PlaytimeResetAll extends Command implements TabExecutor {
  private final Main main;
  private final ConfigHandler configHandler;

  public PlaytimeResetAll(Main main, ConfigHandler configHandler) {
    super("playtimeresetall", "wpt.ptresetall", "ptra", "ptresetall");
    this.main = main;
    this.configHandler = configHandler;
  }

  @Override
  public void execute(CommandSender sender, String[] args) {

    if(!sender.hasPermission("wpt.ptresetall")) {
      sender.sendMessage(configHandler.getNO_PERMISSION());
      return;
    }

    switch (args.length) {
      case 0 -> {
        BaseComponent message = configHandler.getPTRESETALL_CONFIRM();
        message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ptresetall confirm"));
        sender.sendMessage(message);
      }
      case 1 -> {
        if(!args[0].equalsIgnoreCase("confirm")) {
          sender.sendMessage(configHandler.getINVALID_ARGS());
          return;
        }
        configHandler.nullDataConfig();
        main.playtimeCache.keySet().removeIf(pname -> {
          Optional<ProxiedPlayer> player = Optional.ofNullable(main.getProxy().getPlayer(pname));
          if (player.isPresent()) {
            main.playtimeCache.replace(pname, 0L);
            return false;
          } else {
            return true;
          }
        });
        sender.sendMessage(configHandler.getPTRESETALL());
      }
      default -> sender.sendMessage(configHandler.getINVALID_ARGS());
    }
  }

  @Override
  public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
    List<String> tabargs = new ArrayList<>();
    if(!sender.hasPermission("wpt.ptresetall"))
      return tabargs;
    tabargs.add("confirm");
    return tabargs;
  }
}
