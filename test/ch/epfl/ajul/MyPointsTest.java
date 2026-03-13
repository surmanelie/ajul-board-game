package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MyPointsTest {

    @Test
    void fullRowBonusConstantIs2() {
        assertEquals(2, Points.FULL_ROW_BONUS_POINTS);
    }
    @Test
    void fullColumnBonusConstantIs7() {
        assertEquals(7, Points.FULL_COLUMN_BONUS_POINTS);
    }
    @Test
    void fullColorBonusConstantIs10() {
        assertEquals(10, Points.FULL_COLOR_BONUS_POINTS);
    }
    @Test
    void newWallPointsSingleTileIs1() {
        assertEquals(1, Points.newWallTilePoints(1, 1));
    }
    @Test
    void newWallPointsHorizontalOnlyUsesHorizontal() {
        assertEquals(3, Points.newWallTilePoints(3, 1));
    }
    @Test
    void newWallPointsVerticalOnlyUsesVertical() {
        assertEquals(4, Points.newWallTilePoints(1, 4));
    }
    @Test
    void newWallPointsTwoByTwoIsFour() {
        assertEquals(4, Points.newWallTilePoints(2, 2));
    }
    @Test
    void newWallPointsFullHorizontalIsFive() {
        assertEquals(5, Points.newWallTilePoints(5, 1));
    }
    @Test
    void newWallPointsFullVerticalIsFive() {
        assertEquals(5, Points.newWallTilePoints(1, 5));
    }
    @Test
    void newWallPointsCrossAddsBothSizes() {
        assertEquals(8, Points.newWallTilePoints(3, 5));
    }
    @Test
    void floorPenaltyIndex0() {
        assertEquals(1, Points.floorPenalty(0));
    }
    @Test
    void floorPenaltyIndex1() {
        assertEquals(1, Points.floorPenalty(1));
    }
    @Test
    void floorPenaltyIndex2() {
        assertEquals(2, Points.floorPenalty(2));
    }
    @Test
    void floorPenaltyIndex3() {
        assertEquals(2, Points.floorPenalty(3));
    }
    @Test
    void floorPenaltyIndex4() {
        assertEquals(2, Points.floorPenalty(4));
    }
    @Test
    void floorPenaltyIndex5() {
        assertEquals(3, Points.floorPenalty(5));
    }
    @Test
    void floorPenaltyIndex6() {
        assertEquals(3, Points.floorPenalty(6));
    }
    @Test
    void totalFloorPenaltyCount0() {
        assertEquals(0, Points.totalFloorPenalty(0));
    }
    @Test
    void totalFloorPenaltyCount1() {
        assertEquals(1, Points.totalFloorPenalty(1));
    }
    @Test
    void totalFloorPenaltyCount2() {
        assertEquals(2, Points.totalFloorPenalty(2));
    }
    @Test
    void totalFloorPenaltyCount3() {
        assertEquals(4, Points.totalFloorPenalty(3));
    }
    @Test
    void totalFloorPenaltyCount4() {
        assertEquals(6, Points.totalFloorPenalty(4));
    }
    @Test
    void totalFloorPenaltyCount5() {
        assertEquals(8, Points.totalFloorPenalty(5));
    }
    @Test
    void totalFloorPenaltyCount6() {
        assertEquals(11, Points.totalFloorPenalty(6));
    }
    @Test
    void totalFloorPenaltyCount7() {
        assertEquals(14, Points.totalFloorPenalty(7));
    }

    @Test
    void newWallTilePointsIsSymmetricWhenBothGroupsAreBiggerThan1() {
        assertEquals(Points.newWallTilePoints(2, 5), Points.newWallTilePoints(5, 2));
    }

    @Test
    void totalFloorPenaltyMatchesSumOfIndividualPenalties() {
        for (int k = 0; k <= 7; k++) {
            int sum = 0;
            for (int i = 0; i < k; i++) sum += Points.floorPenalty(i);
            assertEquals(sum, Points.totalFloorPenalty(k));
        }
    }

}
