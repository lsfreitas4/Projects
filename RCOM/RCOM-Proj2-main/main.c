#include "clientFTP.h"

int main(int argc, char *argv[])
{

    if (argc != 2)
    {
        perror("Usage: ftp://[<user>:<password>@]<host>/<url>\n");
        return -1;
    }

    struct Connection connection;
    memset(&connection, 0, sizeof(connection));

    if (parseURL(argv[1], &connection))
    {
        perror("parseURL()");
        exit(-1);
    }

    printf("URL parsed successfully\n");
    printf("----------------------\n");

    int socketA, socketB;
    if (connectFTP(connection.ip, TCP_PORT, &socketA))
    {
        perror("connectFTP()");
        exit(-1);
    }

    printf("Connected to FTP server\n");
    printf("----------------------\n");

    if (login(socketA, connection.user, connection.password))
    {
        perror("login()");
        disconnectSocket(socketA, -1);
        exit(-1);
    }

    printf("Logged in\n");
    printf("----------------------\n");

    char *IP = malloc(MAX_SIZE);
    int port = 0;
    if (passiveMode(socketA, IP, &port))
    {
        perror("passiveMode()");
        disconnectSocket(socketA, -1);
        exit(-1);
    }

    printf("Passive mode enabled\n");
    printf("----------------------\n");

    if (connectSocket(IP, port, &socketB))
    {
        perror("connectSocket()");
        disconnectSocket(socketA, -1);
        exit(-1);
    }

    printf("Connected to data socket\n");
    printf("----------------------\n");

    if (download(socketA, socketB, connection.path, connection.filename))
    {
        perror("download()");
        disconnectSocket(socketA, socketB);
        exit(-1);
    }

    if (disconnectSocket(socketA, socketB))
    {
        perror("disconnectSocket()");
        exit(-1);
    }

    return 0;
}