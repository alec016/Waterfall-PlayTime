package es.degrassi.playtimeLink;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import es.degrassi.common.CommonConfigHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;

@SuppressWarnings("unused")
public class ConfigHandler implements CommonConfigHandler {
  private final PlaytimeLink main;
  private YamlDocument config;

  private String LOADING;

  private Component NO_PERMISSION;
  private Component CONFIG_RELOAD;
  private Component INVALID_ARGS;

  private boolean CHECK_FOR_UPDATES;
  private boolean BSTATS;
  private int TOPLIST_LIMIT;

  private long genTime;
  private long start;

  private String dbUser;
  private String dbPassword;
  private String dbHost;
  private String dbName;
  private int dbPort;

  public ConfigHandler(PlaytimeLink main) {
    this.main = main;
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

  public boolean isBSTATS() {
    return BSTATS;
  }

  public boolean isCHECK_FOR_UPDATES() {
    return CHECK_FOR_UPDATES;
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
    INVALID_ARGS = initComp("Messages.INVALID_ARGS");
    CONFIG_RELOAD = initComp("Messages.CONFIG_RELOAD");
    TOPLIST_LIMIT = config.getInt("Data.TOPLIST_LIMIT");
    //Rewards.
    genTime = System.currentTimeMillis() - start;
  }

  public void makeNonChanging() {
    start = System.currentTimeMillis();
    LOADING = config.getString("Messages.LOADING");
    // Database
    dbUser = config.getString("Database.username");
    dbPassword = config.getString("Database.password");
    dbName = config.getString("Database.dbName");
    dbHost = config.getString("Database.host");
    dbPort = config.getInt("Database.port");

    BSTATS = config.getBoolean("Data.BSTATS");
    CHECK_FOR_UPDATES = config.getBoolean("Data.CHECK_FOR_UPDATES");
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

  public String getLoadingMsg() {
    return LOADING;
  }
  public Component getNO_PERMISSION() {
    return NO_PERMISSION;
  }

  public Component getCONFIG_RELOAD() {
    return CONFIG_RELOAD;
  }

  public Component getINVALID_ARGS() {
    return INVALID_ARGS;
  }

  public int getTOPLIST_LIMIT() {
    return TOPLIST_LIMIT;
  }

  private Component initComp(String path) {
    final String message = config.getString(path);
    return decideNonComponent(message);
  }

  public Component decideNonComponent(String message) {
    return Component.text(message);
  }
}
