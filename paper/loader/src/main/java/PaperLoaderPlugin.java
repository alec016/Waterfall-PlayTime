import es.degrassi.common.loader.JarInJarClassLoader;
import es.degrassi.common.loader.LoaderBootstrap;
import org.bukkit.plugin.java.JavaPlugin;

public class PaperLoaderPlugin extends JavaPlugin {
  private static final String JAR_NAME = "luckperms-bungee.jarinjar";
  private static final String BOOTSTRAP_CLASS = "me.lucko.luckperms.bungee.LPBungeeBootstrap";

  private final LoaderBootstrap plugin;

  public PaperLoaderPlugin() {
    JarInJarClassLoader loader = new JarInJarClassLoader(getClass().getClassLoader(), JAR_NAME);
    this.plugin = loader.instantiatePlugin(BOOTSTRAP_CLASS, JavaPlugin.class, this);
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
