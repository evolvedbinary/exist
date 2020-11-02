/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

public class DbaRole extends InternalRole {

    public static final int ROLE_ID = 1048575;
    public static final String ROLE_NAME = "dba";

    DbaRole() {
        super(ROLE_ID, ROLE_NAME, "The eXist-db DBA (Database Administrator) role");
    }
}
