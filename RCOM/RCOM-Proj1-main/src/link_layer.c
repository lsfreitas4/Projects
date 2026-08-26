#include "link_layer.h"
#include "serial_port.h"
#include <fcntl.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/stat.h>
#include <termios.h>
#include <unistd.h>
#include <signal.h>

// MISC
#define _POSIX_SOURCE 1 // POSIX compliant source

#define FRAME_SIZE 5

#define FALSE 0
#define TRUE 1

#define FLAG 0x7E
#define ESC_OCT 0x7D
#define ESC_FLAG 0x5E
#define ESC_ESC_OCT 0x5D
#define ADD_TRANS 0x03
#define ADD_REC 0x01
#define CONTROL_SET 0x03
#define CONTROL_UA 0x07
#define CONTROL_DISC 0x0B

#define INFO0 0x00
#define INFO1 0x80

#define CONTROL_RR0         0xAA
#define CONTROL_RR1         0xAB
#define CONTROL_REJ0        0x54
#define CONTROL_REJ1        0x55

typedef struct
{
    unsigned int nFrames;
    unsigned int errorFrames;
    double time_send_DATA_REC;
    struct timeval start;
} Statistics;

enum state{
    START, 
    FLAG_REC, 
    A_REC,
    C_REC,
    BCC_OK,
    DATA_REC,
    STOP
};

LinkLayer connectionParameters;
Statistics statistics = {0, 0.0, 0.0};
struct termios oldtio;
int alarmEnabled = FALSE;
int alarmCount = 0;
int writeInfoNumbering = 0;
int readInfoNumbering = 0;
extern int fd;

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
    newtio.c_cc[VTIME] = 10;
    newtio.c_cc[VMIN] = 0;

    tcflush(fd, TCIOFLUSH);

    if (tcsetattr(fd, TCSANOW, &newtio) == -1)
    {
        perror("tcsetattr");
        return -1;
    }

    return 0;
}

void alarmHandler(int signal)
{
    alarmCount++;
    alarmEnabled = TRUE;
    printf("Alarm count: %d\n", alarmCount);
}

void disableAlarm()
{
    alarm(0);
    alarmEnabled = FALSE;
    alarmCount = 0;
}

int sendPacket(unsigned char address, unsigned char control)
{
    unsigned char bcc = address ^ control;
    unsigned char buf[FRAME_SIZE] = {FLAG, address, control, bcc, FLAG};

    if (writeBytesSerialPort(buf, FRAME_SIZE) < 0)
    {
        perror("Error write send command");
        return -1;
    }
    return 0;
}

int receivePacket(unsigned char expectedAddress, unsigned char expectedControl)
{
    enum state currentState = START;

    while (currentState != STOP)
    {
        unsigned char currentByte = 0;
        if (readByteSerialPort(&currentByte) < 0)
        {
            perror("READ ERROR");
            return -1;
        }

        switch (currentState)
        {
        case START:
            if (currentByte == FLAG)
                currentState = FLAG_REC;
            break;

        case FLAG_REC:
            if (currentByte == FLAG)
                continue;
            else if (currentByte == expectedAddress)
                currentState = A_REC;
            else
                currentState = START;
            break;

        case A_REC:
            if (currentByte == FLAG)
                currentState = FLAG_REC;
            else if (currentByte == expectedControl)
                currentState = C_REC;
            else
                currentState = START;
            break;

        case C_REC:
            if (currentByte == FLAG)
                currentState = FLAG_REC;
            else if (currentByte == (expectedAddress ^ expectedControl))
                currentState = BCC_OK;
            else
                currentState = START;
            break;

        case BCC_OK:
            if (currentByte == FLAG)
                currentState = STOP;
            else
                currentState = START;
            break;

        default:
            currentState = START;
            break;
        }
    }

    return 0;
}

int packetRetransmission(unsigned char expectedAddress, unsigned char expectedControl, unsigned char address, unsigned char control)
{

    enum state currentState = START;
    (void)signal(SIGALRM, alarmHandler);

    if (sendPacket(address, control) < 0)
    {
        perror("ERROR SENDING PACKET");
        return -1;
    }

    alarm(connectionParameters.timeout);

    while (currentState != STOP && alarmCount <= connectionParameters.nRetransmissions)
    {
        unsigned char currentByte = 0;

        if (readByteSerialPort(&currentByte) < 0)
        {
            perror("ERROR READING BYTE");
            return -1;
        }

        switch (currentState)
        {
        case START:
            if (currentByte == FLAG)
                currentState = FLAG_REC;
            break;

        case FLAG_REC:
            if (currentByte == FLAG)
                continue;
            else if (currentByte == expectedAddress)
                currentState = A_REC;
            else
                currentState = START;
            break;

        case A_REC:
            if (currentByte == FLAG)
                currentState = FLAG_REC;
            else if (currentByte == expectedControl)
                currentState = C_REC;
            else
                currentState = START;
            break;

        case C_REC:
            if (currentByte == FLAG)
                currentState = FLAG_REC;
            else if (currentByte == (expectedAddress ^ expectedControl))
                currentState = BCC_OK;
            else
                currentState = START;
            break;

        case BCC_OK:
            if (currentByte == FLAG)
                currentState = STOP;
            else
                currentState = START;
            break;

        default:
            currentState = START;
            break;
        }

        if (currentState == STOP)
        {
            disableAlarm();
            return 0;
        }

        if (alarmEnabled)
        {
            alarmEnabled = FALSE;
            if (alarmCount <= connectionParameters.nRetransmissions)
            {
                if (sendPacket(address, control))
                {
                    perror("ERROR SENDING PACKET");
                    return -1;
                }

                alarm(connectionParameters.timeout);
            }
            currentState = START;
        }
    }

    return 0;
}

