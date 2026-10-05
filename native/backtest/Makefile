# avanza-2-native/backtest Makefile

ROOT_DIR := ..

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
	CXX := clang++
else
	CXX := g++
endif

BUILD_DIR := build

# Binary names
BIN := $(BUILD_DIR)/libbacktest.a
SO  := $(BUILD_DIR)/libbacktest.so
TEST_BIN := $(BUILD_DIR)/backtest_tests

# ----- Local source files -----
SRC_DIR      := src
TEST_SRC_DIR := tests

SRC      := $(shell find -L $(SRC_DIR) -type f -name '*.cpp')
TEST_SRC := $(shell find -L $(TEST_SRC_DIR) -type f -name '*.cpp')

# C test helper library (compiled with the C compiler)
CC := $(if $(filter Darwin,$(UNAME_S)),clang,cc)

C_TEST_LIBS := common/tests
C_TEST_LIB_SRC_DIRS  := $(foreach lib,$(C_TEST_LIBS),$(ROOT_DIR)/$(lib)/src)
C_TEST_LIB_SRC       := $(shell find -L $(C_TEST_LIB_SRC_DIRS) -type f -name '*.c' 2>/dev/null)
C_TEST_LIB_INCLUDES  := $(foreach lib,$(C_TEST_LIBS),-I$(ROOT_DIR)/$(lib)/include)

# risk/ headers only (volatility.h, data_utils.h) - the implementations come
# from the prebuilt risk/build/librisk.a below, not from recompiling risk's
# sources here. simd_config.h (pulled in by volatility.h) lives under
# common/config, hence that include path too.
RISK_INCLUDES := -I$(ROOT_DIR)/risk/include -I$(ROOT_DIR)/common/utils/include \
	-I$(ROOT_DIR)/common/config/include

# Run `make` from native/ (builds risk before backtest, per MODULES order in
# native/Makefile) so this archive exists - `make -C backtest` alone assumes
# it was already built.
RISK_LIB := $(ROOT_DIR)/risk/build/librisk.a

# Compilation flags
WARN := -Wall -Wextra -Wfatal-errors
CXXFLAGS  := -std=c++17 -MMD -MP $(WARN) -g -fPIC \
	-Iinclude $(RISK_INCLUDES)
TEST_CXXFLAGS := $(CXXFLAGS) $(C_TEST_LIB_INCLUDES)

# C test helper library flags (compiled with the C compiler, but must use
# -fPIC so its objects can be linked into the same binary as the C++ PIC
# objects)
CFLAGS := -std=c11 -MMD -MP $(WARN) -g -fPIC $(C_TEST_LIB_INCLUDES)

ASAN_FLAGS := -O1 -g3 -fno-omit-frame-pointer -fsanitize=address,undefined

# For MacOS
ifeq ($(UNAME_S),Darwin)
	LDFLAGS := -flto -lm
else
	LDFLAGS := -flto -Wl,--gc-sections -lm
endif

# Map local .cpp -> .o
OBJ_LOCAL  := $(patsubst $(SRC_DIR)/%.cpp,$(BUILD_DIR)/$(SRC_DIR)/%.o,$(SRC))
SO_OBJ     := $(patsubst $(SRC_DIR)/%.cpp,$(BUILD_DIR)/so/$(SRC_DIR)/%.o,$(SRC))
TEST_OBJ_LOCAL := $(patsubst $(TEST_SRC_DIR)/%.cpp,$(BUILD_DIR)/$(TEST_SRC_DIR)/%.o,$(TEST_SRC))

# Map C test helper library .c -> .o
OBJ_C_TEST_LIB := $(foreach src,$(C_TEST_LIB_SRC),$(BUILD_DIR)/$(patsubst $(ROOT_DIR)/%,%,$(src:.c=.o)))

# Combine all objects
OBJ      := $(OBJ_LOCAL)
TEST_OBJ := $(OBJ_LOCAL) $(TEST_OBJ_LOCAL) $(OBJ_C_TEST_LIB)

