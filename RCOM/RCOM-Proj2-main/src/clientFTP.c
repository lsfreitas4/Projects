#include "clientFTP.h"

int connectFTP(const char *serverAdress, const int serverPort, int *socketfd)
{

    if (connectSocket(serverAdress, serverPort, socketfd))
    {
        return -1;
    }

    char *buf = malloc(MAX_SIZE_RESPONSE);
    int code = 0;

    if (readResponse(*socketfd, buf, &code))
    {
        perror("readResponse()");
        free(buf);
        return -1;
    }

    if (code != 220)
    {
        perror("FAILED TO ESTABLISH CONNECTION");
        return -1;
    }
    free(buf);

    return 0;
}

int readResponse(int socketfd, char *buf, int *code)
{

    if (buf == NULL)
    {
        return -1;
    }

    if (code == NULL)
    {
        return -1;
    }

    enum state currentState = START;
    int index = 0;
    char byte = 0;
    *code = 0;

    int status = read(socketfd, &byte, 1);
    if (status < 0)
    {
        perror("read()");
        exit(-1);
    }

    while (currentState != STOP)
    {
        int status = read(socketfd, &byte, 1);
        if (status < 0)
        {
            perror("read()");
            exit(-1);
        }
        // printf("BYTE - %c\n", byte);

        switch (currentState)
        {
        case START:
            if (byte >= '0' && byte <= '9')
            {
                *code *= 10;
                *code += byte - '0';
                // printf("CODE - %d\n", *code);
            }
            else if (byte == ' ')
                currentState = SL_MESSAGE;
            else if (byte == '-')
                currentState = MLT_MESSAGE;
            else if (byte == '\n')
                currentState = STOP;
            break;

        case SL_MESSAGE:
            // printf("SL_MESSAGE\n");
            if (byte == '\n')
            {
                buf[index] = '\0';
                currentState = STOP;
            }

            else
                buf[index++] = byte;

            break;

        case MLT_MESSAGE:
            // printf("MLT_MESSAGE\n");
            buf[index++] = byte;

            if (byte == '\n')
            {
                currentState = START;
                *code = 0;
            }
            break;

        case STOP:
            printf("STOP\n");
            break;
        default:
            break;
        }
    }

    printf("**CODE** - %d\n", *code);
    // printf("**TEXT** - %s\n", buf);

    return 0;
}

/*
int readResponse(int socketfd, char *buf, int *code)
{

    if (buf == NULL)
    {
        return -1;
    }

    if (code == NULL)
    {
        return -1;
    }

    enum state currentState = START;
    int index = 0;
    *code = 0;
    int prevCode = 0;

    while (currentState != STOP)
    {
        char byte = 0;

        int status = read(socketfd, &byte, 1);
        if (status < 0)
        {
            perror("read()");
            exit(-1);
        }

        switch (currentState)
        {
        case START:
            if (status == 0 || byte == '\n')
                currentState = STOP;

            else if (status == 1)
            {

                if (byte >= 48 && byte <= 57)
                {
                    *code = byte - 48;
                    *code *= 10;
                }

                else if (byte == ' ')
                {
                    currentState = SL_MESSAGE;
                }

                else if (byte == '-')
                {
                    currentState = MLT_MESSAGE;
                }
            }
            break;
        case SL_MESSAGE:
            if (status == 0 || byte == '\n')
            {
                currentState = STOP;
                buf[index] = '\0';
            }
            else if (status == 1)
            {
                buf[index++] = byte;
            }
            break;
        case MLT_MESSAGE:
            if (byte == '\n')
            {
                currentState = START;
                if (prevCode != 0 && prevCode != *code)
                {
                    return -1;
                }
                prevCode = *code;
                *code = 0;
            }
            buf[index++] = byte;
            break;
        default:
            currentState = START;
            break;
        }
    }
    printf("**CODE** - %d\n", *code);
    printf("**TEXT** - %s\n", buf);

    return 0;
}
*/
int writeCommand(int socketfd, const char *command)
{
    size_t bytes;
    bytes = write(socketfd, command, strlen(command));

    if (bytes < 0)
    // printf("Bytes escritos %ld\n", bytes);
    // else
    {
        perror("write()");
        exit(-1);
    }

    return 0;
}

