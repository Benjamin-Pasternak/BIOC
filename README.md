# BIoC - Ben's IoC Container

## Goal
- Understand the java language better, including reflection and dependency injection
- build out a really simple initial container without any AOP, Lifecycle management or other optimizations

Notes:
- Security implications, expecially running in an applet. May need to do some work there.


## Things to handle:

### Constructor Injection
- Handling Optional<?> dependencies 
- lifecycle
- resolving which constructor based on type and number of parameters ... like how spring does it
should be noted that this can introduce runtime errors so this one is a hard maybe
- 
