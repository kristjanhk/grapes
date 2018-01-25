package eu.kyngas.grapes.common.util;

import java.io.File;

/**
 * @author <a href="https://github.com/kristjanhk">Kristjan Hendrik Küngas</a>
 */
public class ConfigUtil {

  private static String getJarName() {
    return new File(ConfigUtil.class.getProtectionDomain()
        .getCodeSource()
        .getLocation()
        .getPath())
        .getName();
  }

  public static boolean isRunningFromJar() {
    return getJarName().contains(".jar");
  }
}
