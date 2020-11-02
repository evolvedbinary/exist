/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.internal;

import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.exist.Database;
import org.exist.EXistException;
import org.exist.config.ConfigurationException;
import org.exist.security.*;
import org.exist.security.SecurityManager;
import org.exist.security.realm.Realm;
import org.exist.storage.DBBroker;
import org.exist.storage.txn.Txn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ExistShiroInternalRealmAdapter implements Realm {

    private final Database database;
    private final DefaultSecurityManager shiroSecurityManager;
    private final InternalRealm internalRealm;

    public ExistShiroInternalRealmAdapter(final Database database, final DefaultSecurityManager shiroSecurityManager, final InternalRealm internalRealm) {
        this.database = database;
        this.shiroSecurityManager = shiroSecurityManager;
        this.internalRealm = internalRealm;
    }

    @Override
    public String getId() {
        return InternalRealm.REALM_NAME;
    }

    @Override
    public Collection<Account> getAccounts() {
        final List<Account> accounts = new ArrayList<>(3);
        accounts.add(new ExistShiroInternalAccountAdapter(database, shiroSecurityManager, internalRealm.getSystemAccount()));
        accounts.add(new ExistShiroInternalAccountAdapter(database, shiroSecurityManager, internalRealm.getGuestAccount()));
        accounts.add(new ExistShiroInternalAccountAdapter(database, shiroSecurityManager, internalRealm.getAdminAccount()));
        return accounts;
    }

    @Override
    public Collection<Group> getGroups() {
        final List<Group> groups = new ArrayList<>(2);
        groups.add(new ExistShiroInternalRoleAdapter(database, shiroSecurityManager, internalRealm.getDbaRole()));
        groups.add(new ExistShiroInternalRoleAdapter(database, shiroSecurityManager, internalRealm.getGuestRole()));
        return groups;
    }

    @Override
    public Database getDatabase() {
        return database;
    }

    @Override
    public Group getExternalGroup(final String name) {
        return null;
    }

    @Override
    public List<String> findUsernamesWhereNameStarts(final String startsWith) {
        //TODO(AR) this should be metadata based
        return findUsernamesWhereUsernameStarts(startsWith);
    }

    @Override
    public List<String> findUsernamesWhereNamePartStarts(final String startsWith) {
        //TODO(AR) this should be metadata based
        return findUsernamesWhereUsernameStarts(startsWith);
    }

    @Override
    public List<String> findUsernamesWhereUsernameStarts(String startsWith) {
        final List<String> matches = new ArrayList<>(1);

        if (SystemAccount.ACCOUNT_USERNAME.startsWith(startsWith)) {
            matches.add(SystemAccount.ACCOUNT_USERNAME);
        }

        if (AdminAccount.ACCOUNT_USERNAME.startsWith(startsWith)) {
            matches.add(AdminAccount.ACCOUNT_USERNAME);
        }

        if (GuestAccount.ACCOUNT_USERNAME.startsWith(startsWith)) {
            matches.add(GuestAccount.ACCOUNT_USERNAME);
        }

        return matches;
    }

    @Override
    public List<String> findAllGroupNames() {
        return Arrays.asList(DbaRole.ROLE_NAME, GuestRole.ROLE_NAME);
    }

    @Override
    public List<String> findAllGroupMembers(final String groupName) {
        final List<String> matches = new ArrayList<>(2);

        if (GuestRole.ROLE_NAME.equals(groupName)) {
            matches.add(GuestAccount.ACCOUNT_USERNAME);
        }

        if (DbaRole.ROLE_NAME.equals(groupName)) {
            matches.add(SystemAccount.ACCOUNT_USERNAME);
            matches.add(AdminAccount.ACCOUNT_USERNAME);
        }

        return matches;
    }

    @Override
    public List<String> findAllUserNames() {
        final List<String> matches = new ArrayList<>(3);
        matches.add(SystemAccount.ACCOUNT_USERNAME);
        matches.add(GuestAccount.ACCOUNT_USERNAME);
        matches.add(AdminAccount.ACCOUNT_USERNAME);
        return matches;
    }

    @Override
    public SecurityManager getSecurityManager() {
        return (SecurityManager) shiroSecurityManager;
    }

    @Override
    public Collection<? extends String> findGroupnamesWhereGroupnameStarts(final String startsWith) {
        final List<String> matches = new ArrayList<>(1);

        if (DbaRole.ROLE_NAME.startsWith(startsWith)) {
            matches.add(DbaRole.ROLE_NAME);
        }

        if (GuestRole.ROLE_NAME.startsWith(startsWith)) {
            matches.add(GuestRole.ROLE_NAME);
        }

        return matches;
    }

    @Override
    public Collection<? extends String> findGroupnamesWhereGroupnameContains(final String fragment) {
        final List<String> matches = new ArrayList<>(1);

        if (DbaRole.ROLE_NAME.contains(fragment)) {
            matches.add(DbaRole.ROLE_NAME);
        }

        if (GuestRole.ROLE_NAME.contains(fragment)) {
            matches.add(GuestRole.ROLE_NAME);
        }

        return matches;
    }

    @Override
    public void start(DBBroker broker, Txn transaction) throws EXistException {

    }

    @Override
    public void sync(DBBroker broker) throws EXistException {

    }

    @Override
    public void stop(DBBroker broker) throws EXistException {

    }

    @Override
    public Account addAccount(Account account) throws PermissionDeniedException, EXistException, ConfigurationException {
        throw new PermissionDeniedException("It is not possible to add accounts to the internal realm");
    }

    @Override
    public Account getAccount(final String name) {
        if (SystemAccount.ACCOUNT_USERNAME.equals(name)) {
            return new ExistShiroInternalAccountAdapter(database, shiroSecurityManager, internalRealm.getSystemAccount());
        }

        if (GuestAccount.ACCOUNT_USERNAME.equals(name)) {
            return new ExistShiroInternalAccountAdapter(database, shiroSecurityManager, internalRealm.getGuestAccount());
        }

        if (AdminAccount.ACCOUNT_USERNAME.equals(name)) {
            return new ExistShiroInternalAccountAdapter(database, shiroSecurityManager, internalRealm.getAdminAccount());
        }

        return null;
    }

    @Override
    public boolean hasAccount(final Account account) {
        if (SystemAccount.ACCOUNT_ID == account.getId()) {
            return true;
        }

        if (GuestAccount.ACCOUNT_ID == account.getId()) {
            return true;
        }

        if (AdminAccount.ACCOUNT_ID == account.getId()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean hasAccount(final String name) {
        if (SystemAccount.ACCOUNT_USERNAME.equals(name)) {
            return true;
        }

        if (GuestAccount.ACCOUNT_USERNAME.equals(name)) {
            return true;
        }

        if (AdminAccount.ACCOUNT_USERNAME.equals(name)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean hasAccountLocal(final Account account) {
        return hasAccount(account);
    }

    @Override
    public boolean hasAccountLocal(final String name) {
        return hasAccount(name);
    }

    @Override
    public boolean updateAccount(Account account) throws PermissionDeniedException, EXistException, ConfigurationException {
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public boolean deleteAccount(Account account) throws PermissionDeniedException, EXistException, ConfigurationException {
        throw new PermissionDeniedException("It is not possible to delete accounts from the internal realm");
    }

    @Override
    public Group addGroup(DBBroker broker, Group group) throws PermissionDeniedException, EXistException, ConfigurationException {
        throw new PermissionDeniedException("It is not possible to add groups to the internal realm");
    }

    @Override
    public Group getGroup(final String name) {
        if (DbaRole.ROLE_NAME.equals(name)) {
            return new ExistShiroInternalRoleAdapter(database, shiroSecurityManager, internalRealm.getDbaRole());
        }

        if (GuestRole.ROLE_NAME.equals(name)) {
            return new ExistShiroInternalRoleAdapter(database, shiroSecurityManager,internalRealm.getGuestRole());
        }

        return null;
    }

    @Override
    public boolean hasGroup(final Group group) {
        if (DbaRole.ROLE_ID == group.getId()) {
            return true;
        }

        if (GuestRole.ROLE_ID == group.getId()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean hasGroup(final String name) {
        if (DbaRole.ROLE_NAME.equals(name)) {
            return true;
        }

        if (GuestRole.ROLE_NAME.equals(name)) {
            return true;
        }

        return false;
    }

    @Override
    public boolean hasGroupLocal(final Group group) {
        return hasGroup(group);
    }

    @Override
    public boolean hasGroupLocal(final String name) {
        return hasGroup(name);
    }

    @Override
    public boolean updateGroup(Group group) throws PermissionDeniedException, EXistException, ConfigurationException {
        throw new UnsupportedOperationException("TODO(AR) NOT YET IMPLEMENTED");
    }

    @Override
    public boolean deleteGroup(Group group) throws PermissionDeniedException, EXistException, ConfigurationException {
        throw new PermissionDeniedException("It is not possible to delete groups from the internal realm");
    }

    @Override
    public Subject authenticate(final String accountName, final Object credentials) throws AuthenticationException {
        if (SystemAccount.ACCOUNT_USERNAME.equals(accountName)) {
            if (null != shiroSecurityManager.authenticate(new SystemAccount.SystemAuthenticationToken(credentials))) {
                return new ExistShiroInternalAccountAdapter(database, shiroSecurityManager, internalRealm.getSystemAccount());
            }
        }

        if (GuestAccount.ACCOUNT_USERNAME.equals(accountName) || AdminAccount.ACCOUNT_USERNAME.equals(accountName)
            && null != shiroSecurityManager.authenticate(
                new UsernamePasswordToken(accountName, credentials.toString()))) {

            if (GuestAccount.ACCOUNT_USERNAME.equals(accountName)) {
                return new ExistShiroInternalAccountAdapter(database, shiroSecurityManager, internalRealm.getGuestAccount());
            }

            if (AdminAccount.ACCOUNT_USERNAME.equals(accountName)) {
                return new ExistShiroInternalAccountAdapter(database, shiroSecurityManager, internalRealm.getAdminAccount());
            }
        }

        return null;
    }
}
