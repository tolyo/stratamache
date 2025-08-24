package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnvLoader {
  private static final Map<String, String> fileVars = new HashMap<>();

  public static void loadFromFile(String path) {
    List<String> lines;
    try {
      lines = Files.readAllLines(Paths.get(path));
      for (String line : lines) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;

        String[] parts = line.split("=", 2);
        if (parts.length == 2) {
          fileVars.put(parts[0].trim(), parts[1].trim());
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String get(String key) {
    String value = System.getenv(key);
    return (value != null) ? value : fileVars.get(key);
  }
}
