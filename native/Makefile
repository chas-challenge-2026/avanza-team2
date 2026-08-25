# PoC bridge for #30.
CC       ?= gcc
CFLAGS   ?= -O2 -fPIC -Wall -Wextra
TARGET   := libpoc.so

.PHONY: all clean

all: $(TARGET)

$(TARGET): bridge.c bridge.h
	$(CC) $(CFLAGS) -shared -o $(TARGET) bridge.c

clean:
	rm -f $(TARGET)