# Dependency files (.d) for local and library sources
DEP := $(OBJ:.o=.d) $(SO_OBJ:.o=.d) $(TEST_OBJ_LOCAL:.o=.d) $(OBJ_C_TEST_LIB:.o=.d)

# ==================== Recipes ====================

all: $(BIN) $(SO)
	@echo "Build complete."

so: $(SO)
	@echo "Shared library build complete."

solib: so

$(BIN): $(OBJ)
	@echo "Archiving build as $(BIN)..."
	@mkdir -p $(dir $@)
	@ar rcs $@ $(OBJ)
	@ranlib $@

# Position-independent variants for the $(SO) target
$(BUILD_DIR)/so/$(SRC_DIR)/%.o: $(SRC_DIR)/%.cpp
	@echo "Compiling (PIC) $<..."
	@mkdir -p $(dir $@)
	@$(CXX) $(CXXFLAGS) -c $< -o $@

# Recipe to link SO library
$(SO): $(SO_OBJ)
	@echo "Linking shared SO library as $(SO)..."
	@mkdir -p $(dir $@)
	@$(CXX) -shared -o $@ $(SO_OBJ) $(LDFLAGS)

# Rule for local C++ source files
$(BUILD_DIR)/$(SRC_DIR)/%.o: $(SRC_DIR)/%.cpp
	@echo "Compiling $<..."
	@mkdir -p $(dir $@)
	@$(CXX) $(CXXFLAGS) -c $< -o $@

# Rule for test C++ source files
$(BUILD_DIR)/$(TEST_SRC_DIR)/%.o: $(TEST_SRC_DIR)/%.cpp
	@echo "Compiling test $<..."
	@mkdir -p $(dir $@)
	@$(CXX) $(TEST_CXXFLAGS) -c $< -o $@

# Rule for C test helper library source files
$(OBJ_C_TEST_LIB): $(BUILD_DIR)/%.o: $(ROOT_DIR)/%.c
	@echo "Compiling C test lib $<..."
	@mkdir -p $(dir $@)
	@$(CC) $(CFLAGS) -c $< -o $@

# Link test binary (test sources + local objects + C helper library, linked
# against the prebuilt risk archive for volatility/data_utils symbols)
$(TEST_BIN): $(TEST_OBJ) $(RISK_LIB)
	@echo "Linking test..."
	@mkdir -p $(dir $@)
	@$(CXX) $(TEST_OBJ) $(RISK_LIB) -o $@ $(LDFLAGS)

asan: clean
	@echo "Building ASan binary..."
	@$(MAKE) CXX="$(CXX)" CXXFLAGS="$(CXXFLAGS) $(ASAN_FLAGS)" \
		CFLAGS="$(CFLAGS) $(ASAN_FLAGS)" LDFLAGS="$(LDFLAGS) $(ASAN_FLAGS)" all

profile: clean
	@echo "Building profiling binary..."
	@$(MAKE) \
		CXX="$(CXX)" \
		CXXFLAGS="$(CXXFLAGS) -pg -O0 -g" \
		CFLAGS="$(CFLAGS) -pg -O0 -g" \
		LDFLAGS="-Wl,--gc-sections -pg" \
		all

clean:
	@echo "Cleaning build files..."
	@rm -rf $(BUILD_DIR)
	@echo "Clean complete."

valgrind: $(TEST_BIN)
	@echo "Debugging $< using valgrind..."
	@valgrind -s --leak-check=full --show-leak-kinds=all --track-origins=yes ./$(TEST_BIN)

gdb: $(TEST_BIN)
	@echo "Debugging $< using gdb..."
	@gdb ./$(TEST_BIN)

print:
	@echo "Local Source files: $(SRC)"
	@echo "Test source files: $(TEST_SRC)"
	@echo "C test lib sources: $(C_TEST_LIB_SRC)"
	@echo "Total Object files: $(OBJ)"
	@echo "SO Object files: $(SO_OBJ)"
	@echo "Test Object files: $(TEST_OBJ)"
	@echo "Dependency files: $(DEP)"

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

.PHONY: all so solib asan profile clean print gdb valgrind \
	test test-run test-gdb test-valgrind
