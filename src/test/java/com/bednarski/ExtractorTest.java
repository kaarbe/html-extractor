package com.bednarski;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExtractorTest {

  @InjectMocks
  Extractor extractor;

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldExtractAndTrimFromHtmlWithoutNesting(boolean shouldTrim) {
    // given
    var htmlInput =
        "hej</h1><h1>Dog</h1>" +
        "<h1>Cat</h1>" +
        "<h1>Fish</H1>Hej";
    var expected = "hejDogCatHej";
    var expectedWithTrimming = "DogCat";

    // when
    String result = extractor.extractPlainText(htmlInput, shouldTrim);

    // then
    assertEquals(shouldTrim ? expectedWithTrimming : expected, result);
  }

  @Test
  void shouldExtractAndTrimFromHtmlWithNesting() {
    // given
    var htmlInput =
        "<h1>" +
            "Dog" +
            "<h1>AnotherDog</h1>" +
        "</h1>" +
        "<h1>Cat</h1>" +
        "<h1>Fish</H1>";
    var correctlyExtractedText = "DogAnotherDogCat";

    // when
    String result = extractor.extractPlainText(htmlInput, true);

    // then
    assertEquals(correctlyExtractedText, result);
  }
}
