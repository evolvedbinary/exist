/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm;

public class RealmUtil {
    public static String getUsernameAtRealmPrincipal(final String username, final String realmName) {
        return username + '@' + realmName;
    }
}
