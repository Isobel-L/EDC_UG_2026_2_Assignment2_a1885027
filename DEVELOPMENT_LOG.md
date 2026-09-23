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
- Began implementing InterlockingImpl in Java.
- Added data structures for train state and track-section occupancy.
- Implemented getSection() and getTrain().
- Implemented addTrain() with route validation, duplicate train checks and occupied entry-section checks.