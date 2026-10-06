import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InterlockingImpl implements Interlocking {

    /*
     * Indexes 1-11 correspond to physical track sections.
     * Index 0 is unused.
     *
     * null means the section is free.
     */
    private final String[] sections = new String[12];

    /*
     * Stores all trains that have ever been added.
     *
     * A train with currentSection == -1 has left the corridor.
     */
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

    // ADD TRAIN

    @Override
    public void addTrain(
            String trainName,
            int entryTrackSection,
            int destinationTrackSection) {

        if (trainName == null) {
            throw new IllegalArgumentException(
                    "Train name cannot be null");
        }

        /*
         * A name may be reused after the previous train
         * has completely left the corridor.
         */
        TrainState existingTrain = trains.get(trainName);

        if (existingTrain != null
                && existingTrain.currentSection != -1) {

            throw new IllegalArgumentException(
                    "Train name is already in use");
        }

        /*
         * Validate the route before using the section
         * numbers as array indexes.
         */
        if (!isValidRoute(
                entryTrackSection,
                destinationTrackSection)) {

            throw new IllegalArgumentException(
                    "No valid path exists between entry and destination");
        }

        /*
         * The interface only permits a train to enter
         * if its entry section is free.
         */
        if (sections[entryTrackSection] != null) {
            throw new IllegalStateException(
                    "Entry track section is occupied");
        }

        sections[entryTrackSection] = trainName;

        trains.put(
                trainName,
                new TrainState(
                        entryTrackSection,
                        destinationTrackSection));
    }
    // VALID ROUTES

    private boolean isValidRoute(
            int entryTrackSection,
            int destinationTrackSection) {

        /*
         * Passenger southbound:
         *
         * 1 -> 5 -> 8
         * 1 -> 5 -> 9
         */
        if (entryTrackSection == 1) {
            return destinationTrackSection == 8
                    || destinationTrackSection == 9;
        }

        /*
         * Passenger northbound:
         *
         * 9  -> 6 -> 2
         * 10 -> 6 -> 2
         */
        if (entryTrackSection == 9
                || entryTrackSection == 10) {

            return destinationTrackSection == 2;
        }

        /*
         * Freight southbound:
         *
         * 3 -> 4
         * 3 -> 7 -> 11
         */
        if (entryTrackSection == 3) {
            return destinationTrackSection == 4
                    || destinationTrackSection == 11;
        }

        /*
         * Freight northbound:
         *
         * 4  -> 3
         * 11 -> 7 -> 3
         */
        if (entryTrackSection == 4
                || entryTrackSection == 11) {

            return destinationTrackSection == 3;
        }

        return false;
    }

    // NEXT SECTION

    private int getNextSection(TrainState train) {

        int current = train.currentSection;
        int destination = train.destinationSection;

        /*
         * A train already at its destination leaves
         * the corridor the next time it is moved.
         */
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

        throw new IllegalStateException(
                "Train is on an invalid route");
    }

    // CROSSOVER

    private boolean isFreightCrossoverMove(
            int currentSection,
            int nextSection) {

        return (currentSection == 3 && nextSection == 4)
                || (currentSection == 4 && nextSection == 3);
    }


    /*
     * Passenger trains have priority at the passenger/freight
     * crossover.
     *
     * A freight train crossing between 3 and 4 must wait
     * whenever a passenger occupies either crossover approach:
     *
     * Section 1 -> 5
     * Section 6 -> 2
     */
    private boolean passengerWaitingAtCrossover(
            String[] startingSections) {

        return startingSections[1] != null
                || startingSections[6] != null;
    }

    // MOVE TRAINS

    @Override
    public int moveTrains(String[] trainNames) {

        if (trainNames == null) {
            throw new IllegalArgumentException(
                    "Train list cannot be null");
        }

        /*
         * A train may only move once in a single invocation,
         * even if its name appears multiple times.
         *
         * LinkedHashSet preserves the caller's order.
         */
        LinkedHashSet<String> requestedTrains =
                new LinkedHashSet<>();

        for (String trainName : trainNames) {

            if (trainName == null) {
                throw new IllegalArgumentException(
                        "Train name cannot be null");
            }

            requestedTrains.add(trainName);
        }

        /*
         * Validate every train before changing any state.
         *
         * This ensures an invalid name cannot cause partial
         * movement of valid trains.
         */
        for (String trainName : requestedTrains) {

            TrainState train = trains.get(trainName);

            if (train == null
                    || train.currentSection == -1) {

                throw new IllegalArgumentException(
                        "Train does not exist or has already exited");
            }
        }

        /*
         * Record the state before this event.
         *
         * This is used for collision/dependency analysis,
         * but unlike the previous implementation a train MAY
         * enter an occupied section if its occupant is also
         * successfully leaving during this same event.
         */
        String[] startingSections = sections.clone();

        /*
         * Proposed next section for every requested train.
         *
         * -1 means the train exits.
         */
        LinkedHashMap<String, Integer> proposedMoves =
                new LinkedHashMap<>();

        for (String trainName : requestedTrains) {

            TrainState train = trains.get(trainName);

            proposedMoves.put(
                    trainName,
                    getNextSection(train));
        }


        /*
         * Begin with every requested train as a possible move.
         */
        LinkedHashSet<String> possibleMoves =
                new LinkedHashSet<>(requestedTrains);

        // PASSENGER PRIORITY

        /*
         * A freight movement between Sections 3 and 4
         * crosses both passenger tracks.
         *
         * Passenger trains waiting at Section 1 or Section 6
         * receive priority.
         */
        if (passengerWaitingAtCrossover(startingSections)) {

            List<String> blockedFreight =
                    new ArrayList<>();

            for (String trainName : possibleMoves) {

                TrainState train = trains.get(trainName);
                int nextSection =
                        proposedMoves.get(trainName);

                if (nextSection != -1
                        && isFreightCrossoverMove(
                                train.currentSection,
                                nextSection)) {

                    blockedFreight.add(trainName);
                }
            }

            possibleMoves.removeAll(blockedFreight);
        }

        // DESTINATION CONFLICTS

        /*
         * If multiple trains attempt to enter the same section,
         * only the first train named in moveTrains() is allowed
         * to claim it.
         *
         * This gives deterministic behaviour for cases such as
         * both Sections 9 and 10 trying to enter Section 6.
         */
        Set<Integer> claimedDestinations =
                new HashSet<>();

        List<String> duplicateClaims =
                new ArrayList<>();

        for (String trainName : possibleMoves) {

            int nextSection =
                    proposedMoves.get(trainName);

            if (nextSection == -1) {
                continue;
            }

            if (claimedDestinations.contains(nextSection)) {
                duplicateClaims.add(trainName);
            } else {
                claimedDestinations.add(nextSection);
            }
        }

        possibleMoves.removeAll(duplicateClaims);


        // OCCUPANCY DEPENDENCIES

        /*
         * Important:
         *
         * If Train A wants to enter a section occupied by
         * Train B, A may move only if B is ALSO successfully
         * moving away during this same moveTrains() event.
         *
         * Example:
         *
         * A: Section 10 -> Section 6
         * B: Section 6  -> Section 2
         * C: Section 2  -> exit
         *
         * All three can move in one event.
         */
        boolean changed;

        do {

            changed = false;

            List<String> blocked =
                    new ArrayList<>();

            for (String trainName : possibleMoves) {

                int nextSection =
                        proposedMoves.get(trainName);

                /*
                 * Exiting trains never need a destination
                 * section.
                 */
                if (nextSection == -1) {
                    continue;
                }

                String occupant =
                        startingSections[nextSection];

                /*
                 * Destination was initially free.
                 */
                if (occupant == null) {
                    continue;
                }

                /*
                 * Destination is occupied.
                 *
                 * It is only usable if that exact occupant is
                 * also one of the trains that will move during
                 * this event.
                 */
                if (!possibleMoves.contains(occupant)) {
                    blocked.add(trainName);
                }
            }

            if (!blocked.isEmpty()) {

                possibleMoves.removeAll(blocked);
                changed = true;
            }

        } while (changed);


        // PREVENT HEAD-ON SWAPS / CYCLES

        /*
         * Allowing vacated sections does NOT mean two trains
         * may simply swap places.
         *
         * For example:
         *
         * 3 -> 7
         * 7 -> 3
         *
         * would cause the trains to cross head-on.
         *
         * Find dependency cycles and prevent every train in
         * those cycles from moving.
         */
        Set<String> cycleMembers =
                findMovementCycles(
                        possibleMoves,
                        proposedMoves,
                        startingSections);

        if (!cycleMembers.isEmpty()) {

            possibleMoves.removeAll(cycleMembers);

            /*
             * Removing a cyclic movement may also invalidate
             * another train that depended on one of those
             * sections becoming free.
             */
            do {

                changed = false;

                List<String> blocked =
                        new ArrayList<>();

                for (String trainName : possibleMoves) {

                    int nextSection =
                            proposedMoves.get(trainName);

                    if (nextSection == -1) {
                        continue;
                    }

                    String occupant =
                            startingSections[nextSection];

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

        // APPLY MOVEMENTS SIMULTANEOUSLY

        /*
         * First clear ALL source sections.
         *
         * This must happen before filling destinations.
         *
         * Otherwise a chain such as:
         *
         * Section 1 -> Section 5
         * Section 5 -> Section 9
         *
         * could accidentally erase a train depending on the
         * iteration order.
         */
        for (String trainName : possibleMoves) {

            TrainState train =
                    trains.get(trainName);

            sections[train.currentSection] = null;
        }


        /*
         * Now update train states and fill destination
         * sections.
         */
        for (String trainName : possibleMoves) {

            TrainState train =
                    trains.get(trainName);

            int nextSection =
                    proposedMoves.get(trainName);

            if (nextSection == -1) {

                /*
                 * Train has exited the corridor.
                 */
                train.currentSection = -1;

            } else {

                /*
                 * Train moves into its next section.
                 */
                train.currentSection = nextSection;
                sections[nextSection] = trainName;
            }
        }

        return possibleMoves.size();
    }

    // MOVEMENT CYCLE DETECTION

    private Set<String> findMovementCycles(
            Set<String> possibleMoves,
            Map<String, Integer> proposedMoves,
            String[] startingSections) {

        Set<String> cycleMembers =
                new HashSet<>();

        /*
         * Every train has at most one destination, therefore
         * every train has at most one dependency:
         *
         * "I need the train currently in my destination
         * section to move."
         *
         * This makes cycle detection straightforward.
         */
        for (String startTrain : possibleMoves) {

            List<String> path =
                    new ArrayList<>();

            Map<String, Integer> positionInPath =
                    new HashMap<>();

            String currentTrain = startTrain;

            while (currentTrain != null
                    && possibleMoves.contains(currentTrain)) {

                /*
                 * We have returned to a train already present
                 * in the current dependency path.
                 */
                if (positionInPath.containsKey(currentTrain)) {

                    int cycleStart =
                            positionInPath.get(currentTrain);

                    for (int i = cycleStart;
                         i < path.size();
                         i++) {

                        cycleMembers.add(path.get(i));
                    }

                    break;
                }

                /*
                 * Reaching a train already known to belong to a
                 * cycle does not require finding the same cycle
                 * again.
                 */
                if (cycleMembers.contains(currentTrain)) {
                    break;
                }

                positionInPath.put(
                        currentTrain,
                        path.size());

                path.add(currentTrain);

                int nextSection =
                        proposedMoves.get(currentTrain);

                /*
                 * Exit has no dependency.
                 */
                if (nextSection == -1) {
                    break;
                }

                /*
                 * Dependency is the train occupying our
                 * destination at the beginning of the event.
                 */
                currentTrain =
                        startingSections[nextSection];
            }
        }

        return cycleMembers;
    }

    // GET SECTION

    @Override
    public String getSection(int trackSection) {

        if (trackSection < 1
                || trackSection > 11) {

            throw new IllegalArgumentException(
                    "Invalid track section");
        }

        return sections[trackSection];
    }

    // GET TRAIN

    @Override
    public int getTrain(String trainName) {

        if (trainName == null) {
            throw new IllegalArgumentException(
                    "Train name cannot be null");
        }

        TrainState train =
                trains.get(trainName);

        if (train == null) {
            throw new IllegalArgumentException(
                    "Train does not exist");
        }

        return train.currentSection;
    }
}