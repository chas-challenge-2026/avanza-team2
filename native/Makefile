# PoC bridge for #30.
CC       ?= gcc
CFLAGS   ?= -O2 -fPIC -Wall -Wextra
TARGET   := libpoc.so
TEST_BIN := test_poc

.PHONY: all test clean

all: $(TARGET)

$(TARGET): bridge.c bridge.h
	$(CC) $(CFLAGS) -shared -o $(TARGET) bridge.c

test: $(TARGET) $(TEST_BIN)
	./$(TEST_BIN)

$(TEST_BIN): test_poc.c
	$(CC) $(CFLAGS) -o $(TEST_BIN) test_poc.c -ldl

clean:
	rm -f $(TARGET) $(TEST_BIN)
