/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

import org.apache.shiro.authc.AuthenticationToken;

public class SystemAccount extends InternalAccount {

    public static final int ACCOUNT_ID = 1048575;
    public static final String ACCOUNT_USERNAME = "SYSTEM";

    SystemAccount(final Object credentials) {
        super(ACCOUNT_ID, ACCOUNT_USERNAME, "The eXist-db internal System account", credentials);
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    static class SystemAuthenticationToken implements AuthenticationToken {
        private final Object credentials;

        SystemAuthenticationToken(final Object credentials) {
            this.credentials = credentials;
        }

        @Override
        public Object getPrincipal() {
            return InternalRealm.getUsernameAtRealmPrincipal(ACCOUNT_USERNAME);
        }

        @Override
        public Object getCredentials() {
            return credentials;
        }
    }
}
