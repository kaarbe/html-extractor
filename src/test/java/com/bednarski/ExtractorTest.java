package com.bednarski;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ExtractorTest {

  @InjectMocks
  Extractor extractor;

  @Test
  void shouldExtractFromHtmlWithoutNesting() {
    // given
    var htmlInput =
        "hej</h1><h1>Dog</h1>" +
        "<h1>Cat</h1>" +
        "<h1>Fish</H1><h1>hej";
    var correctlyExtractedText = "DogCat";

    // when
    String result = extractor.extractPlainText(htmlInput, true);

    // then
    assertEquals(correctlyExtractedText, result);
  }

  @Test
  void shouldExtractFromHtmlWithNesting() {
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
