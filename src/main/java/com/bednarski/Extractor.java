package com.bednarski;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Extractor {

  private static final String EMPTY_STRING = "";

  private Extractor() { }

  public static String extract(final String input, final boolean shouldTrim) {
    if (!Validator.isValid(input)) {
      return EMPTY_STRING;
    }
    List<Character> chars = shouldTrim ? Trimmer.trim(toCharList(input)) : toCharList(input);

    int index = 0;
    while (mayContainHtmlTag(chars) && index < chars.size()) {
      HtmlTag firstTag = HtmlTag.findOne(0, chars);
      index = firstTag.getEndIndex();

      HtmlTag secondTag = HtmlTag.findOne(index, chars);
      index = secondTag.getEndIndex();

      while (!(firstTag.isOpening() && secondTag.isClosing())) {
        firstTag = secondTag;
        secondTag = HtmlTag.findOne(index, chars);
        index = secondTag.getEndIndex();
      }

      chars = firstTag.isPairWith(secondTag)
          ? getWithoutTags(chars, firstTag, secondTag)
          : getWithoutTagsAndContent(chars, firstTag, secondTag);
    }
    return getWithoutRemainingTags(chars)
        .stream()
        .map(Object::toString)
        .collect(Collectors.joining(EMPTY_STRING));
  }

  private static List<Character> toCharList(final String text) {
    return text
        .chars()
        .mapToObj(codePointValue -> (char) codePointValue)
        .collect(Collectors.toList());
  }

  private static boolean mayContainHtmlTag(final List<Character> chars) {
    return chars.contains('<')
        && chars.contains('>');
  }

  private static List<Character> getWithoutTags(
      final List<Character> chars, final HtmlTag opening, final HtmlTag closing) {
    List<Character> beforeOpeningTag = chars.subList(0, opening.getStartIndex());
    List<Character> inBetweenTags = chars.subList(opening.getEndIndex() + 1, closing.getStartIndex());
    List<Character> afterClosingTag = chars.subList(closing.getEndIndex() + 1, chars.size());
    List<Character> remainingTags =
        new ArrayList<>(beforeOpeningTag.size() + inBetweenTags.size() + afterClosingTag.size());
    remainingTags.addAll(beforeOpeningTag);
    remainingTags.addAll(inBetweenTags);
    remainingTags.addAll(afterClosingTag);
    return remainingTags;
  }

  private static List<Character> getWithoutTagsAndContent(
      final List<Character> chars, final HtmlTag firstTag, final HtmlTag secondTag) {
    List<Character> remainingChars = new ArrayList<>(chars);
    remainingChars
        .subList(firstTag.getStartIndex(), secondTag.getEndIndex() + 1)
        .clear();
    return remainingChars;
  }

  private static List<Character> getWithoutRemainingTags(final List<Character> chars) {
    if (!mayContainHtmlTag(chars)) {
      return chars;
    }
    List<Character> remainingChars = new ArrayList<>(chars);
    while (mayContainHtmlTag(remainingChars)) {
      int index = 0;
      HtmlTag tag = HtmlTag.findOne(index, remainingChars);
      index = tag.getEndIndex();
      if (Identifier.isTagClosingChar(remainingChars.get(index))) {
        remainingChars
            .subList(tag.getStartIndex(), tag.getEndIndex() + 1)
            .clear();
      }
    }
    return remainingChars;
  }
}
