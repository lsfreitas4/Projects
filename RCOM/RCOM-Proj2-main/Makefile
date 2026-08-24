CC = gcc
CFLAGS = -Wall -Wextra -Werror
BIN = bin
SRC = src
INCLUDE = include

URL = ftp://ftp.up.pt/pub/gnu/emacs/elisp-manual-21-2.8.tar.gz

TARGET = $(BIN)/download
SOURCES = main.c $(SRC)/*.c

# Targets
.PHONY: all clean run

all: $(TARGET)

$(TARGET): $(SOURCES)
	mkdir -p $(BIN)
	$(CC) $(CFLAGS) -I$(INCLUDE) -o $@ $^

run: $(BIN)/download
	./$(BIN)/download $(URL)

clean:
	rm -f $(TARGET)
