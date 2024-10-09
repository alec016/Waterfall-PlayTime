package es.degrassi.playtime.handler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import es.degrassi.playtime.Main;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.logging.Level;

public class UpdateHandler {
  private final Gson gson = new Gson();
  private final Main main;

  public UpdateHandler(Main main) {
    this.main = main;
  }
  public void checkForUpdates() {
//    String currentVersion = main.getDescription().getVersion();
//    String latestVersion = getLatestVersion();
//
//    if (latestVersion != null && !currentVersion.equals(latestVersion))
//      main.getLogger().log(Level.WARNING, "New version available: {}.", latestVersion);
//    else
//      main.getLogger().info("You are using the latest version.");
  }

  private String getLatestVersion() {
    // Spigot resource ID
    int resourceId = 117308; // TODO: change resourceId
    String versionUrl = "https://api.spiget.org/v2/resources/" + resourceId + "/versions/latest";
    try {
      HttpURLConnection connection = (HttpURLConnection) new URI(versionUrl).toURL().openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);

      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        return jsonObject.get("name").getAsString().trim();
      }
    } catch (Exception e) {
      main.getLogger().log(Level.SEVERE, "Failed to check for updates: {0}", e.getMessage());
      return null;
    }
  }
}
