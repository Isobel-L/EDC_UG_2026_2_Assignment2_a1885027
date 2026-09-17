# Assignment 2 Planning

## Railway Network Analysis

### Passenger line
- 1 connects to 5
- 2 connects to 6
- 5 connects to 8 and 9
- 6 connects to 9 and 10

### Freight line
- 3 connects to 7
- 3 connects to 4
- 7 connects to 11



## Allowed Train Routes

### Passenger Trains
Southbound (left-to-right):
- 1 -> 5 -> 8 -> Exit
- 1 -> 5 -> 9 -> Exit

Northbound (right-to-left):
- 9 -> 6 -> 2 -> Exit
- 10 -> 6 -> 2 -> Exit

### Freight Trains
Southbound (left-to-right):
- 3 -> 4 -> Exit
- 3 -> 7 -> 11 -> Exit

Northbound (right-to-left):
- 4 -> 3 -> Exit
- 11 -> 7 -> 3 -> Exit

<!-- Passenger and freight trains must remain on their respective networks and cannot switch between the two lines. -->


## Conflict Analysis

### Section Occupancy
Only one train may occupy a track section at a time

A train must not be allowed to move into a section if that section is already occupied

### Freight / Passenger Crossover
The freight route between sections 3 and 4 crosses the passenger tracks.

Therefore, freight movement through 3 <-> 4 can conflict with passenger movements through:
- 1 <-> 5
- 2 <-> 6

Passenger trains have priority at this crossover. If a passenger and freight train both require the crossing, the passenger train should be allowed through first

### Passenger Junction
The passenger line changes from two tracks to three tracks at the right-hand junction
Possible movements include:
- 5 <-> 8
- 5 <-> 9
- 6 <-> 9
- 6 <-> 10

Movements that attempt to use the same destination section cannot occur at the same time. For example, trains cannot simultaneously move from 5 -> 9 and 6 -> 9 because both would attempt to enter section 9

The junction will need to prevent any movements whose paths overlap or cross

## Initial Petri-net Components

### Places
- A train occupying a section
- A section being available
- A junction or crossing being available

### Transitions
- A train entering the network
- A train moving from one section to another
- A train leaving the network

### Tokens
Tokens will represent the current state of the system, such as a train being present in a section or a resource being available.