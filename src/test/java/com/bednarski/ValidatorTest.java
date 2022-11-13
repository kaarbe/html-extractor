package com.bednarski;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

  @ParameterizedTest
  @MethodSource("getStringsForValidation")
  void checksIfValid(String input, boolean expectedResult) {
    boolean result = Validator.isValid(input);

    assertEquals(expectedResult, result);
  }

  static Stream<Arguments> getStringsForValidation() {
    return Stream.of(
        Arguments.of("", false),
        Arguments.of("<<<<>>>><</>/>", false),
        Arguments.of("1234567890", false),
        Arguments.of("test input without any tags", false),
        Arguments.of("<a></a>", false),
        Arguments.of("<h1>test input with some tags</h1>", true),
        Arguments.of("<a>a</a>", true),
        Arguments.of("<a>b</a>", true)
    );
  }
}