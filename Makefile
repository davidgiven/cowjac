CXXFLAGS = \
	-ffunction-sections \
	-fdata-sections \
	-fomit-frame-pointer \
	-Os -g \
	-I cowjacOutput \
	-I library/include
	
SRCS = $(wildcard cowjacOutput/*.cc)
OBJS = $(patsubst %.cc, %.o, $(SRCS))

all: $(OBJS)

%.o: %.cc
	@echo '$<'
	@clang $(CXXFLAGS) -c '$<' -o '$@'
	