int passiveMode(int socketfd, char *ip, int *port)
{

    if (ip == NULL)
    {
        perror("IP cannot be null");
        return -1;
    }
    if (port == NULL)
    {
        perror("Port cannot be null");
        return -1;
    }

    char *passiveCommand = "pasv\r\n";
    char *buf = malloc(MAX_SIZE_RESPONSE);
    int code = 0;

    if (writeCommand(socketfd, passiveCommand))
    {
        perror("writeCommand()");
        free(buf);
        return -1;
    }

    if (readResponse(socketfd, buf, &code))
    {
        perror("readResponse()");
        free(buf);
        return -1;
    }

    // printf("RESPONSE - %s\n", buf);
    if (code != 27)
    {
        perror("FAILED TO ENTER PASSIVE MODE");
        free(buf);
        return -1;
    }
    printf("RESPONSE - %s\n", buf);

    int ip1, ip2, ip3, ip4, port1, port2;
    int res = sscanf(buf, "Entering Passive Mode (%d,%d,%d,%d,%d,%d)", &ip1, &ip2, &ip3, &ip4, &port1, &port2);
    if (res != 6)
    {
        perror("FAILED TO PARSE PASSIVE MODE");
        free(buf);
        return -1;
    }

    *port = (port1 << 8) + port2;

    snprintf(ip, MAX_SIZE, "%d.%d.%d.%d", ip1, ip2, ip3, ip4);
    printf("[INFO] Socket2\n - IP: %s\n - Port: %d\n", ip, *port);
    free(buf);

    return 0;
}

int connectSocket(const char *serverAdress, const int serverPort, int *socketfd)
{

    if (socketfd == NULL)
    {
        return -1;
    }

    if (serverAdress == NULL)
    {
        return -1;
    }

    int sockfd;
    struct sockaddr_in server_addr;
    // char buf[] = "Mensagem de teste na travessia da pilha TCP/IP\n";
    // size_t bytes;

    /*server address handling*/
    bzero((char *)&server_addr, sizeof(server_addr));
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = inet_addr(serverAdress); /*32 bit Internet address network byte ordered*/
    server_addr.sin_port = htons(serverPort);              /*server TCP port must be network byte ordered */

    /*open a TCP socket*/
    if ((sockfd = socket(AF_INET, SOCK_STREAM, 0)) < 0)
    {
        perror("socket()");
        exit(-1);
    }
    /*connect to the server*/
    if (connect(sockfd,
                (struct sockaddr *)&server_addr,
                sizeof(server_addr)) < 0)
    {
        perror("Unable to connect socket()");
        exit(-1);
    }

    *socketfd = sockfd;

    return 0;
}

int disconnectSocket(const int socketA, const int socketB)
{

    char *disconnetCommand = "quit\r\n";
    char *buf = malloc(MAX_SIZE_RESPONSE);
    int code = 0;

    if (writeCommand(socketA, disconnetCommand))
    {
        perror("writeCommand()");
        return -1;
    }

    if (readResponse(socketA, buf, &code))
    {
        perror("readResponse()");
        return -1;
    }

    if (code != 21)
    {
        perror("FAILED TO DISCONNECT");
        return -1;
    }

    if (close(socketA) < 0)
    {
        perror("close() A");
        return -1;
    }

    if (socketB == -1)
    {
        return 0;
    }

    if (close(socketB) < 0)
    {
        perror("close() B");
        return -1;
    }

    return 0;
}

