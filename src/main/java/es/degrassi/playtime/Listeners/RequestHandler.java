package es.degrassi.playtime.Listeners;

import es.degrassi.playtime.Commands.PlaytimeTopCommand;
import es.degrassi.playtime.Main;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.scheduler.ScheduledTask;

@SuppressWarnings({ "unused"})
public class RequestHandler implements Listener {
  private final Main main;
  private final PlaytimeTopCommand playtimeTopCommand;
  private final Gson gson = new Gson();
  private ScheduledTask task;
  public RequestHandler(Main main, PlaytimeTopCommand playtimeTopCommand) {
    this.main = main;
    this.playtimeTopCommand = playtimeTopCommand;
  }
  private final List<Server> pttServers = new ArrayList<>();

  public void onRequest(PluginMessageEvent e) {
    main.getProxy().getScheduler().runAsync(main, () -> {
      if(!(e.getSender() instanceof Server conn) || !Objects.equals(e.getTag(), main.MCI))
        return;
      final ByteArrayDataInput in = ByteStreams.newDataInput(e.getData());
      final String req = in.readUTF();
      switch (req) {
        case "rpt" -> main.getProxy().getScheduler().schedule(main, () -> {
          final HashMap<String, Long> pTempMap = new HashMap<>();
          conn.getInfo().getPlayers().forEach(player -> {
            final String name = player.getName();
            pTempMap.put(name, main.playtimeCache.get(name));
          });
          final ByteArrayDataOutput out = ByteStreams.newDataOutput();
          out.writeUTF("pt");
          out.writeUTF(gson.toJson(pTempMap));
          final ServerInfo server = conn.getInfo();
          server.sendData(main.MCI, out.toByteArray());
        }, 1L, TimeUnit.SECONDS);
        case "rtl" -> {
          pttServers.add(conn);
          if(task == null) {
            task = main.getProxy().getScheduler().schedule(main, () -> {
              final LinkedHashMap<String, Long> topMap = playtimeTopCommand.doSort(null);
              final String json = gson.toJson(topMap);
              final ByteArrayDataOutput out = ByteStreams.newDataOutput();
              out.writeUTF("ptt");
              out.writeUTF(json);
              pttServers.forEach(server -> server.sendData(main.MCI, out.toByteArray()));
            }, 1L, TimeUnit.SECONDS);
          }
        }
      }
    });
  }
}
