import java.util.HashMap;
import java.util.Map;

public class InterlockingImpl implements Interlocking {

    // Indexes 1-11 represent physical track sections.
    // null means the section is currently unoccupied.
    private final String[] sections = new String[12];

    // Stores trains that have been added to the system.
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

        // Train exits on the move after reaching its destination.
        if (current == destination) {
            return -1;
        }

        // Passenger southbound
        if (current == 1) {
            return 5;
        }

        if (current == 5) {
            if (destination == 8) {
                return 8;
            }

            if (destination == 9) {
                return 9;
            }
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

    @Override
    public int moveTrains(String[] trainNames) {

        if (trainNames == null) {
            throw new IllegalArgumentException("Train list cannot be null");
        }

        int movedCount = 0;

        for (String trainName : trainNames) {

            TrainState train = trains.get(trainName);

            if (train == null || train.currentSection == -1) {
                throw new IllegalArgumentException(
                        "Train does not exist or has already exited");
            }

            int currentSection = train.currentSection;
            int nextSection = getNextSection(train);

            // If already at destination, this move exits the corridor.
            if (nextSection == -1) {
                sections[currentSection] = null;
                train.currentSection = -1;
                movedCount++;
                continue;
            }

            // Destination section is occupied, so the train cannot move.
            if (sections[nextSection] != null) {
                continue;
            }

            // Move the train to the next section.
            sections[currentSection] = null;
            sections[nextSection] = trainName;
            train.currentSection = nextSection;

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