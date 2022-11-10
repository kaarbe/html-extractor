package com.bednarski;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Extractor {

  private static final String EMPTY_STRING = "";

  public String extractPlainText(String input, boolean shouldTrim) {
    List<Character> chars = toValidatedCharList(input);
    if (chars.isEmpty()) {
      return EMPTY_STRING;
    }
    if (shouldTrim) {
      trim(chars);
    }
    int index = 0;
    while (mayContainHtmlTag(chars) && index < chars.size()) {
      index = findFirstTagOpeningChar(chars);
      // save initial first tag
      HtmlTag firstTag = findEndingAndSaveHtmlTag(index, chars);
      index = firstTag.getEndIndex();
      // skip content in-between two tags (for now)
      index = findTagOpeningChar(index, chars);
      // initial second tag
      HtmlTag secondTag = findEndingAndSaveHtmlTag(index, chars);
      index = secondTag.getEndIndex();

      while (!(firstTag.isOpening() && secondTag.isClosing())) {
        // move first 'pointer' to the second 'pointer'
        firstTag = secondTag;
        // skip content in-between two tags (for now)
        index = findTagOpeningChar(index, chars);
        // set second 'pointer' on new html tag
        secondTag = findEndingAndSaveHtmlTag(index, chars);
        index = secondTag.getEndIndex();
      }

      chars = firstTag.isPairWith(secondTag)
          ? getWithoutTags(chars, firstTag, secondTag)
          : getWithoutTagsAndContent(chars, firstTag, secondTag);
    }

    return getWithoutRemainingTags(chars)
        .stream()
        .map(Object::toString)
        .collect(Collectors.joining(""));
  }

  private List<Character> toValidatedCharList(String text) {
    List<Character> chars = new ArrayList<>();
    for (char c : text.toCharArray()) {
      chars.add(c);
    }
    Set<Character> uniqueChars = new HashSet<>(chars);
    return uniqueChars.size() < 5 ? Collections.emptyList() : chars;
  }

  private void trim(List<Character> chars) {
    CompletableFuture<Integer> lastValidCharIndex = findLastValidHtmlTagCharIndex(chars);
    CompletableFuture<Integer> firstValidCharIndex = findFirstValidHtmlTagCharIndex(chars);
    CompletableFuture.allOf(lastValidCharIndex, firstValidCharIndex).join();
    Integer endIndex = lastValidCharIndex.join();
    if (endIndex != -1) {
      chars
          .subList(endIndex, chars.size())
          .clear();
    }
    Integer startIndex = firstValidCharIndex.join();
    if (startIndex != -1) {
      chars
          .subList(0, startIndex)
          .clear();
    }
  }

  private CompletableFuture<Integer> findLastValidHtmlTagCharIndex(List<Character> chars) {
    return CompletableFuture.supplyAsync(() -> {
      if (isTagClosingChar(chars.get(chars.size() - 1))) {
        return -1;
      }
      int i = chars.size() - 1;
      while (i >= 0 && !isTagClosingChar(chars.get(i))) {
        i--;
      }
      int j = i;
      while (j >= 0 && !isTagOpeningChar(chars.get(j))) {
        j--;
      }
      return !Character.valueOf('/').equals(chars.get(j + 1)) ? j : i + 1;
    });
  }

  private boolean isTagClosingChar(char c) {
    return Character.valueOf('>').equals(c);
  }

  private boolean isTagOpeningChar(char c) {
    return Character.valueOf('<').equals(c);
  }

  private CompletableFuture<Integer> findFirstValidHtmlTagCharIndex(List<Character> chars) {
    return CompletableFuture.supplyAsync(() -> {
      if (isTagOpeningChar(chars.get(0))) {
        return -1;
      }
      int i = 0;
      int j = 0;
      while (i < chars.size() && !isTagOpeningChar(chars.get(i))) {
        i++;
        if (Character.valueOf('/').equals(chars.get(i + 1))) {
          j = i + 1;
          while (!isTagClosingChar(chars.get(j))) {
            j++;
          }
        }
      }
      return Math.max(i, ++j);
    });
  }

  private boolean mayContainHtmlTag(List<Character> chars) {
    return chars.contains('<')
        && chars.contains('>');
  }

  private int findFirstTagOpeningChar(List<Character> chars) {
    return findTagOpeningChar(0, chars);
  }

  private int findTagOpeningChar(final int startIndex, List<Character> chars) {
    int index = startIndex;
    while (!isTagOpeningChar(chars.get(index))) {
      index++;
    }
    return index;
  }

  private HtmlTag findEndingAndSaveHtmlTag(final int startIndex, List<Character> chars) {
    int currentIndex = startIndex;
    HtmlTag tag = HtmlTag.withStartIndex(currentIndex);
    while (!isTagClosingChar(chars.get(currentIndex)) && currentIndex < chars.size()) {
      tag.append(chars.get(currentIndex++));
    }
    tag.append(chars.get(currentIndex));
    tag.setEndIndex(currentIndex);
    return tag;
  }

  private List<Character> getWithoutTags(final List<Character> chars, HtmlTag opening, HtmlTag closing) {
    List<Character> beforeOpeningTag = chars.subList(0, opening.getStartIndex());
    List<Character> inBetweenTags = chars.subList(opening.getEndIndex() + 1, closing.getStartIndex());
    List<Character> afterClosingTag = chars.subList(closing.getEndIndex() + 1, chars.size());
    List<Character> charsWithoutTagsFound =
        new ArrayList<>(beforeOpeningTag.size() + inBetweenTags.size() + afterClosingTag.size());
    charsWithoutTagsFound.addAll(beforeOpeningTag);
    charsWithoutTagsFound.addAll(inBetweenTags);
    charsWithoutTagsFound.addAll(afterClosingTag);
    return charsWithoutTagsFound;
  }

  private List<Character> getWithoutTagsAndContent(final List<Character> chars, HtmlTag firstTag, HtmlTag secondTag) {
    chars
        .subList(firstTag.getStartIndex(), secondTag.getEndIndex() + 1)
        .clear();
    return new ArrayList<>(chars);
  }

  private List<Character> getWithoutRemainingTags(final List<Character> chars) {
    if (!mayContainHtmlTag(chars)) {
      return chars;
    }
    while (mayContainHtmlTag(chars)) {
      int index = 0;
      index = findTagOpeningChar(index, chars);
      HtmlTag tag = findEndingAndSaveHtmlTag(index, chars);
      index = tag.getEndIndex();
      if (isTagClosingChar(chars.get(index))) {
        chars
            .subList(tag.getStartIndex(), tag.getEndIndex() + 1)
            .clear();
      }
    }
    return new ArrayList<>(chars);
  }
}
