import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InterlockingImpl_Test {

    private InterlockingImpl interlocking;

    @Before
    public void setUp() {
        interlocking = new InterlockingImpl();
    }

    // BASIC STATE / INPUT TESTS

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

    @Test
    public void testAllSectionsInitiallyEmpty() {
        for (int section = 1; section <= 11; section++) {
            assertNull(interlocking.getSection(section));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSectionAboveRange() {
        interlocking.getSection(12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSectionZero() {
        interlocking.getSection(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSectionNegative() {
        interlocking.getSection(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownTrain() {
        interlocking.getTrain("DoesNotExist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTrainLookup() {
        interlocking.getTrain(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidRoute() {
        interlocking.addTrain("TrainA", 1, 11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidReverseRoute() {
        interlocking.addTrain("TrainA", 8, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFreightPassengerMixedRoute() {
        interlocking.addTrain("TrainA", 3, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateTrainName() {
        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainA", 10, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTrainNameCannotBeAdded() {
        interlocking.addTrain(null, 1, 8);
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotAddTrainToOccupiedEntry() {
        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainB", 1, 9);
    }

    // INVALID ENTRY / DESTINATION SECTION TESTS

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeEntrySection() {
        interlocking.addTrain("TrainA", -1, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEntrySectionAboveRange() {
        interlocking.addTrain("TrainA", 12, 2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNegativeDestinationSection() {
        interlocking.addTrain("TrainA", 1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDestinationSectionAboveRange() {
        interlocking.addTrain("TrainA", 1, 12);
    }


    // BASIC MOVEMENT TESTS

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

        // Exit.
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

    @Test
    public void testEmptyMoveArrayMovesNothing() {
        assertEquals(
                0,
                interlocking.moveTrains(new String[]{})
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullMoveArray() {
        interlocking.moveTrains(null);
    }

    @Test
    public void testNullNameInMoveListPreventsPartialMovement() {
        interlocking.addTrain("TrainA", 1, 8);

        try {
            interlocking.moveTrains(
                    new String[]{"TrainA", null}
            );

            fail("Expected IllegalArgumentException");

        } catch (IllegalArgumentException exception) {
            // Expected.
        }

        assertEquals(1, interlocking.getTrain("TrainA"));
        assertEquals("TrainA", interlocking.getSection(1));
        assertNull(interlocking.getSection(5));
    }

    @Test
    public void testTrainNameCanBeReusedAfterExit() {
        interlocking.addTrain("Reusable", 1, 8);

        // 1 -> 5 -> 8 -> exit
        interlocking.moveTrains(new String[]{"Reusable"});
        interlocking.moveTrains(new String[]{"Reusable"});
        interlocking.moveTrains(new String[]{"Reusable"});

        assertEquals(-1, interlocking.getTrain("Reusable"));

        interlocking.addTrain("Reusable", 10, 2);

        assertEquals(10, interlocking.getTrain("Reusable"));
        assertEquals("Reusable", interlocking.getSection(10));
    }

    // CROSSOVER / PASSENGER PRIORITY TESTS
    @Test
    public void testSouthboundPassengerHasPriorityOverFreight() {
        interlocking.addTrain("Passenger", 1, 8);
        interlocking.addTrain("Freight", 3, 4);

        /*
         * Freight deliberately appears first.
         * Passenger priority must still be respected.
         */
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

    @Test
    public void testFreightMovesAfterPassengerClearsCrossover() {
        interlocking.addTrain("Passenger", 1, 8);
        interlocking.addTrain("Freight", 3, 4);

        // Passenger has priority and moves 1 -> 5.
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"Passenger"}
                )
        );

        assertEquals(5, interlocking.getTrain("Passenger"));

        /*
         * Section 1 is now free, so the freight train is
         * permitted to cross from 3 -> 4.
         */
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"Freight"}
                )
        );

        assertEquals(4, interlocking.getTrain("Freight"));
    }

    @Test
    public void testBothPassengerCrossingsCanMoveTogether() {
        interlocking.addTrain("NorthPassenger", 9, 2);

        // Put northbound passenger into Section 6.
        interlocking.moveTrains(
                new String[]{"NorthPassenger"}
        );

        interlocking.addTrain("SouthPassenger", 1, 8);

        /*
         * 1 -> 5 uses the upper crossing.
         * 6 -> 2 uses the lower crossing.
         */
        int moved = interlocking.moveTrains(
                new String[]{
                        "SouthPassenger",
                        "NorthPassenger"
                }
        );

        assertEquals(2, moved);
        assertEquals(5, interlocking.getTrain("SouthPassenger"));
        assertEquals(2, interlocking.getTrain("NorthPassenger"));
    }

    // MULTI-TRAIN MOVEMENT TESTS

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

    @Test
    public void testExitingTrainDoesNotFreeSectionForSameMovementStep() {
        /*
         * First passenger reaches Section 2.
         */
        interlocking.addTrain("PassengerA", 9, 2);

        interlocking.moveTrains(
                new String[]{"PassengerA"}
        );

        interlocking.moveTrains(
                new String[]{"PassengerA"}
        );

        assertEquals(2, interlocking.getTrain("PassengerA"));

        /*
         * Second passenger reaches Section 6 and wants Section 2.
         */
        interlocking.addTrain("PassengerB", 10, 2);

        interlocking.moveTrains(
                new String[]{"PassengerB"}
        );

        assertEquals(6, interlocking.getTrain("PassengerB"));

        /*
         * PassengerA exits, but PassengerB sees Section 2 as
         * occupied at the beginning of this movement step.
         */
        int moved = interlocking.moveTrains(
                new String[]{"PassengerA", "PassengerB"}
        );

        assertEquals(1, moved);
        assertEquals(-1, interlocking.getTrain("PassengerA"));
        assertEquals(6, interlocking.getTrain("PassengerB"));
        assertNull(interlocking.getSection(2));

        /*
         * On the next movement step PassengerB can enter Section 2.
         */
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"PassengerB"}
                )
        );

        assertEquals(2, interlocking.getTrain("PassengerB"));
    }

    // FREIGHT ROUTE RESERVATION TESTS

    @Test(expected = IllegalStateException.class)
    public void testOpposingMainFreightTrainCannotEnterReservedRoute() {
        interlocking.addTrain("FreightSouth", 3, 11);

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

        // Exit.
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"FreightSouth"}
                )
        );

        assertEquals(-1, interlocking.getTrain("FreightSouth"));

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

        // Exit.
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"FreightSouth"}
                )
        );

        interlocking.addTrain("FreightNorth", 4, 3);

        assertEquals(
                4,
                interlocking.getTrain("FreightNorth")
        );
    }

    @Test(expected = IllegalStateException.class)
    public void testOpposingWorkshopFreightTrainCannotEnterReservedRoute() {
        interlocking.addTrain("FreightSouth", 3, 4);

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

    @Test
    public void testWorkshopAndMainFreightReservationsAreIndependent() {
        interlocking.addTrain("Workshop", 3, 4);
        interlocking.addTrain("Main", 11, 3);

        // Workshop: 3 -> 4
        // Main:     11 -> 7
        assertEquals(
                2,
                interlocking.moveTrains(
                        new String[]{"Workshop", "Main"}
                )
        );

        assertEquals(4, interlocking.getTrain("Workshop"));
        assertEquals(7, interlocking.getTrain("Main"));

        // Workshop exits while Main moves 7 -> 3.
        assertEquals(
                2,
                interlocking.moveTrains(
                        new String[]{"Workshop", "Main"}
                )
        );

        assertEquals(-1, interlocking.getTrain("Workshop"));
        assertEquals(3, interlocking.getTrain("Main"));

        // Main exits.
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"Main"}
                )
        );

        assertEquals(-1, interlocking.getTrain("Main"));
    }

    @Test
    public void testFailedOccupiedEntryDoesNotReserveOtherFreightRoute() {
        /*
         * Workshop train occupies Section 3.
         */
        interlocking.addTrain("Workshop", 3, 4);

        try {
            /*
             * This fails because Section 3 is occupied.
             * It must not accidentally reserve the Main route.
             */
            interlocking.addTrain("FailedMain", 3, 11);

            fail("Expected IllegalStateException");

        } catch (IllegalStateException exception) {
            // Expected.
        }

        // Move Workshop 3 -> 4 -> exit.
        interlocking.moveTrains(new String[]{"Workshop"});
        interlocking.moveTrains(new String[]{"Workshop"});

        /*
         * If the failed add corrupted the reservation state,
         * this valid main-route train would fail to enter.
         */
        interlocking.addTrain("Main", 11, 3);

        assertEquals(11, interlocking.getTrain("Main"));
    }

    @Test
    public void testRejectedOpposingFreightCanEnterAfterRouteReleased() {
        interlocking.addTrain("FreightSouth", 3, 11);

        try {
            interlocking.addTrain("FreightNorth", 11, 3);

            fail("Expected IllegalStateException");

        } catch (IllegalStateException exception) {
            // Expected.
        }

        // South train completes 3 -> 7 -> 11 -> exit.
        interlocking.moveTrains(new String[]{"FreightSouth"});
        interlocking.moveTrains(new String[]{"FreightSouth"});
        interlocking.moveTrains(new String[]{"FreightSouth"});

        /*
         * The previously rejected northbound train name was never
         * added, so it may now enter once the route is available.
         */
        interlocking.addTrain("FreightNorth", 11, 3);

        assertEquals(11, interlocking.getTrain("FreightNorth"));
    }

    @Test
    public void testFreightNameReuseDoesNotKeepOldReservation() {
        interlocking.addTrain("Freight", 3, 11);

        // 3 -> 7 -> 11 -> exit.
        interlocking.moveTrains(new String[]{"Freight"});
        interlocking.moveTrains(new String[]{"Freight"});
        interlocking.moveTrains(new String[]{"Freight"});

        assertEquals(-1, interlocking.getTrain("Freight"));

        /*
         * Reuse the same name in the opposite direction.
         * The previous route reservation must have been released.
         */
        interlocking.addTrain("Freight", 11, 3);

        assertEquals(11, interlocking.getTrain("Freight"));
    }

    // =========================================================
    // COMPLETE VALID ROUTE TESTS
    // =========================================================

    @Test
    public void testPassengerRouteOneToNineComplete() {
        interlocking.addTrain("Passenger", 1, 9);

        // 1 -> 5
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(5, interlocking.getTrain("Passenger"));

        // 5 -> 9
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(9, interlocking.getTrain("Passenger"));

        // Exit.
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(-1, interlocking.getTrain("Passenger"));
        assertNull(interlocking.getSection(9));
    }

    @Test
    public void testPassengerRouteNineToTwoComplete() {
        interlocking.addTrain("Passenger", 9, 2);

        // 9 -> 6
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(6, interlocking.getTrain("Passenger"));

        // 6 -> 2
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(2, interlocking.getTrain("Passenger"));

        // Exit.
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(-1, interlocking.getTrain("Passenger"));
    }

    @Test
    public void testPassengerRouteTenToTwoComplete() {
        interlocking.addTrain("Passenger", 10, 2);

        // 10 -> 6
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(6, interlocking.getTrain("Passenger"));

        // 6 -> 2
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(2, interlocking.getTrain("Passenger"));

        // Exit.
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Passenger"})
        );

        assertEquals(-1, interlocking.getTrain("Passenger"));
    }

    @Test
    public void testFreightRouteFourToThreeComplete() {
        interlocking.addTrain("Freight", 4, 3);

        // 4 -> 3
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Freight"})
        );

        assertEquals(3, interlocking.getTrain("Freight"));

        // Exit.
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Freight"})
        );

        assertEquals(-1, interlocking.getTrain("Freight"));
        assertNull(interlocking.getSection(3));
    }

    @Test
    public void testFreightRouteElevenToThreeComplete() {
        interlocking.addTrain("Freight", 11, 3);

        // 11 -> 7
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Freight"})
        );

        assertEquals(7, interlocking.getTrain("Freight"));

        // 7 -> 3
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Freight"})
        );

        assertEquals(3, interlocking.getTrain("Freight"));

        // Exit.
        assertEquals(
                1,
                interlocking.moveTrains(new String[]{"Freight"})
        );

        assertEquals(-1, interlocking.getTrain("Freight"));
        assertNull(interlocking.getSection(3));
    }
}