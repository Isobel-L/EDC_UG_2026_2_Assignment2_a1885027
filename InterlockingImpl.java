import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class InterlockingImpl implements Interlocking {

    // Sections 1-11. null means the section is free.
    private final String[] sections = new String[12];

    // All train names that have been used.
    private final Map<String, TrainState> trains = new HashMap<>();

    /*
     * Directional freight-route reservations.
     *
     *  1  = southbound
     * -1  = northbound
     *  0  = unused
     *
     * Multiple trains travelling in the SAME direction are allowed.
     * An opposing train is rejected until all trains travelling in the
     * current direction have left that route.
     */
    private int workshopDirection = 0;
    private int workshopTrainCount = 0;

    private int mainDirection = 0;
    private int mainTrainCount = 0;


    private static class TrainState {

        private final int entrySection;
        private final int destinationSection;
        private int currentSection;

        TrainState(int entrySection, int destinationSection) {
            this.entrySection = entrySection;
            this.destinationSection = destinationSection;
            this.currentSection = entrySection;
        }
    }


    @Override
    public void addTrain(String trainName,
                         int entryTrackSection,
                         int destinationTrackSection) {

        if (trainName == null) {
            throw new IllegalArgumentException(
                    "Train name cannot be null");
        }

        // Validate the route before indexing the sections array.
        if (!isValidRoute(entryTrackSection,
                          destinationTrackSection)) {

            throw new IllegalArgumentException(
                    "No valid path exists between entry and destination");
        }

        TrainState existing = trains.get(trainName);

        if (existing != null && existing.currentSection != -1) {
            throw new IllegalArgumentException(
                    "Train name is already in use");
        }

        if (sections[entryTrackSection] != null) {
            throw new IllegalStateException(
                    "Entry track section is occupied");
        }

        /*
         * Check the freight direction BEFORE modifying any state.
         */
        checkFreightReservation(
                entryTrackSection,
                destinationTrackSection);

        TrainState train =
                new TrainState(entryTrackSection,
                               destinationTrackSection);

        sections[entryTrackSection] = trainName;
        trains.put(trainName, train);

        /*
         * Only reserve after the train has successfully been added.
         */
        reserveFreightRoute(
                entryTrackSection,
                destinationTrackSection);
    }


    /*
     * Legal complete routes through the corridor.
     */
    private boolean isValidRoute(int entry, int destination) {

        // Passenger southbound.
        if (entry == 1) {
            return destination == 8 || destination == 9;
        }

        // Passenger northbound.
        if (entry == 9 || entry == 10) {
            return destination == 2;
        }

        // Freight southbound.
        if (entry == 3) {
            return destination == 4 || destination == 11;
        }

        // Freight northbound.
        if (entry == 4 || entry == 11) {
            return destination == 3;
        }

        return false;
    }


    private boolean isWorkshopRoute(int entry, int destination) {

        return (entry == 3 && destination == 4)
                || (entry == 4 && destination == 3);
    }


    private boolean isMainRoute(int entry, int destination) {

        return (entry == 3 && destination == 11)
                || (entry == 11 && destination == 3);
    }


    /*
     * Southbound freight = +1
     * Northbound freight = -1
     */
    private int freightDirection(int entry, int destination) {

        if (entry == 3 &&
                (destination == 4 || destination == 11)) {
            return 1;
        }

        if ((entry == 4 || entry == 11)
                && destination == 3) {
            return -1;
        }

        return 0;
    }


    /*
     * Make sure an opposing freight train has not already reserved
     * the route.
     */
    private void checkFreightReservation(int entry,
                                         int destination) {

        int direction = freightDirection(entry, destination);

        if (isWorkshopRoute(entry, destination)) {

            if (workshopTrainCount > 0
                    && workshopDirection != direction) {

                throw new IllegalStateException(
                        "Workshop route occupied by opposing traffic");
            }
        }

        if (isMainRoute(entry, destination)) {

            if (mainTrainCount > 0
                    && mainDirection != direction) {

                throw new IllegalStateException(
                        "Main freight route occupied by opposing traffic");
            }
        }
    }


    private void reserveFreightRoute(int entry,
                                     int destination) {

        int direction = freightDirection(entry, destination);

        if (isWorkshopRoute(entry, destination)) {

            if (workshopTrainCount == 0) {
                workshopDirection = direction;
            }

            workshopTrainCount++;
        }

        if (isMainRoute(entry, destination)) {

            if (mainTrainCount == 0) {
                mainDirection = direction;
            }

            mainTrainCount++;
        }
    }


    private void releaseFreightRoute(TrainState train) {

        if (isWorkshopRoute(
                train.entrySection,
                train.destinationSection)) {

            workshopTrainCount--;

            if (workshopTrainCount == 0) {
                workshopDirection = 0;
            }
        }

        if (isMainRoute(
                train.entrySection,
                train.destinationSection)) {

            mainTrainCount--;

            if (mainTrainCount == 0) {
                mainDirection = 0;
            }
        }
    }


    /*
     * Determine the next physical track section.
     *
     * -1 means the train is already at its destination and
     * should now leave the corridor.
     */
    private int getNextSection(TrainState train) {

        int current = train.currentSection;
        int destination = train.destinationSection;

        if (current == destination) {
            return -1;
        }

        // -------------------------------------------------
        // PASSENGER SOUTHBOUND
        // -------------------------------------------------

        // 1 -> 5
        if (current == 1) {
            return 5;
        }

        // 5 -> 8
        if (current == 5 && destination == 8) {
            return 8;
        }

        // 5 -> 9
        if (current == 5 && destination == 9) {
            return 9;
        }

        // PASSENGER NORTHBOUND

        // 9 -> 6
        if (current == 9 && destination == 2) {
            return 6;
        }

        // 10 -> 6
        if (current == 10 && destination == 2) {
            return 6;
        }

        // 6 -> 2
        if (current == 6 && destination == 2) {
            return 2;
        }

        // FREIGHT SOUTHBOUND

        // 3 -> 4
        if (current == 3 && destination == 4) {
            return 4;
        }

        // 3 -> 7
        if (current == 3 && destination == 11) {
            return 7;
        }

        // 7 -> 11
        if (current == 7 && destination == 11) {
            return 11;
        }

        // FREIGHT NORTHBOUND
        
        // 4 -> 3
        if (current == 4 && destination == 3) {
            return 3;
        }

        // 11 -> 7
        if (current == 11 && destination == 3) {
            return 7;
        }

        // 7 -> 3
        if (current == 7 && destination == 3) {
            return 3;
        }

        throw new IllegalStateException(
                "Train is on an invalid route");
    }


    /*
     * Freight 3 <-> 4 crosses both passenger tracks.
     */
    private boolean isFreightCrossoverMove(int current,
                                            int next) {

        return (current == 3 && next == 4)
                || (current == 4 && next == 3);
    }


    @Override
    public int moveTrains(String[] trainNames) {

        if (trainNames == null) {
            throw new IllegalArgumentException(
                    "Train list cannot be null");
        }

        /*
         * Remove duplicate names while preserving order.
         * This prevents a train moving twice during one call.
         */
        Set<String> requested = new LinkedHashSet<>();

        for (String trainName : trainNames) {

            if (trainName == null) {
                throw new IllegalArgumentException(
                        "Train name cannot be null");
            }

            requested.add(trainName);
        }


        /*
         * Validate EVERY train before changing anything.
         *
         * This makes moveTrains atomic with respect to invalid input.
         */
        for (String trainName : requested) {

            TrainState train = trains.get(trainName);

            if (train == null || train.currentSection == -1) {

                throw new IllegalArgumentException(
                        "Train does not exist or has exited");
            }
        }


        /*
         * Movement decisions use the railway state at the beginning
         * of this call.
         *
         * Therefore, if a train leaves a section during this call,
         * another train cannot immediately move into that section
         * during the same call.
         */
        String[] initialSections = sections.clone();

        Map<String, Integer> approved =
                new LinkedHashMap<>();

        Set<Integer> claimedDestinations =
                new LinkedHashSet<>();


        /*
         * Passenger priority must not depend on the order in which
         * names were supplied.
         *
         * We therefore determine whether the passenger crossover
         * approaches were occupied at the start of the step.
         */
        boolean passengerAtUpperCrossing =
                initialSections[1] != null;

        boolean passengerAtLowerCrossing =
                initialSections[6] != null;


        for (String trainName : requested) {

            TrainState train = trains.get(trainName);

            int current = train.currentSection;
            int next = getNextSection(train);


            /*
             * Train is already at destination:
             * its next movement exits the railway.
             */
            if (next == -1) {

                approved.put(trainName, -1);
                continue;
            }


            /*
             * The next section was occupied at the beginning
             * of the movement step.
             */
            if (initialSections[next] != null) {
                continue;
            }


            /*
             * Another train has already been approved to enter
             * this section during this movement step.
             */
            if (claimedDestinations.contains(next)) {
                continue;
            }


            /*
             * Freight movement 3 <-> 4 crosses BOTH passenger
             * tracks.
             *
             * Passenger trains have priority, so freight cannot
             * cross while a passenger occupies Section 1 or 6.
             */
            if (isFreightCrossoverMove(current, next)) {

                if (passengerAtUpperCrossing
                        || passengerAtLowerCrossing) {

                    continue;
                }
            }


            approved.put(trainName, next);
            claimedDestinations.add(next);
        }


        /*
         * Apply all approved movements only after all decisions
         * have been made.
         */
        for (Map.Entry<String, Integer> movement
                : approved.entrySet()) {

            String trainName = movement.getKey();
            int next = movement.getValue();

            TrainState train = trains.get(trainName);
            int current = train.currentSection;

            // Release the current physical section.
            sections[current] = null;

            if (next == -1) {

                // Train leaves the railway.
                releaseFreightRoute(train);

                train.currentSection = -1;

            } else {

                // Train enters its next section.
                sections[next] = trainName;
                train.currentSection = next;
            }
        }


        return approved.size();
    }


    @Override
    public String getSection(int trackSection) {

        if (trackSection < 1 || trackSection > 11) {

            throw new IllegalArgumentException(
                    "Invalid track section");
        }

        return sections[trackSection];
    }


    @Override
    public int getTrain(String trainName) {

        if (trainName == null) {

            throw new IllegalArgumentException(
                    "Train name cannot be null");
        }

        TrainState train = trains.get(trainName);

        if (train == null) {

            throw new IllegalArgumentException(
                    "Train does not exist");
        }

        return train.currentSection;
    }
}