// Application layer protocol implementation

#include "application_layer.h"
#include "link_layer.h"
#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdint.h>
#include <stddef.h>
#include <unistd.h>

#define DATA 2
#define C_START 1
#define C_END 3

#define fileSizeType 0
#define fileNameType 1

enum state
{
    START_REC,
    CONTROL_REC,
    STOP_REC
};

typedef struct
{
    size_t fileSize;
    char *fileName;
    size_t bytesRead;
} FileInfo;

enum state currentState = START_REC;
FileInfo fileStats = {0, "", 0};

int sendDataPacket(size_t size, size_t seqNum, unsigned char* data){

    if(data == NULL){
        perror("NULL DATA");
        return -1;
    }

    unsigned char* packet = (unsigned char *)malloc(size + 4);
    
    if(packet == NULL){
        perror("NULL CREATED PACKET");
        return -1;
    }

    packet[0] = DATA;
    packet[1] = seqNum;
    packet[2] = (size >> 8);
    packet[3] = size & 0xFF;

    memcpy(packet + 4, data, size);

    if(llwrite(packet, size + 4) < 0){
        free(packet);
        return -1;
    }    

    free(packet);
    return 0;
}

unsigned char* receiveDataPacket(unsigned char* packet, size_t seqNum, size_t* newSize) {
    
    if(packet == NULL) {
        perror("NULL DATA PACKET");
        return NULL;
    }

    if(newSize == NULL) {
        perror("NULL NEW SIZE");
        return NULL;
    }

    if(packet[0] != DATA) {
        perror("NOT A DATA PACKET BEING RECEIVED");
        return NULL;
    }

    if(packet[1] != seqNum) {
        perror("SEQUENCE NUMBER NOT VALID");
        return NULL;
    }

    *newSize = packet[3] + (packet[2] << 8);

    return packet + 4;
}

unsigned char *itouchar(size_t value, unsigned char *size) {
    if (size == NULL) return NULL;

    size_t length = 0;
    size_t tmp_value = value;
    
    do {
        length++;
        tmp_value >>= 8;
    } while (tmp_value);

    unsigned char *bytes = malloc(length);
    
    if (bytes == NULL) return NULL;

    for (size_t i = 0; i < length; i++) {
        bytes[length - 1 - i] = (unsigned char)(value & 0xFF);
        value >>= 8; 
    }

    *size = length;
    return bytes;
}

size_t uchartoi(unsigned char n, unsigned char *numbers) {
    if (numbers == NULL) {
        return 0;
    }

    size_t value = 0;
    unsigned char *ptr = numbers;

    for (int i = 0; i < n; i++)
        value = (value << 8) | *(ptr++);

    return value;
}

int sendControlPacket(int control, const char* filename, size_t fileSize) {
    if(filename == NULL) {
        perror("NULL FILENAME");
        return -1;
    }

    unsigned char L1 = 0;
    unsigned char* V1 = itouchar(fileSize, &L1); // Fix V1 type

    if (V1 == NULL) {
        perror("Conversion error for fileSize");
        return -1;
    }

    unsigned char L2 = strlen(filename);

    unsigned char* packet = malloc(5 + L1 + L2);
    if (packet == NULL) {
        perror("NULL PACKET");
        free(V1);
        return -1;
    }

    packet[0] = control;
    packet[1] = fileSizeType;
    packet[2] = L1;

    memcpy(packet + 3, V1, L1);
    int i = 3 + L1;

    packet[i++] = fileNameType;
    packet[i++] = L2;

    memcpy(packet + i, filename, L2);
    i += L2;

    if (llwrite(packet, i) < 0) {
        free(packet);
        free(V1);
        return -1;
    }

    free(packet);
    free(V1);
    return 0;
}

int readControlPacket(unsigned char *buff) {
    if (buff == NULL)
        return -1;

    size_t bufferIndex = 0; 
    size_t fileSize = 0;
    char *fileName = NULL;
    unsigned char *V1 = NULL;
    int result = 0;

    if (buff[bufferIndex] == C_START) 
        currentState = CONTROL_REC;

    else if (buff[bufferIndex] == C_END)
        currentState = STOP_REC;

    else
        return -1;

    bufferIndex++;

    if (buff[bufferIndex++] != fileSizeType) 
        return -1;

    unsigned char L1 = buff[bufferIndex++];
    V1 = malloc(L1);
    
    if (V1 == NULL) 
        return -1;

    memcpy(V1, buff + bufferIndex, L1);
    bufferIndex += L1;
    fileSize = uchartoi(L1, V1);
    free(V1);

    if (buff[bufferIndex++] != fileNameType) 
        return -1;

    unsigned char L2 = buff[bufferIndex++];
    fileName = malloc(L2 + 1);

    if (fileName == NULL) 
        return -1;

    memcpy(fileName, buff + bufferIndex, L2);
    fileName[L2] = '\0';

    if (buff[0] == C_START) {
        fileStats.fileSize = fileSize;
        fileStats.fileName = fileName;
        printf("[INFO] Started receiving file: '%s'\n", fileName);
    }

    else if (buff[0] == C_END) {
        if (fileStats.fileSize != fileStats.bytesRead) {
            perror("Number of bytes read doesn't match size of file\n");
            result = -1;
        }

        if (strcmp(fileStats.fileName, fileName) != 0) {
            perror("Names of file given in the start and end packets don't match\n");
            result = -1;
        }

        printf("[INFO] Finished receiving file: '%s'\n", fileName);
    }

    free(fileName);
    return result;
}

