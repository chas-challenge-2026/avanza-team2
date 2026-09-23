# avanza-2-native/fx Makefile

UNAME_S := $(shell uname -s)
ifeq ($(UNAME_S),Darwin)
	CXX := clang++
else
	CXX := g++
endif

BUILD_DIR := build

# Shared library for the JNA bridge (rates.h's extern "C" surface)
SO := $(BUILD_DIR)/libfx.so

# Test binary: offline self-test, `--live` hits the network
TEST_BIN := $(BUILD_DIR)/fx_test

# ----- Local source files (C++ library, no entry point) -----
SRC_DIR := src
SRC := $(shell find -L $(SRC_DIR) -type f -name '*.cpp')

# ----- Test source files (own main, links against the library above) -----
TEST_DIR := test
TEST_SRC := $(shell find -L $(TEST_DIR) -type f -name '*.cpp')

WARN := -Wall -Wextra -Wfatal-errors

CXXFLAGS := -std=c++17 -MMD -MP $(WARN) -g -fPIC -Iinclude

ASAN_FLAGS := -O1 -g3 -fno-omit-frame-pointer -fsanitize=address,undefined

# ecb.cpp calls libcurl directly
ifeq ($(UNAME_S),Darwin)
	LDFLAGS := -flto -lcurl
else
	LDFLAGS := -flto -Wl,--gc-sections -lcurl
endif

# Map local .cpp → .o (the library, linked into both $(SO) and $(TEST_BIN))
OBJ := $(patsubst $(SRC_DIR)/%.cpp,$(BUILD_DIR)/$(SRC_DIR)/%.o,$(SRC))

# Same mapping again, but under build/so so PIC objects never collide with
# the plain ones $(TEST_BIN) uses.
SO_OBJ := $(patsubst $(SRC_DIR)/%.cpp,$(BUILD_DIR)/so/$(SRC_DIR)/%.o,$(SRC))

# Map test .cpp → .o, linked with $(OBJ) into $(TEST_BIN)
TEST_OBJ_LOCAL := $(patsubst $(TEST_DIR)/%.cpp,$(BUILD_DIR)/$(TEST_DIR)/%.o,$(TEST_SRC))
TEST_OBJ       := $(OBJ) $(TEST_OBJ_LOCAL)

# Dependency files (.d)
DEP := $(OBJ:.o=.d) $(SO_OBJ:.o=.d) $(TEST_OBJ_LOCAL:.o=.d)

# ==================== Recipes ====================

# The module's deliverable is the shared library; the test binary is built
# on demand by `test`/`run`.
all: $(SO)
	@echo "Build complete."

so: $(SO)
	@echo "Shared library build complete."

$(SO): $(SO_OBJ)
	@echo "Linking shared library..."
	@mkdir -p $(dir $@)
	@$(CXX) -shared $(SO_OBJ) $(LDFLAGS) -o $@

test: $(TEST_BIN)
	@echo "Test build complete."

$(TEST_BIN): $(TEST_OBJ)
	@echo "Linking test binary..."
	@mkdir -p $(dir $@)
	@$(CXX) $(TEST_OBJ) $(LDFLAGS) -o $@

asan: clean
	@echo "Building ASan test binary..."
	@$(MAKE) CXXFLAGS="$(CXXFLAGS) $(ASAN_FLAGS)" \
		LDFLAGS="$(LDFLAGS) $(ASAN_FLAGS)" test

profile: clean
	@echo "Building profiling test binary..."
	@$(MAKE) \
		CXXFLAGS="$(CXXFLAGS) -pg -O0 -g" \
		LDFLAGS="-Wl,--gc-sections -pg -lcurl" \
		test

# Rule for local C++ library source files
$(BUILD_DIR)/$(SRC_DIR)/%.o: $(SRC_DIR)/%.cpp
	@echo "Compiling $<..."
	@mkdir -p $(dir $@)
	@$(CXX) $(CXXFLAGS) -c $< -o $@

# Rule for test source files
$(BUILD_DIR)/$(TEST_DIR)/%.o: $(TEST_DIR)/%.cpp
	@echo "Compiling test $<..."
	@mkdir -p $(dir $@)
	@$(CXX) $(CXXFLAGS) -c $< -o $@

# Position-independent variants for the $(SO) target
$(BUILD_DIR)/so/$(SRC_DIR)/%.o: $(SRC_DIR)/%.cpp
	@echo "Compiling (PIC) $<..."
	@mkdir -p $(dir $@)
	@$(CXX) $(CXXFLAGS) -fPIC -c $< -o $@

run: $(TEST_BIN)
	@echo "Running $(TEST_BIN)..."
	@./$(TEST_BIN)

run-live: $(TEST_BIN)
	@echo "Running $(TEST_BIN) --live..."
	@./$(TEST_BIN) --live

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
	@echo "Total Object files: $(OBJ) $(SO_OBJ) $(TEST_OBJ_LOCAL)"
	@echo "Dependency files: $(DEP)"

-include $(DEP)

.PHONY: all so test run run-live clean print gdb valgrind asan profile

# Rule for building the shared library libfx.so
SOLIB := $(BUILD_DIR)/libfx.so

# Target for building the shared library libfx.so
solib: $(OBJ)
	@echo "Linking $(SOLIB)"
	@mkdir -p $(dir $(SOLIB))
	@$(CXX) -shared $(OBJ) $(LDFLAGS) -o $(SOLIB)

.PHONY: solib
