#include <stdio.h>
#include <stdlib.h>
#include <netdb.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdio.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>

#define TRUE 1
#define FALSE 0

#define MAX_SIZE 256
#define MAX_SIZE_RESPONSE 1024

#define TCP_PORT 21

#define CODE_220 220 // Response code for "Service ready for new user."
#define CODE_331 331 // Response code for "User name okay, need password."
#define CODE_230 230 // Response code for "User logged in, proceed."
#define CODE_227 227 // Response code for "Entering Passive Mode (h1,h2,h3,h4,p1,p2)."
#define CODE_150 150 // Response code for "File status okay; about to open data connection."
#define CODE_125 125 // Response code for "Data connection already open; transfer starting."
#define CODE_226 226 // Response code for "Closing data connection."
#define CODE_221 221 // Response code for "Service closing control connection."

struct Connection
{
    char user[MAX_SIZE];
    char password[MAX_SIZE];
    char host[MAX_SIZE];
    char hostName[MAX_SIZE];
    char path[MAX_SIZE];
    char filename[MAX_SIZE];
    char ip[MAX_SIZE];
};

enum state
{
    START,       // state of receive code
    SL_MESSAGE,  // state of receive message
    MLT_MESSAGE, // case quando pqp tem 200-text
    STOP
};

int connectFTP(const char *serverAdress, const int serverPort, int *socketfd);

int readResponse(int socketfd, char *buf, int *code);

int writeCommand(int socketfd, const char *command);

int passiveMode(int socketfd, char *ip, int *port);

int connectSocket(const char *serverAdress, const int serverPort, int *socketfd);

int disconnectSocket(const int socketA, const int socketB);

int parseURL(const char *url, struct Connection *connection);

int login(const int socketfd, const char *username, const char *password);

int download(const int socketA, const int socketB, const char *path, const char *filename);