void applicationLayer(const char *serialPort, const char *role, int baudRate,
                   int nTries, int timeout, const char *filename)
{
    if (serialPort == NULL)
    {
        perror("SERIAL PORT ARGUMENT MUST NOT BE NULL");
        return;
    }

    if (role == NULL)
    {
        perror("ROLE ARGUMENT MUST NOT BE NULL");
        return;
    }

    if (filename == NULL)
    {
        perror("FILENAME ARGUMENT MUST NOT BE NULL");
        return;
    }


    LinkLayer connectionParameters;
    strcpy(connectionParameters.serialPort, serialPort);
    connectionParameters.role = strcmp(role, "tx") ? LlRx : LlTx;
    connectionParameters.baudRate = baudRate;
    connectionParameters.nRetransmissions = nTries;
    connectionParameters.timeout = timeout;

    size_t seqNum = 0;
    unsigned char *buffer = malloc(MAX_PAYLOAD_SIZE + 20);

    if (llopen(connectionParameters) < 0) {
        perror("Failed to open connection.");
        return;
    }

    if (connectionParameters.role == LlTx) {
        FILE *file = fopen(filename, "rb");
        if (file == NULL) {
            perror("File error: Unable to open file for reading.");
            llclose(FALSE);
            return;
        }

        fseek(file, 0, SEEK_END);
        size_t fileSize = ftell(file);
        rewind(file);
        
        if (buffer == NULL) {
            perror("Buffer allocation error.");
            fclose(file);
            llclose(FALSE);
            return;
        }

        if (sendControlPacket(C_START, filename, fileSize) < 0) {
            perror("Transmission error: Failed to send START packet.");
            free(buffer);
            fclose(file);
            llclose(FALSE);
            return;
        }

        size_t bytesRead;
        while ((bytesRead = fread(buffer, 1, MAX_PAYLOAD_SIZE, file)) > 0) {
            if (sendDataPacket(bytesRead, seqNum++, buffer) < 0) {
                perror("Transmission error: Failed to send DATA packet.");
                free(buffer);
                fclose(file);
                llclose(FALSE);
                return;
            }
            if (seqNum == 100) seqNum = 0;
        }

        if (sendControlPacket(C_END, filename, fileSize) < 0) {
            perror("Transmission error: Failed to send END packet.");
            free(buffer);
            fclose(file);
            llclose(FALSE);
            return;
        }

        free(buffer);
        fclose(file);
    } else if (connectionParameters.role == LlRx) {
        FILE *newFile = fopen(filename, "wb");
        if (newFile == NULL) {
            perror("File error: Unable to open file for writing.");
            llclose(FALSE);
            return;
        }

        if (buffer == NULL) {
            perror("Buffer allocation error.");
            fclose(newFile);
            llclose(FALSE);
            return;
        }

        while (currentState != STOP_REC) {
            ssize_t bytesReceived = llread(buffer);
            if (bytesReceived < 0) {
                perror("Link layer error: Failed to read from link.");
                free(buffer);
                fclose(newFile);
                llclose(FALSE);
                return;
            }

            if (buffer[0] == C_START || buffer[0] == C_END) {
                if (readControlPacket(buffer) < 0) {
                    perror("Packet error: Failed to read control packet.");
                    free(buffer);
                    fclose(newFile);
                    llclose(FALSE);
                    return;
                }

            } else if (buffer[0] == DATA) {
                size_t dataSize= 0;
                unsigned char *data = receiveDataPacket(buffer, seqNum, &dataSize);
                if (data == NULL) {
                    perror("Packet error: Failed to read data packet.");
                    free(buffer);
                    fclose(newFile);
                    llclose(FALSE);
                    return;
                }
                fwrite(data, 1, dataSize, newFile);
                fileStats.bytesRead += dataSize;
                seqNum++;
            }
        }

        free(buffer);
        fclose(newFile);
    }

    if (llclose(TRUE) < 0) {
        perror("Link layer error: Failed to close connection.");
    }
}