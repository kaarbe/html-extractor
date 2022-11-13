package com.bednarski;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Extractor {

  private static final String EMPTY_STRING = "";

  public String extractPlainText(final String input, final boolean shouldTrim) {
    if (!Validator.isValid(input)) {
      return EMPTY_STRING;
    }
    List<Character> chars = shouldTrim ? trim(toCharList(input)) : toCharList(input);

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

  private List<Character> toCharList(final String text) {
    return text
        .chars()
        .mapToObj(codePointValue -> (char) codePointValue)
        .collect(Collectors.toList());
  }

  private List<Character> trim(final List<Character> chars) {
    CompletableFuture<Integer> lastValidCharIndex = findLastValidHtmlTagCharIndex(chars);
    CompletableFuture<Integer> firstValidCharIndex = findFirstValidHtmlTagCharIndex(chars);
    CompletableFuture.allOf(lastValidCharIndex, firstValidCharIndex).join();

    List<Character> trimmedCharList = new ArrayList<>(chars);
    Integer endIndex = lastValidCharIndex.join();
    if (endIndex != -1) {
      trimmedCharList
          .subList(endIndex, chars.size())
          .clear();
    }
    Integer startIndex = firstValidCharIndex.join();
    if (startIndex != -1) {
      trimmedCharList
          .subList(0, startIndex)
          .clear();
    }
    return trimmedCharList;
  }

  private CompletableFuture<Integer> findLastValidHtmlTagCharIndex(final List<Character> chars) {
    return CompletableFuture.supplyAsync(() -> {
      if (Identifier.isTagClosingChar(chars.get(chars.size() - 1))) {
        return -1;
      }
      int i = chars.size() - 1;
      while (i >= 0 && !Identifier.isTagClosingChar(chars.get(i))) {
        i--;
      }
      int j = i;
      while (j >= 0 && !Identifier.isTagOpeningChar(chars.get(j))) {
        j--;
      }
      return !Character.valueOf('/').equals(chars.get(j + 1)) ? j : i + 1;
    });
  }

  private CompletableFuture<Integer> findFirstValidHtmlTagCharIndex(final List<Character> chars) {
    return CompletableFuture.supplyAsync(() -> {
      if (Identifier.isTagOpeningChar(chars.get(0))) {
        return -1;
      }
      int i = 0;
      int j = 0;
      while (i < chars.size() && !Identifier.isTagOpeningChar(chars.get(i))) {
        i++;
        if (Character.valueOf('/').equals(chars.get(i + 1))) {
          j = i + 1;
          while (!Identifier.isTagClosingChar(chars.get(j))) {
            j++;
          }
        }
      }
      return Math.max(i, ++j);
    });
  }

  private boolean mayContainHtmlTag(final List<Character> chars) {
    return chars.contains('<')
        && chars.contains('>');
  }

  private List<Character> getWithoutTags(final List<Character> chars, final HtmlTag opening, final HtmlTag closing) {
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

  private List<Character> getWithoutTagsAndContent(
      final List<Character> chars, final HtmlTag firstTag, final HtmlTag secondTag) {
    List<Character> remainingChars = new ArrayList<>(chars);
    remainingChars
        .subList(firstTag.getStartIndex(), secondTag.getEndIndex() + 1)
        .clear();
    return remainingChars;
  }

  private List<Character> getWithoutRemainingTags(final List<Character> chars) {
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
