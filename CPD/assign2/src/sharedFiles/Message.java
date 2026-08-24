package sharedFiles;

import java.io.Serializable;
import java.util.*;

public class Message implements Serializable {
    private String sender;
    private String content;
    private Date timestamp;
    private String roomName;


    // Constructor
    public Message(String sender, String content, Date timestamp, String roomName) {
        this.sender = sender;
        this.content = content;
        this.timestamp = new Date();
        this.roomName = roomName;
    }

    // Getters
    public String getSender(){
        return sender;
    }

    public String getContent(){
        return content;
    }

    public Date getTimestamp(){
        return timestamp;
    }

    public String getRoomName(){
        return roomName;
    }


    // Serialization of Message into Readable String Format
    public String serialize(){
        return sender + '|'  + content + '|' + timestamp.getTime() + '|' + roomName;
    }

    // Deserialization of String into Message Object
    public static Message deserialize(String data){
        String[] splitSerialized = data.split("\\|");
        if (splitSerialized.length != 4) {
            throw new IllegalArgumentException("Invalid serialized message format");
        }
        String sender = splitSerialized[0];
        String content = splitSerialized[1];
        Date timestamp = new Date(Long.parseLong(splitSerialized[2]));
        String roomName = splitSerialized[3];
        Message message = new Message(sender, content, timestamp, roomName);
        message.timestamp = timestamp;
        return message;
    }



    /* Potential Methods/Functions
     *   serialize() : converts the message object to a transmittable format
     *   deserialize(String data) : reconstructs the message object from a string
     * */
}