double get_time_difference(struct timeval ti, struct timeval tf) {
    return (tf.tv_sec - ti.tv_sec) + (tf.tv_usec - ti.tv_usec) / 1e6;
}

////////////////////////////////////////////////
// LLOPEN
////////////////////////////////////////////////
int llopen(LinkLayer connectionParametersOpen)
{
    connectionParameters = connectionParametersOpen;

    if (setConnection(connectionParameters) == -1)
    {
        perror("CONNECTION ERROR");
        return -1;
    }

    switch (connectionParameters.role)
    {
    case LlTx:
    {
        if (packetRetransmission(ADD_TRANS, CONTROL_UA, ADD_TRANS, CONTROL_SET) < 0)
            return -1;

        break;
    }

    case LlRx:
    {
        if (receivePacket(ADD_TRANS, CONTROL_SET) < 0)
            return -1;

        if (sendPacket(ADD_TRANS, CONTROL_UA) < 0)
            return -1;

        break;
    }

    default:
        return -1;
    }

    return 0;
}


const unsigned char * byteStuffing(const unsigned char *buf, int bufSize, int *newSize)
{
    if(buf == NULL){
        perror("NULL BUFFER");
        return NULL;
    }

    if(newSize == NULL){
        perror("NEW SIZE NULL");
        return NULL;
    }

    int frameSize = bufSize * 2;
    unsigned char *stuffedBuf = (unsigned char *) malloc(frameSize);

    if(stuffedBuf == NULL) return NULL;
    int j = 0;

    for(size_t i = 0; i < bufSize; i++){
        if(buf[i] == FLAG) {
            stuffedBuf[j++] = ESC_OCT;
            stuffedBuf[j] = ESC_FLAG;
        }
        else if (buf[i] == ESC_OCT) {
            stuffedBuf[j++] = ESC_OCT;
            stuffedBuf[j] = ESC_ESC_OCT;
        }
        else
            stuffedBuf[j] = buf[i];
        j++;
    }

    *newSize = j;
    stuffedBuf = realloc(stuffedBuf, j);

    if (stuffedBuf == NULL) return NULL;

    return stuffedBuf;
}

