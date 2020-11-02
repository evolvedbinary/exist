/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

public class AdminAccount extends LockableInternalAccount {

    public static final int ACCOUNT_ID = 1048574;
    public static final String ACCOUNT_USERNAME = "admin";
    public static final String DEFAULT_ACCOUNT_PASSWORD = "";  // TODO(AR) we can do better... can we auto-generate something at first-startup

    AdminAccount() {
        super(ACCOUNT_ID, ACCOUNT_USERNAME, "The eXist-db Admin (root) account", DEFAULT_ACCOUNT_PASSWORD, false);
    }
}
