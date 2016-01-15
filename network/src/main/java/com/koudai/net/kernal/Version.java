
package com.koudai.net.kernal;

public final class Version {
  public static String userAgent() {
    return "okhttp/${project.version}";
  }

  private Version() {
  }
}
