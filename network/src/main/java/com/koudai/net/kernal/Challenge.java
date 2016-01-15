
package com.koudai.net.kernal;

import static com.koudai.net.kernal.internal.Util.equal;

/** An RFC 2617 challenge. */
public final class Challenge {
  private final String scheme;
  private final String realm;

  public Challenge(String scheme, String realm) {
    this.scheme = scheme;
    this.realm = realm;
  }

  /** Returns the authentication scheme, like {@code Basic}. */
  public String scheme() {
    return scheme;
  }

  /** Returns the protection space. */
  public String realm() {
    return realm;
  }

  @Override public boolean equals(Object o) {
    return o instanceof Challenge
        && equal(scheme, ((Challenge) o).scheme)
        && equal(realm, ((Challenge) o).realm);
  }

  @Override public int hashCode() {
    int result = 29;
    result = 31 * result + (realm != null ? realm.hashCode() : 0);
    result = 31 * result + (scheme != null ? scheme.hashCode() : 0);
    return result;
  }

  @Override public String toString() {
    return scheme + " realm=\"" + realm + "\"";
  }
}
