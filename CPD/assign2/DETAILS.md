# Project Structure and Components

This project is structured into three main directories, each containing Java files that implement specific functionalities:

- **`sharedFiles`**: Contains shared classes and constants used by both the client and server.
- **`serverSide`**: Contains the server-side logic for the chat system, including user management, chat rooms, and message handling.
- **`clientSide`**: Contains the client-side logic for the chat system, including user interface and message handling.

---

## Directory and Classes Breakdown

### `clientSide/ChatClient.java`
Implements the main client-side logic of the secure chat system. Handles user interactions, connects to the server, and manages chat room operations.

#### **Key Variables**
- **Server Configuration**:
  - `SERVER_HOST` *(String)*: The hostname of the server.
  - `SERVER_PORT` *(int)*: The port number of the server.
- **Connection Resources**:
  - `socket` *(Socket)*: The socket for communication with the server.
  - `in` *(BufferedReader)*: Input stream to receive messages from the server.
  - `out` *(PrintWriter)*: Output stream to send messages to the server.
- **User Interaction**:
  - `scanner` *(Scanner)*: Used to read user input.
  - `running` *(boolean)*: Indicates if the client is running.

#### **Key Functions**
- **Main Workflow**:
  - `start()`: Starts the client and handles the connection to the server.
- **Authentication**:
  - `authenticateMenu()`: Displays the authentication menu and processes user input.
  - `handleLogin(String username)`: Handles user login.
  - `handleRegister(String username)`: Handles user registration.
- **Chat Room Management**:
  - `enterChatMenu()`: Displays the chat menu and processes user input.
  - `startChatLoop()`: Handles the chat loop for sending and receiving messages.

---

### `clientSide/MessageListener.java`
Listens for incoming messages from the server and processes them.

#### **Key Variables**
- `inputStream` *(BufferedReader)*: The input stream from the server to read incoming messages.
- `currentUsername` *(String)*: The username of the client.

#### **Key Functions**
- `run()`: Continuously listens for incoming messages from the server and processes them.

---

### `clientSide/TokenManager.java`
Manages the generation, storage, and retrieval of authentication tokens for secure communication.

#### **Key Variables**
- `TOKEN_FILE` *(String)*: The file path where the authentication tokens are stored.
- `tokens` *(Map\<String, String\>)*: A map of usernames to their corresponding authentication tokens.
- `currentUsername` *(String)*: The username of the currently logged-in user.
- `currentToken` *(String)*: The authentication token for the currently logged-in user.

#### **Key Functions**
- **Token Management**:
  - `loadTokens()`: Loads tokens from the file.
  - `saveTokens()`: Saves tokens to the file.
  - `hasTokenForCurrentUser()`: Checks if a token exists for the current user.
  - `storeTokenForUser(String username, String token)`: Stores a token for a user.
  - `clearToken(String username)`: Clears a token for a user.
- **Session Management**:
  - `setCurrentUser(String username)`: Sets the current user.
  - `getCurrentUsername()`: Retrieves the current username.
  - `getCurrentToken()`: Retrieves the current token.

---

### `serverSide/ChatServer.java`
Implements the main server-side logic of the secure chat system. It listens for client connections, manages chat rooms, and broadcasts messages.

#### **Key Variables**
- `PORT` *(int)*: The port number on which the server listens for client connections.
- `connectedClients` *(HashMap\<String, ClientHandler\>)*: A map of connected clients, identified by their usernames.
- `chatRooms` *(HashMap\<String, ChatRoom\>)*: A map of active chat rooms, identified by their names.
- `clientsLock` *(ReentrantLock)*: A lock to manage concurrent access to the `connectedClients` map.
- `roomsLock` *(ReentrantLock)*: A lock to manage concurrent access to the `chatRooms` map.

#### **Key Functions**
- `main(String[] args)`: Entry point of the server. Initializes the SSL server socket and starts accepting client connections.

---

### `serverSide/ClientHandler.java`
Handles individual client connections. Processes incoming messages, manages user sessions, and interacts with chat rooms.

#### **Key Variables**
- **Connection Resources**:
  - `socket` *(Socket)*: The socket for communication with the client.
  - `out` *(PrintWriter)*: Output stream to send messages to the client.
  - `in` *(BufferedReader)*: Input stream to receive messages from the client.
