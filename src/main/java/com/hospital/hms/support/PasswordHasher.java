package com.hospital.hms.support;

import at.favre.lib.crypto.bcrypt.BCrypt;

public final class PasswordHasher {

    private PasswordHasher() {
    }

    public static String hash(String plain) {
        return BCrypt.withDefaults().hashToString(12, plain.toCharArray());
    }

    public static boolean verify(String plain, String hash) {
        return BCrypt.verifyer().verify(plain.toCharArray(), hash).verified;
    }
}
