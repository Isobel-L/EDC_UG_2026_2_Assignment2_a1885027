import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InterlockingImpl implements Interlocking {

    // Indexes 1-11 are physical track sections. Index 0 is unused.
    private final String[] sections = new String[12];

    // Stores every train ever added. currentSection == -1 means exited.
    private final Map<String, TrainState> trains = new HashMap<>();

    private static class TrainState {
        private final int entrySection;
        private int currentSection;
        private final int destinationSection;

        TrainState(int entrySection, int destinationSection) {
            this.entrySection = entrySection;
            this.currentSection = entrySection;
            this.destinationSection = destinationSection;
        }
    }

    @Override
    public void addTrain(String trainName,
                         int entryTrackSection,
                         int destinationTrackSection) {

        if (trainName == null) {
            throw new IllegalArgumentException("Train name cannot be null");
        }

        TrainState existingTrain = trains.get(trainName);
        if (existingTrain != null && existingTrain.currentSection != -1) {
            throw new IllegalArgumentException("Train name is already in use");
        }

        if (!isValidRoute(entryTrackSection, destinationTrackSection)) {
            throw new IllegalArgumentException(
                    "No valid path exists between entry and destination");
        }

        if (sections[entryTrackSection] != null) {
            throw new IllegalStateException("Entry track section is occupied");
        }

        sections[entryTrackSection] = trainName;
        trains.put(trainName,
                new TrainState(entryTrackSection, destinationTrackSection));
    }

    private boolean isValidRoute(int entryTrackSection,
                                 int destinationTrackSection) {

        // Passenger southbound: 1 -> 5 -> 8/9
        if (entryTrackSection == 1) {
            return destinationTrackSection == 8
                    || destinationTrackSection == 9;
        }

        // Passenger northbound: 9/10 -> 6 -> 2
        if (entryTrackSection == 9 || entryTrackSection == 10) {
            return destinationTrackSection == 2;
        }

        // Freight southbound: 3 -> 4 OR 3 -> 7 -> 11
        if (entryTrackSection == 3) {
            return destinationTrackSection == 4
                    || destinationTrackSection == 11;
        }

        // Freight northbound: 4 -> 3 OR 11 -> 7 -> 3
        if (entryTrackSection == 4 || entryTrackSection == 11) {
            return destinationTrackSection == 3;
        }

        return false;
    }

    private int getNextSection(TrainState train) {
        int current = train.currentSection;
        int destination = train.destinationSection;

        // A train at its destination exits on its next move.
        if (current == destination) {
            return -1;
        }

        // Passenger southbound
        if (current == 1) {
            return 5;
        }
        if (current == 5 && destination == 8) {
            return 8;
        }
        if (current == 5 && destination == 9) {
            return 9;
        }

        // Passenger northbound
        if (current == 9 && destination == 2) {
            return 6;
        }
        if (current == 10 && destination == 2) {
            return 6;
        }
        if (current == 6 && destination == 2) {
            return 2;
        }

        // Freight southbound
        if (current == 3 && destination == 4) {
            return 4;
        }
        if (current == 3 && destination == 11) {
            return 7;
        }
        if (current == 7 && destination == 11) {
            return 11;
        }

        // Freight northbound
        if (current == 4 && destination == 3) {
            return 3;
        }
        if (current == 11 && destination == 3) {
            return 7;
        }
        if (current == 7 && destination == 3) {
            return 3;
        }

        throw new IllegalStateException("Train is on an invalid route");
    }

    private boolean isFreightCrossoverMove(int currentSection,
                                           int nextSection) {
        return (currentSection == 3 && nextSection == 4)
                || (currentSection == 4 && nextSection == 3);
    }

    private boolean passengerWaitingAtCrossover(String[] startingSections) {
        return startingSections[1] != null
                || startingSections[6] != null;
    }

    @Override
    public int moveTrains(String[] trainNames) {

        if (trainNames == null) {
            throw new IllegalArgumentException("Train list cannot be null");
        }

        // Deduplicate while preserving input order.
        LinkedHashSet<String> requestedTrains = new LinkedHashSet<>();
        for (String trainName : trainNames) {
            if (trainName == null) {
                throw new IllegalArgumentException("Train name cannot be null");
            }
            requestedTrains.add(trainName);
        }

        // Validate every name before changing any state.
        for (String trainName : requestedTrains) {
            TrainState train = trains.get(trainName);
            if (train == null || train.currentSection == -1) {
                throw new IllegalArgumentException(
                        "Train does not exist or has already exited");
            }
        }

        String[] startingSections = sections.clone();

        // Work out each requested train's proposed destination.
        LinkedHashMap<String, Integer> proposedMoves = new LinkedHashMap<>();
        for (String trainName : requestedTrains) {
            proposedMoves.put(trainName,
                    getNextSection(trains.get(trainName)));
        }

        LinkedHashSet<String> possibleMoves =
                new LinkedHashSet<>(requestedTrains);

        // Passenger priority over freight crossing 3 <-> 4.
        if (passengerWaitingAtCrossover(startingSections)) {
            List<String> blockedFreight = new ArrayList<>();

            for (String trainName : possibleMoves) {
                TrainState train = trains.get(trainName);
                int nextSection = proposedMoves.get(trainName);

                if (nextSection != -1
                        && isFreightCrossoverMove(
                                train.currentSection,
                                nextSection)) {
                    blockedFreight.add(trainName);
                }
            }

            possibleMoves.removeAll(blockedFreight);
        }

        // Destination conflicts
        // If multiple trains want the same destination section in
        // the same event, NONE of them may move into that section.
        Map<Integer, Integer> destinationClaimCounts = new HashMap<>();

        for (String trainName : possibleMoves) {
            int nextSection = proposedMoves.get(trainName);

            if (nextSection == -1) {
                continue;
            }

            destinationClaimCounts.put(
                    nextSection,
                    destinationClaimCounts.getOrDefault(nextSection, 0) + 1);
        }

        List<String> conflictingMoves = new ArrayList<>();

        for (String trainName : possibleMoves) {
            int nextSection = proposedMoves.get(trainName);

            if (nextSection == -1) {
                continue;
            }

            if (destinationClaimCounts.get(nextSection) > 1) {
                conflictingMoves.add(trainName);
            }
        }

        possibleMoves.removeAll(conflictingMoves);

        // Occupancy dependencies
        // A train may enter a currently occupied section only when
        // that exact occupant is also successfully moving away in
        // this same moveTrains() event.
        boolean changed;

        do {
            changed = false;
            List<String> blocked = new ArrayList<>();

            for (String trainName : possibleMoves) {
                int nextSection = proposedMoves.get(trainName);

                if (nextSection == -1) {
                    continue;
                }

                String occupant = startingSections[nextSection];

                if (occupant == null) {
                    continue;
                }

                if (!possibleMoves.contains(occupant)) {
                    blocked.add(trainName);
                }
            }

            if (!blocked.isEmpty()) {
                possibleMoves.removeAll(blocked);
                changed = true;
            }

        } while (changed);

        // Prevent swaps / dependency cycles
        Set<String> cycleMembers = findMovementCycles(
                possibleMoves,
                proposedMoves,
                startingSections);

        if (!cycleMembers.isEmpty()) {
            possibleMoves.removeAll(cycleMembers);

            // Removing a cycle can invalidate trains that depended
            // on a cycle member vacating a section.
            do {
                changed = false;
                List<String> blocked = new ArrayList<>();

                for (String trainName : possibleMoves) {
                    int nextSection = proposedMoves.get(trainName);

                    if (nextSection == -1) {
                        continue;
                    }

                    String occupant = startingSections[nextSection];

                    if (occupant != null
                            && !possibleMoves.contains(occupant)) {
                        blocked.add(trainName);
                    }
                }

                if (!blocked.isEmpty()) {
                    possibleMoves.removeAll(blocked);
                    changed = true;
                }

            } while (changed);
        }

        // Apply all approved movements simultaneously
        // Clear every source first so movement chains are not
        // affected by iteration order.
        for (String trainName : possibleMoves) {
            TrainState train = trains.get(trainName);
            sections[train.currentSection] = null;
        }

        // Then place trains into their destinations or mark exits.
        for (String trainName : possibleMoves) {
            TrainState train = trains.get(trainName);
            int nextSection = proposedMoves.get(trainName);

            if (nextSection == -1) {
                train.currentSection = -1;
            } else {
                train.currentSection = nextSection;
                sections[nextSection] = trainName;
            }
        }

        return possibleMoves.size();
    }

    private Set<String> findMovementCycles(
            Set<String> possibleMoves,
            Map<String, Integer> proposedMoves,
            String[] startingSections) {

        Set<String> cycleMembers = new HashSet<>();

        for (String startTrain : possibleMoves) {
            List<String> path = new ArrayList<>();
            Map<String, Integer> positionInPath = new HashMap<>();

            String currentTrain = startTrain;

            while (currentTrain != null
                    && possibleMoves.contains(currentTrain)) {

                if (positionInPath.containsKey(currentTrain)) {
                    int cycleStart = positionInPath.get(currentTrain);

                    for (int i = cycleStart; i < path.size(); i++) {
                        cycleMembers.add(path.get(i));
                    }
                    break;
                }

                if (cycleMembers.contains(currentTrain)) {
                    break;
                }

                positionInPath.put(currentTrain, path.size());
                path.add(currentTrain);

                int nextSection = proposedMoves.get(currentTrain);

                if (nextSection == -1) {
                    break;
                }

                currentTrain = startingSections[nextSection];
            }
        }

        return cycleMembers;
    }

    @Override
    public String getSection(int trackSection) {
        if (trackSection < 1 || trackSection > 11) {
            throw new IllegalArgumentException("Invalid track section");
        }

        return sections[trackSection];
    }

    @Override
    public int getTrain(String trainName) {
        if (trainName == null) {
            throw new IllegalArgumentException("Train name cannot be null");
        }

        TrainState train = trains.get(trainName);

        if (train == null) {
            throw new IllegalArgumentException("Train does not exist");
        }

        return train.currentSection;
    }
}
