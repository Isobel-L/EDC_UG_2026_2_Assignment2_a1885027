import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InterlockingImpl implements Interlocking {

    // Indexes 1-11 represent physical track sections.
    // null means the section is currently unoccupied.
    private final String[] sections = new String[12];

    // Stores every train that has been added.
    private final Map<String, TrainState> trains = new HashMap<>();

    private static class TrainState {
        private int currentSection;
        private final int destinationSection;

        TrainState(int currentSection, int destinationSection) {
            this.currentSection = currentSection;
            this.destinationSection = destinationSection;
        }
    }

    @Override
    public void addTrain(String trainName, int entryTrackSection,
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

        trains.put(
                trainName,
                new TrainState(entryTrackSection, destinationTrackSection)
        );
    }

    private boolean isValidRoute(int entryTrackSection,
                                 int destinationTrackSection) {

        return
                // Passenger southbound
                (entryTrackSection == 1
                        && (destinationTrackSection == 8
                        || destinationTrackSection == 9))

                // Passenger northbound
                || (entryTrackSection == 9
                        && destinationTrackSection == 2)

                || (entryTrackSection == 10
                        && destinationTrackSection == 2)

                // Freight southbound
                || (entryTrackSection == 3
                        && (destinationTrackSection == 4
                        || destinationTrackSection == 11))

                // Freight northbound
                || (entryTrackSection == 4
                        && destinationTrackSection == 3)

                || (entryTrackSection == 11
                        && destinationTrackSection == 3);
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

    /**
     * Returns true when a movement is one of the two passenger
     * movements through the freight/passenger crossover.
     */
    private boolean isPassengerCrossoverMove(int currentSection,
                                             int nextSection) {

        return (currentSection == 1 && nextSection == 5)
                || (currentSection == 6 && nextSection == 2);
    }

    /**
     * Returns true when a freight train attempts to cross
     * the passenger tracks.
     */
    private boolean isFreightCrossoverMove(int currentSection,
                                           int nextSection) {

        return (currentSection == 3 && nextSection == 4)
                || (currentSection == 4 && nextSection == 3);
    }

    @Override
    public int moveTrains(String[] trainNames) {

        if (trainNames == null) {
            throw new IllegalArgumentException("Train list cannot be null");
        }

        /*
         * Validate every requested train before moving anything.
         * This prevents a partially completed move operation if
         * one of the supplied names is invalid.
         */
        for (String trainName : trainNames) {

            TrainState train = trains.get(trainName);

            if (train == null || train.currentSection == -1) {
                throw new IllegalArgumentException(
                        "Train does not exist or has already exited");
            }
        }

        /*
         * Determine whether a passenger crossover movement is
         * requested and currently able to move.
         *
         * Freight must give way to such a passenger movement.
         */
        boolean passengerHasCrossoverPriority = false;

        for (String trainName : trainNames) {

            TrainState train = trains.get(trainName);

            int currentSection = train.currentSection;
            int nextSection = getNextSection(train);

            if (nextSection != -1
                    && isPassengerCrossoverMove(currentSection, nextSection)
                    && sections[nextSection] == null) {

                passengerHasCrossoverPriority = true;
                break;
            }
        }

        int movedCount = 0;

        /*
         * Keeps track of destination sections already claimed
         * during this moveTrains call.
         */
        Set<Integer> claimedSections = new HashSet<>();

        for (String trainName : trainNames) {

            TrainState train = trains.get(trainName);

            int currentSection = train.currentSection;
            int nextSection = getNextSection(train);

            // Train is already at its destination and exits now.
            if (nextSection == -1) {
                sections[currentSection] = null;
                train.currentSection = -1;
                movedCount++;
                continue;
            }

            // The next physical section is already occupied.
            if (sections[nextSection] != null) {
                continue;
            }

            // Another train in this same call already claimed it.
            if (claimedSections.contains(nextSection)) {
                continue;
            }

            /*
             * Passenger trains have priority at the crossover.
             * A freight crossover movement is blocked whenever
             * an able passenger crossover movement is requested
             * in the same call.
             */
            if (isFreightCrossoverMove(currentSection, nextSection)
                    && passengerHasCrossoverPriority) {
                continue;
            }

            /*
             * This also follows the Petri-net priority guard:
             * freight may not enter the crossover while a
             * passenger is occupying either approach section.
             */
            if (isFreightCrossoverMove(currentSection, nextSection)
                    && (sections[1] != null || sections[6] != null)) {
                continue;
            }

            // Perform the movement.
            sections[currentSection] = null;
            sections[nextSection] = trainName;
            train.currentSection = nextSection;

            claimedSections.add(nextSection);
            movedCount++;
        }

        return movedCount;
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

        TrainState train = trains.get(trainName);

        if (train == null) {
            throw new IllegalArgumentException("Train does not exist");
        }

        return train.currentSection;
    }
}