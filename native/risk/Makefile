# avanza-2-native/risk Makefile

ROOT_DIR := ..

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
	CC := clang
else
	CC := gcc
endif

BUILD_DIR := build

# Binary names
BIN := $(BUILD_DIR)/librisk.a
TEST_BIN := $(BUILD_DIR)/risk_test

# ----- Local source files -----
SRC_DIR 		 := src
TEST_SRC_DIR := tests
MAIN_SRC 		 := $(SRC_DIR)/risk.c

SRC 		 := $(shell find -L $(SRC_DIR) -type f -name '*.c')
TEST_SRC := $(filter-out $(MAIN_SRC), \
						$(SRC)) $(shell find -L $(TEST_SRC_DIR) -type f -name '*.c')

# Linked libraries (directories containing 'src' and 'include')
LIBS 			:= common/utils common/modules common/config
TEST_LIBS := common/tests

# ----- Library Source Processing -----
LIB_SRC_DIRS := $(foreach lib,$(LIBS),$(ROOT_DIR)/$(lib)/src)
LIB_SRC 		 := $(shell find -L $(LIB_SRC_DIRS) -type f -name '*.c' 2>/dev/null)
LIB_INCLUDES := $(foreach lib,$(LIBS),-I$(ROOT_DIR)/$(lib)/include)

# Include cJSON dependency
LIB_SRC      += $(ROOT_DIR)/libs/cJSON/cJSON.c $(ROOT_DIR)/libs/cJSON/cJSON_Utils.c
LIB_INCLUDES += -I$(ROOT_DIR)/libs/cJSON

TEST_LIB_SRC_DIRS := $(foreach lib,$(TEST_LIBS),$(ROOT_DIR)/$(lib)/src)
TEST_LIB_SRC      := $(shell find -L $(TEST_LIB_SRC_DIRS) -type f -name '*.c' 2>/dev/null)
TEST_LIB_INCLUDES := $(foreach lib,$(TEST_LIBS),-I$(ROOT_DIR)/$(lib)/include)

# Compilation flags
CFLAGS := -std=c11 -MMD -MP -Wall -Wextra -Wfatal-errors -g \
					-Iinclude $(LIB_INCLUDES) 
TEST_CFLAGS := $(CFLAGS) -Iinclude $(TEST_LIB_INCLUDES) 

## DETECT AVX version and add flags accordingly
# Default to auto-detect if not specified
# Can override this by setting AVX_LEVEL, one of:
# - avx512 (includes bw and vl)
# - avx2 (256)
AVX_LEVEL ?= auto

ifeq ($(AVX_LEVEL),auto)
	ifeq ($(shell echo 'int main() { return 0; }' | $(CC) -mavx512bw -x c - -o /dev/null 2>/dev/null && echo yes), yes)
		CFLAGS += -mavx512f -mavx512vl  -mavx512bw
	else ifeq ($(shell echo 'int main() { return 0; }' | $(CC) -mavx512vl -x c - -o /dev/null 2>/dev/null && echo yes), yes)
		CFLAGS += -mavx512f -mavx512vl
	else ifeq ($(shell echo 'int main() { return 0; }' | $(CC) -mavx512f -x c - -o /dev/null 2>/dev/null && echo yes), yes)
		CFLAGS += -mavx512f
	else ifeq ($(shell echo 'int main() { return 0; }' | $(CC) -mavx2 -x c - -o /dev/null 2>/dev/null && echo yes), yes)
		CFLAGS += -mavx2
	else ifeq ($(shell echo 'int main() { return 0; }' | $(CC) -mavx -x c - -o /dev/null 2>/dev/null && echo yes), yes)
		CFLAGS += -mavx
	endif
else ifeq ($(AVX_LEVEL),avx512bw)
	CFLAGS += -mavx512f -mavx512vl -mavx512bw 
else ifeq ($(AVX_LEVEL),avx512vl)
	CFLAGS += -mavx512f -mavx512vl
else ifeq ($(AVX_LEVEL),avx512)
	CFLAGS += -mavx512f
else ifeq ($(AVX_LEVEL),avx2)
	CFLAGS += -mavx2
else ifeq ($(AVX_LEVEL),avx)
	CFLAGS += -mavx
else ifeq ($(AVX_LEVEL),sse2)
	CFLAGS += -msse2
else ifeq ($(AVX_LEVEL),sse)
	CFLAGS += -msse
else ifeq ($(AVX_LEVEL),none)
	# No SIMD flags
endif

ASAN_CFLAGS := -O1 -g3 -fno-omit-frame-pointer -fsanitize=address,undefined

LDFLAGS := -flto -Wl,--gc-sections -lm -lcurl

