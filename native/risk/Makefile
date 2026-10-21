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
SO := $(BUILD_DIR)/librisk.so
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
SO_CFLAGS := $(CFLAGS) -fPIC

## DETECT AVX version and add flags accordingly
# Default to auto-detect if not specified
# Can override this by passing AVX_LEVEL flag
AVX_LEVEL ?= auto

ifeq ($(AVX_LEVEL),auto)

	# Detect AVX512 support by checking CPU flags
	ifeq ($(UNAME_S),Darwin)
		# macOS: use sysctl to get cpu flags (returns 1 if supported)
		HAS_AVX512F := $(shell sysctl -n hw.optional.avx512f 2>/dev/null || echo 0)
		HAS_AVX512BW := $(shell sysctl -n hw.optional.avx512bw 2>/dev/null || echo 0)
		HAS_AVX512VL := $(shell sysctl -n hw.optional.avx512vl 2>/dev/null || echo 0)
		HAS_AVX2 := $(shell sysctl -n hw.optional.avx2 2>/dev/null || echo 0)
		HAS_AVX := $(shell sysctl -n hw.optional.avx 2>/dev/null || echo 0)
		HAS_SSE2 := $(shell sysctl -n hw.optional.sse2 2>/dev/null || echo 0)
		HAS_SSE := $(shell sysctl -n hw.optional.sse 2>/dev/null || echo 0)
	else
		# Linux: use lscpu to get cpu flags
		CPU_FLAGS := $(shell lscpu 2>/dev/null | grep "Flags" | cut -d':' -f2)
		HAS_AVX512F := $(if $(findstring avx512f,$(CPU_FLAGS)),yes,no)
		HAS_AVX512BW := $(if $(findstring avx512bw,$(CPU_FLAGS)),yes,no)
		HAS_AVX512VL := $(if $(findstring avx512vl,$(CPU_FLAGS)),yes,no)
		HAS_AVX2 := $(if $(findstring avx2,$(CPU_FLAGS)),yes,no)
		HAS_AVX := $(if $(findstring avx,$(CPU_FLAGS)),yes,no)
		HAS_SSE2 := $(if $(findstring sse2,$(CPU_FLAGS)),yes,no)
		HAS_SSE := $(if $(findstring sse,$(CPU_FLAGS)),yes,no)
	endif

	# Set AVX_LEVEL based on detection
	ifeq ($(HAS_AVX512BW),yes)
		AVX_LEVEL = avx512bw
	else ifeq ($(HAS_AVX512VL),yes)
		AVX_LEVEL = avx512vl
	else ifeq ($(HAS_AVX512F),yes)
		AVX_LEVEL = avx512
	else ifeq ($(HAS_AVX2),yes)
		AVX_LEVEL = avx2
	else ifeq ($(HAS_AVX),yes)
		AVX_LEVEL = avx
	else ifeq ($(HAS_SSE2),yes)
		AVX_LEVEL = see2
	else ifeq ($(HAS_SSE),yes)
		AVX_LEVEL = see
	else
		AVX_LEVEL = none
	endif

endif

$(info CPU AVX_LEVEL set: $(AVX_LEVEL))

# Set compiler flags based on AVX level
ifeq ($(AVX_LEVEL),avx512bw)
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

# SO_OBJ := $(patsubst %.o, so/%.o, $(OBJ))
SO_OBJ_LOCAL := $(patsubst $(SRC_DIR)/%.c,$(BUILD_DIR)/so/$(SRC_DIR)/%.o,$(SRC))
SO_OBJ_LIB   := $(foreach src,$(LIB_SRC),$(BUILD_DIR)/so/$(patsubst $(ROOT_DIR)/%,%,$(src:.c=.o)))
SO_OBJ       := $(SO_OBJ_LOCAL) $(SO_OBJ_LIB)

# # For library sources in the SO build, we need a separate static rule 
# SO_LIB_OBJ := $(foreach src,$(LIB_SRC),$(BUILD_DIR)/so/$(patsubst $(ROOT_DIR)/%,%,$(src:.c=.o)))

# ==================== Recipes ====================

all: $(BIN) $(SO)
	@echo "Build complete."

so: $(SO)
	@echo "Build complete."

solib: so

$(BIN): $(OBJ)
	@echo "Archiving build as $(BIN)..."
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

# Static Pattern Rule for SO Local Objects
$(SO_OBJ_LOCAL): $(BUILD_DIR)/so/$(SRC_DIR)/%.o: $(SRC_DIR)/%.c
	@mkdir -p $(dir $@)
	@echo "Compiling SO $<..."
	@$(CC) $(SO_CFLAGS) -c $< -o $@

# Static Pattern Rule for SO Library Objects
$(SO_OBJ_LIB): $(BUILD_DIR)/so/%.o: $(ROOT_DIR)/%.c
	@mkdir -p $(dir $@)
	@echo "Compiling SO lib $<..."
	@$(CC) $(SO_CFLAGS) -c $< -o $@

# Recipe to link SO library
$(SO): $(SO_OBJ)
	@echo "Linking shared SO library as $(SO)..."
	@mkdir -p $(dir $@)
	@$(CC) -shared -o $@ $(SO_OBJ) $(LDFLAGS)

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
	@rm -rf $(BIN) $(SO) $(BUILD_DIR)
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
