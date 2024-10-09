package es.degrassi.playtime.handler;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import es.degrassi.common.CommonConfigHandler;
import es.degrassi.playtime.Main;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

@SuppressWarnings("unused")
public class ConfigHandler implements CommonConfigHandler {
  private final Main main;
  private YamlDocument config;

  private BaseComponent NO_CONSOLE_USE;
  private String YOUR_PLAYTIME;
  private BaseComponent NO_PLAYER;
  private String OTHER_PLAYTIME;
  private BaseComponent NO_PERMISSION;
  private BaseComponent CONFIG_RELOAD;
  private String PTRESET;
  private BaseComponent PTRESET_HELP;
  private BaseComponent PTRESETALL;
  private BaseComponent PTRESETALL_CONFIRM;
  private BaseComponent INVALID_ARGS;
  private String TOP_PLAYTIME_HEADER;
  private String TOP_PLAYTIME_LIST;
  private BaseComponent TOP_PLAYTIME_FOOTER;

  private int TOPLIST_LIMIT;
  private boolean BSTATS;
  private boolean CHECK_FOR_UPDATES;
  private boolean USE_CACHE;
  private long CACHE_UPDATE_INTERVAL;
  private boolean VIEW_OWN_TIME;
  private boolean VIEW_OTHERS_TIME;
  private boolean VIEW_TOPLIST;
  private boolean isDataFileUpToDate;
  private long genTime;
  private long start;

  private String dbUser;
  private String dbPassword;
  private String dbHost;
  private String dbName;
  private int dbPort;

//  public HashMap<Long, String> rewardsH = new HashMap<>();

  public HashMap<String, HashMap<Long, String>> rewardsH = new HashMap<>();

  public ConfigHandler(Main main) {
    this.main = main;
  }

  public BaseComponent getNO_CONSOLE_USE() {
    return NO_CONSOLE_USE;
  }

  public String getYOUR_PLAYTIME() {
    return YOUR_PLAYTIME;
  }

  public BaseComponent getNO_PLAYER() {
    return NO_PLAYER;
  }

  public String getOTHER_PLAYTIME() {
    return OTHER_PLAYTIME;
  }

  public BaseComponent getNO_PERMISSION() {
    return NO_PERMISSION;
  }

  public BaseComponent getCONFIG_RELOAD() {
    return CONFIG_RELOAD;
  }

  public String getPTRESET() {
    return PTRESET;
  }

  public BaseComponent getPTRESET_HELP() {
    return PTRESET_HELP;
  }

  public BaseComponent getPTRESETALL() {
    return PTRESETALL;
  }

  public BaseComponent getPTRESETALL_CONFIRM() {
    return PTRESETALL_CONFIRM;
  }

  public BaseComponent getINVALID_ARGS() {
    return INVALID_ARGS;
  }

  public String getTOP_PLAYTIME_HEADER() {
    return TOP_PLAYTIME_HEADER;
  }

  public String getTOP_PLAYTIME_LIST() {
    return TOP_PLAYTIME_LIST;
  }

  public BaseComponent getTOP_PLAYTIME_FOOTER() {
    return TOP_PLAYTIME_FOOTER;
  }

  public String getDBUser() {
    return dbUser;
  }

  public String getDBPassword() {
    return dbPassword;
  }

  public String getDBHost() {
    return dbHost;
  }

  public String getDBName() {
    return dbName;
  }

  public int getDBPort() {
    return dbPort;
  }

  public int getTOPLIST_LIMIT() {
    return TOPLIST_LIMIT;
  }

  public boolean isBSTATS() {
    return BSTATS;
  }

  public boolean isCHECK_FOR_UPDATES() {
    return CHECK_FOR_UPDATES;
  }

  public boolean isUSE_CACHE() {
    return USE_CACHE;
  }

  public long getCACHE_UPDATE_INTERVAL() {
    return CACHE_UPDATE_INTERVAL;
  }

  public boolean isVIEW_OWN_TIME() {
    return VIEW_OWN_TIME;
  }

  public boolean isVIEW_OTHERS_TIME() {
    return VIEW_OTHERS_TIME;
  }

  public boolean isVIEW_TOPLIST() {
    return VIEW_TOPLIST;
  }

  public boolean isDataFileUpToDate() {
    return isDataFileUpToDate;
  }

  public long getGenTime() {
    return genTime;
  }

  public long getStart() {
    return start;
  }