# Map local .c → .o
OBJ_LOCAL := $(patsubst $(SRC_DIR)/%.c,$(BUILD_DIR)/$(SRC_DIR)/%.o,$(SRC))
OBJ_TEST_LOCAL := $(patsubst $(SRC_DIR)/%.c,$(BUILD_DIR)/$(SRC_DIR)/%.o, \
									$(filter-out $(MAIN_SRC), $(SRC)))

OBJ_LIB      := $(foreach src,$(LIB_SRC),$(BUILD_DIR)/$(patsubst $(ROOT_DIR)/%,%,$(src:.c=.o)))
OBJ_TEST_LIB := $(foreach src,$(TEST_LIB_SRC),$(BUILD_DIR)/$(patsubst $(ROOT_DIR)/%,%,$(src:.c=.o)))

OBJ_TEST := $(patsubst $(TEST_SRC_DIR)/%.c,$(BUILD_DIR)/$(TEST_SRC_DIR)/%.o,$(shell find -L $(TEST_SRC_DIR) -type f -name '*.c'))
# OBJ_TEST := $(patsubst $(TEST_SRC_DIR)/%.c,$(BUILD_DIR)/$(TEST_SRC_DIR)/%.o,$(TEST_SRC))

# Combine all objects
OBJ := $(OBJ_LOCAL) $(OBJ_LIB)
TEST_OBJ := $(OBJ_TEST_LOCAL) $(OBJ_LIB) $(OBJ_TEST) $(OBJ_TEST_LIB)

# Dependency files (.d) for local and library sources
DEP := $(OBJ_LOCAL:.o=.d) $(OBJ_LIB:.o=.d)
TEST_DEP := $(OBJ_TEST_LOCAL:.o=.d) $(OBJ_LIB:.o=.d)

# ==================== Recipes ====================

all: $(BIN)
	@echo "Build complete."

$(BIN): $(OBJ)
	@echo "Archiving as $(BIN)..."
	@mkdir -p $(dir $@)
	@ar rcs $@ $(OBJ)
	@ranlib $@  # Optional: improves indexing for large archives

asan: clean
	@echo "Building ASan binary..."
	@$(MAKE) CC=$(CC) CFLAGS="$(CFLAGS) $(ASAN_CFLAGS)" LDFLAGS="$(LDFLAGS) $(ASAN_FLAGS)" all

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

# Rule for test library source files
$(OBJ_TEST_LIB): $(BUILD_DIR)/%.o: $(ROOT_DIR)/%.c
	@echo "Compiling test lib $<..."
	@mkdir -p $(dir $@)
	@$(CC) $(TEST_CFLAGS) -c $< -o $@

# Rule for library source files
$(BUILD_DIR)/%.o: $(ROOT_DIR)/%.c
	@echo "Compiling lib $<..."
	@mkdir -p $(dir $@)
	@$(CC) $(CFLAGS) -c $< -o $@

# Rule for test source files
$(BUILD_DIR)/$(TEST_SRC_DIR)/%.o: $(TEST_SRC_DIR)/%.c
	@echo "Compiling test $<..."
	@mkdir -p $(dir $@)
	@$(CC) $(TEST_CFLAGS) -c $< -o $@

# Link test binary
$(TEST_BIN): $(TEST_OBJ)
	@echo "Linking test..."
	@mkdir -p $(dir $@)
	@$(CC) $(TEST_OBJ) $(LDFLAGS) -o $@

clean:
	@echo "Cleaning build files..."
	@rm -rf $(BUILD_DIR)
	@echo "Clean complete."

print:
	@echo "Local Source files: $(SRC)"
	@echo "Library Source files: $(LIB_SRC)"
	@echo "Total Object files: $(OBJ)"
	@echo "Dependency files: $(DEP)"
	@echo "CFLAGS: $(CFLAGS)"
	@echo "TEST_CFLAGS: $(TEST_CFLAGS)"
	@echo "TEST_SRC: $(TEST_SRC)"
	@echo "TEST_OBJ: $(TEST_OBJ)"

-include $(DEP)

# ---- Test Targets ----

test: $(TEST_BIN)
	@echo "Building tests..."
	@./$(TEST_BIN)

test-run: $(TEST_BIN)
	@echo "Building and running tests..."
	@./$(TEST_BIN)

test-gdb: $(TEST_BIN)
	@echo "Debugging test using gdb..."
	@gdb ./$(TEST_BIN)

# WARNING: valgrind has poor AVX-512 support, use AVX_LEVEL=avx2 or less
test-valgrind: $(TEST_BIN)
	@echo "Debugging test using valgrind..."
	@valgrind -s --leak-check=full --show-leak-kinds=all --track-origins=yes ./$(TEST_BIN)

.PHONY: all run clean print gdb valgrind asan profile
