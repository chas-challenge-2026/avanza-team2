# avanza-2-native/fx Makefile

ROOT_DIR := ..

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
	CC  := clang
	CXX := clang++
else
	CC  := gcc
	CXX := g++
endif

BUILD_DIR := build

# Binary name
BIN := $(BUILD_DIR)/fx

# ----- Local source files (C++) -----
SRC_DIR := src
SRC := $(shell find -L $(SRC_DIR) -type f -name '*.cpp')

# Linked libraries (plain C, compiled as C and linked in)
LIBS := common/modules

# ----- Library Source Processing -----
LIB_SRC_DIRS := $(foreach lib,$(LIBS),$(ROOT_DIR)/$(lib)/src)
LIB_SRC      := $(shell find -L $(LIB_SRC_DIRS) -type f -name '*.c' 2>/dev/null)
LIB_INCLUDES := $(foreach lib,$(LIBS),-I$(ROOT_DIR)/$(lib)/include)

WARN := -Wall -Wextra -Wfatal-errors

# Local sources build as C++17, shared helpers stay C11
CXXFLAGS := -std=c++17 -MMD -MP $(WARN) -g -Iinclude $(LIB_INCLUDES)
CFLAGS   := -std=c11   -MMD -MP $(WARN) -g $(LIB_INCLUDES)

ASAN_FLAGS := -O1 -g3 -fno-omit-frame-pointer -fsanitize=address,undefined

# libcurl is pulled in via common/modules/curl_helper
ifeq ($(UNAME_S),Darwin)
	LDFLAGS := -flto -lcurl
else
	LDFLAGS := -flto -Wl,--gc-sections -lcurl
endif

# Map local .cpp → .o
OBJ_LOCAL := $(patsubst $(SRC_DIR)/%.cpp,$(BUILD_DIR)/$(SRC_DIR)/%.o,$(SRC))

# Map library .c → .o
# Converts: ../common/modules/src/bar.c -> build/common/modules/src/bar.o
OBJ_LIB := $(foreach src,$(LIB_SRC),$(BUILD_DIR)/$(patsubst $(ROOT_DIR)/%,%,$(src:.c=.o)))

# Combine all objects
OBJ := $(OBJ_LOCAL) $(OBJ_LIB)

# Dependency files (.d)
DEP := $(OBJ:.o=.d)

# ==================== Recipes ====================

all: $(BIN)
	@echo "Build complete."

# Link with the C++ front end so the standard library is pulled in
$(BIN): $(OBJ)
	@echo "Linking..."
	@mkdir -p $(dir $@)
	@$(CXX) $(OBJ) $(LDFLAGS) -o $@

asan: clean
	@echo "Building ASan binary..."
	@$(MAKE) CXXFLAGS="$(CXXFLAGS) $(ASAN_FLAGS)" CFLAGS="$(CFLAGS) $(ASAN_FLAGS)" \
		LDFLAGS="$(LDFLAGS) $(ASAN_FLAGS)" all

profile: clean
	@echo "Building profiling binary..."
	@$(MAKE) \
		CXXFLAGS="$(CXXFLAGS) -pg -O0 -g" \
		CFLAGS="$(CFLAGS) -pg -O0 -g" \
		LDFLAGS="-Wl,--gc-sections -pg -lcurl" \
		all

# Rule for local C++ source files
$(BUILD_DIR)/$(SRC_DIR)/%.o: $(SRC_DIR)/%.cpp
	@echo "Compiling $<..."
	@mkdir -p $(dir $@)
	@$(CXX) $(CXXFLAGS) -c $< -o $@

# Rule for shared C library source files
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
