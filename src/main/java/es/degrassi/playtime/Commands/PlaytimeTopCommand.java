package es.degrassi.playtime.Commands;

import es.degrassi.playtime.Handlers.CacheHandler;
import es.degrassi.playtime.Handlers.ConfigHandler;
import es.degrassi.playtime.Main;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PlaytimeTopCommand extends Command {
  private final Main main;
  private final CacheHandler cacheHandler;
  private final ConfigHandler configHandler;

  public PlaytimeTopCommand(Main main, CacheHandler cacheHandler, ConfigHandler configHandler) {
    super("playtimetop", "wpt.gettoplist", "pttop", "ptt");
    this.main = main;
    this.cacheHandler = cacheHandler;
    this.configHandler = configHandler;
  }

  @Override
  public void execute(CommandSender sender, String[] args) { //do c
    if (configHandler.isVIEW_TOPLIST() && !sender.hasPermission("wpt.gettoplist")) {
      sender.sendMessage(configHandler.getNO_PERMISSION());
      return;
    }
    if(args.length > 0) {
      sender.sendMessage(configHandler.getINVALID_ARGS());
      return;
    }
    doSort(sender);
  }

  public LinkedHashMap<String, Long> doSort(CommandSender sender) {
    final HashMap<String, Long> TempCache = configHandler.isUSE_CACHE() ? cacheHandler.generateTempCache() : getInRuntime();
    final boolean isForPlaceholder = sender == null;
    final LinkedHashMap<String, Long> placeholderH  = new LinkedHashMap<>();
    if(!isForPlaceholder)
      sender.sendMessage(configHandler.getTOP_PLAYTIME_HEADER());
    for(int i = 0; i < configHandler.getTOPLIST_LIMIT(); i++) {
      Optional<Map.Entry<String, Long>> member = TempCache != null ? TempCache.entrySet().stream().max(Map.Entry.comparingByValue()) : Optional.empty();
      if(member.isEmpty())
        break;
      Map.Entry<String, Long> Entry = member.get();
      long playTime = Entry.getValue();
      if(playTime == 0)
        continue;

      if(isForPlaceholder)
        placeholderH.put(Entry.getKey(), playTime);
      else {
        String message = configHandler.getTOP_PLAYTIME_LIST()
          .replace("%player%", Entry.getKey())
          .replace("%hours%", String.valueOf(main.calculatePlayTime(Entry.getValue(), 'h')))
          .replace("%minutes%", String.valueOf(main.calculatePlayTime(Entry.getValue(), 'm')))
          .replace("%seconds%", String.valueOf(main.calculatePlayTime(Entry.getValue(), 's')));
        sender.sendMessage(configHandler.decideNonComponent(message));
      }
      TempCache.remove(Entry.getKey());
    }
    if(!isForPlaceholder)
      sender.sendMessage(configHandler.getTOP_PLAYTIME_FOOTER());
    return placeholderH;
  }

  public HashMap<String, Long> getInRuntime() {
    Iterator<Object> iterator = configHandler.getConfigIterator("Player-Data", true);
    final HashMap<String, Long> TempCache = new HashMap<>();
    if(iterator != null) {
      while (iterator.hasNext()) {
        String Pname = (String) iterator.next();
        Optional<ProxiedPlayer> player = Optional.ofNullable(main.getProxy().getPlayer(Pname));
        if (player.isEmpty()) {
          long Ptime = configHandler.getPtFromConfig(Pname);
          TempCache.put(Pname, Ptime);
        }
        iterator.remove();
      }
      while (TempCache.size() > configHandler.getTOPLIST_LIMIT()) {
        Optional<Map.Entry<String, Long>> member = TempCache.entrySet().stream().min(Map.Entry.comparingByValue());
        if (member.isEmpty())
          break;
        Map.Entry<String, Long> Entry = member.get();
        TempCache.remove(Entry.getKey());
      }
    }

    main.playtimeCache.forEach((String, Long) -> {
      Optional<ProxiedPlayer> player = Optional.ofNullable(main.getProxy().getPlayer(String));
      player.ifPresent(player1 -> {
        Optional<Map.Entry<String, Long>> ad = TempCache.entrySet().stream().min(Map.Entry.comparingByValue());
        ad.ifPresentOrElse(Entry -> {
          if(TempCache.size() >= configHandler.getTOPLIST_LIMIT()) {
            if (Entry.getValue() < Long) {
              TempCache.put(player1.getName(), Long);
              TempCache.remove(Entry.getKey());
            }
          }else
            TempCache.put(player1.getName(), Long);
        }, () -> TempCache.put(player1.getName(), Long));
      });
    });
    return TempCache;
  }

}
