package es.degrassi.common;

public interface CommonConfigHandler {
  String getDBUser();

  String getDBPassword();

  String getDBHost();

  String getDBName();

  int getDBPort();
}
