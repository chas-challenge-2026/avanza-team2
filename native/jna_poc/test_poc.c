#include <dlfcn.h>
#include <stdio.h>

// Loads libpoc.so the same way a real consumer (JNA, Java's Native.load)
// would, through the dynamic loader, not by executing it directly.
typedef int (*poc_add_one_fn)(int);

int main(void) {
	void *handle = dlopen("./libpoc.so", RTLD_NOW);
	if (!handle) {
		fprintf(stderr, "dlopen failed: %s\n", dlerror());
		return 1;
	}

	poc_add_one_fn poc_add_one = (poc_add_one_fn) dlsym(handle, "poc_add_one");
	if (!poc_add_one) {
		fprintf(stderr, "dlsym failed: %s\n", dlerror());
		dlclose(handle);
		return 1;
	}

	int result = poc_add_one(41);
	printf("poc_add_one(41) = %d\n", result);

	dlclose(handle);
	return result == 42 ? 0 : 1;
}
