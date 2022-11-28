package com.github.kaarbe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class Trimmer {

  private Trimmer() { }

  /**
   * Returns new list of {@link Character} objects without excessive characters. It removes all characters from
   * the beginning until the opening HTML mark is found and all characters from the ending until the closing HTML tag
   * is found. The process of finding those marks happens concurrently.
   *
   * @param chars a list of {@link Character} objects to trim.
   * @return a list without excessive characters.
   */
  static List<Character> trim(final List<Character> chars) {
    CompletableFuture<Integer> lastValidCharIndex = findLastValidHtmlTagCharIndex(chars);
    CompletableFuture<Integer> firstValidCharIndex = findFirstValidHtmlTagCharIndex(chars);
    CompletableFuture.allOf(lastValidCharIndex, firstValidCharIndex).join();

    List<Character> charsCopy = new ArrayList<>(chars);
    lastValidCharIndex.thenAccept(endIndex -> trimTail(charsCopy, endIndex)).join();
    firstValidCharIndex.thenAccept(startIndex -> trimHead(charsCopy, startIndex)).join();
    return charsCopy;
  }

  private static CompletableFuture<Integer> findLastValidHtmlTagCharIndex(final List<Character> chars) {
    return CompletableFuture.supplyAsync(() -> {
      if (chars.isEmpty()
          || (Identifier.isTagClosingChar(chars.get(chars.size() - 1))
            && !Identifier.isTagClosingChar(chars.get(chars.size() - 2)))) {
        return -1;
      }
      int i = chars.size() - 1;
      while (i >= 0 && areLastTwoTagClosingChars(i, chars)) {
        i--;
      }
      int j = i;
      while (j >= 0 && !Identifier.isTagOpeningChar(chars.get(j))) {
        j--;
      }
      return !Character.valueOf('/').equals(chars.get(j + 1)) ? j : i + 1;
    });
  }

  private static boolean areLastTwoTagClosingChars(final int index, final List<Character> chars) {
    return !(Identifier.isTagClosingChar(chars.get(index)) && !Identifier.isTagClosingChar(chars.get(index - 1)));
  }

  private static CompletableFuture<Integer> findFirstValidHtmlTagCharIndex(final List<Character> chars) {
    return CompletableFuture.supplyAsync(() -> {
      if (chars.isEmpty() || Identifier.isTagOpeningChar(chars.get(0))) {
        return -1;
      }
      int i = 0;
      int j = 0;
      while (i < chars.size() && !Identifier.isTagOpeningChar(chars.get(i))) {
        i++;
        if (i + 1 < chars.size() && Character.valueOf('/').equals(chars.get(i + 1))) {
          j = i + 1;
          while (!Identifier.isTagClosingChar(chars.get(j))) {
            j++;
          }
        }
      }
      return Math.max(i, ++j);
    });
  }

  private static void trimTail(List<Character> trimmedCharList, int endIndex) {
    if (endIndex != -1) {
      trimmedCharList
          .subList(endIndex, trimmedCharList.size())
          .clear();
    }
  }

  private static void trimHead(List<Character> trimmedCharList, int startIndex) {
    if (startIndex != -1) {
      trimmedCharList
          .subList(0, startIndex)
          .clear();
    }
  }
}
