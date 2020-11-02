/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

import org.exist.security.shiro.LockableAccount;

public abstract class LockableInternalAccount extends InternalAccount implements LockableAccount {

    private boolean locked;

    protected LockableInternalAccount(final int id, final String username, final String description,
            final Object credentials, final boolean locked) {
        super(id, username, description, credentials);
        this.locked = locked;
    }

    @Override
    public void setLocked(final boolean locked) {
        this.locked = locked;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }
}