  public void initConfig(Path dataDirectory) {
    try {
      config = YamlDocument.create(new File(dataDirectory.toFile(), "config.yml"),
        Objects.requireNonNull(getClass().getResourceAsStream("/config.yml")),
        GeneralSettings.DEFAULT,
        LoaderSettings.builder().setAutoUpdate(true).build(),
        DumperSettings.DEFAULT,
        UpdaterSettings.builder().setVersioning(new BasicVersioning("file-version"))
          .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
      );
      config.update();
      config.save();
    } catch (IOException e) {
      main.getLogger().log(Level.SEVERE, "Config initialize error. {0}", e);
      main.onDisable();
    }
  }

  public void makeConfigCache() {
    NO_PERMISSION = initComp("Messages.NO_PERMISSION");
    NO_CONSOLE_USE = initComp("Messages.NO_CONSOLE_USE");
    YOUR_PLAYTIME = config.getString("Messages.YOUR_PLAYTIME");
    NO_PLAYER = initComp("Messages.NO_PLAYER");
    OTHER_PLAYTIME = config.getString("Messages.OTHER_PLAYTIME");
    CONFIG_RELOAD = initComp("Messages.CONFIG_RELOAD");
    PTRESET = config.getString("Messages.PTRESET");
    PTRESET_HELP = initComp("Messages.PTRESET_HELP");
    PTRESETALL = initComp("Messages.PTRESETALL");
    PTRESETALL_CONFIRM = initComp("Messages.PTRESETALL_CONFIRM");
    INVALID_ARGS = initComp("Messages.INVALID_ARGS");
    TOP_PLAYTIME_HEADER = config.getString("Messages.TOP_PLAYTIME_HEADER");
    TOP_PLAYTIME_LIST = config.getString("Messages.TOP_PLAYTIME_LIST");
    TOP_PLAYTIME_FOOTER = initComp("Messages.TOP_PLAYTIME_FOOTER");

    VIEW_OWN_TIME = config.getBoolean("Data.PERMISSIONS.VIEW_OWN_TIME");
    VIEW_OTHERS_TIME = config.getBoolean("Data.PERMISSIONS.VIEW_OTHERS_TIME");
    VIEW_TOPLIST = config.getBoolean("Data.PERMISSIONS.VIEW_TOPLIST");
    if(!USE_CACHE)
      TOPLIST_LIMIT = config.getInt("Data.TOPLIST_LIMIT");

    //Rewards.
    getConfigIterator("Rewards").forEachRemaining(key -> {
      HashMap<Long, String> rewardPerServer = rewardsH.computeIfAbsent(((String) key).toLowerCase(Locale.ROOT), (k) -> new HashMap<>());
      getConfigIterator("Rewards." + key).forEachRemaining(key2 -> {
        rewardPerServer.put(Long.valueOf((String) key2), config.getString("Rewards." + key + "." + key2));
      });
    });
//    getConfigIterator("Rewards").forEachRemaining(key -> rewardsH.put(Long.valueOf((String) key), config.getString("Rewards." + key)));
    genTime = System.currentTimeMillis() - start;
  }

  public void makeNonChanging() {
    start = System.currentTimeMillis();
    // Database
    dbUser = config.getString("Database.username");
    dbPassword = config.getString("Database.password");
    dbName = config.getString("Database.dbName");
    dbHost = config.getString("Database.host");
    dbPort = config.getInt("Database.port");

    USE_CACHE = config.getBoolean("Data.CACHING.USE_CACHE");
    CACHE_UPDATE_INTERVAL = config.getLong("Data.CACHING.CACHE_UPDATE_INTERVAL");
    BSTATS = config.getBoolean("Data.BSTATS");
    CHECK_FOR_UPDATES = config.getBoolean("Data.CHECK_FOR_UPDATES");
    isDataFileUpToDate = config.getBoolean("isDataFileUpToDate");
    if(USE_CACHE)
      TOPLIST_LIMIT = config.getInt("Data.TOPLIST_LIMIT");
  }

  public Iterator<?> getConfigIterator(String path) {
    final Section section = config.getSection(path);
    return section != null ? section.getKeys().iterator() : Collections.emptyIterator();
  }

  public String getPermsUsageCount() {
    int i = 0;
    final String basePath = "Data.PERMISSIONS";
    Iterator<?> iterator = getConfigIterator(basePath);
    while (iterator.hasNext()) {
      String path = (String) iterator.next();
      if(config.getBoolean(basePath + "." + path))
        i++;
      iterator.remove();
    }
    return String.valueOf(i);
  }

  public void modifyConfig(String path, Object value) {
    config.set(path, value);
    try {
      config.save();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void reloadConfig() {
    try {
      config.reload();
      makeConfigCache();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private BaseComponent initComp(String path) {
    final String message = config.getString(path);
    return decideNonComponent(message);
  }

  public BaseComponent decideNonComponent(String message) {
    return new TextComponent(message);
  }
}