////////////////////////////////////////////////
// LLWRITE
////////////////////////////////////////////////
int llwrite(const unsigned char *buf, int bufSize)
{   
    if(buf == NULL) {
        perror("NULL BUFFER");
        return -1;
    }
    
    int newSize;
    int frameSize= bufSize + 5;
    
    const unsigned char *stuffedBuf = byteStuffing(buf, frameSize, &newSize);

    if(stuffedBuf == NULL) {
        perror("NULL STUFFED BUFFER");
        return -1;
    }
    
    printf("Bytes sent: %d\n", newSize);
    
    unsigned char *frame = (unsigned char *) malloc(newSize + 6);
    if(frame == NULL){
        free((unsigned char *) stuffedBuf);
        perror("NULL FRAME");
        return -1;
    } 

    frame[0] = FLAG;
    frame[1] = ADD_TRANS;
    frame[2] = writeInfoNumbering ? INFO1 : INFO0;
    frame[3] = frame[1] ^ frame[2];
    memcpy(frame + 4, stuffedBuf, newSize);

    writeInfoNumbering = writeInfoNumbering ? 0 : 1;

    unsigned char BCC2 = 0x00;

    for(size_t i = 0; i < bufSize; i++)
        BCC2 ^=  buf[i];

    int posBCC2= newSize + 4;

    frame[posBCC2] = BCC2; 
    
    if(BCC2 == FLAG){
        frame[posBCC2] = ESC_OCT;
        newSize++;
        frame[posBCC2] = ESC_FLAG;
        frame = realloc(frame, newSize + 6);
        frame[newSize + 5] = FLAG;
    }

    else
        frame[newSize + 5] = FLAG;

    enum state current_state = START;
    (void)signal(SIGALRM, alarmHandler);

    struct timeval temp_start;
    gettimeofday(&temp_start, NULL);

    if(writeBytesSerialPort(frame, (newSize + 6)) < 0)
    {
        free(frame);
        perror("Error write send command");
        return -1;
    }

    alarm(connectionParameters.timeout); 
    unsigned char real_A = 0, real_C = 0;

     while(current_state != STOP && alarmCount <= connectionParameters.nRetransmissions) {
        unsigned char current_byte;
        int bytes;
        if((bytes = readByteSerialPort(&current_byte)) < 0){
            free(frame);
            perror("READ ERROR");
            return -1;
        }

        else {
            switch(current_state) {
                case START:
                    real_A= 0;
                    real_C= 0;
                    if(current_byte == FLAG)
                        current_state= FLAG_REC;
                    break;

                case FLAG_REC:
                    if(current_byte == FLAG)
                        continue;
                    if(current_byte == ADD_TRANS || ADD_REC) {
                        current_state= A_REC;
                        real_A= current_byte;
                    }
                    else
                        current_state= START;
                    break;
                case A_REC:
                    if(current_byte == CONTROL_RR0  || current_byte == CONTROL_RR1 || current_byte == CONTROL_REJ0 || current_byte == CONTROL_REJ1) {
                        current_state= C_REC;
                        real_C= current_byte;
                    }
                    else if(current_byte == FLAG) 
                        current_state= FLAG_REC;
                    else 
                        current_state= START;
                    break;
                case C_REC:
                    if(current_byte == (real_A ^ real_C))
                        current_state= BCC_OK;
                    else if(current_byte == FLAG)
                        current_state= FLAG_REC;
                    else
                        current_state= START;
                    break;
                case BCC_OK:
                    if(current_byte == FLAG)
                        current_state= STOP;
                    else
                        current_state= START;
                    break;
                default:
                    current_state= START;
            }
        }

        if(current_state == STOP) 
        {
            if(real_C == CONTROL_REJ0 || real_C == CONTROL_REJ1){
                alarmEnabled = TRUE;
                alarmCount = 0; 
                printf("Received reject; Second try.\n");
            }
            if(real_C == CONTROL_RR0 || real_C == CONTROL_RR1) {
                struct timeval temp_end;
                gettimeofday(&temp_end, NULL);

                statistics.time_send_DATA_REC += get_time_difference(temp_start, temp_end);

                disableAlarm();
                statistics.nFrames++;
                free(frame);
                return bufSize;
            }
        }
        
        if(alarmEnabled)
        {
            alarmEnabled = FALSE;

            if (alarmCount <= connectionParameters.nRetransmissions) {
                if(writeBytesSerialPort(frame, (newSize + 6)) < 0)
                {
                    perror("Error write send command");
                    return -1;
                }
                alarm(connectionParameters.timeout);
            }

            current_state = START;
        }
    }

    disableAlarm();
    free(frame);

    return -1;
}

int byteDestuffing(unsigned char *buf, int bufSize, int *newSize, unsigned char *BCC2Rec)
{ 
    if (buf == NULL || newSize == NULL || BCC2Rec == NULL) {
        perror("Invalid input pointers");
        return -1;
    }

    if (bufSize < 0) 
        return -1;

    unsigned char *writePtr = buf;
    unsigned char *bufferEnd = buf + bufSize;

    for (unsigned char *readPtr = buf; readPtr < bufferEnd; ) {
        if (*readPtr == ESC_OCT) {
            switch (*(readPtr + 1)) {
                case ESC_FLAG:
                    *writePtr++ = FLAG;
                    break;
                case ESC_ESC_OCT:
                    *writePtr++ = ESC_OCT;
                    break;
                default:
                    return -1;
            }
            readPtr += 2; 
        } 

        else {
            *writePtr++ = *readPtr++;
        }
    }

    *BCC2Rec = *(writePtr - 1);
    *newSize = writePtr - buf - 1;
    return 0;
}


