/*
 * Copyright (c) 2014 Evolved Binary Ltd
 */
package org.exist.security.shiro.realm.db;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HashedPassword {

    public enum Hash {
        MD5,
        RIPEMD160
    }

    private final Hash algorithm;
    private final String hashed;

    final Pattern ptnHash = Pattern.compile("\\{([A-Z0-9]+)\\}(.*)");

    public HashedPassword(final String hashedPasswordString) {

        final Matcher mtcHash = ptnHash.matcher(hashedPasswordString);

        if (mtcHash.matches()) {
            this.algorithm = Hash.valueOf(mtcHash.group(1));
            this.hashed = mtcHash.group(2);
        } else {
            throw new IllegalArgumentException("Unsupported hashed password string type");
        }
    }
}
