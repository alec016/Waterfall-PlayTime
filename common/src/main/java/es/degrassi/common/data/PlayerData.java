package es.degrassi.common.data;

import com.google.gson.JsonObject;
import es.degrassi.database.Database;
import es.degrassi.database.core.sql.annotations.modifier.Column;
import es.degrassi.database.core.sql.annotations.modifier.Default;
import es.degrassi.database.core.sql.annotations.modifier.NotNull;
import es.degrassi.database.core.sql.annotations.modifier.PrimaryKey;
import es.degrassi.database.core.sql.annotations.modifier.Table;
import es.degrassi.database.core.sql.annotations.type.LongInt;
import es.degrassi.database.core.sql.annotations.type.Varchar;
import es.degrassi.database.core.sql.query.Query;
import es.degrassi.database.util.InvalidStateException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("unused")
@Table("wpt_player_data")
public class PlayerData implements Comparable<PlayerData> {
  @Column("player")
  @PrimaryKey
  @NotNull
  @Varchar
  private final String player;
  @Column("name")
  @NotNull
  @Varchar
  private final String name;
  @Column("server")
  @PrimaryKey
  @NotNull
  @Varchar
  private final String server;
  @LongInt
  @NotNull
  @Default("0")
  @Column("time")
  private long time;

  private boolean isNew = true;

  public PlayerData(UUID player, String name, String server, long time) {
    this.player = player.toString();
    this.name = name;
    this.server = server;
    this.time = time;
  }

  public PlayerData(UUID player, String name, String server) {
    this(player, name, server, 0);
  }

  public String getPlayerString() {
    return player;
  }

  public UUID getPlayer() {
    return UUID.fromString(player);
  }

