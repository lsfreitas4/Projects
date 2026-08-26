// Link layer protocol implementation

#include "link_layer.h"
#include "serial_port.h"
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <termios.h>
#include <unistd.h>
#include <signal.h>

// MISC
#define _POSIX_SOURCE 1 // POSIX compliant source

#define BUF_SIZE 256
#define FRAME_SIZE 5
#define BAUDRATE 38400

#define FALSE 0
#define TRUE 1

#define FLAG 0X7E
#define ADDRESS_TRANS 0x03
#define ADDRESS_REC 0x01
#define CONTROL_SET 0x03
#define CONTROL_UA 0x07
#define CONTROL_DISC 0x0B
#define ESC_OCT 0x7D
#define ESC_XOR 0x20

typedef enum
{
    START,
    FLAG_REC,
    ADD_REC,
    C_REC,
    BCC_OK,
    DATA_REC,
    STOP
} LinkLayerState;

volatile int stop = FALSE;
int alarmCount = 0;
int alarmEnabled = 0;
extern int fd;
LinkLayer connectionParameters;

void alarmHandler(int signal)
{
    alarmCount++;
    alarmEnabled = TRUE;
}

void disableAlarm()
{
    alarm(0);
    alarmEnabled = FALSE;
    alarmCount = 0;
}

int setConnection(LinkLayer connectionParameters)
{

    fd = open(connectionParameters.serialPort, O_RDWR | O_NOCTTY);

    if (fd < 0)
    {
        perror("OPEN ERROR");
        return -1;
    }

    struct termios newtio;

    memset(&newtio, 0, sizeof(newtio));
    newtio.c_cflag = connectionParameters.baudRate | CS8 | CLOCAL | CREAD;
    newtio.c_iflag = IGNPAR;
    newtio.c_oflag = 0;
    newtio.c_lflag = 0;
    newtio.c_cc[VTIME] = 1;
    newtio.c_cc[VMIN] = 0;

    tcflush(fd, TCIOFLUSH);

    if (tcsetattr(fd, TCSANOW, &newtio) == -1)
    {
        perror("tcsetattr");
        return -1;
    }

    return 0;
}

int sendPacket(unsigned char address, unsigned char control)
{

    unsigned char bcc = address ^ control;
    unsigned char buffer[FRAME_SIZE] = {FLAG, address, control, bcc, FLAG};

    if (write(fd, buffer, FRAME_SIZE))
    {
        perror("SEND ERROR");
        return -1;
    }
    return 0;
}

int receivePacket(unsigned char address, unsigned char control)
{

    LinkLayerState current_state = START;
    unsigned char expectedBCC = address ^ control;

    while (current_state != STOP)
    {
        unsigned char current_byte;
        int bytes = read(fd, &current_byte, sizeof(current_byte));

        if (bytes < 0)
        {
            perror("READ ERROR");
            return -1;
        }
        else
        {
            switch (current_state)
            {

            case START:
                if (current_byte == FLAG)
                    current_state = FLAG_REC;
                break;

            case FLAG_REC:
                if (current_byte == FLAG)
                    continue;
                if (current_byte == address)
                    current_state = ADD_REC;
                else
                    current_state = START;
                break;

            case ADD_REC:
                if (current_byte == address)
                    continue;
                if (current_byte == control)
                    current_state = C_REC;
                else
                    current_state = START;
                break;

            case C_REC:
                if (current_byte == control)
                    continue;
                if (current_byte == expectedBCC)
                    current_state = BCC_OK;
                else
                    current_state = START;
                break;

            case BCC_OK:
                if (current_byte == expectedBCC)
                    continue;
                if (current_byte == FLAG)
                    current_state = STOP;
                else
                    current_state = START;
                break;

            default:
                break;
            }
        }
    }

    return 0;
}

