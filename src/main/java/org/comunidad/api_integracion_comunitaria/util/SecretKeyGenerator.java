package org.comunidad.api_integracion_comunitaria.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {

    public static void main(String[] args) {
        // Generamos una clave de 256 bits (32 bytes), suficiente para HS256
        byte[] keyBytes = new byte[32];
        new SecureRandom().nextBytes(keyBytes);

        // Codificamos a Base64 para que sea fácil de copiar y pegar
        String secretKey = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("\n========================================================");
        System.out.println(" TU NUEVA CLAVE SECRETA (Copia la línea de abajo):");
        System.out.println("========================================================");
        System.out.println(secretKey);
        System.out.println("========================================================\n");
    }
}