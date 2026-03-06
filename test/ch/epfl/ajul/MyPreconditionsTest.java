
package ch.epfl.ajul;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de {@link Preconditions}.
 *
 * @author Danny Levy (394098)
 * @author Elie Menashe Reuben Surman (410685)
 */
public final class MyPreconditionsTest {

    @Test
    void checkArgumentDoesNotThrowWhenTrue() {
        assertDoesNotThrow(() -> Preconditions.checkArgument(true));
    }

    @Test
    void checkArgumentThrowsWhenFalse() {
        assertThrows(IllegalArgumentException.class, () -> Preconditions.checkArgument(false));
    }
}