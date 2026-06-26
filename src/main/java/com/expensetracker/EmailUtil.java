/*
 * EmailUtil.java
 *
 * Description (start):
 * - Small helper for sending email from the app.
 * - Purpose: send OTP and reset messages using SMTP credentials.
 * - Why: the signup and reset-password screens need email delivery without mixing SMTP code into UI classes.
 *
 * End of start description.
 */
package com.expensetracker;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public final class EmailUtil {

    private EmailUtil() {}

    // Send a simple text email using SMTP.
    // This method is used by signup and password reset screens to send OTP codes.
    // It keeps all email configuration in one helper so UI code stays clean.
    public static void sendEmail(String to, String subject, String body) throws MessagingException { //actual email code send
        String user = getConfigValue("APP_SMTP_USER");
        String pass = getConfigValue("APP_SMTP_PASS");
        if (user == null || pass == null) {
            throw new MessagingException("SMTP credentials not set in environment variables or .env file (APP_SMTP_USER / APP_SMTP_PASS)");
        }

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, pass);
            }
        });
    //email banai jati hai
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(user)); //send from which email
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false)); //who is receiver?
        msg.setSubject(subject); //otp verifcation
        msg.setText(body);
        Transport.send(msg);
    }

    private static String getConfigValue(String key) { //email crediential lata hai
        String value = System.getenv(key);
        if (value != null && !value.trim().isEmpty()) {
            String v = value.trim(); //trim karke check karo ki empty to nahi hai
            if ("APP_SMTP_PASS".equals(key)) {
                v = v.replaceAll("\\s+", "");
            }
            return v;
        }

        // Try loading credentials from a local .env file if environment variables are not set.
        File envFile = new File(".env"); 
        if (!envFile.exists()) {
            return null;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }
                int idx = line.indexOf('=');
                String name = line.substring(0, idx).trim();
                String raw = line.substring(idx + 1).trim();
                if (name.equals(key)) {
                    String v = stripQuotes(raw);
                    if ("APP_SMTP_PASS".equals(key)) {
                        v = v.replaceAll("\\s+", "");
                    }
                    return v;
                }
            }
        } catch (IOException ignored) {
            // If .env cannot be read, fall back to null and let caller show the error.
        }

        return null;
    }

    // Remove wrapping quotes from values read from .env files.
    // This lets users write APP_SMTP_PASS="my secret" or APP_SMTP_PASS='my secret'.
    private static String stripQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1).trim();
        }
        return value;
    }
}

/*
 * EmailUtil.java
 *
 * End description: Helper that centralizes SMTP email sending so UI classes do not contain mail protocol logic.
 */