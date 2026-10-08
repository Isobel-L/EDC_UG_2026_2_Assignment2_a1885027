# Development Log

## 16 September 2026
- Created the GitHub repository for Assignment 2
- Reviewed the assignment requirements and repository setup
- Created development log to record progress throughout the assignment

## 17 September 2026
- Analyzed the railway layout and identified passenger and freight routes
- Identified section occupancy conflicts and junction/crossover conflicts plus freight/passenger priorities 
- Started planning how the railway behaviour will map to a Petri-net model 

## 17 September 2026
- Modelled a basic single-section train movement using the Petri-net buffer concept
- Extended the model to the simple freight route through Sections 3, 7 and 11
- Added the freight branch between Sections 3 and 4
- Added northbound freight movements to complete the freight network model
- Created the initial passenger network model for Sections 1, 2, 5, 6, 8, 9 and 10
- Confirmed that each movement requires the current section to be occupied and the destination section to be free

## 22 September 2026
- Continued development of the Petri-net model by adding passenger junction and freight/passenger crossover conflict handling
- Combined the passenger and freight networks into a complete interlocking model
- Added separate northbound and southbound occupied states to preserve train direction
- Added explicit entry and exit transitions based on the permitted railway routes
- Added initial tokens to each section's Free place to represent the initial state of the network
- Added Upper Crossing and Lower Crossing resource places to prevent conflicting movements through the freight/passenger crossover
- Refined passenger priority using ordinary Petri-net structure: freight movements between Sections 3 and 4 require Sections 1 and 6 to be free
- Added Workshop Route and Main Freight Route reservation tokens to prevent opposing freight trains from entering the same bidirectional route simultaneously
- Reviewed the final model for section collisions, crossover collisions, invalid direction changes, freight/passenger separation and deadlock risks
- Exported and reviewed the completed Petri-net model as a PDF for submission

## 23 September 2026
- Began implementing InterlockingImpl in Java
- Added data structures for train state and track-section occupancy
- Implemented getSection() and getTrain()
- Implemented addTrain() with route validation, duplicate train checks and occupied entry-section checks

## 24 September 2026
- Implemented route mapping for all valid passenger and freight paths
- Implemented basic moveTrains() behaviour for one-section movement and exiting the corridor
- Added JUnit 4 tests for train creation, section lookup, route validation, movement and exiting
- Ran the initial test suite successfully with all 10 tests passing

## 24 September 2026
- Implemented freight/passenger crossover conflict handling.
- Implemented passenger priority at the crossover independently of moveTrains() argument order
- Added crossover-specific JUnit tests including both passenger directions and freight blocking behaviour

## 24 September 2026
- Improved moveTrains() so movement decisions use the railway state at the start of each movement step
- Prevented duplicate train names from causing the same train to move more than once in a single call
- Added protection against multiple trains claiming the same destination section
- Implemented freight route reservations based on the Petri-net model
- Added separate reservations for the Workshop Route (Sections 3-4) and Main Freight Route (Sections 3-7-11)
- Freight route reservations are held from train entry until the train exits the corridor
- Added tests for opposing freight movements, reservation release, simultaneous passenger conflicts and invalid train movement
- Ran the expanded JUnit test suite successfully with all 23 tests passing

## 6 October 2026
- Expanded the JUnit test suite to cover edge cases and hidden-test-style scenarios
- Added validation tests for invalid track section numbers, invalid routes and null inputs
- Tested all valid passenger and freight routes from entry through to exit
- Added tests for duplicate movement requests, simultaneous train movements and movement into occupied sections
- Tested passenger priority and crossover behaviour in both directions
- Added tests for freight route reservation release and state cleanup after failed entry attempts
- Tested train-name reuse after exit and freight reservation cleanup
- Verified that an exiting train does not make its section available to another train during the same movement step
- Ran the complete JUnit suite successfully with all 51 tests passing

## 6 October 2026
- Reviewed the initial Gradescope results, which showed that 66 of 175 automated tests were passing despite all local tests succeeding
- Analysed the failed transition cases and identified that the freight route reservation logic was too restrictive because it allowed only one train to reserve a route at a time
- Refined the freight reservation model so multiple trains travelling in the same direction can use the same freight route while still preventing opposing traffic from entering and causing deadlock
- Updated the Workshop Route and Main Freight Route reservation logic to track the active travel direction and the number of trains using each route
- Preserved passenger priority, section occupancy protection, simultaneous-movement rules and route separation while making better use of available track capacity
- Re-ran the complete local JUnit test suite after the changes and confirmed that all 51 tests still passed successfully
- Prepared the revised implementation for another Gradescope submission to evaluate the remaining hidden transition cases

## 6 October 2026
- Reviewed the second Gradescope submission, which improved from 66/175 to 88/175 passing tests
- Analysed the remaining hidden-test failures and identified that the movement logic was too restrictive when one train vacated a section during the same moveTrains() call
- Updated moveTrains() to support simultaneous movement chains where a train may enter a section being vacated by another train in the same event
- Added dependency checking so trains remain blocked if the occupying train is not also moving
- Added cycle detection to prevent unsafe head-on swaps while still allowing valid movement pipelines
- Removed the earlier assumption that opposing freight trains must be rejected during addTrain(), based on Gradescope scenarios that successfully construct opposing freight traffic
- Expanded the JUnit test suite from 51 to 63 tests, including Gradescope-style passenger and freight pipeline scenarios, opposing freight traffic, simultaneous exits and movements, and collision/deadlock cases
- Recompiled and ran the complete local JUnit suite successfully with all 63 tests passing

## 9 October 2026
- Reviewed the 164/175 Gradescope result and analysed the remaining visible failures
- Identified a common arbitration issue: when multiple trains simultaneously requested the same destination section, the implementation previously allowed the first requested train to proceed
- Updated destination conflict handling so that if two or more trains claim the same section in one moveTrains() event, all conflicting trains remain stationary
- Added regression tests reproducing the Gradescope scenarios involving:
  - passenger trains from Sections 9 and 10 competing for Section 6
  - opposing freight trains competing for Section 7
  - freight trains competing for a newly vacated Section 3
- Updated existing passenger junction tests to remove the earlier first-requested-wins assumption
- Recompiled and ran the expanded local JUnit suite successfully: 66/66 tests passed