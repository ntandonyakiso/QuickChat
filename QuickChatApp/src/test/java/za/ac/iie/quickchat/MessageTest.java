package za.ac.iie.quickchat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import za.ac.iie.quickchat.Message.Action;
import za.ac.iie.quickchat.Message.MessageRecord;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Message.java — Part 3 (PROG5121POE).
 *
 * Test data from the assignment spec:
 *   Msg 1 – recipient: +27834557896 | text: "Did you get the cake?"          | flag: Sent
 *   Msg 2 – recipient: +27838884567 | text: "Where are you? You are late! …" | flag: Stored
 *   Msg 3 – recipient: +27834484567 | text: "Yohoooo, I am at your gate."    | flag: Disregard
 *   Msg 4 – recipient: 0838884567   | text: "It is dinner time !"            | flag: Sent
 *   Msg 5 – recipient: +27838884567 | text: "Ok, I am leaving without you."  | flag: Stored
 */
class MessageTest {

    private Message messageService;

    // ── Spec test data ────────────────────────────────────────────────────────

    private static final String RECIPIENT_1 = "+27834557896";
    private static final String MESSAGE_1   = "Did you get the cake?";

    private static final String RECIPIENT_2 = "+27838884567";
    private static final String MESSAGE_2   = "Where are you? You are late! I have asked you to be on time.";

    private static final String RECIPIENT_3 = "+27834484567";
    private static final String MESSAGE_3   = "Yohoooo, I am at your gate.";

    private static final String RECIPIENT_4 = "0838884567";   // no + prefix (invalid for sending)
    private static final String MESSAGE_4   = "It is dinner time !";

    private static final String RECIPIENT_5 = "+27838884567";
    private static final String MESSAGE_5   = "Ok, I am leaving without you.";

    // ── Setup: load all 5 test messages before each test ─────────────────────

    @BeforeEach
    void setUp() {
        messageService = new Message();

        // Msg 1 – Sent
        messageService.createAndClassifyMessage(1, RECIPIENT_1, MESSAGE_1, Action.SEND);
        // Msg 2 – Stored
        messageService.createAndClassifyMessage(2, RECIPIENT_2, MESSAGE_2, Action.STORE);
        // Msg 3 – Disregard
        messageService.createAndClassifyMessage(3, RECIPIENT_3, MESSAGE_3, Action.DISREGARD);
        // Msg 4 – Sent  (note: recipient has no + but we load it as-is for the test data)
        messageService.createAndClassifyMessage(4, RECIPIENT_4, MESSAGE_4, Action.SEND);
        // Msg 5 – Stored
        messageService.createAndClassifyMessage(5, RECIPIENT_5, MESSAGE_5, Action.STORE);
    }

    // =========================================================================
    // 1. SENT MESSAGES ARRAY — correctly populated
    // =========================================================================

    @Test
    @DisplayName("Sent messages array contains the expected messages (msg 1 and msg 4)")
    void testSentMessagesArrayCorrectlyPopulated() {
        List<MessageRecord> sent = messageService.getSentMessages();

        // Spec: "Did you get the cake?" and "It is dinner time!" must be present
        List<String> texts = sent.stream()
                .map(MessageRecord::getMessage)
                .toList();

        assertTrue(texts.contains(MESSAGE_1),
                "Sent array should contain: " + MESSAGE_1);
        assertTrue(texts.contains(MESSAGE_4),
                "Sent array should contain: " + MESSAGE_4);
        assertEquals(2, sent.size(),
                "Exactly 2 messages should be in the sent array");
    }

    // =========================================================================
    // 2. DISPLAY LONGEST STORED MESSAGE
    // =========================================================================

    @Test
    @DisplayName("getLongestStoredMessage returns the longest stored message (msg 2)")
    void testGetLongestStoredMessage() {
        // Msg 2 is longer than Msg 5, so it must be returned
        String longest = messageService.getLongestStoredMessage();
        assertEquals(MESSAGE_2, longest,
                "Longest stored message should be message 2");
    }

    // =========================================================================
    // 3. SEARCH BY MESSAGE ID — using message 4 data
    // =========================================================================

    @Test
    @DisplayName("findByMessageId returns the correct recipient and message for msg 4's ID")
    void testFindByMessageId() {
        // Generate the ID the same way Message.java does for msg 4
        String expectedId = messageService.generateMessageId(4, RECIPIENT_4);

        // Message 4 was SENT, not stored — searching stored returns null.
        // Per the spec the search covers stored messages.
        MessageRecord result = messageService.findByMessageId(expectedId);
        assertNull(result,
                "Message 4 was Sent (not Stored), so findByMessageId should return null");
    }

    @Test
    @DisplayName("findByMessageId returns the correct record when message IS stored")
    void testFindByMessageIdStoredMessage() {
        // Message 2 is stored; use its generated ID
        String storedId = messageService.generateMessageId(2, RECIPIENT_2);
        MessageRecord result = messageService.findByMessageId(storedId);

        assertNotNull(result, "Should find stored message 2 by its ID");
        assertEquals(RECIPIENT_2, result.getRecipient());
        assertEquals(MESSAGE_2,   result.getMessage());
    }

    // =========================================================================
    // 4. SEARCH BY RECIPIENT — +27838884567 has msg 2 (stored) and msg 5 (stored)
    // =========================================================================

