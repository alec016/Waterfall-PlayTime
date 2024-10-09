package es.degrassi.playtime.command;

import es.degrassi.common.data.PlayerData;
import es.degrassi.playtime.handler.ConfigHandler;
import es.degrassi.playtime.Main;
import es.degrassi.playtime.handler.cache.PlayerDataCacheHandler;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class PlaytimeTopCommand extends Command {
  private final Main main;
  private final PlayerDataCacheHandler cacheHandler;
  private final ConfigHandler configHandler;

  public PlaytimeTopCommand(Main main, PlayerDataCacheHandler cacheHandler, ConfigHandler configHandler) {
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

  public void doSort(CommandSender sender) {
    final List<PlayerData> TempCache = configHandler.isUSE_CACHE() ? cacheHandler.generateTempCache() : getInRuntime();
    ProxiedPlayer player = Main.instance.getProxy().getPlayer(sender.getName());
    if (player == null) return;
    sender.sendMessage(configHandler.decideNonComponent(configHandler.getTOP_PLAYTIME_HEADER().replace("%server%", player.getServer().getInfo().getName())));
    for(int i = 0; i < configHandler.getTOPLIST_LIMIT(); i++) {
      Optional<PlayerData> member = TempCache != null ? TempCache.stream().filter(data ->
        data.getServer().equals(player.getServer().getInfo().getName())
      ).max(PlayerData::compareTo) : Optional.empty();
      if(member.isEmpty())
        break;
      PlayerData Entry = member.get();
      long playTime = Entry.getTime();
      if(playTime == 0)
        continue;
      else {
        String message = configHandler.getTOP_PLAYTIME_LIST()
          .replace("%player%", Entry.getName())
          .replace("%hours%", String.valueOf(main.calculatePlayTime(Entry.getTime(), 'h')))
          .replace("%minutes%", String.valueOf(main.calculatePlayTime(Entry.getTime(), 'm')))
          .replace("%seconds%", String.valueOf(main.calculatePlayTime(Entry.getTime(), 's')));
        sender.sendMessage(configHandler.decideNonComponent(message));
      }
      TempCache.remove(Entry);
    }
    sender.sendMessage(configHandler.getTOP_PLAYTIME_FOOTER());
  }

  public List<PlayerData> doSort(String sender) {
    final List<PlayerData> TempCache = configHandler.isUSE_CACHE() ? cacheHandler.generateTempCache() : getInRuntime();
    final List<PlayerData> placeholderH  = new LinkedList<>();
    ProxiedPlayer player = Main.instance.getProxy().getPlayer(sender);
    if (player == null) return TempCache;
    for(int i = 0; i < configHandler.getTOPLIST_LIMIT(); i++) {
      Optional<PlayerData> member = TempCache != null ? TempCache.stream().filter(data ->
        data.getServer().equals(player.getServer().getInfo().getName())
      ).max(PlayerData::compareTo) : Optional.empty();
      if(member.isEmpty())
        break;
      PlayerData Entry = member.get();
      long playTime = Entry.getTime();
      if(playTime == 0)
        continue;

      placeholderH.add(Entry);
      TempCache.remove(Entry);
    }
    return placeholderH;
  }

  public List<PlayerData> getInRuntime() {
    Iterator<PlayerData> iterator = cacheHandler.generateTempCache().iterator();
    final List<PlayerData> TempCache = new LinkedList<>();
    while (iterator.hasNext()) {
      UUID Pname = iterator.next().getPlayer();
      Optional<ProxiedPlayer> player = Optional.ofNullable(main.getProxy().getPlayer(Pname));
      if (player.isEmpty()) {
        PlayerData Ptime = cacheHandler.get(Pname);
        TempCache.add(Ptime);
      }
      iterator.remove();
    }
    while (TempCache.size() > configHandler.getTOPLIST_LIMIT()) {
      Optional<PlayerData> member = TempCache.stream().min(PlayerData::compareTo);
      if (member.isEmpty())
        break;
      TempCache.remove(member.get());
    }

    main.playTimeCache.forEach(playtime -> {
      Optional<ProxiedPlayer> player = Optional.ofNullable(main.getProxy().getPlayer(playtime.getPlayer()));
      player.ifPresent(player1 -> {
        Optional<PlayerData> ad = TempCache.stream().min(PlayerData::compareTo);
        ad.ifPresentOrElse(Entry -> {
          if(TempCache.size() >= configHandler.getTOPLIST_LIMIT()) {
            if (Entry.getTime() < playtime.getTime()) {
              TempCache.add(playtime);
              TempCache.remove(Entry);
            }
          }else
            TempCache.add(playtime);
        }, () -> TempCache.add(playtime));
      });
    });
    return TempCache;
  }
}
