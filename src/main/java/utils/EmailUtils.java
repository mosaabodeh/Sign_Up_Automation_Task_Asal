package utils;

import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailUtils {

    private static final String HOST = "imap.gmail.com";

    public static String getVerificationCode(String email, String password) {

        Properties props = new Properties();
        props.put("mail.imap.host", HOST);
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl.enable", "true");

        Pattern pattern = Pattern.compile("\\b\\d{6}\\b");

        for (int i = 0; i < 6; i++) {

            try (Store store = Session.getInstance(props).getStore("imaps")) {

                store.connect(HOST, email, password);

                try (Folder inbox = store.getFolder("INBOX")) {
                    inbox.open(Folder.READ_WRITE);

                    Message[] messages =
                            inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

                    if (messages.length > 0) {

                        Message latest = messages[messages.length - 1];
                        String body = getTextFromMessage(latest);

                        Matcher matcher = pattern.matcher(body);

                        if (matcher.find()) {
                            latest.setFlag(Flags.Flag.SEEN, true);
                            return matcher.group();
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("IMAP attempt failed: " + e.getMessage());
            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {}
        }

        throw new RuntimeException("Email OTP not received in time");
    }

    private static String getTextFromMessage(Message message) throws Exception {

        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        }

        if (message.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) message.getContent();

            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart part = mp.getBodyPart(i);

                if (part.isMimeType("text/plain")) {
                    return part.getContent().toString();
                }
            }
        }

        return "";
    }
}