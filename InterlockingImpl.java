import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class InterlockingImpl implements Interlocking {

    // Indexes 1-11 represent physical track sections.
    // null means the section is currently unoccupied.
    private final String[] sections = new String[12];

    // Stores every train that has been added.
    private final Map<String, TrainState> trains = new HashMap<>();

    /*
     * These represent the two freight-route reservation tokens
     * from the Petri-net model.
     *
     * null means that the route is currently available.
     */
    private String workshopRouteOwner = null;
    private String mainFreightRouteOwner = null;

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

        /*
         * Reserve the appropriate freight route before the train
         * enters the corridor.
         */
        reserveFreightRoute(
                trainName,
                entryTrackSection,
                destinationTrackSection
        );

        sections[entryTrackSection] = trainName;

        trains.put(
                trainName,
                new TrainState(
                        entryTrackSection,
                        destinationTrackSection
                )
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

    /**
     * Returns true when the route is the short freight route
     * between Sections 3 and 4.
     */
    private boolean isWorkshopFreightRoute(int entrySection,
                                           int destinationSection) {

        return (entrySection == 3 && destinationSection == 4)
                || (entrySection == 4 && destinationSection == 3);
    }

    /**
     * Returns true when the route is the main freight route
     * through Sections 3, 7 and 11.
     */
    private boolean isMainFreightRoute(int entrySection,
                                       int destinationSection) {

        return (entrySection == 3 && destinationSection == 11)
                || (entrySection == 11 && destinationSection == 3);
    }

    /**
     * Reserves a freight route when a freight train enters.
     *
     * Passenger routes do not use these reservation tokens.
     */
    private void reserveFreightRoute(String trainName,
                                     int entrySection,
                                     int destinationSection) {

        if (isWorkshopFreightRoute(entrySection, destinationSection)) {

            if (workshopRouteOwner != null) {
                throw new IllegalStateException(
                        "Workshop freight route is currently reserved");
            }

            workshopRouteOwner = trainName;
            return;
        }

        if (isMainFreightRoute(entrySection, destinationSection)) {

            if (mainFreightRouteOwner != null) {
                throw new IllegalStateException(
                        "Main freight route is currently reserved");
            }

            mainFreightRouteOwner = trainName;
        }
    }

    /**
     * Releases a freight route when its train leaves the corridor.
     */
    private void releaseFreightRoute(String trainName,
                                     TrainState train) {

        if (isWorkshopFreightRoute(
                train.entrySection,
                train.destinationSection)) {

            if (trainName.equals(workshopRouteOwner)) {
                workshopRouteOwner = null;
            }

            return;
        }

        if (isMainFreightRoute(
                train.entrySection,
                train.destinationSection)) {

            if (trainName.equals(mainFreightRouteOwner)) {
                mainFreightRouteOwner = null;
            }
        }
    }

    /**
     * Determines the next section for a train.
     *
     * Returns -1 when the train is already at its destination
     * and should leave the corridor.
     */
    private int getNextSection(TrainState train) {

        int current = train.currentSection;
        int destination = train.destinationSection;

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
     * Freight movements between Sections 3 and 4 cross both
     * passenger tracks.
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
         * Remove duplicate names while maintaining their original
         * order. A train may move at most once per invocation.
         */
        Set<String> requestedTrains = new LinkedHashSet<>();

        for (String trainName : trainNames) {
            requestedTrains.add(trainName);
        }

        /*
         * Validate all names before making any state changes.
         */
        for (String trainName : requestedTrains) {

            TrainState train = trains.get(trainName);

            if (train == null || train.currentSection == -1) {
                throw new IllegalArgumentException(
                        "Train does not exist or has already exited");
            }
        }

        /*
         * All movement decisions are based on the railway state
         * at the beginning of this movement step.
         */
        String[] startingSections = sections.clone();

        Map<String, Integer> approvedMoves = new LinkedHashMap<>();
        Set<Integer> claimedSections = new LinkedHashSet<>();

        for (String trainName : requestedTrains) {

            TrainState train = trains.get(trainName);

            int currentSection = train.currentSection;
            int nextSection = getNextSection(train);

            /*
             * A train already at its destination exits now.
             */
            if (nextSection == -1) {
                approvedMoves.put(trainName, -1);
                continue;
            }

            /*
             * The destination section must have been free at the
             * beginning of this movement step.
             */
            if (startingSections[nextSection] != null) {
                continue;
            }

            /*
             * Two trains cannot claim the same section during the
             * same movement step.
             */
            if (claimedSections.contains(nextSection)) {
                continue;
            }

            /*
             * Passenger priority at the crossover.
             *
             * Freight 3 <-> 4 cannot cross while a passenger
             * occupies Section 1 or Section 6.
             */
            if (isFreightCrossoverMove(currentSection, nextSection)
                    && (startingSections[1] != null
                    || startingSections[6] != null)) {
                continue;
            }

            approvedMoves.put(trainName, nextSection);
            claimedSections.add(nextSection);
        }

        /*
         * Apply every approved movement after all movement
         * decisions have been made.
         */
        for (Map.Entry<String, Integer> move
                : approvedMoves.entrySet()) {

            String trainName = move.getKey();
            int nextSection = move.getValue();

            TrainState train = trains.get(trainName);
            int currentSection = train.currentSection;

            sections[currentSection] = null;

            if (nextSection == -1) {

                /*
                 * The train is leaving the corridor, so its freight
                 * route reservation can now be released.
                 */
                releaseFreightRoute(trainName, train);

                train.currentSection = -1;

            } else {

                sections[nextSection] = trainName;
                train.currentSection = nextSection;
            }
        }

        return approvedMoves.size();
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