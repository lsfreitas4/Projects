package sharedFiles;

public class ProtocolConstants {
    // Command Types
    public static final String LOGIN = "LOGIN";
    public static final String REGISTER = "REGISTER";
    public static final String LOGOUT = "LOGOUT";
    public static final String MESSAGE = "MESSAGE";
    public static final String JOIN_ROOM = "JOIN_ROOM";
    public static final String LEAVE_ROOM = "LEAVE_ROOM";
    public static final String CREATE_ROOM = "CREATE_ROOM";
    public static final String DELETE_ROOM = "DELETE_ROOM";

    // Response Types
    public static final String SUCCESS = "SUCCESS";
    public static final String ERROR = "ERROR";
    public static final String INVALID_COMMAND = "INVALID_COMMAND";

    // Other Constants
    public static final int DEFAULT_PORT = 12345;
    public static final int MAX_MESSAGE_LENGTH = 1024;
}