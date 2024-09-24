import es.degrassi.common.loader.JarInJarClassLoader;
import es.degrassi.common.loader.LoaderBootstrap;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeLoaderPlugin extends Plugin {
  private static final String JAR_NAME = "luckperms-bungee.jarinjar";
  private static final String BOOTSTRAP_CLASS = "me.lucko.luckperms.bungee.LPBungeeBootstrap";

  private final LoaderBootstrap plugin;

  public BungeeLoaderPlugin() {
    JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
    this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, Plugin.class, this);
  }

  @Override
  public void onLoad() {
    this.plugin.onLoad();
  }

  @Override
  public void onEnable() {
    this.plugin.onEnable();
  }

  @Override
  public void onDisable() {
    this.plugin.onDisable();
  }
}
