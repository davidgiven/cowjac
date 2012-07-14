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
	library/rt/classes.cc
	
OBJS = $(patsubst %.cc, %.o, $(SRCS))
DEPS = $(patsubst %.cc, %.d, $(SRCS))

all: cowjac

cowjac: $(OBJS)
	@echo linking...
	@g++ -Os -Wl,--gc-sections -g -o '$@' $(OBJS)

%.o: %.cc
	@echo '$@'
	@g++ $(CXXFLAGS) -c $< -o $@
	
%.d: %.cc
	@echo '$@'
	@g++ $(CXXFLAGS) -MM -MT $(patsubst %.cc,%.o,$<) $< -MF $@

-include $(DEPS)

