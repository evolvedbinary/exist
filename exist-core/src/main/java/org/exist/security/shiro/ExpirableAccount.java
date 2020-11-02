/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro;

public interface ExpirableAccount {

    boolean isCredentialsExpired();

    void setCredentialsExpired();
}
