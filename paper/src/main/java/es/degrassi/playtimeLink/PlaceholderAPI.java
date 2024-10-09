package es.degrassi.playtimeLink;

import es.degrassi.common.data.PlayerData;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPI extends PlaceholderExpansion {
  private final PlaytimeLink main;

  public PlaceholderAPI(PlaytimeLink main) {
    this.main = main;
  }

  @Override
  public @NotNull String getIdentifier() {
    return "wptlink";
  }

  @Override
  public @NotNull String getAuthor() {
    return "alec016";
  }

  @Override
  public @NotNull String getVersion() {
    return main.getPluginMeta().getVersion();
  }

  @Override
  public boolean canRegister() {
    return true;
  }

  @Override
  public boolean persist() {
    return true;
  }

  @Override
  public @Nullable String onPlaceholderRequest(Player player, @NotNull String ID) {
    if (!ID.startsWith("server")) return "ERR_NO_SERVER_PROVIDDED";
    ID = ID.substring("server_".length());
    int serverIndexEnd = ID.indexOf('_') + 1;
    String server = ID.substring(0, serverIndexEnd).replace("_", "");
    ID = ID.substring(serverIndexEnd);
    List<PlayerData> list = getTopList(server);

    if (ID.equals("place")) {
      AtomicInteger i = new AtomicInteger(0);
      list.stream().filter(data -> data.getPlayer().equals(player.getUniqueId())).findFirst().ifPresent(data -> {
        i.set(list.indexOf(data) + 1);
      });
      if (i.get() > 0)
        return i.get() + "";
      return "NOT_IN_TOPLIST";
    }

    if (!ID.startsWith("top")) {
      if (ID.startsWith("all")) {
        ID = ID.substring(4);
        AtomicLong time = new AtomicLong(0);
        PlayerData.get(player.getUniqueId()).forEach(data -> time.getAndAdd(data.getTime()));
        return getTimeString(ID, time.get());
      }
      PlayerData data = PlayerData.get(player.getUniqueId(), server);
      return getTimeString(ID, data.getTime());
    }

    ID = ID.substring(4);

    if (ID.substring(0, 2).replaceAll("^\\d", "").length() == 2) {
      int pos = Integer.parseInt(ID.substring(0, 2)) - 1;
      if (pos < 0) pos = 0;
      if (pos >= list.size()) pos = list.size() - 1;
      ID = ID.substring(2);
      if (ID.equals("name"))
        return list.get(pos).getName();
      long time = list.get(pos).getTime();
      return getTimeString(ID, time);
    } else if (ID.substring(0, 1).replaceAll("^\\d", "").length() == 1) {
      int pos = Integer.parseInt(ID.substring(0, 1)) - 1;
      if (pos < 0) pos = 0;
      if (pos >= list.size()) pos = list.size() - 1;
      ID = ID.substring(1);
      if (ID.equals("name"))
        return list.get(pos).getName();
      long time = list.get(pos).getTime();
      return getTimeString(ID, time);
    }
    return main.configHandler.getLoadingMsg();
  }

  private List<PlayerData> getTopList(String server) {
    List<PlayerData> list = PlayerData.getByServer(server);
    list.sort(null);
    list = list.reversed();
    if (list.size() < main.configHandler.getTOPLIST_LIMIT()) return list;
    return list.subList(0, main.configHandler.getTOPLIST_LIMIT());
  }

  private String getTimeString(String ID, long time) {
    return switch (ID) {
      case "hours", "h" -> main.calculatePlayTime(time, 'h');
      case "minutes", "min" -> main.calculatePlayTime(time, 'm');
      case "seconds", "s", "sec" -> main.calculatePlayTime(time, 's');
      default -> main.configHandler.getLoadingMsg();
    };
  }
}
