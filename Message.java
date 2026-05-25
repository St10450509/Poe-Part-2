package com.mycompany.message;


import java.util.Scanner;
import java.util.Random;
import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;

public class Message {

    //LOGIN CLASS
    static class Login {
        private String firstName;
        private String lastName;
        private String username;
        private String password;
        private String cellPhoneNumber;

        public Login(String firstName, String lastName, String username, String password, String cellPhoneNumber) {
            this.firstName       = firstName;
            this.lastName        = lastName;
            this.username        = username;
            this.password        = password;
            this.cellPhoneNumber = cellPhoneNumber;
        }

        public boolean checkUserName() {
            return username.contains("_") && username.length() <= 5;
        }

        public boolean checkPasswordComplexity() {
            if (password.length() < 8) return false;
            boolean hasCapital = false, hasNumber = false, hasSpecial = false;
            for (char c : password.toCharArray()) {
                if (Character.isUpperCase(c))      hasCapital = true;
                else if (Character.isDigit(c))     hasNumber  = true;
                else if (!Character.isLetterOrDigit(c)) hasSpecial = true;
            }
            return hasCapital && hasNumber && hasSpecial;
        }

        //CHECK CELL NUMBER - must start with +27 and be exactly 12 characters//
        public boolean checkCellPhoneNumber() {
            return cellPhoneNumber.matches("^\\+27\\d{9}$");
        }

        public boolean loginUser(String enteredUsername, String enteredPassword) {
            return this.username.equals(enteredUsername) && this.password.equals(enteredPassword);
        }
        public String returnLoginStatus(String enteredUsername, String enteredPassword) {
            if (loginUser(enteredUsername, enteredPassword)) {
                return "Welcome " + firstName + " " + lastName + ", it is great to see you again!";
            } else {
                return "Username or password incorrect, please try again.";
            }
        }

        public String getFirstName() { return firstName; }
        public String getLastName()  { return lastName;  }
    }

    static class MessageData {
        private String messageID;
        private int    messageNumber;
        private String recipient;
        private String messageText;
        private String messageHash;
        private String flag;

        static int totalSent = 0;
        static ArrayList<MessageData> sentMessages      = new ArrayList<>();
        static ArrayList<MessageData> storedMessages    = new ArrayList<>();
        static ArrayList<MessageData> disregardMessages = new ArrayList<>();
        static ArrayList<String>      messageHashes     = new ArrayList<>();
        static ArrayList<String>      messageIDs        = new ArrayList<>();

        public MessageData(int messageNumber, String recipient, String messageText) {
            this.messageNumber = messageNumber;
            this.recipient     = recipient;
            this.messageText   = messageText;
            this.messageID     = generateMessageID();
            this.messageHash   = createMessageHash();
        }

        private String generateMessageID() {
            Random rand = new Random();
            long id = (long)(rand.nextDouble() * 9_000_000_000L) + 1_000_000_000L;
            return String.valueOf(id);
        }

        public boolean checkMessageID() {
            return messageID.length() <= 10;
        }

        public String checkRecipientCell() {
            if (recipient.startsWith("+") && recipient.length() <= 13) {
                return "Cell phone number successfully captured.";
            } else {
                return "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
            }
        }