- **User Management**:
  - `user` *(User)*: Represents the connected user.
  - `connectedClients` *(Map\<String, ClientHandler\>)*: A map of all connected clients.
- **Chat Room Management**:
  - `chatRooms` *(Map\<String, ChatRoom\>)*: A map of all active chat rooms.
  - `clientsLock` *(Lock)*: Lock for managing concurrent access to `connectedClients`.
  - `roomsLock` *(Lock)*: Lock for managing concurrent access to `chatRooms`.

#### **Key Functions**

- **Initialization**:
  - `ClientHandler(Socket socket, Map\<String, ClientHandler\> connectedClients, Map\<String, ChatRoom\> chatRooms, Lock clientsLock, Lock roomsLock)`: Constructor to initialize the client handler with necessary resources.

- **Management**:
  - `run()`: Continuously listens for incoming messages from the client and processes them.
  - `processCommand(String line)`: Processes commands received from the client.
  
- **Command Handlers**:
  - `handleLogin(String line)`: Processes login requests.
  - `handleLogout(String line)`: Logs out the user and cleans up their session.
  - `handleRegister(String line)`: Processes user registration requests.
  - `handleToken(String line)`: Handles token-based reconnections.
  - `handleCreateRoom(String line)`: Creates a new chat room.
  - `handleJoinRoom(String line)`: Adds a user to a chat room.
  - `handleMessage(String line)`: Broadcasts a message to the current chat room.
  - `handleLeaveRoom()`: Removes a user from their current chat room.

---

### `serverSide/ChatRoom.java`
Manages chat rooms, including user management, message broadcasting, and AI integration for AI-enabled rooms.

#### **Key Variables**
- **Room Details**:
  - `roomName` *(String)*: The name of the chat room.
  - `isAIRoom` *(boolean)*: Indicates if the room is AI-enabled.
  - `aiPrompt` *(String)*: The AI prompt for AI-enabled rooms.
- **User and Message Management**:
  - `users` *(List\<User\>)*: A list of users in the chat room.
  - `messageHistory` *(List\<String\>)*: A list of messages sent in the chat room.
  - `lock` *(ReentrantLock)*: A lock for managing concurrent access to the room.
- **Persistence**:
  - `historyFile` *(File)*: A file to store the chat room's message history.

#### **Key Functions**
- **Room Management**:
  - `addUser(User user, PrintWriter writer)`: Adds a user to the chat room.
  - `removeUser(User user)`: Removes a user from the chat room.
  - `broadcastMessage(User sender, String content)`: Broadcasts a message to all users in the room.
  - `getUsers()`: Retrieves the list of users in the chat room.
  - `getLastMessage()`: Retrieves the last message sent in the chat room.
  - `clearHistory()`: Clears the chat room's message history.
- **AI Integration**:
  - `isAIRoom()`: Checks if the room is AI-enabled.
  - `handleAIResponse()`: Generates and broadcasts an AI response in AI-enabled rooms.
- **Persistence**:
  - `saveMessageToFile(String message)`: Saves a message to the room's history file.
  - `loadHistoryFromFile()`: Loads the room's message history from the file.

---

### `serverSide/AuthenticationManager.java`
Handles user authentication and session management, including user registration and login processes.

#### **Key Variables**
- `USERS_FILE` *(String)*: The file path where user credentials are stored.
- `credentials` *(Map\<String, String\>)*: A map of usernames to hashed passwords.

#### **Key Functions**
- **User Management**:
  - `registerUser(String username, String password)`: Registers a new user with a hashed password.
  - `validateCredentials(String username, String password)`: Validates a user's login credentials.
- **Persistence**:
  - `saveUserToFile(String username, String hashedPassword)`: Saves a user's credentials to the file.
  - `loadUsersFromFile()`: Loads user credentials from the file.
- **Password Hashing**:
  - `hashPassword(String password)`: Hashes a password using SHA-256.
- **Utility Functions**:
  - `bytesToHex(byte[] hash)`: Converts a byte array to a hexadecimal string.

---

### `serverSide/TokenManager.java`
Manages authentication tokens for user sessions. It handles the generation, storage, and retrieval of tokens.