int packetRetransmissions(unsigned char expected_A, unsigned char expected_C, unsigned char real_A, unsigned char real_C)
{
    LinkLayerState current_state = START;
    (void)signal(SIGALRM, alarmHandler);
    if (receivePacket(real_A, real_C))
        return -1;
    alarm(connectionParameters.timeout);
    while (current_state != STOP && alarmCount <= connectionParameters.nRetransmissions)
    {
        unsigned char current_byte;
        int bytes = read(fd, &current_byte, sizeof(current_byte));

        if (bytes < 0)
        {
            perror("READ ERROR");
            return -1;
        }

        else
        {
            switch (current_state)
            {

            case START:
                if (current_byte == FLAG)
                    current_state = FLAG_REC;
                break;

            case FLAG_REC:
                if (current_byte == FLAG)
                    continue;
                if (current_byte == expected_A)
                    current_state = ADD_REC;
                else
                    current_state = START;
                break;

            case ADD_REC:
                if (current_byte == expected_C)
                    current_state = C_REC;
                if (current_byte == FLAG)
                    current_state = FLAG_REC;
                else
                    current_state = START;
                break;

            case C_REC:
                if (current_byte == (expected_A ^ expected_C))
                    current_state = BCC_OK;
                if (current_byte == FLAG)
                    current_state = FLAG_REC;
                else
                    current_state = START;
                break;

            case BCC_OK:
                if (current_byte == FLAG)
                    current_state = STOP;
                else
                    current_state = START;
                break;

            default:
                break;
            }
        }

        if (current_state == STOP)
        {
            disableAlarm();
            return 0;
        }

        if (alarmEnabled)
        {
            alarmEnabled = FALSE;
            if (alarmCount <= connectionParameters.nRetransmissions)
            {
                if (sendPacket(real_A, real_C))
                    return -1;
                alarm(connectionParameters.timeout);
            }
            current_state = START;
        }
    }
    disableAlarm();
    return -1;
}

////////////////////////////////////////////////
// LLOPEN
////////////////////////////////////////////////
int llopen(LinkLayer connectionParameters)
{

    if (setConnection(connectionParameters) == -1)
    {
        perror("CONNECTION ERROR");
        return -1;
    }

    switch (connectionParameters.role)
    {
    case LlTx:
    {

        if (packetRetransmissions(ADDRESS_TRANS, CONTROL_UA, ADDRESS_TRANS, CONTROL_SET) < 0)
            return -1;
    }

    case LlRx:
    {

        if (receivePacket(ADDRESS_TRANS, CONTROL_SET) < 0)
            return -1;

        if (sendPacket(ADDRESS_TRANS, CONTROL_SET) < 0)
            return -1;
    }

    default:
        return -1;
        break;
    }

    return 0;
}
const unsigned char *byteStuffer(unsigned char *buffer, int bufSize)
{

    if (buffer == NULL)
        return -1;

    unsigned char *newBuffer;

    for (int i; i < bufSize; i++)
    {
        if (buffer[i] == FLAG)
        {
            newBuffer[i] = FLAG ^ ESC_XOR;
            newBuffer[i + 1] = ESC_OCT;
        }
        else if (buffer[i] == ESC_OCT)
        {
            newBuffer[i] = ESC_OCT ^ ESC_XOR;
            newBuffer[i + 1] = ESC_OCT;
        }
    }

    return newBuffer;
}
////////////////////////////////////////////////
// LLWRITE
////////////////////////////////////////////////
int llwrite(const unsigned char *buf, int bufSize)
{
    if (buf == NULL)
        return -1;

    unsigned char *stuffedBuffer;
    stuffedBuffer = byteStuffer(buf, bufSize);

    if (stuffedBuffer == NULL)
        return -1;

    return 0;
}

////////////////////////////////////////////////
// LLREAD
////////////////////////////////////////////////
int llread(unsigned char *packet)
{
    // TODO

    return 0;
}

////////////////////////////////////////////////
// LLCLOSE
////////////////////////////////////////////////
int llclose(int showStatistics)
{
    // TODO

    int clstat = closeSerialPort();
    return clstat;
}