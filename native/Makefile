# avanza-2-native Makefile

PROJECT_NAME   := avanza-2-native

# Define dir paths that should be created with make install
LOG_DIR 			 			:= /var/log/avanza2/
DATA_DIR      			:= /var/lib/avanza2/
DATA_CACHE_DIR      := /var/lib/avanza2/cache/
DATA_CACHE_RATE_DIR := /var/lib/avanza2/cache/rate/

# Header where paths are stored
USER_CONFIG_H  := common/config/include/user_config.h

# User to install for
UID ?= 1000
GID ?= 1000

# Modules to build. Update this list if you add subdirectories.
MODULES := jna_poc risk fx

# Include paths for root-level headers
export CFLAGS  += -I./include

#############################
# PHONIES
#############################

.PHONY: all clean deps install \
	$(MODULES) \
	$(addsuffix /install,$(MODULES)) \
	$(addsuffix /so,$(MODULES)) \
	$(addsuffix /run,$(MODULES)) \
	$(addsuffix /run-live,$(MODULES)) \
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

# Install runtime environment
install:
	@if [ "$$(id -u)" -ne 0 ]; then \
		echo "Root privileges required, please run with sudo."; \
		exit 1; \
	fi

	@echo "Creating directories with necessary permissions..."
	install -o $(UID) -g $(GID) -d -m 0755 $(LOG_DIR)
	install -o $(UID) -g $(GID) -d -m 0755 $(DATA_DIR)
	install -o $(UID) -g $(GID) -d -m 0755 $(DATA_CACHE_DIR)
	install -o $(UID) -g $(GID) -d -m 0755 $(DATA_CACHE_RATE_DIR)

	@echo "Generating new $(USER_CONFIG_H)..."
	@echo "#ifndef __USER_CONFIG_H__" 			 	   									 >  $(USER_CONFIG_H)
	@echo "#define __USER_CONFIG_H__" 			 	   									 >> $(USER_CONFIG_H)
	@echo "#define LOG_DIR \"$(LOG_DIR)\"" 									       >> $(USER_CONFIG_H)
	@echo "#define DATA_DIR \"$(DATA_DIR)\"" 	   									 >> $(USER_CONFIG_H)
	@echo "#define DATA_CACHE_DIR \"$(DATA_CACHE_DIR)\"" 	         >> $(USER_CONFIG_H)
	@echo "#define DATA_CACHE_RATE_DIR \"$(DATA_CACHE_RATE_DIR)\"" >> $(USER_CONFIG_H)
	@echo "#endif" 															 									 >> $(USER_CONFIG_H)

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

# Build the shared library for a module using make [module]/so
$(addsuffix /so,$(MODULES)):
	@MODULE=$(@D); \
	echo "Building shared library for module $$MODULE..."; \
	$(MAKE) -C $$MODULE so

# Run target using make [module]/run
$(addsuffix /run,$(MODULES)):
	@MODULE=$(@D); \
	echo "Running module $$MODULE..."; \
	$(MAKE) -C $$MODULE run

# Run with real network access using make [module]/run-live
$(addsuffix /run-live,$(MODULES)):
	@MODULE=$(@D); \
	echo "Running module $$MODULE against live data..."; \
	$(MAKE) -C $$MODULE run-live

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

# ---- Test Targets ----

# Build target test using make [module]/test
$(addsuffix /test,$(MODULES)):
	@MODULE=$(@D); \
	echo "Installing module $$MODULE..."; \
	$(MAKE) -C $$MODULE test 

# Run test using make [module]/test/run
$(addsuffix /test-run,$(MODULES)):
	@MODULE=$(@D); \
	echo "Running module $$MODULE..."; \
	$(MAKE) -C $$MODULE test-run

# Run gdb test on target using make [module]/testgdb
$(addsuffix /test-gdb,$(MODULES)):
	@MODULE=$(@D); \
	echo "Debugging module $$MODULE using gdb..."; \
	$(MAKE) -C $$MODULE test-gdb

# Run valgrind on target using make [module]/valgrind
$(addsuffix /test-valgrind,$(MODULES)):
	@MODULE=$(@D); \
	echo "Debugging module $$MODULE using valgrind..."; \
	$(MAKE) -C $$MODULE test-valgrind
