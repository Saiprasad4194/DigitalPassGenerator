package com.digitalpass.util;

import java.security.SecureRandom;

/**
 * Utility class for generating secure, collision-resistant digital pass codes.
 * Example generated code: "DP-8K3M2QX7"
 *
 * OOP Concept Demonstrated: Encapsulation & Abstraction
 * - The internal alphabet, random generator, and length constants are private (encapsulated).
 * - Callers interact with a single, clean static method generate().
 */
public final class PassCodeGenerator {

    // Prefix for all Digital Pass codes
    private static final String PREFIX = "DP-";

    // Unambiguous alphanumeric alphabet (excludes easily confused chars like 0/O, 1/I)
    private static final String ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";

    // Number of random characters following the prefix
    private static final int CODE_LENGTH = 8;

    // Cryptographically strong random number generator
    private static final SecureRandom RANDOM = new SecureRandom();

    // Private constructor prevents instantiation
    private PassCodeGenerator() {
    }

    /**
     * Generates a unique digital pass code.
     * Format: DP-XXXXXXXX (e.g., DP-8K3M2QX7)
     *
     * @return generated pass code string
     */
    public static String generate() {
        StringBuilder code = new StringBuilder(PREFIX);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = RANDOM.nextInt(ALPHABET.length());
            code.append(ALPHABET.charAt(randomIndex));
        }
        return code.toString();
    }
}
