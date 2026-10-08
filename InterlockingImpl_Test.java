import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

/**
 * Expanded test suite for InterlockingImpl.
 *
 * This suite keeps the core interface/route tests, removes the earlier
 * assumption that opposing freight trains must be rejected by addTrain(),
 * and adds more simultaneous-movement cases that match the behaviour
 * observed in the Gradescope traces.
 */
public class InterlockingImpl_Test {

    private InterlockingImpl interlocking;

    @Before
    public void setUp() {
        interlocking = new InterlockingImpl();
    }

    // =========================================================
    // BASIC STATE / LOOKUP TESTS
    // =========================================================

    @Test
    public void testAllSectionsInitiallyEmpty() {
        for (int section = 1; section <= 11; section++) {
            assertNull(interlocking.getSection(section));
        }
    }

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
    public void testInvalidSectionZero() {
        interlocking.getSection(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSectionNegative() {
        interlocking.getSection(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidSectionAboveRange() {
        interlocking.getSection(12);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownTrain() {
        interlocking.getTrain("DoesNotExist");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullTrainLookup() {
        interlocking.getTrain(null);
    }

    // =========================================================
    // ADD / ROUTE VALIDATION TESTS
    // =========================================================

    @Test(expected = IllegalArgumentException.class)
    public void testNullTrainNameCannotBeAdded() {
        interlocking.addTrain(null, 1, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDuplicateActiveTrainNameRejected() {
        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainA", 10, 2);
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotAddTrainToOccupiedEntry() {
        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainB", 1, 9);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidPassengerToFreightRoute() {
        interlocking.addTrain("TrainA", 1, 11);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidFreightToPassengerRoute() {
        interlocking.addTrain("TrainA", 3, 8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidReversePassengerRoute() {
        interlocking.addTrain("TrainA", 8, 1);
    }

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

    // =========================================================
    // VALID ENTRY / DESTINATION PAIRS
    // =========================================================

    @Test
    public void testAllValidRoutesCanBeAddedIndividually() {
        InterlockingImpl x;

        x = new InterlockingImpl();
        x.addTrain("A", 1, 8);
        assertEquals(1, x.getTrain("A"));

        x = new InterlockingImpl();
        x.addTrain("A", 1, 9);
        assertEquals(1, x.getTrain("A"));

        x = new InterlockingImpl();
        x.addTrain("A", 9, 2);
        assertEquals(9, x.getTrain("A"));

        x = new InterlockingImpl();
        x.addTrain("A", 10, 2);
        assertEquals(10, x.getTrain("A"));

        x = new InterlockingImpl();
        x.addTrain("A", 3, 4);
        assertEquals(3, x.getTrain("A"));

        x = new InterlockingImpl();
        x.addTrain("A", 3, 11);
        assertEquals(3, x.getTrain("A"));

        x = new InterlockingImpl();
        x.addTrain("A", 4, 3);
        assertEquals(4, x.getTrain("A"));

        x = new InterlockingImpl();
        x.addTrain("A", 11, 3);
        assertEquals(11, x.getTrain("A"));
    }

    // =========================================================
    // BASIC MOVEMENT / EXIT TESTS
    // =========================================================

    @Test
    public void testPassengerOneToEightComplete() {
        interlocking.addTrain("P", 1, 8);

        assertEquals(1, interlocking.moveTrains(new String[]{"P"}));
        assertEquals(5, interlocking.getTrain("P"));

        assertEquals(1, interlocking.moveTrains(new String[]{"P"}));
        assertEquals(8, interlocking.getTrain("P"));

        assertEquals(1, interlocking.moveTrains(new String[]{"P"}));
        assertEquals(-1, interlocking.getTrain("P"));
        assertNull(interlocking.getSection(8));
    }

    @Test
    public void testPassengerOneToNineComplete() {
        interlocking.addTrain("P", 1, 9);

        interlocking.moveTrains(new String[]{"P"});
        assertEquals(5, interlocking.getTrain("P"));

        interlocking.moveTrains(new String[]{"P"});
        assertEquals(9, interlocking.getTrain("P"));

        interlocking.moveTrains(new String[]{"P"});
        assertEquals(-1, interlocking.getTrain("P"));
    }

    @Test
    public void testPassengerNineToTwoComplete() {
        interlocking.addTrain("P", 9, 2);

        interlocking.moveTrains(new String[]{"P"});
        assertEquals(6, interlocking.getTrain("P"));

        interlocking.moveTrains(new String[]{"P"});
        assertEquals(2, interlocking.getTrain("P"));

        interlocking.moveTrains(new String[]{"P"});
        assertEquals(-1, interlocking.getTrain("P"));
    }

    @Test
    public void testPassengerTenToTwoComplete() {
        interlocking.addTrain("P", 10, 2);

        interlocking.moveTrains(new String[]{"P"});
        assertEquals(6, interlocking.getTrain("P"));

        interlocking.moveTrains(new String[]{"P"});
        assertEquals(2, interlocking.getTrain("P"));

        interlocking.moveTrains(new String[]{"P"});
        assertEquals(-1, interlocking.getTrain("P"));
    }

    @Test
    public void testFreightThreeToFourComplete() {
        interlocking.addTrain("F", 3, 4);

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(4, interlocking.getTrain("F"));

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(-1, interlocking.getTrain("F"));
    }

    @Test
    public void testFreightFourToThreeComplete() {
        interlocking.addTrain("F", 4, 3);

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(3, interlocking.getTrain("F"));

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(-1, interlocking.getTrain("F"));
    }

    @Test
    public void testFreightThreeToElevenComplete() {
        interlocking.addTrain("F", 3, 11);

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(7, interlocking.getTrain("F"));

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(11, interlocking.getTrain("F"));

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(-1, interlocking.getTrain("F"));
    }

    @Test
    public void testFreightElevenToThreeComplete() {
        interlocking.addTrain("F", 11, 3);

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(7, interlocking.getTrain("F"));

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(3, interlocking.getTrain("F"));

        interlocking.moveTrains(new String[]{"F"});
        assertEquals(-1, interlocking.getTrain("F"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testExitedTrainCannotMoveAgain() {
        interlocking.addTrain("P", 1, 8);
        interlocking.moveTrains(new String[]{"P"});
        interlocking.moveTrains(new String[]{"P"});
        interlocking.moveTrains(new String[]{"P"});
        interlocking.moveTrains(new String[]{"P"});
    }

    @Test
    public void testTrainNameCanBeReusedAfterExit() {
        interlocking.addTrain("Reusable", 1, 8);

        interlocking.moveTrains(new String[]{"Reusable"});
        interlocking.moveTrains(new String[]{"Reusable"});
        interlocking.moveTrains(new String[]{"Reusable"});

        assertEquals(-1, interlocking.getTrain("Reusable"));

        interlocking.addTrain("Reusable", 10, 2);
        assertEquals(10, interlocking.getTrain("Reusable"));
    }

    // =========================================================
    // moveTrains INPUT VALIDATION
    // =========================================================

    @Test
    public void testEmptyMoveArrayMovesNothing() {
        assertEquals(0, interlocking.moveTrains(new String[]{}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullMoveArray() {
        interlocking.moveTrains(null);
    }

    @Test
    public void testNullNamePreventsPartialMovement() {
        interlocking.addTrain("A", 1, 8);

        try {
            interlocking.moveTrains(new String[]{"A", null});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        assertEquals(1, interlocking.getTrain("A"));
        assertEquals("A", interlocking.getSection(1));
        assertNull(interlocking.getSection(5));
    }

    @Test
    public void testUnknownNamePreventsPartialMovement() {
        interlocking.addTrain("A", 1, 8);

        try {
            interlocking.moveTrains(new String[]{"A", "Unknown"});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        assertEquals(1, interlocking.getTrain("A"));
    }

    @Test
    public void testDuplicateNameMovesOnlyOnce() {
        interlocking.addTrain("A", 1, 8);

        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"A", "A", "A"}));

        assertEquals(5, interlocking.getTrain("A"));
    }

    // =========================================================
    // PASSENGER JUNCTION / DESTINATION CONFLICT TESTS
    // =========================================================

    @Test
    public void testNineAndTenBothBlockedWhenCompetingForSix() {
        interlocking.addTrain("From9", 9, 2);
        interlocking.addTrain("From10", 10, 2);

        int moved = interlocking.moveTrains(
                new String[]{"From9", "From10"});

        assertEquals(0, moved);
        assertEquals(9, interlocking.getTrain("From9"));
        assertEquals(10, interlocking.getTrain("From10"));
        assertNull(interlocking.getSection(6));
    }

    @Test
    public void testConflictForSixIndependentOfArgumentOrder() {
        interlocking.addTrain("From9", 9, 2);
        interlocking.addTrain("From10", 10, 2);

        int moved = interlocking.moveTrains(
                new String[]{"From10", "From9"});

        assertEquals(0, moved);
        assertEquals(9, interlocking.getTrain("From9"));
        assertEquals(10, interlocking.getTrain("From10"));
        assertNull(interlocking.getSection(6));
    }

    // =========================================================
    // PASSENGER PRIORITY AT FREIGHT CROSSOVER
    // =========================================================

    @Test
    public void testSouthPassengerPriorityOverFreight() {
        interlocking.addTrain("Passenger", 1, 8);
        interlocking.addTrain("Freight", 3, 4);

        int moved = interlocking.moveTrains(
                new String[]{"Freight", "Passenger"});

        assertEquals(1, moved);
        assertEquals(5, interlocking.getTrain("Passenger"));
        assertEquals(3, interlocking.getTrain("Freight"));
    }

    @Test
    public void testNorthPassengerPriorityOverFreight() {
        interlocking.addTrain("Passenger", 9, 2);
        interlocking.moveTrains(new String[]{"Passenger"});
        assertEquals(6, interlocking.getTrain("Passenger"));

        interlocking.addTrain("Freight", 3, 4);

        int moved = interlocking.moveTrains(
                new String[]{"Freight", "Passenger"});

        assertEquals(1, moved);
        assertEquals(2, interlocking.getTrain("Passenger"));
        assertEquals(3, interlocking.getTrain("Freight"));
    }

    @Test
    public void testFreightWaitsIfSouthPassengerPresentEvenIfPassengerNotRequested() {
        interlocking.addTrain("Passenger", 1, 8);
        interlocking.addTrain("Freight", 3, 4);

        assertEquals(
                0,
                interlocking.moveTrains(
                        new String[]{"Freight"}));

        assertEquals(1, interlocking.getTrain("Passenger"));
        assertEquals(3, interlocking.getTrain("Freight"));
    }

    @Test
    public void testFreightWaitsIfNorthPassengerPresentEvenIfPassengerNotRequested() {
        interlocking.addTrain("Passenger", 10, 2);
        interlocking.moveTrains(new String[]{"Passenger"});
        assertEquals(6, interlocking.getTrain("Passenger"));

        interlocking.addTrain("Freight", 3, 4);

        assertEquals(
                0,
                interlocking.moveTrains(
                        new String[]{"Freight"}));

        assertEquals(6, interlocking.getTrain("Passenger"));
        assertEquals(3, interlocking.getTrain("Freight"));
    }

    @Test
    public void testFreightMovesWhenPassengerApproachesFree() {
        interlocking.addTrain("Freight", 3, 4);

        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"Freight"}));

        assertEquals(4, interlocking.getTrain("Freight"));
    }

    @Test
    public void testBothPassengerCrossingsMoveTogether() {
        interlocking.addTrain("North", 9, 2);
        interlocking.moveTrains(new String[]{"North"});
        assertEquals(6, interlocking.getTrain("North"));

        interlocking.addTrain("South", 1, 8);

        assertEquals(
                2,
                interlocking.moveTrains(
                        new String[]{"South", "North"}));

        assertEquals(5, interlocking.getTrain("South"));
        assertEquals(2, interlocking.getTrain("North"));
    }

    // =========================================================
    // GRADESCOPE-ALIGNED SIMULTANEOUS MOVEMENT TESTS
    // =========================================================

    /**
     * Observed Gradescope pattern:
     * section 2 exits while section 6 enters 2 and section 10 enters 6.
     */
    @Test
    public void testNorthPassengerThreeTrainPipelineMovesTogether() {
        interlocking.addTrain("Front", 10, 2);
        interlocking.moveTrains(new String[]{"Front"}); // 10 -> 6
        interlocking.moveTrains(new String[]{"Front"}); // 6 -> 2
        assertEquals(2, interlocking.getTrain("Front"));

        interlocking.addTrain("Middle", 10, 2);
        interlocking.moveTrains(new String[]{"Middle"}); // 10 -> 6
        assertEquals(6, interlocking.getTrain("Middle"));

        interlocking.addTrain("Back", 10, 2);
        assertEquals(10, interlocking.getTrain("Back"));

        assertEquals(
                3,
                interlocking.moveTrains(
                        new String[]{"Front", "Middle", "Back"}));

        assertEquals(-1, interlocking.getTrain("Front"));
        assertEquals(2, interlocking.getTrain("Middle"));
        assertEquals(6, interlocking.getTrain("Back"));

        assertEquals("Middle", interlocking.getSection(2));
        assertEquals("Back", interlocking.getSection(6));
        assertNull(interlocking.getSection(10));
    }

    @Test
    public void testNinePassengerPipelineMovesIntoVacatedSections() {
        interlocking.addTrain("Front", 9, 2);
        interlocking.moveTrains(new String[]{"Front"}); // 9 -> 6
        interlocking.moveTrains(new String[]{"Front"}); // 6 -> 2

        interlocking.addTrain("Middle", 9, 2);
        interlocking.moveTrains(new String[]{"Middle"}); // 9 -> 6

        interlocking.addTrain("Back", 9, 2);

        assertEquals(
                3,
                interlocking.moveTrains(
                        new String[]{"Front", "Middle", "Back"}));

        assertEquals(-1, interlocking.getTrain("Front"));
        assertEquals(2, interlocking.getTrain("Middle"));
        assertEquals(6, interlocking.getTrain("Back"));
    }

    @Test
    public void testExitingTrainFreesSectionForSameMovementCall() {
        interlocking.addTrain("Front", 9, 2);
        interlocking.moveTrains(new String[]{"Front"});
        interlocking.moveTrains(new String[]{"Front"});
        assertEquals(2, interlocking.getTrain("Front"));

        interlocking.addTrain("Back", 10, 2);
        interlocking.moveTrains(new String[]{"Back"});
        assertEquals(6, interlocking.getTrain("Back"));

        assertEquals(
                2,
                interlocking.moveTrains(
                        new String[]{"Front", "Back"}));

        assertEquals(-1, interlocking.getTrain("Front"));
        assertEquals(2, interlocking.getTrain("Back"));
    }

    @Test
    public void testSouthPassengerPipelineFiveToNineAndOneToFive() {
        interlocking.addTrain("At9", 1, 9);
        interlocking.moveTrains(new String[]{"At9"});
        interlocking.moveTrains(new String[]{"At9"});
        assertEquals(9, interlocking.getTrain("At9"));

        interlocking.addTrain("At5", 1, 9);
        interlocking.moveTrains(new String[]{"At5"});
        assertEquals(5, interlocking.getTrain("At5"));

        interlocking.addTrain("At1", 1, 8);
        assertEquals(1, interlocking.getTrain("At1"));

        assertEquals(
                3,
                interlocking.moveTrains(
                        new String[]{"At9", "At5", "At1"}));

        assertEquals(-1, interlocking.getTrain("At9"));
        assertEquals(9, interlocking.getTrain("At5"));
        assertEquals(5, interlocking.getTrain("At1"));
    }

    @Test
    public void testSouthPassengerPipelineFiveToEightAndOneToFive() {
        interlocking.addTrain("At8", 1, 8);
        interlocking.moveTrains(new String[]{"At8"});
        interlocking.moveTrains(new String[]{"At8"});
        assertEquals(8, interlocking.getTrain("At8"));

        interlocking.addTrain("At5", 1, 8);
        interlocking.moveTrains(new String[]{"At5"});
        assertEquals(5, interlocking.getTrain("At5"));

        interlocking.addTrain("At1", 1, 9);

        assertEquals(
                3,
                interlocking.moveTrains(
                        new String[]{"At8", "At5", "At1"}));

        assertEquals(-1, interlocking.getTrain("At8"));
        assertEquals(8, interlocking.getTrain("At5"));
        assertEquals(5, interlocking.getTrain("At1"));
    }

    // =========================================================
    // FREIGHT SIMULTANEOUS MOVEMENT TESTS
    // =========================================================

    @Test
    public void testSouthMainFreightPipelineMovesTogether() {
        interlocking.addTrain("Front", 3, 11);
        interlocking.moveTrains(new String[]{"Front"}); // 3 -> 7
        interlocking.moveTrains(new String[]{"Front"}); // 7 -> 11
        assertEquals(11, interlocking.getTrain("Front"));

        interlocking.addTrain("Middle", 3, 11);
        interlocking.moveTrains(new String[]{"Middle"}); // 3 -> 7
        assertEquals(7, interlocking.getTrain("Middle"));

        interlocking.addTrain("Back", 3, 11);

        assertEquals(
                3,
                interlocking.moveTrains(
                        new String[]{"Front", "Middle", "Back"}));

        assertEquals(-1, interlocking.getTrain("Front"));
        assertEquals(11, interlocking.getTrain("Middle"));
        assertEquals(7, interlocking.getTrain("Back"));
    }

    @Test
    public void testNorthMainFreightPipelineMovesTogether() {
        interlocking.addTrain("Front", 11, 3);
        interlocking.moveTrains(new String[]{"Front"}); // 11 -> 7
        interlocking.moveTrains(new String[]{"Front"}); // 7 -> 3
        assertEquals(3, interlocking.getTrain("Front"));

        interlocking.addTrain("Middle", 11, 3);
        interlocking.moveTrains(new String[]{"Middle"}); // 11 -> 7
        assertEquals(7, interlocking.getTrain("Middle"));

        interlocking.addTrain("Back", 11, 3);

        assertEquals(
                3,
                interlocking.moveTrains(
                        new String[]{"Front", "Middle", "Back"}));

        assertEquals(-1, interlocking.getTrain("Front"));
        assertEquals(3, interlocking.getTrain("Middle"));
        assertEquals(7, interlocking.getTrain("Back"));
    }

    /**
     * Gradescope successfully constructs opposing freight trains.
     * Therefore addTrain() should not reject one merely because another
     * freight train is travelling in the opposite direction.
     */
    @Test
    public void testOpposingMainFreightTrainsMayBothBeAddedWhenEntriesFree() {
        interlocking.addTrain("South", 3, 11);
        interlocking.addTrain("North", 11, 3);

        assertEquals(3, interlocking.getTrain("South"));
        assertEquals(11, interlocking.getTrain("North"));
    }

    @Test
    public void testOpposingWorkshopFreightTrainsMayBothBeAddedWhenEntriesFree() {
        interlocking.addTrain("South", 3, 4);
        interlocking.addTrain("North", 4, 3);

        assertEquals(3, interlocking.getTrain("South"));
        assertEquals(4, interlocking.getTrain("North"));
    }

    @Test
    public void testSameDirectionMainFreightCanOccupyElevenSevenThree() {
        interlocking.addTrain("Front", 3, 11);
        interlocking.moveTrains(new String[]{"Front"});
        interlocking.moveTrains(new String[]{"Front"});
        assertEquals(11, interlocking.getTrain("Front"));

        interlocking.addTrain("Middle", 3, 11);
        interlocking.moveTrains(new String[]{"Middle"});
        assertEquals(7, interlocking.getTrain("Middle"));

        interlocking.addTrain("Back", 3, 11);
        assertEquals(3, interlocking.getTrain("Back"));

        assertEquals("Front", interlocking.getSection(11));
        assertEquals("Middle", interlocking.getSection(7));
        assertEquals("Back", interlocking.getSection(3));
    }

    // =========================================================
    // COLLISION / DEADLOCK SAFETY TESTS
    // =========================================================

    @Test
    public void testTrainCannotMoveIntoOccupiedSectionIfOccupantNotMoving() {
        interlocking.addTrain("Front", 9, 2);
        interlocking.moveTrains(new String[]{"Front"});
        interlocking.moveTrains(new String[]{"Front"});
        assertEquals(2, interlocking.getTrain("Front"));

        interlocking.addTrain("Back", 10, 2);
        interlocking.moveTrains(new String[]{"Back"});
        assertEquals(6, interlocking.getTrain("Back"));

        assertEquals(
                0,
                interlocking.moveTrains(
                        new String[]{"Back"}));

        assertEquals(2, interlocking.getTrain("Front"));
        assertEquals(6, interlocking.getTrain("Back"));
    }

    @Test
    public void testPassengerAtSixBlocksTrainAtTenIfSixDoesNotMove() {
        interlocking.addTrain("At6", 9, 2);
        interlocking.moveTrains(new String[]{"At6"});
        assertEquals(6, interlocking.getTrain("At6"));

        interlocking.addTrain("At10", 10, 2);

        assertEquals(
                0,
                interlocking.moveTrains(
                        new String[]{"At10"}));

        assertEquals(10, interlocking.getTrain("At10"));
        assertEquals(6, interlocking.getTrain("At6"));
    }

    @Test
    public void testHeadOnWorkshopSwapDoesNotOccur() {
        interlocking.addTrain("South", 3, 4);
        interlocking.addTrain("North", 4, 3);

        assertEquals(
                0,
                interlocking.moveTrains(
                        new String[]{"South", "North"}));

        assertEquals(3, interlocking.getTrain("South"));
        assertEquals(4, interlocking.getTrain("North"));
    }

    @Test
    public void testHeadOnMainFreightDoesNotSwapThroughSectionSeven() {
        interlocking.addTrain("South", 3, 11);
        interlocking.addTrain("North", 11, 3);

        // First request South only, putting South at 7.
        assertEquals(
                1,
                interlocking.moveTrains(
                        new String[]{"South"}));

        assertEquals(7, interlocking.getTrain("South"));
        assertEquals(11, interlocking.getTrain("North"));

        // North wants 7, but South is not being moved.
        assertEquals(
                0,
                interlocking.moveTrains(
                        new String[]{"North"}));

        assertEquals(7, interlocking.getTrain("South"));
        assertEquals(11, interlocking.getTrain("North"));
    }

    // =========================================================
    // MIXED PASSENGER + FREIGHT TESTS
    // =========================================================

    @Test
    public void testIndependentPassengerAndMainFreightMovesCanOccurTogether() {
        interlocking.addTrain("Passenger", 1, 9);
        interlocking.addTrain("Freight", 3, 11);

        assertEquals(
                2,
                interlocking.moveTrains(
                        new String[]{"Passenger", "Freight"}));

        assertEquals(5, interlocking.getTrain("Passenger"));
        assertEquals(7, interlocking.getTrain("Freight"));
    }

    @Test
    public void testWorkshopExitAndMainFreightMovementCanOccurTogether() {
        interlocking.addTrain("Workshop", 3, 4);
        interlocking.moveTrains(new String[]{"Workshop"});
        assertEquals(4, interlocking.getTrain("Workshop"));

        interlocking.addTrain("Main", 11, 3);
        interlocking.moveTrains(new String[]{"Main"});
        assertEquals(7, interlocking.getTrain("Main"));

        assertEquals(
                2,
                interlocking.moveTrains(
                        new String[]{"Workshop", "Main"}));

        assertEquals(-1, interlocking.getTrain("Workshop"));
        assertEquals(3, interlocking.getTrain("Main"));
    }

    @Test
    public void testFreightBlockedWhilePassengerAtOneButMainFreightStillIndependent() {
        interlocking.addTrain("Passenger", 1, 8);
        interlocking.addTrain("Workshop", 3, 4);
        interlocking.addTrain("MainNorth", 11, 3);

        assertEquals(
                2,
                interlocking.moveTrains(
                        new String[]{"Workshop", "Passenger", "MainNorth"}));

        assertEquals(3, interlocking.getTrain("Workshop"));
        assertEquals(5, interlocking.getTrain("Passenger"));
        assertEquals(7, interlocking.getTrain("MainNorth"));
    }

    // =========================================================
    // GRADESCOPE CONFLICT REGRESSION TESTS
    // =========================================================

    @Test
    public void testTwoTrainsCompetingForVacatedSixBothWait() {
        interlocking.addTrain("Front", 9, 2);
        interlocking.moveTrains(new String[]{"Front"});
        assertEquals(6, interlocking.getTrain("Front"));

        interlocking.addTrain("From9", 9, 2);
        interlocking.addTrain("From10", 10, 2);

        int moved = interlocking.moveTrains(
                new String[]{"Front", "From9", "From10"});

        assertEquals(1, moved);
        assertEquals(2, interlocking.getTrain("Front"));
        assertEquals(9, interlocking.getTrain("From9"));
        assertEquals(10, interlocking.getTrain("From10"));
    }

    @Test
    public void testOpposingMainFreightBothBlockedFromSectionSeven() {
        interlocking.addTrain("South", 3, 11);
        interlocking.addTrain("North", 11, 3);

        int moved = interlocking.moveTrains(
                new String[]{"South", "North"});

        assertEquals(0, moved);
        assertEquals(3, interlocking.getTrain("South"));
        assertEquals(11, interlocking.getTrain("North"));
        assertNull(interlocking.getSection(7));
    }

    @Test
    public void testWorkshopAndMainTrainBothBlockedWhenCompetingForThree() {
        interlocking.addTrain("AtThree", 11, 3);
        interlocking.moveTrains(new String[]{"AtThree"});
        interlocking.moveTrains(new String[]{"AtThree"});
        assertEquals(3, interlocking.getTrain("AtThree"));

        interlocking.addTrain("MainFollower", 11, 3);
        interlocking.moveTrains(new String[]{"MainFollower"});
        assertEquals(7, interlocking.getTrain("MainFollower"));

        interlocking.addTrain("Workshop", 4, 3);

        int moved = interlocking.moveTrains(
                new String[]{"Workshop", "AtThree", "MainFollower"});

        assertEquals(1, moved);
        assertEquals(-1, interlocking.getTrain("AtThree"));
        assertEquals(4, interlocking.getTrain("Workshop"));
        assertEquals(7, interlocking.getTrain("MainFollower"));
        assertNull(interlocking.getSection(3));
    }

    // =========================================================
    // STATE CONSISTENCY TESTS
    // =========================================================

    @Test
    public void testSectionStateMatchesTrainStateAfterMovement() {
        interlocking.addTrain("A", 1, 8);

        interlocking.moveTrains(new String[]{"A"});

        assertNull(interlocking.getSection(1));
        assertEquals("A", interlocking.getSection(5));
        assertEquals(5, interlocking.getTrain("A"));
    }

    @Test
    public void testSectionStateMatchesTrainStateAfterExit() {
        interlocking.addTrain("A", 3, 4);

        interlocking.moveTrains(new String[]{"A"});
        assertEquals("A", interlocking.getSection(4));

        interlocking.moveTrains(new String[]{"A"});

        assertNull(interlocking.getSection(4));
        assertEquals(-1, interlocking.getTrain("A"));
    }

    @Test
    public void testFailedOccupiedEntryDoesNotCorruptExistingTrain() {
        interlocking.addTrain("Existing", 3, 4);

        try {
            interlocking.addTrain("Rejected", 3, 11);
            fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
            // expected
        }

        assertEquals(3, interlocking.getTrain("Existing"));
        assertEquals("Existing", interlocking.getSection(3));

        interlocking.moveTrains(new String[]{"Existing"});
        assertEquals(4, interlocking.getTrain("Existing"));
    }

    @Test
    public void testDifferentEntrySectionsCanBeOccupiedTogether() {
        interlocking.addTrain("P1", 1, 8);
        interlocking.addTrain("P9", 9, 2);
        interlocking.addTrain("P10", 10, 2);
        interlocking.addTrain("F3", 3, 11);
        interlocking.addTrain("F4", 4, 3);
        interlocking.addTrain("F11", 11, 3);

        assertEquals("P1", interlocking.getSection(1));
        assertEquals("P9", interlocking.getSection(9));
        assertEquals("P10", interlocking.getSection(10));
        assertEquals("F3", interlocking.getSection(3));
        assertEquals("F4", interlocking.getSection(4));
        assertEquals("F11", interlocking.getSection(11));
    }
}
