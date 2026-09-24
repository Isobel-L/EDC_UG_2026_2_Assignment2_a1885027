import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InterlockingImpl_Test {

    private InterlockingImpl interlocking;

    @Before
    public void setUp() {
        interlocking = new InterlockingImpl();
    }

    // =========================================================
    // BASIC STATE TESTS
    // =========================================================

    @Test
    public void testAddTrain() {
        interlocking.addTrain("TrainA", 1, 8);

        assertEquals("TrainA", interlocking.getSection(1));
        assertEquals(1, interlocking.getTrain("TrainA"));
    }

    @Test
    public void testEmptySectionReturnsNull() {
        assertNull(interlocking.getSection(5));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSection() {
        interlocking.getSection(12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRoute() {
        interlocking.addTrain("TrainA", 1, 11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateTrainName() {
        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainA", 10, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotAddTrainToOccupiedEntry() {
        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainB", 1, 9);
    }

    // =========================================================
    // BASIC MOVEMENT TESTS
    // =========================================================

    @Test
    public void testPassengerTrainMovesOneSection() {
        interlocking.addTrain("TrainA", 1, 8);

        int moved = interlocking.moveTrains(
                new String[]{"TrainA"}
        );

        assertEquals(1, moved);
        assertNull(interlocking.getSection(1));
        assertEquals("TrainA", interlocking.getSection(5));
        assertEquals(5, interlocking.getTrain("TrainA"));
    }

    @Test
    public void testBasicSectionOccupancy() {
        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainB", 9, 2);

        interlocking.moveTrains(new String[]{"TrainA"});

        assertEquals("TrainA", interlocking.getSection(5));
        assertEquals("TrainB", interlocking.getSection(9));
    }

    @Test
    public void testTrainExitsAfterDestination() {
        interlocking.addTrain("TrainA", 1, 8);

        // 1 -> 5
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"TrainA"})
        );

        // 5 -> 8
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"TrainA"})
        );

        assertEquals(8, interlocking.getTrain("TrainA"));
        assertEquals("TrainA", interlocking.getSection(8));

        // At its destination, the next move exits the corridor.
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"TrainA"})
        );

        assertEquals(-1, interlocking.getTrain("TrainA"));
        assertNull(interlocking.getSection(8));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExitedTrainCannotMoveAgain() {
        interlocking.addTrain("TrainA", 1, 8);

        interlocking.moveTrains(new String[]{"TrainA"});
        interlocking.moveTrains(new String[]{"TrainA"});
        interlocking.moveTrains(new String[]{"TrainA"});

        interlocking.moveTrains(new String[]{"TrainA"});
    }

    // =========================================================
    // CROSSOVER / PASSENGER PRIORITY TESTS
    // =========================================================

    @Test
    public void testSouthboundPassengerHasPriorityOverFreight() {
        interlocking.addTrain("Passenger", 1, 8);
        interlocking.addTrain("Freight", 3, 4);

        /*
         * Freight is deliberately listed first.
         * Passenger priority must not depend on array order.
         */
        int moved = interlocking.moveTrains(
                new String[]{"Freight", "Passenger"}
        );

        assertEquals(1, moved);

        // Passenger: 1 -> 5
        assertEquals(5, interlocking.getTrain("Passenger"));
        assertEquals("Passenger", interlocking.getSection(5));

        // Freight must remain in Section 3.
        assertEquals(3, interlocking.getTrain("Freight"));
        assertEquals("Freight", interlocking.getSection(3));
    }

    @Test
    public void testNorthboundPassengerHasPriorityOverFreight() {
        interlocking.addTrain("Passenger", 9, 2);

        // First move passenger from 9 -> 6.
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(6, interlocking.getTrain("Passenger"));

        interlocking.addTrain("Freight", 3, 4);

        /*
         * Passenger 6 -> 2 crosses the lower passenger line.
         * Freight 3 -> 4 crosses both passenger lines.
         */
        int moved = interlocking.moveTrains(
                new String[]{"Freight", "Passenger"}
        );

        assertEquals(1, moved);

        assertEquals(2, interlocking.getTrain("Passenger"));
        assertEquals(3, interlocking.getTrain("Freight"));
    }

    @Test
    public void testFreightCanCrossWhenPassengerApproachesAreFree() {
        interlocking.addTrain("Freight", 3, 4);

        int moved = interlocking.moveTrains(
                new String[]{"Freight"}
        );

        assertEquals(1, moved);
        assertNull(interlocking.getSection(3));
        assertEquals("Freight", interlocking.getSection(4));
        assertEquals(4, interlocking.getTrain("Freight"));
    }

    @Test
    public void testFreightWaitsWhileSouthboundPassengerIsWaiting() {
        interlocking.addTrain("Passenger", 1, 8);
        interlocking.addTrain("Freight", 3, 4);

        /*
         * Even though only Freight is requested, Section 1 is
         * occupied by a passenger waiting at the crossover.
         * This follows the Petri-net priority guard.
         */
        int moved = interlocking.moveTrains(
                new String[]{"Freight"}
        );

        assertEquals(0, moved);
        assertEquals(1, interlocking.getTrain("Passenger"));
        assertEquals(3, interlocking.getTrain("Freight"));
    }

    @Test
    public void testFreightWaitsWhileNorthboundPassengerIsWaiting() {
        interlocking.addTrain("Passenger", 9, 2);

        // Move passenger 9 -> 6.
        interlocking.moveTrains(new String[]{"Passenger"});

        interlocking.addTrain("Freight", 3, 4);

        int moved = interlocking.moveTrains(
                new String[]{"Freight"}
        );

        assertEquals(0, moved);
        assertEquals(6, interlocking.getTrain("Passenger"));
        assertEquals(3, interlocking.getTrain("Freight"));
    }
}