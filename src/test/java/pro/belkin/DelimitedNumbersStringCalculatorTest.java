package pro.belkin;

import lombok.*;
import org.junit.jupiter.api.*;

import java.security.*;

import static org.junit.jupiter.api.Assertions.*;

@ToString
public class DelimitedNumbersStringCalculatorTest {
    private final StringCalculator calculator = new DelimitedNumbersStringCalculator();

    @Test
    public final void givenEmptyString_willReturnZero() {
        final int expected = 0;
        final int actual = calculator.add("");
        assertEquals(expected, actual);
    }

    @Test
    public final void givenSingleInt_willReturnGivenInt() {
        final int randomNumber = new SecureRandom().nextInt(100000);
        final int actual = calculator.add("//.\n" + randomNumber);
        assertEquals(randomNumber, actual);
    }

    @Test
    public final void givenMultipleDelimitedInts_willReturnSumOfInts() {
        final int expected = 15;
        assertEquals(expected, calculator.add("//{\n1{2{3{4{5"));
        assertEquals(expected, calculator.add("//[\n1[2[3[4[5"));
        assertEquals(expected, calculator.add("//(\n1(2(3(4(5"));
    }

    @Test
    public final void givenConsecutiveDelimiters_willThrowImplicitException() {
        assertThrows(
            NumberFormatException.class,
            () -> calculator.add("//.\n1.2..4.5")
        );
    }

    @Test
    public final void ifEndsWithDelimiter_willThrowExplicitException() {
        final Class<? extends Exception> endsWithDelimiter = DelimitedNumbersStringCalculator.DelimiterAtEndOfExpressionException.class;
        assertThrows(endsWithDelimiter, () -> calculator.add("//,\n1,2,"));
        assertThrows(endsWithDelimiter, () -> calculator.add("//|\n5678|21|1|69|0|"));
    }

    @Test
    public final void allowSpecifyingCustomDelimiter() {
        final int expected = 15;
        final int actual = calculator.add("//*++)\n1*++)2*++)3*++)4*++)5");
        assertEquals(expected, actual);
    }

    @Test
    public final void ifInvalidFormat_willThrowExplicitException() {
        final Class<? extends Exception> invalidFormat = DelimitedNumbersStringCalculator.InvalidFormatException.class;
        assertDoesNotThrow(() -> calculator.add("//.\n1.2.3.4.5"));
        assertThrows(invalidFormat, () -> calculator.add("//\n1.2.3.4.5"));
        assertThrows(invalidFormat, () -> calculator.add("//."));
        assertThrows(invalidFormat, () -> calculator.add("//.\n"));
        assertThrows(invalidFormat, () -> calculator.add("1.2.3.4.5"));
        assertThrows(invalidFormat, () -> calculator.add("\n1.2.3.4.5"));
        assertThrows(invalidFormat, () -> calculator.add("//.1.2.3.4.5"));
        assertThrows(invalidFormat, () -> calculator.add("//.\n1.2.3.4\n5"));
    }

    @Test
    public final void ifActualDelimitersDifferFromSpecified_willThrowExplicitException() {
        assertThrows(
            DelimitedNumbersStringCalculator.DifferentDelimitersException.class,
            () -> calculator.add("//i\n1i2i3i4_5")
        );
        assertThrows(
            DelimitedNumbersStringCalculator.DifferentDelimitersException.class,
            () -> calculator.add("//...\n1...2 3;4...5")
        );
        assertThrows(
            DelimitedNumbersStringCalculator.DifferentDelimitersException.class,
            () -> calculator.add("//i\n1dwd2w3;4,5")
        );
        assertThrows(
            DelimitedNumbersStringCalculator.DifferentDelimitersException.class,
            () -> calculator.add("//i\n1i2i3i4_5")
        );
        assertThrows(
            DelimitedNumbersStringCalculator.DifferentDelimitersException.class,
            () -> calculator.add("//i\n1i2i3i4_5")
        );
    }
}
