package es.degrassi.common.loader;

public interface LoaderBootstrap {

  void onLoad();

  default void onEnable() {}

  default void onDisable() {}
}
