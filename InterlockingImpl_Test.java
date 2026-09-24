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

        // At destination, next movement exits.
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

        int moved = interlocking.moveTrains(
                new String[]{"Freight", "Passenger"}
        );

        assertEquals(1, moved);

        assertEquals(5, interlocking.getTrain("Passenger"));
        assertEquals("Passenger", interlocking.getSection(5));

        assertEquals(3, interlocking.getTrain("Freight"));
        assertEquals("Freight", interlocking.getSection(3));
    }

    @Test
    public void testNorthboundPassengerHasPriorityOverFreight() {
        interlocking.addTrain("Passenger", 9, 2);

        // 9 -> 6
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        interlocking.addTrain("Freight", 3, 4);

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

        // 9 -> 6
        interlocking.moveTrains(new String[]{"Passenger"});

        interlocking.addTrain("Freight", 3, 4);

        int moved = interlocking.moveTrains(
                new String[]{"Freight"}
        );

        assertEquals(0, moved);
        assertEquals(6, interlocking.getTrain("Passenger"));
        assertEquals(3, interlocking.getTrain("Freight"));
    }

    // =========================================================
    // MULTI-TRAIN MOVEMENT TESTS
    // =========================================================

    @Test
    public void testDuplicateTrainNameMovesOnlyOnce() {
        interlocking.addTrain("TrainA", 1, 8);

        int moved = interlocking.moveTrains(
                new String[]{"TrainA", "TrainA"}
        );

        assertEquals(1, moved);
        assertEquals(5, interlocking.getTrain("TrainA"));
        assertEquals("TrainA", interlocking.getSection(5));
    }

    @Test
    public void testTwoPassengerTrainsCannotBothEnterSectionSix() {
        interlocking.addTrain("Passenger9", 9, 2);
        interlocking.addTrain("Passenger10", 10, 2);

        int moved = interlocking.moveTrains(
                new String[]{"Passenger9", "Passenger10"}
        );

        assertEquals(1, moved);

        assertEquals(6, interlocking.getTrain("Passenger9"));
        assertEquals(10, interlocking.getTrain("Passenger10"));

        assertEquals(
                "Passenger9",
                interlocking.getSection(6)
        );

        assertEquals(
                "Passenger10",
                interlocking.getSection(10)
        );
    }

    @Test
    public void testInvalidTrainPreventsPartialMovement() {
        interlocking.addTrain("TrainA", 1, 8);

        try {
            interlocking.moveTrains(
                    new String[]{"TrainA", "DoesNotExist"}
            );

            fail("Expected IllegalArgumentException");

        } catch (IllegalArgumentException exception) {
            // Expected.
        }

        assertEquals(1, interlocking.getTrain("TrainA"));
        assertEquals("TrainA", interlocking.getSection(1));
        assertNull(interlocking.getSection(5));
    }

    // =========================================================
    // FREIGHT ROUTE RESERVATION TESTS
    // =========================================================

    @Test(expected = IllegalStateException.class)
    public void testOpposingMainFreightTrainCannotEnterReservedRoute() {
        interlocking.addTrain("FreightSouth", 3, 11);

        // The 3 -> 7 -> 11 route is already reserved.
        interlocking.addTrain("FreightNorth", 11, 3);
    }

    @Test
    public void testMainFreightRouteReleasedAfterExit() {
        interlocking.addTrain("FreightSouth", 3, 11);

        // 3 -> 7
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"FreightSouth"}
                )
        );

        // 7 -> 11
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"FreightSouth"}
                )
        );

        // Exit from 11.
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"FreightSouth"}
                )
        );

        assertEquals(-1, interlocking.getTrain("FreightSouth"));

        // Route should now be available again.
        interlocking.addTrain("FreightNorth", 11, 3);

        assertEquals(
                11,
                interlocking.getTrain("FreightNorth")
        );
    }

    @Test
    public void testWorkshopFreightRouteReleasedAfterExit() {
        interlocking.addTrain("FreightSouth", 3, 4);

        // 3 -> 4
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"FreightSouth"}
                )
        );

        // Exit from 4.
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"FreightSouth"}
                )
        );

        // Opposite direction can now enter.
        interlocking.addTrain("FreightNorth", 4, 3);

        assertEquals(
                4,
                interlocking.getTrain("FreightNorth")
        );
    }

    @Test(expected = IllegalStateException.class)
    public void testOpposingWorkshopFreightTrainCannotEnterReservedRoute() {
        interlocking.addTrain("FreightSouth", 3, 4);

        // Workshop route is still reserved by FreightSouth.
        interlocking.addTrain("FreightNorth", 4, 3);
    }

    @Test
    public void testFreightRouteRemainsReservedAtDestinationUntilExit() {
        interlocking.addTrain("FreightSouth", 3, 11);

        // 3 -> 7
        interlocking.moveTrains(
                new String[]{"FreightSouth"}
        );

        // 7 -> 11
        interlocking.moveTrains(
                new String[]{"FreightSouth"}
        );

        assertEquals(11, interlocking.getTrain("FreightSouth"));

        /*
         * FreightSouth has reached its destination but has not
         * exited yet, so the route should still be reserved.
         */
        try {

            interlocking.addTrain(
                    "FreightNorth",
                    11,
                    3
            );

            fail("Expected IllegalStateException");

        } catch (IllegalStateException exception) {
            // Expected.
        }

        assertEquals(11, interlocking.getTrain("FreightSouth"));
    }
}