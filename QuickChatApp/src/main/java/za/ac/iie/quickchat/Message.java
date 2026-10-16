package za.ac.iie.quickchat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Message service for QuickChat Part 3.
 *
 * Manages five parallel data structures required by the spec:
 *   - sentMessages        : messages flagged SEND
 *   - disregardedMessages : messages flagged DISREGARD
 *   - storedMessages      : messages flagged STORE (also persisted to JSON)
 *   - messageHashes       : hash for every message created
 *   - messageIds          : ID for every message created
 */
public class Message {

    public enum Action { SEND, DISREGARD, STORE }

    private static final int    MAX_MESSAGE_LENGTH  = 250;
    private static final String VALID_RECIPIENT_MSG = "Cell phone number successfully captured.";
    private static final String INVALID_RECIPIENT   = "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    private static final String MESSAGE_READY_MSG   = "Message ready to send.";
    private static final String MESSAGE_TOO_LONG    = "Message exceeds 250 characters by ";

    // ── Part 3 arrays ─────────────────────────────────────────────────────────
    private final List<MessageRecord> sentMessages        = new ArrayList<>();
    private final List<MessageRecord> disregardedMessages = new ArrayList<>();
    private final List<MessageRecord> storedMessages      = new ArrayList<>();
    private final List<String>        messageHashes       = new ArrayList<>();
    private final List<String>        messageIds          = new ArrayList<>();

    // ── Validation ────────────────────────────────────────────────────────────

    public String checkRecipientCell(String cell) {
        if (cell != null && cell.startsWith("+") && cell.substring(1).matches("\\d+")) {
            return VALID_RECIPIENT_MSG;
        }
        return INVALID_RECIPIENT;
    }

    public String checkMessageLength(String message) {
        if (message == null) return MESSAGE_TOO_LONG + MAX_MESSAGE_LENGTH + " characters.";
        int len = message.length();
        if (len <= MAX_MESSAGE_LENGTH) return MESSAGE_READY_MSG;
        return MESSAGE_TOO_LONG + (len - MAX_MESSAGE_LENGTH) + " characters.";
    }

    // ── Message creation ──────────────────────────────────────────────────────

    public MessageRecord createAndClassifyMessage(int messageNumber,
                                                  String recipient,
                                                  String text,
                                                  Action action) {
        String id   = generateMessageId(messageNumber, recipient);
        String hash = generateMessageHash(messageNumber, text);
        MessageRecord record = new MessageRecord(id, hash, recipient, text, action);

        messageIds.add(id);
        messageHashes.add(hash);

        switch (action) {
            case SEND:      sentMessages.add(record);        break;
            case DISREGARD: disregardedMessages.add(record); break;
            case STORE:     storedMessages.add(record);      break;
        }
        return record;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public List<MessageRecord> getSentMessages()        { return new ArrayList<>(sentMessages); }
    public List<MessageRecord> getDisregardedMessages() { return new ArrayList<>(disregardedMessages); }
    public List<MessageRecord> getStoredMessages()      { return new ArrayList<>(storedMessages); }
    public List<String>        getMessageHashes()       { return new ArrayList<>(messageHashes); }
    public List<String>        getMessageIds()          { return new ArrayList<>(messageIds); }

    // ── Stored Messages menu operations ──────────────────────────────────────

    public String getLongestStoredMessage() {
        if (storedMessages.isEmpty()) return "No stored messages found.";
        MessageRecord longest = storedMessages.get(0);
        for (MessageRecord r : storedMessages) {
            if (r.getMessage().length() > longest.getMessage().length()) longest = r;
        }
        return longest.getMessage();
    }

    public MessageRecord findByMessageId(String id) {
        for (MessageRecord r : storedMessages) {
            if (r.getMessageId().equalsIgnoreCase(id)) return r;
        }
        return null;
    }

    public List<String> findMessagesByRecipient(String recipient) {
        List<String> results = new ArrayList<>();
        for (MessageRecord r : sentMessages) {
            if (r.getRecipient().equalsIgnoreCase(recipient)) results.add(r.getMessage());
        }
        for (MessageRecord r : storedMessages) {
            if (r.getRecipient().equalsIgnoreCase(recipient)) results.add(r.getMessage());
        }
        return results;
    }

    public String deleteByHash(String hash) {
        for (MessageRecord r : new ArrayList<>(storedMessages)) {
            if (r.getMessageHash().equalsIgnoreCase(hash)) {
                String msgText = r.getMessage();
                storedMessages.remove(r);
                messageHashes.remove(r.getMessageHash());
                messageIds.remove(r.getMessageId());
                return "Message \"" + msgText + "\" successfully deleted.";
            }
        }
        return "Message with hash \"" + hash + "\" not found.";
    }

    public String displayReport() {
        if (storedMessages.isEmpty()) return "No stored messages to report.";
        StringBuilder sb = new StringBuilder();
        sb.append("---- Stored Messages Report ----\n");
        for (MessageRecord r : storedMessages) {
            sb.append("Message Hash : ").append(r.getMessageHash()).append("\n");
            sb.append("Recipient    : ").append(r.getRecipient()).append("\n");
            sb.append("Message      : ").append(r.getMessage()).append("\n");
            sb.append("--------------------------------\n");
        }
        return sb.toString();
    }

    // ── Counters / status ─────────────────────────────────────────────────────

    public int returnTotalMessages() { return sentMessages.size(); }

    public String sentMessage(Action action) {
        switch (action) {
            case SEND:      return "Message successfully sent.";
            case DISREGARD: return "Message disregarded.";
            case STORE:     return "Message stored.";
            default:        return "Unknown action.";
        }
    }

    // ── JSON persistence (manual — no external lib) ───────────────────────────

    public void storeMessage(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < storedMessages.size(); i++) {
            MessageRecord r = storedMessages.get(i);
            sb.append("  {\n");
            sb.append("    \"messageId\": \"").append(escape(r.getMessageId())).append("\",\n");
            sb.append("    \"messageHash\": \"").append(escape(r.getMessageHash())).append("\",\n");
            sb.append("    \"recipient\": \"").append(escape(r.getRecipient())).append("\",\n");
            sb.append("    \"message\": \"").append(escape(r.getMessage())).append("\"\n");
            sb.append("  }").append(i < storedMessages.size() - 1 ? "," : "").append("\n");
        }
        sb.append("]");
        Files.write(Paths.get(filePath), sb.toString().getBytes());
    }