int parseURL(const char *url, struct Connection *connection)
{
    if (url == NULL)
    {
        perror("URL cannot be null");
        return -1;
    }

    int auth = sscanf(url, "ftp://%255[^:]:%255[^@]@%255[^/]/%255[^\n]",
                      connection->user, connection->password, connection->host, connection->path);

    if (auth != 4)
    {

        auth = sscanf(url, "ftp://%255[^/]/%255[^\n]",
                      connection->host, connection->path);

        if (auth != 2)
        {
            perror("FAILED TO PARSE URL");
            return -1;
        }

        strcpy(connection->user, "anonymous");
        strcpy(connection->password, "anonymous");
    }

    /*

    const char *arroba = strchr(url, '@');

    if (arroba)
    {

        char password[512];
        sscanf(url, "ftp://%511[^@]@%255[^/]/%255[^\n]",
               password,
               connection->host,
               connection->path);

        char *colon = strchr(password, ':');
        if (colon)
        {
            *colon = '\0';
            strcpy(connection->user, password);
            strcpy(connection->password, colon + 1);
        }
        else
        {
            perror("FAILED TO PARSE URL");
            return -1;
        }
    }
    else
    {
        sscanf(url, "ftp://%255[^/]/%255[^\n]",
               connection->host,
               connection->path);

        strcpy(connection->user, "anonymous");
        strcpy(connection->password, "anonymous");
    }
*/
    if (strlen(connection->host) == 0 || strlen(connection->path) == 0)
    {
        perror("FAILED TO PARSE URL");
        return -1;
    }

    struct hostent *h;
    h = gethostbyname(connection->host);

    if (h == NULL)
    {
        perror("FAILED TO GET HOST BY NAME");
        return -1;
    }

    struct in_addr *addr = (struct in_addr *)h->h_addr_list[0];
    strcpy(connection->hostName, h->h_name);
    strcpy(connection->ip, inet_ntoa(*addr));

    const char *filename = strrchr(connection->path, '/');
    if (filename == NULL)
    {
        strcpy(connection->filename, connection->path);
    }
    else
    {
        strcpy(connection->filename, filename + 1);
    }

    connection->filename[MAX_SIZE - 1] = '\0';

    return 0;
}

int login(const int socketfd, const char *username, const char *password)
{

    if (username == NULL)
    {
        perror("Username cannot be null");
        return -1;
    }

    if (password == NULL)
    {
        perror("Password cannot be null");
        return -1;
    }

    int code = 0;
    char *command = malloc(MAX_SIZE_RESPONSE);
    char *buf = malloc(MAX_SIZE_RESPONSE);

    snprintf(command, MAX_SIZE, "USER %s\r\n", username);
    if (writeCommand(socketfd, command))
    {
        free(buf);
        free(command);
        perror("writeCommand() Username");
        return -1;
    }

    if (readResponse(socketfd, buf, &code))
    {
        free(buf);
        free(command);
        perror("readResponse() Username");
        return -1;
    }

    if (code != 31)
    {
        perror("FAILED TO LOGIN");
        printf("CODE - %d\n", code);
        return -1;
    }

    snprintf(command, MAX_SIZE, "PASS %s\r\n", password);
    if (writeCommand(socketfd, command))
    {
        free(buf);
        free(command);
        perror("writeCommand() Password");
        return -1;
    }

    if (readResponse(socketfd, buf, &code))
    {
        free(buf);
        free(command);
        perror("readResponse() Password");
        return -1;
    }

    if (code != 31 && code != 30)
    {
        printf("CODE - %d\n", code);
        free(buf);
        free(command);
        perror("FAILED TO LOGIN 2");
        return -1;
    }
    free(buf);
    free(command);

    return 0;
}

int download(const int socketA, const int socketB, const char *path, const char *filename)
{

    int code = 0;
    char *command = malloc(MAX_SIZE_RESPONSE);
    char *responseBuf = malloc(MAX_SIZE_RESPONSE);

    snprintf(command, MAX_SIZE, "RETR %s\r\n", path);
    if (writeCommand(socketA, command))
    {
        free(responseBuf);
        free(command);
        perror("writeCommand() RETR");
        return -1;
    }

    if (readResponse(socketA, responseBuf, &code))
    {
        free(responseBuf);
        free(command);
        perror("readResponse() RETR");
        return -1;
    }

    if (code != CODE_125 && code != CODE_150)
    {
        free(responseBuf);
        free(command);
        perror("FAILED TO DOWNLOAD");
        return -1;
    }

    FILE *file = fopen(filename, "wb");

    if (file == NULL)
    {
        perror("fopen()");
        return -1;
    }

    char *buf = malloc(MAX_SIZE);

    int bytes = 0;

    while ((bytes = read(socketB, buf, MAX_SIZE)))
    {
        if (fwrite(buf, bytes, 1, file))
        {
            free(buf);
            free(command);
            perror("fwrite()");
            return -1;
        }
    }

    fclose(file);

    if (readResponse(socketA, responseBuf, &code))
    {
        free(buf);
        free(command);
        perror("readResponse() RETR");
        return -1;
    }

    if (code != CODE_226)
    {
        free(buf);
        free(command);
        perror("FAILED TO DOWNLOAD");
        return -1;
    }

    return 0;
}
