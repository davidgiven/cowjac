CXXFLAGS = \
	-ffunction-sections \
	-fdata-sections \
	-fomit-frame-pointer \
	-g \
	-I cowjacOutput \
	-I library/include
	
SRCS = \
	$(wildcard cowjacOutput/*.cc) \
	library/rt/cowjac.cc \
	library/rt/classes.cc \
	library/rt/gc.cc \
	library/rt/system.cc \
	library/rt/osmemory.cc \
	library/rt/osfilesystem.cc
	
OBJS = $(patsubst %.cc, %.o, $(SRCS))
DEPS = $(patsubst %.cc, %.d, $(SRCS))

all: cowjac

cowjac: $(OBJS)
	@echo linking...
	@clang++ -Os -Wl,--gc-sections -g -o '$@' $(OBJS) -lrt

%.o: %.cc
	@echo '$@'
	@clang++ $(CXXFLAGS) -c $< -o $@
	
%.d: %.cc
	@echo '$@'
	@g++ $(CXXFLAGS) -MM -MT $(patsubst %.cc,%.o,$<) $< -MF $@

-include $(DEPS)

