package ${basePackage};

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ${firstClassName}Test {

  private static final double TOLERANCE = 1e-9;

  @Test
  void distanceBetweenKnownPoints() {
    double[] origin = {0.0, 0.0, 0.0};
    double[] point = {1.0, 2.0, 2.0};
    assertEquals(3.0, ${firstClassName}.distance(origin, point), TOLERANCE);
  }

  @Test
  void distanceIsSymmetric() {
    double[] a = {0.5, -1.2, 3.3};
    double[] b = {-2.0, 0.7, 1.1};
    assertEquals(${firstClassName}.distance(a, b), ${firstClassName}.distance(b, a), TOLERANCE);
  }

  @Test
  void distanceToItselfIsZero() {
    double[] a = {1.0, 1.0, 1.0};
    assertEquals(0.0, ${firstClassName}.distance(a, a), TOLERANCE);
  }

  @Test
  void rejectsPointsWithoutThreeCoordinates() {
    assertThrows(IllegalArgumentException.class,
        () -> ${firstClassName}.distance(new double[] {1.0, 2.0}, new double[] {1.0, 2.0, 3.0}));
  }
}