        public String createMessageHash() {
            String[] words   = messageText.trim().split("\\s+");
            String firstWord = words[0];
            String lastWord  = words[words.length - 1];
            firstWord = firstWord.replaceAll("[^a-zA-Z]", "");
            lastWord  = lastWord.replaceAll("[^a-zA-Z]",  "");
            String idPrefix  = messageID.substring(0, Math.min(2, messageID.length()));
            return (idPrefix + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
        }

        //CHECK MESSAGE LENGTH - message must be 250 characters or less
        public String checkMessageLength() {
            if (messageText.length() <= 250) {
                return "Message ready to send.";
            } else {
                int over = messageText.length() - 250;
                return "Message exceeds 250 characters by " + over + "; please reduce the size.";
            }
        }

        public String sentMessage(int choice) {
            switch (choice) {
                case 1:
                    flag = "Sent";
                    totalSent++;
                    sentMessages.add(this);
                    messageHashes.add(messageHash);
                    messageIDs.add(messageID);
                    return "Message successfully sent.";
                case 2:
                    flag = "Disregarded";
                    disregardMessages.add(this);
                    return "Press 0 to delete the message.";
                case 3:
                    flag = "Stored";
                    storedMessages.add(this);
                    messageHashes.add(messageHash);
                    messageIDs.add(messageID);
                    return "Message successfully stored.";
                default:
                    return "Invalid option.";
            }
        }

        public static String printMessages() {
            if (sentMessages.isEmpty()) return "No messages sent.";
            StringBuilder sb = new StringBuilder();
            for (MessageData m : sentMessages) {
                sb.append("Message ID: ").append(m.messageID)
                  .append(" | Hash: ").append(m.messageHash)
                  .append(" | Recipient: ").append(m.recipient)
                  .append(" | Message: ").append(m.messageText)
                  .append("\n");
            }
            return sb.toString();
        }
        public static int returnTotalMessages() {
            return totalSent;
        }
        
        public static void storeMessage(String filename) {
            StringBuilder json = new StringBuilder();
            json.append("[\n");
            for (int i = 0; i < storedMessages.size(); i++) {
                MessageData m = storedMessages.get(i);
                json.append("  {\n");
                json.append("    \"messageID\": \""   ).append(m.messageID  ).append("\",\n");
                json.append("    \"messageHash\": \"" ).append(m.messageHash).append("\",\n");
                json.append("    \"recipient\": \""   ).append(m.recipient  ).append("\",\n");
                json.append("    \"message\": \""     ).append(m.messageText).append("\",\n");
                json.append("    \"flag\": \""        ).append(m.flag       ).append("\"\n");
                json.append("  }");
                if (i < storedMessages.size() - 1) json.append(",");
                json.append("\n");
            }
            json.append("]");
            try (FileWriter fw = new FileWriter(filename)) {
                fw.write(json.toString());
                System.out.println("Messages successfully stored to " + filename);
            } catch (IOException e) {
                System.out.println("Error writing JSON file: " + e.getMessage());
            }
        }

        public static void resetAll() {
            sentMessages.clear();
            storedMessages.clear();
            disregardMessages.clear();
            messageHashes.clear();
            messageIDs.clear();
            totalSent = 0;
        }

        public String getMessageID()   { return messageID;   }
        public String getRecipient()   { return recipient;   }
        public String getMessageText() { return messageText; }
        public String getMessageHash() { return messageHash; }
        public String getFlag()        { return flag;        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Login user = null;

        boolean welcomed = true;
        while (welcomed) {
            System.out.println("\n=== WELCOME TO QUICKCHAT ===");
            System.out.println("1) Register");
            System.out.println("2) Login");
            System.out.println("3) Exit");
            System.out.print("Choose option: ");

            int welcome;
            try {
                welcome = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid option.");
                continue;
            }

            switch (welcome) {
                case 1:
                    //REGISTRATION
                    System.out.println(" REGISTRATION ");

                    System.out.print("First Name: ");
                    String regFirst = scanner.nextLine();
                    System.out.print("Last Name: ");
                    String regLast = scanner.nextLine();

                    // keep asking until username is valid
                    String regUsername;
                    while (true) {
                        System.out.print("Username (must have _ and be max 5 chars): ");
                        regUsername = scanner.nextLine();
                        Login tempU = new Login(regFirst, regLast, regUsername, "", "+27000000000");
                        if (tempU.checkUserName()) {
                            System.out.println("Username successfully captured.");
                            break;
                        } else {
                            System.out.println("Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.");
                        }
                    }

                    // keep asking until password is valid
                    String regPassword;
                    while (true) {
                        System.out.print("Password: ");
                        regPassword = scanner.nextLine();
                        Login tempP = new Login(regFirst, regLast, regUsername, regPassword, "+27000000000");
                        if (tempP.checkPasswordComplexity()) {
                            System.out.println("Password successfully captured.");
                            break;
                        } else {
                            System.out.println("Password is not correctly formatted; please ensure that the password contains at least 8 characters, a capital letter, a number and a special character.");
                        }
                    }

                    // keep asking until cell number is valid
                    String regCell;
                    while (true) {
                        System.out.print("Cell number (+27xxxxxxxxx): ");
                        regCell = scanner.nextLine();
                        Login tempC = new Login(regFirst, regLast, regUsername, regPassword, regCell);
                        if (tempC.checkCellPhoneNumber()) {
                            System.out.println("Cellphone number successfully captured.");
                            break;
                        } else {
                            System.out.println("Cell phone number incorrectly formatted or does not contain international code (+27).");
                        }
                    }

                    user = new Login(regFirst, regLast, regUsername, regPassword, regCell);
                    System.out.println("Registration successful! You can now log in.");
                    break;

                case 2:
                   
                    if (user == null) {
                        System.out.println("No user registered. Please register first.");
                        break;
                    }
                    System.out.println(" LOGIN ");
                    System.out.print("Username: ");
                    String enteredUsername = scanner.nextLine();
                    System.out.print("Password: ");
                    String enteredPassword = scanner.nextLine();

                    System.out.println(user.returnLoginStatus(enteredUsername, enteredPassword));

                    // only leave the welcome screen if login was successful
                    if (user.loginUser(enteredUsername, enteredPassword)) {
                        welcomed = false;
                    }
                    break;

                case 3:
                    System.out.println("Goodbye!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Invalid option.");
            }
        }

        //ASK HOW MANY MESSAGES TO SEND THIS SESSION
        System.out.print("\nHow many messages would you like to send? ");
        int numMessages = 0;
        try {
            numMessages = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid number. Exiting.");
            scanner.close();
            return;
        }
        
        boolean running = true;
        while (running) {
            System.out.println(" WELCOME TO QUICKCHAT ");
            System.out.println("1) Send Messages");
            System.out.println("2) Show Recently Sent Messages");
            System.out.println("3) Quit");
            System.out.print("Choose option: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid option.");
                continue;
            }

            switch (choice) {
                case 1:
                    for (int i = 1; i <= numMessages; i++) {
                        System.out.println(" Message " + i + " of " + numMessages + "");

                        // get recipient number
                        System.out.print("Recipient cell (With +27): ");
                        String recipient = scanner.nextLine().trim();

                        // keep asking until message is 250 characters or less
                        String messageText;
                        while (true) {
                            System.out.print("Message: ");
                            messageText = scanner.nextLine();
                            if (messageText.length() <= 250) {
                                System.out.println("Message ready to send.");
                                break;
                            } else {
                                int over = messageText.length() - 250;
                                System.out.println("Message exceeds 250 characters by " + over + "; please reduce the size.");
                            }
                        }

                        // create the message and show its details
                        MessageData msg = new MessageData(i, recipient, messageText);
                        System.out.println(msg.checkRecipientCell());
                        System.out.println("Message ID generated: " + msg.getMessageID());
                        System.out.println("Message Hash: " + msg.getMessageHash());

                        // ask what to do with the message
                        System.out.println("1) Send Message");
                        System.out.println("2) Disregard Message");
                        System.out.println("3) Store Message to send later");
                        System.out.print("Choose: ");

                        int sendChoice;
                        try {
                            sendChoice = Integer.parseInt(scanner.nextLine().trim());
                        } catch (NumberFormatException e) {
                            sendChoice = 2;
                        }

                        System.out.println(msg.sentMessage(sendChoice));

                        // show full message details after the action
                        System.out.println("\n Message Details ");
                        System.out.println("Message ID   : " + msg.getMessageID());
                        System.out.println("Message Hash : " + msg.getMessageHash());
                        System.out.println("Recipient    : " + msg.getRecipient());
                        System.out.println("Message      : " + msg.getMessageText());
                    }

                    // show total sent after all messages are done
                    System.out.println("\nTotal messages sent: " + MessageData.returnTotalMessages());

                    // save any stored messages to a JSON file
                    if (!MessageData.storedMessages.isEmpty()) {
                        MessageData.storeMessage("stored_messages.json");
                    }
                    break;

                case 2:
                    System.out.println("Coming Soon.");
                    break;

                case 3:
                    System.out.println("Goodbye!");
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option.");
            }
        }

        scanner.close();
    }
}
