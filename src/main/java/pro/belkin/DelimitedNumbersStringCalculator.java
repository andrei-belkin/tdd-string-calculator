package pro.belkin;

import lombok.*;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

public class DelimitedNumbersStringCalculator implements StringCalculator {
    private static final Pattern SECTIONS_SEPARATOR = Pattern.compile("\\n");
    private static final Set<Character> REGEX_LIST_SPECIAL_SYMBOLS = Set.of(
        '/', '\\', '[', '^', '*', '+', '?', '{', '(', ')', '$', '|', '.'
    );

    @Override
    public final int add(final String delimitedNumbers) {
        if (isEmpty(delimitedNumbers))
            return 0;

        final String[] sections = SECTIONS_SEPARATOR.split(delimitedNumbers);
        if (!isExpressionFormatValid(sections))
            throw new InvalidFormatException();

        return sumNumbersSection(sections[1], sections[0].substring(2));
    }

    private static boolean isEmpty(final String input) {
        return input.strip().isBlank();
    }

    private static boolean isExpressionFormatValid(final String[] expressionSections) {
        final Set<Predicate<String[]>> conditions = Set.of(
            sections -> sections.length == 2,
            sections -> sections[0].startsWith("//") && sections[0].length() > 2
        );
        return conditions.stream().allMatch(condition -> condition.test(expressionSections));
    }

    private static int sumNumbersSection(final String rawNumbersSection, final String delimiter) {
        if (rawNumbersSection.endsWith(delimiter))
            throw new DelimiterAtEndOfExpressionException();

        final String[] numberStrings = splitRawNumbersSectionByDelimiter(rawNumbersSection, delimiter);
        final Set<Integer> parsedNumbers = tryToParseNumbersSection(rawNumbersSection, numberStrings);

        return sum(parsedNumbers);
    }

    private static String[] splitRawNumbersSectionByDelimiter(
        final String rawNumbersSection,
        final String delimiter
    ) {
        final String regexifiedDelimiter = regexify(delimiter);
        return rawNumbersSection.split(regexifiedDelimiter);
    }

    private static String regexify(final String delimiter) {
        final StringBuilder regexifiedDelimiter = new StringBuilder(delimiter.length());
        for (final char character : delimiter.toCharArray())
            regexifiedDelimiter.append(regexifySymbol(character));
        return regexifiedDelimiter.toString();
    }

    private static String regexifySymbol(final char character) {
        return REGEX_LIST_SPECIAL_SYMBOLS.contains(character) ? "\\" + character : String.valueOf(character);
    }

    private static Set<Integer> tryToParseNumbersSection(final String rawNumbersSection, final String[] numberStrings) {
        try {
            return parseNumbers(numberStrings);
        } catch (final ParsingException e) {
            throw new DifferentDelimitersException(rawNumbersSection.indexOf(e.delimiter));
        }
    }

    private static Set<Integer> parseNumbers(final String[] numberStrings) {
        final Set<Integer> numbers = new HashSet<>(numberStrings.length);
        for (final String numberString : numberStrings) {
            final int parsedNumber = parseNumber(numberString);
            numbers.add(parsedNumber);
        }
        return numbers;
    }

    private static int parseNumber(final String numberString) {
        final String strippedSegment = numberString.strip();
        return tryToParseIntFromString(strippedSegment);
    }

    private static int tryToParseIntFromString(final String strippedSegment) {
        try {
            return Integer.parseInt(strippedSegment);
        } catch (final NumberFormatException implicitException) {
            throw strippedSegment.isEmpty() ? implicitException : new ParsingException(strippedSegment);
        }
    }

    private static int sum(final Collection<Integer> parsedNumbers) {
        return parsedNumbers.stream()
            .reduce(Integer::sum)
            .orElse(0);
    }

    public static class DelimiterAtEndOfExpressionException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 8123886565294479440L;
    }

    public static class InvalidFormatException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = -4509026449212020210L;
    }

    @AllArgsConstructor
    private static class ParsingException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = 6626294015723958747L;

        public final String delimiter;
    }

    @AllArgsConstructor
    public static class DifferentDelimitersException extends RuntimeException {
        @Serial
        private static final long serialVersionUID = -7879833541999754606L;

        public final int delimiterPosition;
    }
}
