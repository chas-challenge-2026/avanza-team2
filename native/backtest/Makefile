# avanza-2-native/backtest Makefile

ROOT_DIR := ..

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
	CC := clang
else
	CC := gcc
endif

BUILD_DIR := build

# Binary name
BIN := $(BUILD_DIR)/backtest_tests

# ----- Local source files -----
# src/ = library code (no main()), tests/ = the test runner's main().
# SRC_DIR intentionally holds two paths; find(1) accepts multiple starting paths.
SRC_DIR := src tests
SRC := $(shell find -L $(SRC_DIR) -type f -name '*.c')

# Linked libraries (directories containing 'src' and 'include')
LIBS := common/tests

# ----- Library Source Processing -----
LIB_SRC_DIRS := $(foreach lib,$(LIBS),$(ROOT_DIR)/$(lib)/src)
LIB_SRC := $(shell find -L $(LIB_SRC_DIRS) -type f -name '*.c' 2>/dev/null)
LIB_INCLUDES := $(foreach lib,$(LIBS),-I$(ROOT_DIR)/$(lib)/include)

# Compilation flags
CFLAGS := -std=c11 -MMD -MP -Wall -Wextra -Wfatal-errors -g -lm \
	-Iinclude $(LIB_INCLUDES)

ASAN_FLAGS := -O1 -g3 -fno-omit-frame-pointer -fsanitize=address,undefined

# For MacOS
ifeq ($(UNAME_S),Darwin)
	LDFLAGS := -flto -lm
else
	LDFLAGS := -flto -Wl,--gc-sections -lm
endif

# Map local .c -> .o (prefix-preserving, works for both src/ and tests/)
OBJ_LOCAL := $(patsubst %.c,$(BUILD_DIR)/%.o,$(SRC))

# Map library .c -> .o
# Converts: ../common/tests/src/test_data.c -> build/common/tests/src/test_data.o
OBJ_LIB := $(foreach src,$(LIB_SRC),$(BUILD_DIR)/$(patsubst $(ROOT_DIR)/%,%,$(src:.c=.o)))

# Combine all objects
OBJ := $(OBJ_LOCAL) $(OBJ_LIB)

# Dependency files (.d) for local and library sources
DEP_LOCAL := $(OBJ_LOCAL:.o=.d)
DEP_LIB := $(OBJ_LIB:.o=.d)
DEP := $(DEP_LOCAL) $(DEP_LIB)

# ==================== Recipes ====================

all: $(BIN)
	@echo "Build complete."

$(BIN): $(OBJ)
	@echo "Linking..."
	@mkdir -p $(dir $@)
	@$(CC) $(OBJ) -o $@ $(LDFLAGS)

asan: clean
	@echo "Building ASan binary..."
	@$(MAKE) CC=$(CC) CFLAGS="$(CFLAGS) $(ASAN_FLAGS)" LDFLAGS="$(LDFLAGS) $(ASAN_FLAGS)" all

profile: clean
	@echo "Building profiling binary..."
	@$(MAKE) \
		CC=$(CC) \
		CFLAGS="$(CFLAGS) -pg -O0 -g" \
		LDFLAGS="-Wl,--gc-sections -pg" \
		all

# Rule for local source files (src/ and tests/ both matched generically)
$(BUILD_DIR)/%.o: %.c
	@echo "Compiling $<..."
	@mkdir -p $(dir $@)
	@$(CC) $(CFLAGS) -c $< -o $@

# Rule for library source files
$(BUILD_DIR)/%.o: $(ROOT_DIR)/%.c
	@echo "Compiling lib $<..."
	@mkdir -p $(dir $@)
	@$(CC) $(CFLAGS) -c $< -o $@

run: $(BIN)
	@echo "Running $(BIN)..."
	@./$(BIN)

clean:
	@echo "Cleaning build files..."
	@rm -rf $(BUILD_DIR)
	@echo "Clean complete."

valgrind: $(BIN)
	@echo "Debugging $< using valgrind..."
	@valgrind -s --leak-check=full --show-leak-kinds=all --track-origins=yes ./$(BIN)

gdb: $(BIN)
	@echo "Debugging $< using gdb..."
	@gdb ./$(BIN)

print:
	@echo "Local Source files: $(SRC)"
	@echo "Library Source files: $(LIB_SRC)"
	@echo "Total Object files: $(OBJ)"
	@echo "Dependency files: $(DEP)"

-include $(DEP)

.PHONY: all run clean print gdb valgrind asan profile
