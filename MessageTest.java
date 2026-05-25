package com.mycompany.message;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {

    @BeforeEach
    void setUp() {
        Message.MessageData.resetAll();
    }

    @Test
    void testMessageLengthUnder250_Success() {
        Message.MessageData msg = new Message.MessageData(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Message ready to send.", msg.checkMessageLength());
    }

    //TEST MESSAGE LENGTH - message is over 250 characters (should fail)//
    @Test
    void testMessageLengthOver250_Failure() {
        String longMsg = "A".repeat(260);
        Message.MessageData msg = new Message.MessageData(0, "+27718693002", longMsg);
        String result = msg.checkMessageLength();
        assertTrue(result.startsWith("Message exceeds 250 characters by 10"));
    }

    //TEST RECIPIENT CELL - number has international code so it should pass//
    //Test data: +27718693002//
    @Test
    void testRecipientCell_CorrectlyFormatted() {
        Message.MessageData msg = new Message.MessageData(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Cell phone number successfully captured.", msg.checkRecipientCell());
    }

    //TEST RECIPIENT CELL - number has no international code so it should fail//
    //Test data: 08575975889//
    @Test
    void testRecipientCell_IncorrectlyFormatted() {
        Message.MessageData msg = new Message.MessageData(1, "08575975889", "Hi Keegan, did you receive the payment?");
        assertEquals(
            "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
            msg.checkRecipientCell()
        );
    }

    //TEST MESSAGE HASH - checks the hash is in the correct format//
    //Expected: XX:0:HITONIGHT (first 2 of ID : message number : first+last word in caps)//
    @Test
    void testMessageHash_CorrectFormat_Task1() {
        Message.MessageData msg = new Message.MessageData(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        String hash = msg.createMessageHash();
        assertTrue(hash.matches("^[0-9]{2}:0:HITONIGHT$"),
            "Hash should match pattern XX:0:HITONIGHT but was: " + hash);
    }

    //TEST MESSAGE ID - checks the ID was created and is 10 characters or less//
    @Test
    void testMessageID_Created() {
        Message.MessageData msg = new Message.MessageData(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        assertNotNull(msg.getMessageID());
        assertTrue(msg.checkMessageID(), "Message ID should be no more than 10 characters");
        System.out.println("Message ID generated: " + msg.getMessageID());
    }

    //TEST SENT MESSAGE - user picks Send (1), message should be marked as sent//
    @Test
    void testSentMessage_SendOption() {
        Message.MessageData msg = new Message.MessageData(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Message successfully sent.", msg.sentMessage(1));
        assertEquals(1, Message.MessageData.returnTotalMessages());
    }

    //TEST SENT MESSAGE - user picks Disregard (2), total count should stay at 0//
    @Test
    void testSentMessage_DisregardOption() {
        Message.MessageData msg = new Message.MessageData(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Press 0 to delete the message.", msg.sentMessage(2));
        assertEquals(0, Message.MessageData.returnTotalMessages());
    }

    //TEST SENT MESSAGE - user picks Store (3), message should be saved//
    @Test
    void testSentMessage_StoreOption() {
        Message.MessageData msg = new Message.MessageData(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        assertEquals("Message successfully stored.", msg.sentMessage(3));
    }

    //TEST TOTAL MESSAGES - only sent messages count, not disregarded ones//
    @Test
    void testReturnTotalMessages_AfterSending() {
        Message.MessageData msg1 = new Message.MessageData(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");
        msg1.sentMessage(1); // send this one

        Message.MessageData msg2 = new Message.MessageData(1, "+27838968976", "Hi Keegan, did you receive the payment?");
        msg2.sentMessage(2); // disregard this one

        // only 1 was sent so total should be 1
        assertEquals(1, Message.MessageData.returnTotalMessages());
    }

    //TEST FULL TASK 1 FLOW - runs through the full message 1 process from the PoE//
    @Test
    void testFullTask1Flow() {
        Message.MessageData msg = new Message.MessageData(0, "+27718693002", "Hi Mike, can you join us for dinner tonight?");

        // check the recipient number is valid
        assertEquals("Cell phone number successfully captured.", msg.checkRecipientCell());
        // check the message is not too long
        assertEquals("Message ready to send.", msg.checkMessageLength());
        // check the hash ends with the correct words
        assertTrue(msg.createMessageHash().endsWith(":HITONIGHT"));
        // send the message
        assertEquals("Message successfully sent.", msg.sentMessage(1));
        // total count should now be 1
        assertEquals(1, Message.MessageData.returnTotalMessages());
    }

    @Test
    void testFullTask2Flow() {
        Message.MessageData msg = new Message.MessageData(1, "08575975889", "Hi Keegan, did you receive the payment?");

        assertNotEquals("Cell phone number successfully captured.", msg.checkRecipientCell());
        assertEquals("Press 0 to delete the message.", msg.sentMessage(2));
        assertEquals(0, Message.MessageData.returnTotalMessages());
    }
}