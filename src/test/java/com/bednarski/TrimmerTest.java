package com.bednarski;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrimmerTest {

  @ParameterizedTest
  @MethodSource("getInputToTrim")
  void shouldTrim(List<Character> input, List<Character> expectedResult) {
    List<Character> trimmedInput = Trimmer.trim(input);

    assertEquals(expectedResult.toString(), trimmedInput.toString());
  }

  static Stream<Arguments> getInputToTrim() {
    return Stream.of(
        Arguments.of(
            "beginning to trim<h1>header text</h1>ending to trim"
                .chars()
                .mapToObj(codePointValue -> (char) codePointValue)
                .collect(Collectors.toList()),
            "<h1>header text</h1>"
                .chars()
                .mapToObj(codePointValue -> (char) codePointValue)
                .collect(Collectors.toList())
        ),
        Arguments.of(
            "<h1>header text</h1>"
                .chars()
                .mapToObj(codePointValue -> (char) codePointValue)
                .collect(Collectors.toList()),
            "<h1>header text</h1>"
                .chars()
                .mapToObj(codePointValue -> (char) codePointValue)
                .collect(Collectors.toList())
        ),
        Arguments.of(
            "all of it should be wiped out"
                .chars()
                .mapToObj(codePointValue -> (char) codePointValue)
                .collect(Collectors.toList()),
            Collections.emptyList()
        ),
        Arguments.of(
            Collections.emptyList(),
            Collections.emptyList()
        )
    );
  }
}