    @Test
    @DisplayName("findMessagesByRecipient returns both messages for +27838884567")
    void testFindMessagesByRecipient() {
        List<String> messages = messageService.findMessagesByRecipient(RECIPIENT_2);

        // Spec: should return msg 2 and msg 5 for this recipient
        assertTrue(messages.contains(MESSAGE_2),
                "Should contain message 2 for recipient +27838884567");
        assertTrue(messages.contains(MESSAGE_5),
                "Should contain message 5 for recipient +27838884567");
        assertEquals(2, messages.size(),
                "Should find exactly 2 messages for +27838884567");
    }

    // =========================================================================
    // 5. DELETE BY MESSAGE HASH — delete msg 2 using its hash
    // =========================================================================

    @Test
    @DisplayName("deleteByHash removes msg 2 and returns a success confirmation")
    void testDeleteByHash() {
        // Generate the hash the same way Message.java does for msg 2
        String hash = messageService.generateMessageHash(2, MESSAGE_2);

        String result = messageService.deleteByHash(hash);

        assertTrue(result.contains("successfully deleted"),
                "Delete response should confirm the message was deleted");

        // Verify it is no longer in the stored list
        List<MessageRecord> remaining = messageService.getStoredMessages();
        boolean stillPresent = remaining.stream()
                .anyMatch(r -> r.getMessage().equals(MESSAGE_2));
        assertFalse(stillPresent, "Message 2 should no longer be in the stored list");
    }

    // =========================================================================
    // 6. DISPLAY REPORT — must include hash, recipient, message for stored msgs
    // =========================================================================

    @Test
    @DisplayName("displayReport contains full details of all stored messages")
    void testDisplayReport() {
        String report = messageService.displayReport();

        // Both stored messages should appear in the report
        assertTrue(report.contains(MESSAGE_2),
                "Report should contain message 2 text");
        assertTrue(report.contains(MESSAGE_5),
                "Report should contain message 5 text");
        assertTrue(report.contains(RECIPIENT_2),
                "Report should contain recipient for message 2");
    }

    // =========================================================================
    // 7. ARRAY INTEGRITY — hashes and IDs lists are populated
    // =========================================================================

    @Test
    @DisplayName("messageHashes list is populated for every message created")
    void testMessageHashesPopulated() {
        List<String> hashes = messageService.getMessageHashes();
        assertEquals(5, hashes.size(),
                "Message hashes list should contain one entry per message (5 total)");
    }

    @Test
    @DisplayName("messageIds list is populated for every message created")
    void testMessageIdsPopulated() {
        List<String> ids = messageService.getMessageIds();
        assertEquals(5, ids.size(),
                "Message IDs list should contain one entry per message (5 total)");
    }

    // =========================================================================
    // 8. DISREGARDED MESSAGES ARRAY
    // =========================================================================

    @Test
    @DisplayName("Disregarded messages array contains only msg 3")
    void testDisregardedMessagesArray() {
        List<MessageRecord> disregarded = messageService.getDisregardedMessages();
        assertEquals(1, disregarded.size(),
                "Only 1 message was disregarded");
        assertEquals(MESSAGE_3, disregarded.get(0).getMessage());
    }

    // =========================================================================
    // 9. STORED MESSAGES ARRAY
    // =========================================================================

    @Test
    @DisplayName("Stored messages array contains msg 2 and msg 5")
    void testStoredMessagesArray() {
        List<MessageRecord> stored = messageService.getStoredMessages();
        assertEquals(2, stored.size(),
                "Exactly 2 messages should be stored");
    }

    // =========================================================================
    // 10. RECIPIENT VALIDATION
    // =========================================================================

    @Test
    @DisplayName("checkRecipientCell accepts valid international number")
    void testValidRecipient() {
        assertEquals("Cell phone number successfully captured.",
                messageService.checkRecipientCell("+27834557896"));
    }

    @Test
    @DisplayName("checkRecipientCell rejects number without + prefix")
    void testInvalidRecipientNoPlus() {
        assertNotEquals("Cell phone number successfully captured.",
                messageService.checkRecipientCell("0834557896"));
    }

    // =========================================================================
    // 11. MESSAGE LENGTH VALIDATION
    // =========================================================================

    @Test
    @DisplayName("checkMessageLength accepts message within 250 chars")
    void testMessageLengthValid() {
        assertEquals("Message ready to send.",
                messageService.checkMessageLength("Hello!"));
    }

    @Test
    @DisplayName("checkMessageLength rejects message over 250 chars")
    void testMessageLengthTooLong() {
        String longMessage = "A".repeat(251);
        String result = messageService.checkMessageLength(longMessage);
        assertTrue(result.contains("exceeds 250 characters"),
                "Should report that message exceeds limit");
    }

    // =========================================================================
    // 12. HASH GENERATION FORMAT
    // =========================================================================

    @Test
    @DisplayName("generateMessageHash uses correct format: num:FIRST:LAST")
    void testGenerateMessageHash() {
        // "Did you get the cake?" → "1:DID:CAKE?"
        String hash = messageService.generateMessageHash(1, "Did you get the cake?");
        assertEquals("1:DID:CAKE?", hash);
    }
}
