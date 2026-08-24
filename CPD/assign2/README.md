# PROJECT 2: Client-server chat system

This project implements a secure client-server chat system in Java.

## How to Run

### 1. Compile all files:

```bash
javac serverSide/*.java clientSide/*.java sharedFiles/*.java
```

### 2. Generate the keystore (only once):

```bash
keytool -genkeypair -alias chatserver -keyalg RSA -keysize 2048 -keystore serverkeystore.jks
```

Use:
- Password: `123456`
- CN: `localhost`
- Other info: `FEUP`, `Porto`, `PT`

### 3. Run the server:

```bash
java serverSide.ChatServer
```

### 4. Run the client:

```bash
java clientSide.ChatClient
```

## Credentials 

### User 1
- Username - alessia
- Password - 1234

### User 2 
- Username - pedro
- Password - 1234

Each user should open a different terminal, and if you prefer to create a new account you should register instead of loggin in.

---

## Project Details

For a detailed breakdown of the project structure, components, and functionality, refer to the [DETAILS.md](./DETAILS.md) file.

### Key Sections in `DETAILS.md`:


### Client-side Components

- [`ChatClient.java`](./DETAILS.md#clientsidechatclientjava): Manages client-side operations, including authentication and chat room interactions.
- [`MessageListener.java`](./DETAILS.md#clientsidemessagelistenerjava): Listens for incoming messages from the server.
- [`TokenManager.java`](./DETAILS.md#clientsidetokenmanagerjava): Handles token storage and retrieval for secure client sessions.

---

### Server-side Components

- [`ChatServer.java`](./DETAILS.md#serversidechatserverjava): Main server logic, manages client connections and chat rooms.
- [`ClientHandler.java`](./DETAILS.md#serversideclienthandlerjava): Handles individual client sessions and commands.
- [`ChatRoom.java`](./DETAILS.md#serversidechatroomjava): Manages chat room operations, including AI-enabled rooms.
- [`AuthenticationManager.java`](./DETAILS.md#serversideauthenticationmanagerjava): Handles user registration and login.
- [`TokenManager.java`](./DETAILS.md#serversidetokenmanagerjava): Manages session tokens for authentication.
- [`FaultToleranceManager.java`](./DETAILS.md#serversidefaulttolerancemanagerjava): Implements fault tolerance for user reconnections.
- [`AIIntegration.java`](./DETAILS.md#serversideaiintegrationjava): Integrates AI functionality for AI-enabled chat rooms.
- [`User.java`](./DETAILS.md#serversideuserjava): Represents a user in the system.

---

### Shared Components

- [`Message.java`](./DETAILS.md#sharedfilesmessagejava): Represents messages exchanged between users.
- [`ProtocolConstants.java`](./DETAILS.md#sharedfilesprotocolconstantsjava): Defines constants for commands, responses, and protocols.