    public void readStoredMessages(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        // Simple parser: split on "}," to get each object
        String[] objects = content.split("\\},\\s*\\{");
        for (String obj : objects) {
            String id   = extractJsonValue(obj, "messageId");
            String hash = extractJsonValue(obj, "messageHash");
            String rec  = extractJsonValue(obj, "recipient");
            String msg  = extractJsonValue(obj, "message");
            if (id == null || hash == null || rec == null || msg == null) continue;
            boolean exists = storedMessages.stream().anyMatch(r -> r.getMessageId().equals(id));
            if (!exists) {
                MessageRecord record = new MessageRecord(id, hash, rec, msg, Action.STORE);
                storedMessages.add(record);
                if (!messageIds.contains(id))      messageIds.add(id);
                if (!messageHashes.contains(hash)) messageHashes.add(hash);
            }
        }
    }

    // ── ID / Hash generators ──────────────────────────────────────────────────

    public String generateMessageId(int messageNumber, String recipient) {
        String numPart  = String.format("%02d", messageNumber);
        String recStart = recipient.length() >= 2 ? recipient.substring(0, 2) : recipient;
        String recEnd   = recipient.length() >= 2 ? recipient.substring(recipient.length() - 2) : recipient;
        return numPart + ":" + recStart + ":" + recEnd;
    }

    public String generateMessageHash(int messageNumber, String message) {
        String[] words     = message.trim().split("\\s+");
        String   firstWord = words[0].toUpperCase();
        String   lastWord  = words[words.length - 1].toUpperCase();
        return messageNumber + ":" + firstWord + ":" + lastWord;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx + search.length());
        if (colon < 0) return null;
        int start = json.indexOf('"', colon + 1);
        if (start < 0) return null;
        int end   = json.indexOf('"', start + 1);
        if (end < 0) return null;
        return json.substring(start + 1, end);
    }

    // ── Inner record class ────────────────────────────────────────────────────

    public static class MessageRecord {
        private final String messageId;
        private final String messageHash;
        private final String recipient;
        private final String message;
        private final Action action;

        public MessageRecord(String messageId, String messageHash,
                             String recipient, String message, Action action) {
            this.messageId   = messageId;
            this.messageHash = messageHash;
            this.recipient   = recipient;
            this.message     = message;
            this.action      = action;
        }

        public String getMessageId()   { return messageId; }
        public String getMessageHash() { return messageHash; }
        public String getRecipient()   { return recipient; }
        public String getMessage()     { return message; }
        public Action getAction()      { return action; }
    }
}
