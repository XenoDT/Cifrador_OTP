package com.dgomez.testing.service;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class OneTimePadService {

    public String cifrar(String mensajePlano, String clave) {
        if (mensajePlano == null || mensajePlano.isEmpty()) {
            throw new IllegalArgumentException("El mensaje original no puede estar vacío.");
        }
        byte[] msgBytes = mensajePlano.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = validarYConvertirClave(clave, msgBytes.length);

        byte[] cifrado = xorBytes(msgBytes, keyBytes);
        return Base64.getEncoder().encodeToString(cifrado);
    }

    public String descifrar(String mensajeCifradoBase64, String clave) {
        if (mensajeCifradoBase64 == null || mensajeCifradoBase64.isBlank()) {
            throw new IllegalArgumentException("El mensaje cifrado no puede estar vacío.");
        }

        byte[] cifradoBytes;
        try {
            cifradoBytes = Base64.getDecoder().decode(mensajeCifradoBase64.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El mensaje cifrado no es una cadena Base64 válida.");
        }

        byte[] keyBytes = validarYConvertirClave(clave, cifradoBytes.length);
        byte[] planoBytes = xorBytes(cifradoBytes, keyBytes);
        return decodificarUtf8Estricto(planoBytes);
    }

    private byte[] validarYConvertirClave(String clave, int longitudRequeridaBytes) {
        if (clave == null || clave.isEmpty()) {
            throw new IllegalArgumentException("La clave no puede estar vacía.");
        }
        byte[] keyBytes = clave.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < longitudRequeridaBytes) {
            throw new IllegalArgumentException(
                    "Clave demasiado corta para OTP: se requieren al menos " + longitudRequeridaBytes +
                            " bytes y se proporcionaron " + keyBytes.length + "."
            );
        }
        return keyBytes;
    }

    private byte[] xorBytes(byte[] datos, byte[] clave) {
        byte[] salida = new byte[datos.length];
        for (int i = 0; i < datos.length; i++) {
            salida[i] = (byte) (datos[i] ^ clave[i]);
        }
        return salida;
    }
    private String decodificarUtf8Estricto(byte[] bytes) {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            return decoder.decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw new IllegalArgumentException(
                    "El resultado descifrado no es texto UTF-8 válido (clave incorrecta o datos corruptos)."
            );
        }
    }
}
