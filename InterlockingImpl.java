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

        // A train name cannot already belong to a train currently in the corridor.
        TrainState existingTrain = trains.get(trainName);

        if (existingTrain != null && existingTrain.currentSection != -1) {
            throw new IllegalArgumentException("Train name is already in use");
        }

        // The entry/destination combination must correspond to a real route.
        if (!isValidRoute(entryTrackSection, destinationTrackSection)) {
            throw new IllegalArgumentException(
                    "No valid path exists between entry and destination");
        }

        // The train cannot enter an occupied section.
        if (sections[entryTrackSection] != null) {
            throw new IllegalStateException("Entry track section is occupied");
        }

        // Place the train into the entry section.
        sections[entryTrackSection] = trainName;

        // Store its current position and intended destination.
        trains.put(
                trainName,
                new TrainState(entryTrackSection, destinationTrackSection)
        );
    }

    /**
     * Checks whether an entry/destination pair corresponds to
     * one of the permitted railway routes.
     */
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

    @Override
    public int moveTrains(String[] trainNames) {
        // TODO
        return 0;
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