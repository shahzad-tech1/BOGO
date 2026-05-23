package org.BOGO.domain.communication;

import java.time.LocalDateTime;


public class Message {

    private int messageID;
    private String message;
    private LocalDateTime sentTime;
    private int senderID;
    private int recipientID;

    // ---------- Constructors ----------
    public Message(int ID, String content) {
        messageID = ID;
        message = content;
        sentTime = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

}
