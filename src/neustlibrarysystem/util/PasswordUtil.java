package neustlibrarysystem.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for password hashing using SHA-256.
 * For production consider bcrypt via an external library.
 */
public class PasswordUtil {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtil.class.getName());

    private PasswordUtil() {}

    /**
     * Hashes the given plain-text password using SHA-256.
     *
     * @param plainPassword the raw password
     * @return hex-encoded SHA-256 hash, or null on error
     */
    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, "SHA-256 algorithm not available.", e);
            return null;
        }
    }

    /**
     * Verifies a plain-text password against a stored hash.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        String hash = hashPassword(plainPassword);
        return hash != null && hash.equalsIgnoreCase(storedHash);
    }
}