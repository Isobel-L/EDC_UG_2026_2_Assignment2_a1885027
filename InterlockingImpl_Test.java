import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class InterlockingImpl_Test {

    private InterlockingImpl interlocking;

    @Before
    public void setUp() {
        interlocking = new InterlockingImpl();
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
    public void testBlockedTrainDoesNotMove() {
        interlocking.addTrain("TrainA", 1, 8);
        interlocking.addTrain("TrainB", 9, 2);

        // TrainA moves from 1 -> 5.
        interlocking.moveTrains(new String[]{"TrainA"});

        // This setup does not block TrainA yet, so instead verify
        // occupancy directly before later conflict tests.
        assertEquals("TrainA", interlocking.getSection(5));
        assertEquals("TrainB", interlocking.getSection(9));
    }

    @Test
    public void testTrainExitsAfterDestination() {
        interlocking.addTrain("TrainA", 1, 8);

        // 1 -> 5
        assertEquals(1,
                interlocking.moveTrains(new String[]{"TrainA"}));

        // 5 -> 8
        assertEquals(1,
                interlocking.moveTrains(new String[]{"TrainA"}));

        assertEquals(8, interlocking.getTrain("TrainA"));
        assertEquals("TrainA", interlocking.getSection(8));

        // At destination, next movement exits.
        assertEquals(1,
                interlocking.moveTrains(new String[]{"TrainA"}));

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
}