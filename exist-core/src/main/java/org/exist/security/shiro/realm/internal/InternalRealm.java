/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

import org.apache.shiro.authc.*;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.exist.security.shiro.realm.RealmUtil;
import org.exist.util.UUIDGenerator;

public class InternalRealm extends org.apache.shiro.realm.AuthenticatingRealm {

    public static final String REALM_NAME = "existdb.internal";

    private final InternalRole dbaRole;
    private final InternalRole guestRole;

    private final InternalAccount systemAccount;
    private final SystemAccount.SystemAuthenticationToken systemAuthenticationToken;
    private final InternalAccount adminAccount;
    private final InternalAccount guestAccount;


//    public static final Object SYSTEM_OBJECT = new Object();

    public InternalRealm() {
        super();
        setName(REALM_NAME);

        this.dbaRole = new DbaRole();
        this.guestRole = new GuestRole();

        final Object systemCredentials = randomCredentials();
        this.systemAccount = new SystemAccount(systemCredentials);
        this.systemAccount.addRole(dbaRole);
        this.systemAuthenticationToken = new SystemAccount.SystemAuthenticationToken(systemCredentials);

        this.adminAccount = new AdminAccount();  // TODO(AR) the `credentials` and `locked` need to be loaded from the db
        this.adminAccount.addRole(dbaRole);

        this.guestAccount = new GuestAccount();  // TODO(AR) the `locked` needs to be loaded from the db
        this.guestAccount.addRole(guestRole);

    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(final AuthenticationToken token) throws org.apache.shiro.authc.AuthenticationException {
        if (token == null) {
            throw new org.apache.shiro.authc.AuthenticationException("Provided token is null");
        }

        InternalAccount account = null;
        if (token instanceof SystemAccount.SystemAuthenticationToken) {
            final SystemAccount.SystemAuthenticationToken systemAuthenticationToken = ((SystemAccount.SystemAuthenticationToken)token);
            if (systemAuthenticationToken.getPrincipal().equals(systemAccount.getUsername())
                    /*&& systemAuthenticationToken.getCredentials().equals(systemAccount.getCredentials()) */) {
                account = systemAccount;  // TODO(AR) -- should we check credentials here?
            }
        } else if (token instanceof UsernamePasswordToken) {
            final UsernamePasswordToken usernamePasswordToken = ((UsernamePasswordToken)token);

            if (usernamePasswordToken.getUsername().equals(guestAccount.getUsername())
                    /* && usernamePasswordToken.getCredentials().equals(guestAccount.getCredentials()) */) {
                account = guestAccount;  // TODO(AR) -- should we check credentials here?

            } else if (usernamePasswordToken.getUsername().equals(adminAccount.getUsername())
                    /* && usernamePasswordToken.getCredentials().equals(adminAccount.getCredentials()) */) {
                account = adminAccount;  // TODO(AR) -- should we check credentials here?
            }
        } else {
            throw new UnsupportedTokenException("The InternalRealm does not support tokens of type: " + token.getClass().getName());
        }

        if (account != null && account.isLocked()) {
            throw new LockedAccountException("Account [" + account + "] is locked.");
        }

        return account;
    }

    /**
     * This is only used internally for authenticating,
     * therefore a random value chosen at database
     * startup is quite sensible.
     *
     * @return a random UUID.
     */
    static final Object randomCredentials() {
        return UUIDGenerator.getUUIDversion4Object();
    }

    // TODO(AR) move this somewhere common
    static String getUsernameAtRealmPrincipal(final String username) {
        return RealmUtil.getUsernameAtRealmPrincipal(username, REALM_NAME);
    }

    // TODO(AR) move this somewhere common
    static String getRoleAtRealmPrincipal(final String roleName) {
        return roleName + '@' + REALM_NAME;
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    public InternalRole getDbaRole() {
        return dbaRole;
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    public InternalRole getGuestRole() {
        return guestRole;
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    public InternalAccount getSystemAccount() {
        return systemAccount;
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    public InternalAccount getGuestAccount() {
        return guestAccount;
    }

    // TODO(AR) temp for integration with eXist-db's current SecurityManager
    InternalAccount getAdminAccount() {
        return adminAccount;
    }
}
