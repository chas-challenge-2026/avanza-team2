# avanza-2-native Makefile

PROJECT_NAME := avanza-2-native

# Modules to build. Update this list if you add subdirectories.
MODULES := helloworld jna_poc risk

# Include paths for root-level headers
export CFLAGS += -I./include

#############################
# PHONIES
#############################

.PHONY: all clean deps \
	$(MODULES) \
	$(addsuffix /install,$(MODULES)) \
	$(addsuffix /run,$(MODULES)) \
	$(addsuffix /clean,$(MODULES)) \
	$(addsuffix /valgrind,$(MODULES)) \
	$(addsuffix /gdb,$(MODULES)) \
	$(addsuffix /fuzz,$(MODULES)) \
	$(addsuffix /fuzz-asan,$(MODULES)) \
	$(addsuffix /run-asan,$(MODULES)) \
	$(addsuffix /profile,$(MODULES))

#############################
# Recipes
#############################

# Default target: build all modules
all: $(MODULES)

# Clean all modules
clean:
	@for module in $(MODULES); do \
		echo "Cleaning $$module..."; \
		$(MAKE) -C $$module clean; \
	done
	@echo "All modules cleaned."

# Build each module
$(MODULES):
	@echo "Building module $@..."
	$(MAKE) -C $@ all

# Install target using make [module]/install
$(addsuffix /install,$(MODULES)):
	@MODULE=$(@D); \
	echo "Installing module $$MODULE..."; \
	$(MAKE) -C $$MODULE install

# Run target using make [module]/run
$(addsuffix /run,$(MODULES)):
	@MODULE=$(@D); \
	echo "Running module $$MODULE..."; \
	$(MAKE) -C $$MODULE run

# Clean specific module using make [module]/clean
$(addsuffix /clean,$(MODULES)):
	@MODULE=$(@D); \
	echo "Cleaning module $$MODULE..."; \
	$(MAKE) -C $$MODULE clean

# Run valgrind on target using make [module]/valgrind
$(addsuffix /valgrind,$(MODULES)):
	@MODULE=$(@D); \
	echo "Debugging module $$MODULE using valgrind..."; \
	$(MAKE) -C $$MODULE valgrind

# Run gdb on target using make [module]/gdb
$(addsuffix /gdb,$(MODULES)):
	@MODULE=$(@D); \
	echo "Debugging module $$MODULE using gdb..."; \
	$(MAKE) -C $$MODULE gdb

# Fuzz targets
$(addsuffix /fuzz,$(MODULES)):
	@MODULE=$(@D); \
	echo "Building fuzz target for $$MODULE..."; \
	$(MAKE) -C $$MODULE fuzz

$(addsuffix /fuzz-asan,$(MODULES)):
	@MODULE=$(@D); \
	echo "Building ASan fuzz binary for $$MODULE..."; \
	$(MAKE) -C $$MODULE fuzz-asan

# Run with AddressSanitizer
$(addsuffix /run-asan,$(MODULES)):
	@MODULE=$(@D); \
	echo "Debugging module $$MODULE using asan..."; \
	$(MAKE) -C $$MODULE run-asan

# Profiling target
$(addsuffix /profile,$(MODULES)):
	@MODULE=$(@D); \
	echo "Building profiling target for $$MODULE..."; \
	$(MAKE) -C $$MODULE profile
