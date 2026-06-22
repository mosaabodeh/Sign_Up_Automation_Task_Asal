package utils;

import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailUtils {

    public static String getVerificationCode(String email, String password) {
        String host = "imap.gmail.com";
        Properties properties = new Properties();
        properties.put("mail.imap.host", host);
        properties.put("mail.imap.port", "993");
        properties.put("mail.imap.ssl.enable", "true");
        properties.put("mail.imap.impl", "jakarta.mail.imaps");

        // Loop 6 times (Total 30 seconds wait window)
        for (int i = 0; i < 6; i++) {
            try {
                // FIX: Use getInstance instead of getDefaultInstance
                Session emailSession = Session.getInstance(properties);
                Store store = emailSession.getStore("imaps");
                store.connect(host, email, password);

                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_WRITE);

                Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

                if (messages.length > 0) {
                    Message latestMessage = messages[messages.length - 1];
                    String bodyText = getTextFromMessage(latestMessage);

                    Pattern pattern = Pattern.compile("\\b\\d{6}\\b");
                    Matcher matcher = pattern.matcher(bodyText);

                    if (matcher.find()) {
                        String otpCode = matcher.group();
                        latestMessage.setFlag(Flags.Flag.SEEN, true);
                        inbox.close(false);
                        store.close();
                        return otpCode; // Success! Exits immediately
                    }
                }

                inbox.close(false);
                store.close();
                System.out.println("⏳ Email hasn't arrived yet... Checking again soon. (Attempt " + (i + 1) + "/6)");

            } catch (Exception e) {
                // This prints the REAL hidden error blocking your connection!
                System.out.println("⚠️ Connection attempt " + (i + 1) + " failed. Reason: " + e.getMessage());
            }

            // FIX: Moved OUTSIDE the try-catch block so it ALWAYS waits 5 seconds before looping again
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        throw new RuntimeException("❌ Failed to fetch verification code: Email timeout exceeded.");
    }

    private static String getTextFromMessage(Message message) throws Exception {
        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.isMimeType("text/plain")) {
                    return bodyPart.getContent().toString();
                } else if (bodyPart.isMimeType("text/html")) {
                    return (String) bodyPart.getContent();
                }
            }
        }
        return "";
    }
}