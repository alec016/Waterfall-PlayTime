package es.degrassi.playtime.command;

import es.degrassi.playtime.handler.ConfigHandler;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class ConfigReload extends Command {
  private final ConfigHandler configHandler;

  public ConfigReload(ConfigHandler configHandler) {
    super("playtimereload", "wpt.reload", "ptrl", "ptreload");
    this.configHandler = configHandler;
  }


  @Override
  public void execute(CommandSender sender, String[] args) {
    if(!sender.hasPermission("wpt.reload")) {
      sender.sendMessage(configHandler.getNO_PERMISSION());
      return;
    }

    if(args.length > 0) {
      sender.sendMessage(configHandler.getINVALID_ARGS());
      return;
    }
    configHandler.reloadConfig();
    sender.sendMessage(configHandler.getCONFIG_RELOAD());
  }

}