  public String getServer() {
    return server;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public String getName() {
    return name;
  }

  /**
   *
   * @param increment time to increment in milliseconds
   */
  public void increment(long increment) {
    this.time += increment;
  }

  /**
   * Increment by 1s(1000ms)
   */
  public void increment() {
    increment(1_000);
  }

  @Override
  public int compareTo(@org.jetbrains.annotations.NotNull PlayerData o) {
    return Long.compare(getTime(), o.getTime());
  }

  public void update() {
    Database.instance.get(PlayerData.class).ifPresent(table -> {
      try {
        Query query = Database.instance.getManager()
          .query()
          .update()
          .table(getClass())
          .set()
          .set("time", time)
          .build()
          .where()
          .create()
          .firstMember("player")
          .eq()
          .secondMember(player)
          .build()
          .and()
          .firstMember("server")
          .eq()
          .secondMember(server)
          .build().build();
        table.executeQuery(query);
      } catch(InvalidStateException | SQLException ignored) {}
    });
  }

  public void save() {
    if (!isNew) {
      update();
      return;
    }
    Database.instance.get(PlayerData.class).ifPresent(table -> {
      try {
        Query query = Database.instance.getManager()
          .query()
          .insert()
          .table(PlayerData.class)
          .columns(table.prepareColsForInsert())
          .values(table.prepareValues(this));
        table.executeQuery(query);
        isNew = false;
      } catch(InvalidStateException | SQLException ignored) {}
    });
  }

  @SuppressWarnings("unchecked")
  public static PlayerData get(UUID uuid, String server) {
    AtomicReference<PlayerData> data = new AtomicReference<>(null);
    Database.instance.get(PlayerData.class).ifPresent(table -> {
      try {
        Query query = Database.instance.getManager()
          .query()
          .select().all()
          .from().table(PlayerData.class)
          .where()
          .create()
          .firstMember("player").eq().secondMember(uuid.toString()).build()
          .and()
          .firstMember("server").eq().secondMember(server).build()
          .build();
        data.set(cast((List<Object>) table.selectWithQuery(query).get(0)));
      } catch(InvalidStateException | SQLException e) {
        System.out.println(e.getMessage());
      }
    });
    return data.get();
  }

  @SuppressWarnings("unchecked")
  public static List<PlayerData> get(UUID uuid) {
    List<PlayerData> data = new LinkedList<>();
    Database.instance.get(PlayerData.class).ifPresent(table -> {
      try {
        Query query = Database.instance.getManager()
          .query()
          .select().all()
          .from().table(PlayerData.class)
          .where()
          .create()
          .firstMember("player").eq().secondMember(uuid.toString()).build()
          .build();
        data.add(cast((List<Object>) table.selectWithQuery(query).get(0)));
      } catch(InvalidStateException | SQLException e) {
        System.out.println(e.getMessage());
      }
    });
    return data;
  }

  @SuppressWarnings("unchecked")
  public static List<PlayerData> getByServer(String server) {
    List<PlayerData> data = new LinkedList<>();
    Database.instance.get(PlayerData.class).ifPresent(table -> {
      try {
        Query query = Database.instance.getManager()
          .query()
          .select().all()
          .from().table(PlayerData.class)
          .where()
          .create()
          .firstMember("server").eq().secondMember(server).build()
          .build();
        data.add(cast((List<Object>) table.selectWithQuery(query).get(0)));
      } catch(InvalidStateException | SQLException e) {
        System.out.println(e.getMessage());
      }
    });
    return data;
  }

  public static PlayerData cast(List<Object> object) {
    if (object.size() > 4) throw new ClassCastException("Invalid object: " + object);
    AtomicReference<UUID> player = new AtomicReference<>(null);
    AtomicReference<String> name = new AtomicReference<>(null);
    AtomicReference<String> server = new AtomicReference<>(null);
    AtomicReference<Long> time = new AtomicReference<>(null);
    object.forEach(o -> {
      if (o instanceof Long l) time.set(l);
      else if (o instanceof String s) {
        try {
          UUID uuid = UUID.fromString(s);
          player.set(uuid);
        } catch (IllegalArgumentException e) {
          if (name.get() != null)
            server.set(s);
          else
            name.set(s);
        }
      } else {
        throw new ClassCastException("Invalid object: " + object);
      }
    });
    if (player.get() == null || server.get() == null || time.get() == null) throw new ClassCastException("Invalid object: " + object);
    PlayerData playTime = new PlayerData(player.get(), name.get(), server.get(), time.get());
    playTime.isNew = false;
    return playTime;
  }

  @SuppressWarnings("unchecked")
  public static void load(List<PlayerData> playTimeCache) throws InvalidStateException, SQLException, ClassCastException {
    AtomicReference<InvalidStateException> refInvState = new AtomicReference<>(null);
    AtomicReference<SQLException> refSQL = new AtomicReference<>(null);
    AtomicReference<ClassCastException> refClassCast = new AtomicReference<>(null);

    Database.instance.get(PlayerData.class).ifPresent(table -> {
      try {
        Query query = Database.instance.getManager()
          .query()
          .select().all()
          .from().table(PlayerData.class);
        List<Object> list = table.selectWithQuery(query);
        list.stream()
          .filter(object -> object instanceof List)
          .map(object -> (List<Object>) object)
          .forEach(object -> {
            PlayerData playTime = PlayerData.cast(object);
            playTimeCache.add(playTime);
          });
      } catch (InvalidStateException | SQLException | ClassCastException e) {
        if (e instanceof InvalidStateException i) refInvState.set(i);
        else if (e instanceof SQLException i) refSQL.set(i);
        else refClassCast.set((ClassCastException) e);
      }
    });
    if (refInvState.get() != null) throw refInvState.get();
    if (refSQL.get() != null) throw refSQL.get();
    if (refClassCast.get() != null) throw refClassCast.get();
  }

  public JsonObject asJson() {
    JsonObject json = new JsonObject();
    json.addProperty("player", player);
    json.addProperty("name", name);
    json.addProperty("server", server);
    json.addProperty("time", time);
    return json;
  }

  @Override
  public String toString() {
    return asJson().toString();
  }
}
