package utils;

import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;

import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailUtils {

    private static final String HOST = "imap.gmail.com";
    private static final int MAX_ATTEMPTS = 6;
    private static final long RETRY_DELAY_MS = 5000;

    private static final Pattern OTP_PATTERN = Pattern.compile(
            "the following validation code to create your Rainbow account within the next hour:\\s*(\\d{6})",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final String EXISTING_ACCOUNT_INDICATOR =
            "you recently visited our sign up page using an email corresponding to an existing rainbow account";

    public static String getVerificationCode(String email, String password) {
        String code = pollForEmailContent(email, password, body -> {
            Matcher matcher = OTP_PATTERN.matcher(body);
            return matcher.find() ? matcher.group(1) : null;
        });

        if (code == null) {
            throw new RuntimeException("Email OTP not received in time");
        }

        return code;
    }

    public static boolean isExistingAccountEmailReceived(String email, String password) {
        String result = pollForEmailContent(email, password, body -> {
            String normalizedBody = body.toLowerCase().replaceAll("\\s+", " ").trim();
            return normalizedBody.contains(EXISTING_ACCOUNT_INDICATOR) ? "found" : null;
        });

        return result != null;
    }

    private static String pollForEmailContent(
            String email,
            String password,
            Function<String, String> extractor) {

        Properties props = new Properties();
        props.put("mail.imap.host", HOST);
        props.put("mail.imap.port", "993");
        props.put("mail.imap.ssl.enable", "true");

        props.put("mail.imap.expunge", "true");

        for (int i = 0; i < MAX_ATTEMPTS; i++) {

            try (Store store = Session.getInstance(props).getStore("imaps")) {

                store.connect(HOST, email, password);

                try (Folder inbox = store.getFolder("INBOX")) {
                    inbox.open(Folder.READ_WRITE);

                    Message[] messages =
                            inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

                    if (messages.length > 0) {
                        Message latest = messages[messages.length - 1];

                        String body = getTextFromMessage(latest);

                        String extracted = extractor.apply(body);

                        if (extracted != null) {
                            latest.setFlag(Flags.Flag.DELETED, true);

                            return extracted;
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("IMAP attempt failed: " + e.getMessage());
            }

            sleepBeforeRetry();
        }

        return null;
    }

    private static void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String getTextFromMessage(Message message) throws Exception {

        if (message.isMimeType("text/plain")) {
            return message.getContent().toString();
        }

        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart part = multipart.getBodyPart(i);

                if (part.isMimeType("text/plain")) {
                    return part.getContent().toString();
                }

                if (part.isMimeType("text/html")) {
                    String html = part.getContent().toString();
                    return html.replaceAll("<[^>]*>", " ")
                            .replace("&nbsp;", " ")
                            .replaceAll("\\s+", " ")
                            .trim();
                }
            }
        }

        return "";
    }
}