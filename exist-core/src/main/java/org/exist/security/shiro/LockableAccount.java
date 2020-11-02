/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro;

public interface LockableAccount {

    boolean isLocked();

    void setLocked(final boolean locked);
}
