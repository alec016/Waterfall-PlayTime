package es.degrassi.common;

import es.degrassi.common.data.PlayerData;
import es.degrassi.database.Database;

public class Common {
  public static final String MCI = "waterfall:playtime";
  public static void registerDB(CommonConfigHandler commonConfigHandler) {
    Database db = Database.SQLDatabase(
      false,
      commonConfigHandler.getDBUser(),
      commonConfigHandler.getDBPassword(),
      commonConfigHandler.getDBHost(),
      commonConfigHandler.getDBName(),
      commonConfigHandler.getDBPort()
    );
    db.getManager().create(
      () -> PlayerData.class
    );
  }
}
