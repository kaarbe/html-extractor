package com.github.kaarbe;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class IdentifierTest {

  @ParameterizedTest
  @MethodSource("getPotentiallyClosingCharsAndExpectedResults")
  void checksIfTagClosingChar(char c, boolean expectedResult) {
    boolean result = Identifier.isTagClosingChar(c);

    assertEquals(expectedResult, result);
  }

  static Stream<Arguments> getPotentiallyClosingCharsAndExpectedResults() {
    return Stream.of(
        Arguments.of('>', true),
        Arguments.of('a', false),
        Arguments.of('1', false),
        Arguments.of('@', false),
        Arguments.of('<', false)
    );
  }

  @ParameterizedTest
  @MethodSource("getPotentiallyOpeningCharsAndExpectedResults")
  void isTagOpeningChar(char c, boolean expectedResult) {
    boolean result = Identifier.isTagOpeningChar(c);

    assertEquals(expectedResult, result);
  }

  static Stream<Arguments> getPotentiallyOpeningCharsAndExpectedResults() {
    return Stream.of(
        Arguments.of('<', true),
        Arguments.of('a', false),
        Arguments.of('1', false),
        Arguments.of('@', false),
        Arguments.of('>', false)
    );
  }
}