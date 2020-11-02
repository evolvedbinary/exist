/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

public class GuestRole extends InternalRole {

    public static final int ROLE_ID = 1048574;
    public static final String ROLE_NAME = "guest";

    GuestRole() {
        super(ROLE_ID, ROLE_NAME, "The eXist-db Guest (anonymous) role");
    }
}