////////////////////////////////////////////////
// LLREAD
////////////////////////////////////////////////
int llread(unsigned char *packet)
{
    enum state current_state= START;
    int bufferIndex= 0, newSize= 0;
    unsigned char control, address, controlRec= 0, BCC2= 0x00, BCC2Rec= 0;

    while (current_state != STOP)
    {
        unsigned char current_byte; 
        int bytes= readByteSerialPort(&current_byte);

        if (bytes < 0)
        {
            perror("READ ERROR");
            return -1;
        }

        else {
            switch (current_state)
            {
            case START:
                if(current_byte == FLAG) 
                    current_state= FLAG_REC;
                controlRec= 0;
                bufferIndex= 0;
                break;

            case FLAG_REC:
                if(current_byte == ADD_TRANS) 
                    current_state= A_REC;

                else 
                    current_state= START;
                break;

            case A_REC:
                if(current_byte == INFO0 || current_byte == INFO1){
                    controlRec= current_byte;
                    current_state= C_REC;
                }

                else if(current_byte == FLAG) 
                    current_state= FLAG_REC;

                else 
                    current_state= START;
                break;

            case C_REC:
                if (current_byte == (controlRec ^ ADD_TRANS)) 
                    current_state= DATA_REC; 

                else {
                    statistics.errorFrames++;
                    if(current_byte == FLAG) 
                        current_state= FLAG_REC;
                    
                    else 
                        current_state= START;
                }
                break;

            case DATA_REC: 
                if(current_byte == FLAG) {
                    if (byteDestuffing(packet, bufferIndex, &newSize, &BCC2Rec)) 
                        return -1;

                    for (size_t i = 0; i < newSize; i++) 
                        	BCC2 ^= packet[i];

                    if (BCC2 == BCC2Rec) {
                        control= (controlRec == INFO0)? CONTROL_RR1 : CONTROL_RR0;
                        address= ADD_TRANS;
                    }

                    else {
                        if ((readInfoNumbering == 0 && controlRec == INFO1) || (readInfoNumbering == 1 && controlRec == INFO0)) {
                            control= (controlRec == INFO0)? CONTROL_RR1 : CONTROL_RR0;
                            address= ADD_TRANS;
                        }

                        else {
                            control= (controlRec == INFO0) ? CONTROL_REJ0 : CONTROL_REJ1;
                            address= ADD_TRANS;
                        }
                    }

                    if (sendPacket(address, control)) 
                        return -1;

                    if (control == CONTROL_REJ0 || control == CONTROL_REJ1) {
                        statistics.errorFrames++;
                        break;
                    }

                    if ((readInfoNumbering == 0 && controlRec == INFO0) || (readInfoNumbering == 1 && controlRec == INFO1)) {
                        readInfoNumbering = readInfoNumbering ? 0 : 1;
                        printf("Bytes received: %d\n", newSize);
                        statistics.nFrames++;
                        return newSize;
                    }

                    printf("Received duplicate\n");
                } 
                else 
                    packet[bufferIndex++]= current_byte;
                break;
                
            default:
                current_state= START;
            }
        }
    }
    return -1;
}

////////////////////////////////////////////////
// LLCLOSE
////////////////////////////////////////////////
int llclose(int showStatistics)
{
    unsigned char current_byte;
    int retransmissions = connectionParameters.nRetransmissions;
    int timeout = connectionParameters.timeout;
    enum state current_state = START;
    alarmCount = 0;

    (void) signal(SIGALRM, alarmHandler);

    if (connectionParameters.role == LlTx) {
        while (retransmissions > 0 && current_state != STOP) {
            sendPacket(ADD_TRANS, CONTROL_DISC);
            alarm(timeout);
            alarmEnabled = FALSE;
        
            while (alarmEnabled == FALSE && current_state != STOP) {
                if (readByteSerialPort(&current_byte) > 0) {
                    switch (current_state) {
                        case START:
                            if (current_byte == FLAG) current_state = FLAG_REC;
                            break;
                        case FLAG_REC:
                            if (current_byte == A_REC) current_state = A_REC;
                            else if (current_byte != FLAG) current_state = START;
                            break;
                        case A_REC:
                            if (current_byte == CONTROL_DISC) current_state = C_REC;
                            else if (current_byte == FLAG) current_state = FLAG_REC;
                            else current_state = START;
                            break;
                        case C_REC:
                            if (current_byte == (A_REC ^ CONTROL_DISC)) current_state = BCC_OK;
                            else if (current_byte == FLAG) current_state = FLAG_REC;
                            else current_state = START;
                            break;
                        case BCC_OK:
                            if (current_byte == FLAG) current_state = STOP;
                            else current_state = START;
                            break;
                        default:
                            break;
                    }
                }
            }
            
            if (current_state == STOP) break;
            retransmissions--;
        }
        
        if (current_state != STOP) return -1;
        
        sendPacket(ADD_TRANS, CONTROL_UA);
    } 
    
    else if (connectionParameters.role == LlRx) {
        receivePacket(ADD_TRANS, CONTROL_DISC);
        sendPacket(A_REC, CONTROL_DISC);
        
        if (receivePacket(A_REC, CONTROL_UA) < 0) {
            perror("Erro: Recepção do UA falhou.");
            return -1;
        }
    }

    disableAlarm();

    if (showStatistics) {
        printf("\n---------- Statistics: ----------\n");
        printf("Good frames sent/received: %u\n", statistics.nFrames);
        printf("Error frames encountered: %u\n", statistics.errorFrames);
        printf("Time spent sending frames: %.6f seconds\n", statistics.time_send_DATA_REC);
        printf("------------------------------------\n");
    }

    int clstat = closeSerialPort();
    return clstat;
}