#### **Key Variables**
- `tokenToSession` *(Map\<String, SessionInfo\>)*: A map of tokens to session information.
- `TOKEN_EXPIRATION_SECONDS` *(long)*: The duration (in seconds) before a token expires.
- `CHAR_POOL` *(String)*: A string containing characters used for generating tokens.
- `RANDOM` *(Random)*: A random number generator for token creation.

#### **Key Functions**
- **Token Management**:
  - `generateShortToken()`: Generates a short, unique token for user sessions.
  - `generateToken(User user)`: Generates a unique token for a user session.
  - `getUserByToken(String token)`: Retrieves the user associated with a token.
  - `removeToken(String token)`: Removes a token from the system.

---

### `serverSide/FaultToleranceManager.java`
Implements fault tolerance mechanisms for the chat system, allowing users to reconnect and maintain their session state.

#### **Key Variables**
- `tokenToUserState` *(Map\<String, User\>)*: A map of tokens to user states.

#### **Key Functions**
- **State Management**:
  - `saveUserState(String token, User user)`: Saves the state of a user session.
  - `restoreUserState(String token)`: Restores the state of a user session.
  - `handleReconnection(String token)`: Handles user reconnection using a token.
  - `invalidateToken(String token)`: Invalidates a token and removes its associated state.
  - `unbindUser(String username)`: Removes all states associated with a username.

---

### `serverSide/AIIntegration.java`
Integrates AI functionality into chat rooms, allowing for AI-generated responses in AI-enabled rooms.

#### **Key Variables**
- `LLM_ENDPOINT` *(String)*: The endpoint URL for the AI model.

#### **Key Functions**
- **AI Communication**:
  - `sendPrompt(String prompt)`: Sends a prompt to the AI model and retrieves the generated response.
- **Response Parsing**:
  - `extractResponseText(String json)`: Extracts the AI-generated response from the JSON response.

---

### `serverSide/User.java`
Represents a user in the chat system, including authentication and session management.

#### **Key Variables**
- **User Details**:
  - `username` *(String)*: The username of the user.
  - `sessionToken` *(String)*: The session token for the user.
  - `currentRoom` *(String)*: The name of the chat room the user is currently in.
- **Connection Resources**:
  - `socket` *(Socket)*: The socket for communication with the user.
  - `writer` *(PrintWriter)*: Output stream to send messages to the user.

#### **Key Functions**
- **User Management**:
  - `getUsername()`: Retrieves the username of the user.
  - `getSessionToken()`: Retrieves the session token of the user.
  - `setSessionToken(String token)`: Sets the session token for the user.
  - `getCurrentRoom()`: Retrieves the current chat room of the user.
  - `setCurrentRoom(String roomName)`: Sets the current room for the user.
  - `sendMessage(String message)`: Sends a message to the user.

---

### `sharedFiles/Message.java`
Represents a message exchanged between users in the chat system.

#### **Key Variables**
- `sender` *(String)*: The username of the message sender.
- `content` *(String)*: The content of the message.
- `timestamp` *(Date)*: The timestamp when the message was created.
- `roomName` *(String)*: The name of the chat room where the message was sent.

#### **Key Functions**
- `serialize()`: Converts the message object into a string format for transmission.
- `deserialize(String data)`: Reconstructs a `Message` object from a serialized string.

- ** Message Management**:
  - `getSender()`: Retrieves the sender of the message.
  - `getContent()`: Retrieves the content of the message.
  - `getTimestamp()`: Retrieves the timestamp of the message.
  - `getRoomName()`: Retrieves the name of the chat room where the message was sent.

---

### `sharedFiles/ProtocolConstants.java`
Defines constants for commands, protocols, and response codes used in both the client and server.

#### **Key Constants**
- **Command Types**:
  - `LOGIN`, `REGISTER`, `LOGOUT`, `MESSAGE`, `JOIN_ROOM`, `LEAVE_ROOM`, `CREATE_ROOM`, `DELETE_ROOM`
- **Response Types**:
  - `SUCCESS`, `ERROR`, `INVALID_COMMAND`
- **Other Constants**:
  - `DEFAULT_PORT` *(int)*: The default port number for the server (12345).
  - `MAX_MESSAGE_LENGTH` *(int)*: The maximum allowed length for a message (1024).