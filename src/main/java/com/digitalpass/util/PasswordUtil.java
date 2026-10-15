package com.digitalpass.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing passwords using SHA-256.
 *
 * OOP Concept Demonstrated: Abstraction & Utility Pattern
 * - Utility classes have private constructors to prevent instantiation.
 * - Hides the complex cryptographic details behind a simple static hash() method.
 *
 * OOP Concept Demonstrated: Exception Handling
 * - Catches checked exception NoSuchAlgorithmException and handles it cleanly.
 */
public final class PasswordUtil {

    // Private constructor to prevent object creation (Utility Class Pattern)
    private PasswordUtil() {
        throw novelUnsupportedOperationException();
    }

    private static UnsupportedOperationException novelUnsupportedOperationException() {
        return new UnsupportedOperationException("PasswordUtil is a utility class and cannot be instantiated.");
    }

    /**
     * Hashes a plaintext password using SHA-256 algorithm.
     *
     * @param plainText the raw password string
     * @return 64-character lowercase hexadecimal hash string
     */
    public static String hash(String plainText) {
        if (plainText == null) {
            throw new IllegalArgumentException("Password string cannot be null.");
        }

        try {
            // Obtain SHA-256 MessageDigest instance from standard Java Security API
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convert string into UTF-8 bytes and calculate hash
            byte[] hashBytes = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));

            // Convert raw byte array into a human-readable 64-character hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                // Format each byte as 2-digit hexadecimal with leading zero if needed
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Exception Handling: SHA-256 is guaranteed in all standard Java SE runtimes.
            // Wrap in RuntimeException if it somehow fails, preventing checked exception leakage.
            throw new RuntimeException("SHA-256 algorithm not available in current Java runtime", e);
        }
    }
}
