# avanza-2-native/risk Makefile

ROOT_DIR := ..

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
	CC := clang
else
	CC := gcc
endif

BUILD_DIR := build

# Binary name
BIN := $(BUILD_DIR)/risk

# ----- Local source files -----
SRC_DIR := src
SRC := $(shell find -L $(SRC_DIR) -type f -name '*.c')

# Linked libraries (directories containing 'src' and 'include')
LIBS := common/utils common/modules common/config

# ----- Library Source Processing -----
LIB_SRC_DIRS := $(foreach lib,$(LIBS),$(ROOT_DIR)/$(lib)/src)
LIB_SRC := $(shell find -L $(LIB_SRC_DIRS) -type f -name '*.c' 2>/dev/null)
LIB_INCLUDES := $(foreach lib,$(LIBS),-I$(ROOT_DIR)/$(lib)/include)

# Compilation flags
CFLAGS := -std=c11 -MMD -MP -Wall -Wextra -Wfatal-errors -g -lm \
	-Iinclude $(LIB_INCLUDES)

## DETECT AVX version and add flags accordingly
# Default to auto-detect if not specified
AVX_LEVEL ?= auto

ifeq ($(AVX_LEVEL),auto)
    ifeq ($(shell echo 'int main() { return 0; }' | $(CC) -mavx512f -x c - -o /dev/null 2>/dev/null && echo yes), yes)
        CFLAGS += -mavx512f -mavx512bw -mavx512vl
    else ifeq ($(shell echo 'int main() { return 0; }' | $(CC) -mavx2 -x c - -o /dev/null 2>/dev/null && echo yes), yes)
        CFLAGS += -mavx2
    else ifeq ($(shell echo 'int main() { return 0; }' | $(CC) -mavx -x c - -o /dev/null 2>/dev/null && echo yes), yes)
        CFLAGS += -mavx
    endif
else ifeq ($(AVX_LEVEL),avx512)
    CFLAGS += -mavx512f -mavx512bw -mavx512vl
else ifeq ($(AVX_LEVEL),avx2)
    CFLAGS += -mavx2
else ifeq ($(AVX_LEVEL),avx)
    CFLAGS += -mavx
else ifeq ($(AVX_LEVEL),none)
    # No SIMD flags
endif

ASAN_FLAGS := -O1 -g3 -fno-omit-frame-pointer -fsanitize=address,undefined

# For MacOS
ifeq ($(UNAME_S),Darwin)
	LDFLAGS := -flto -lm
else
	LDFLAGS := -flto -Wl,--gc-sections -lm
endif

# Map local .c → .o
OBJ_LOCAL := $(patsubst $(SRC_DIR)/%.c,$(BUILD_DIR)/$(SRC_DIR)/%.o,$(SRC))

# Map library .c → .o
# Converts: ../common/src/bar.c -> build/common/src/bar.o
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
	@$(CC) $(LDFLAGS) $(OBJ) -o $@

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

# Rule for local source files
$(BUILD_DIR)/$(SRC_DIR)/%.o: $(SRC_DIR)/%.c
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
