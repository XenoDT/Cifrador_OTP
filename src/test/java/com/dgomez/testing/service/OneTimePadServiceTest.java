package com.dgomez.testing.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OneTimePadServiceTest {

    private OneTimePadService service;
    private GeneradorClaveOtp generadorClaveOtp;
    @BeforeEach
    void setUp() {
        service = new OneTimePadService();
    }

    @Test
    void cifrarYDescifrarConClaveSuficienteDevuelveOriginal() {
        String mensaje = "Hola mundo con tildes: áéíóú ñ";
        // "áéíóú" y "ñ" ocupan más de 1 byte en UTF-8, así que usamos una clave holgada
        String clave = "x".repeat(100);

        String cifrado = service.cifrar(mensaje, clave);
        String descifrado = service.descifrar(cifrado, clave);

        assertNotEquals(mensaje, cifrado);
        assertEquals(mensaje, descifrado);
    }

    @Test
    void funcionaConClaveDelGenerador5k() {
        String clave = generadorClaveOtp.generarClave5k();
        String mensaje = "Mensaje de prueba con generador de 5k.";

        String cifrado = service.cifrar(mensaje, clave);
        assertEquals(mensaje, service.descifrar(cifrado, clave));
    }

    @Test
    void fallaSiLaClaveEsMasCortaEnBytesQueElMensaje() {
        String mensaje = "Hola"; // 4 bytes en UTF-8
        String claveCorta = "abc"; // 3 bytes en UTF-8

        assertThrows(IllegalArgumentException.class, () -> service.cifrar(mensaje, claveCorta));
    }

    @Test
    void fallaSiMensajeCifradoEstaEnBlanco() {
        assertThrows(IllegalArgumentException.class, () -> service.descifrar("   ", "clavevalida123"));
    }

    @Test
    void fallaSiBase64NoEsValido() {
        assertThrows(IllegalArgumentException.class, () -> service.descifrar("esto-no-es-base64!", "clavevalida123"));
    }
}