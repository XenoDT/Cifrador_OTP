package com.dgomez.testing.service;

import java.security.SecureRandom;
import java.util.Base64;

public class GeneradorClaveOtp {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int LONGITUD_CLAVE = 5_000;

    public static String generarClave5k() {
        int numBytes = ((LONGITUD_CLAVE + 3) / 4) * 3;
        byte[] randomBytes = new byte[numBytes];
        SECURE_RANDOM.nextBytes(randomBytes);

        String base64 = Base64.getEncoder().encodeToString(randomBytes);
        return base64.substring(0, LONGITUD_CLAVE);
    }
}
