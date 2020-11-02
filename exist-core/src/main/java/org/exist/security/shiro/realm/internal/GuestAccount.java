/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

public class GuestAccount extends LockableInternalAccount {

    public static final int ACCOUNT_ID = 1048573;
    public static final String ACCOUNT_USERNAME = "guest";

    GuestAccount() {
        super(ACCOUNT_ID, ACCOUNT_USERNAME, "The eXist-db Guest (anonymous) account", ACCOUNT_USERNAME, false);
    }
}
