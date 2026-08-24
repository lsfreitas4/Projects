# Source Files Division and Explanation

### The source files are divided into the following categories:

> Open to any changes and improvements. This is just an initial division of files. A suggestion.

## Directories:


### clientSide
- Contains files related to the client-side logic. This includes:
   - User Interaction
   - Communication with the server

1. ChatClient.java
   - Main client class
   - Connects to the server and handles user input (e.g., login, room selection, sending messages).
   - Manages the main client loop for interacting with the server.

2. MessageListener.java
   - A separate thread or virtual thread that listens for incoming messages from the server.
   - Continuously updates the client UI or console with new messages.

3. TokenManager.java
   - Manages session tokens for reconnection.
   - Stores the token securely and sends it to the server during reconnection.
   - Handles token expiration and renewal if needed.

---

### serverSide
- Contains files related to the server-side logic. This includes:
   - User management
   - Chat Room Handling
   - AI Integration

1. ChatServer.java
   - Main server class that initializes the server.
   - Listens for client connections and spawns threads (or virtual threads) to handle them.
   - Manages chat rooms, user authentication, and message broadcasting.

2. ChatRoom.java
   - Represents a chat room.
   - Stores a list of users in the room and a history of messages.
   - Provides methods for adding/removing users and broadcasting messages.

3. User.java
   - Represents a user connected to the server.
   - Stores user details like username, session token, and current room.
   - May include methods for managing user state.

4. AuthenticationManager.java
   - Handles user registration and login.
   - Validates credentials and generates session tokens.
   - Manages a database or file for storing user credentials securely.

5. AIIntegration.java
   - Manages communication with the local LLM for AI rooms.
   - Sends the chat context and prompt to the LLM and retrieves responses.
   - Adds the AI's response to the chat room under the "Bot" username.

6. FaultToleranceManager.java
   - Handles reconnection logic for users with broken TCP connections.
   - Maps session tokens to user states (e.g., current room, message history).
   - Ensures users can resume their session seamlessly.

---

### sharedFiles
- Contains files that are shared between the client and server. This includes:
   - Data Models
   - Protocols Definitions

1. Message.java
   - Represents a message exchanged between the client and server.
   - Contains fields like sender, content, timestamp, and room name.
   - Includes serialization/deserialization methods if needed.

2. ProtocolConstants.java
   - Defines constants for communication protocols.
   - Examples: commands like LOGIN, MESSAGE, JOIN_ROOM, CREATE_ROOM, etc.
   - Ensures consistency between client and server communication.

---

---

## Possible Order of Implementation :

trying to ensure dependencies are resolved and functionality builds logically

1. **`sharedFiles/ProtocolConstants`**
   - Define constants for commands and protocols to be used across the project.

2. **`sharedFiles/Message`**
   - Implement the message structure for communication between the client and server.

3. **`serverSide/User`**
   - Create the user model to manage user-related data.

4. **`serverSide/ChatRoom`**
   - Implement chat room functionality to manage users and messages within a room.

5. **`serverSide/AuthenticationManager`**
   - Handle user authentication and session management.

6. **`serverSide/FaultToleranceManager`**
   - Implement fault tolerance mechanisms for user reconnections and state management.

7. **`serverSide/AIIntegration`**
   - Add AI-related functionality for chat rooms requiring AI responses.

8. **`serverSide/ChatServer`**
   - Build the server logic to manage client connections, chat rooms, and message broadcasting.

9. **`clientSide/TokenManager`**
   - Implement token management for client authentication.

10. **`clientSide/MessageListener`**
   - Create the listener to handle incoming messages from the server.

11. **`clientSide/ChatClient`**
   - Implement the main client logic to connect to the server, send messages, and interact with the user.


---------

## Extra Info

> Use OrgJson for JSON parsing and serialization from Ollama


# Division

1st - Infraestrutura e Autenticação (Servidor) (Aléssia)
- ChatServer.java
- AuthenticationManager.java
- User.java
- TokenManager.java
- FaultToleranceManager.java

2nd - Cliente e Comunicação (Mariana)
- ChatClient.java
- TokenManager.java
- MessageListener.java

3rd - Chat Room Handling and AI Integration (Luís)
- ChatRoom.java
- AIIntegration.java