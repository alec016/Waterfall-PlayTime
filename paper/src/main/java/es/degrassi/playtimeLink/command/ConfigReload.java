package es.degrassi.playtimeLink.command;

import es.degrassi.playtimeLink.ConfigHandler;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ConfigReload extends Command {
  private final ConfigHandler configHandler;

  public ConfigReload(ConfigHandler configHandler) {
    super("playtimelinkreload", "reloads playtimelink config", "/[playtimereload|ptrl|ptreload]", List.of("ptlrl", "ptlreload"));
    this.configHandler = configHandler;
  }

  @Override
  public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
    if(!sender.hasPermission("wpt.reload")) {
      sender.sendMessage(configHandler.getNO_PERMISSION());
      return false;
    }

    if(args.length > 0) {
      sender.sendMessage(configHandler.getINVALID_ARGS());
      return false;
    }
    configHandler.reloadConfig();
    sender.sendMessage(configHandler.getCONFIG_RELOAD());

    return true;
  }
}
