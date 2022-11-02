package com.bednarski;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Extractor {

  public String extractPlainText(String input, boolean shouldTrim) {
    List<Character> inputChars = toCharList(input);
    if (shouldTrim) {
      parallelTrim(inputChars);
//      trim(inputChars);
    }
    int index = 0;
    while (mayContainHtmlTag(inputChars) && index < inputChars.size()) {
      index = findTagOpeningChar(inputChars, 0);
      // save initial first tag
      HtmlTag firstTag = HtmlTag.withStartIndex(index);
      while (!isTagClosingChar(inputChars.get(index))) {
        firstTag.append(inputChars.get(index++));
      }
      firstTag.append(inputChars.get(index));
      firstTag.setEndIndex(index);

      // skip content in-between two tags (for now)
      index = findTagOpeningChar(inputChars, index);

      // initial second tag
      HtmlTag secondTag = HtmlTag.withStartIndex(index);
      while (!isTagClosingChar(inputChars.get(index))) {
        secondTag.append(inputChars.get(index++));
      }
      secondTag.append(inputChars.get(index));
      secondTag.setEndIndex(index);

      // while first isn't opening and second isn't closing, keep looking
      while (!(firstTag.isOpening() && secondTag.isClosing())) {
        // move first 'pointer' to the second 'pointer'
        firstTag = secondTag;

        // skip content in-between two tags (for now)
        index = findTagOpeningChar(inputChars, index);

        // set second 'pointer' on new html tag
        secondTag = HtmlTag.withStartIndex(index);
        while (!isTagClosingChar(inputChars.get(index))) {
          secondTag.append(inputChars.get(index++));
        }
        secondTag.append(inputChars.get(index));
        secondTag.setEndIndex(index);
      }

      if (firstTag.isPairWith(secondTag)) {
        inputChars = getWithoutTagsFound(inputChars, firstTag, secondTag);
      } else {
        // remove tags and content
        inputChars
            .subList(firstTag.getStartIndex(), secondTag.getEndIndex() + 1)
            .clear();
      }
    }


    return removeRemainingTags(inputChars)
        .stream()
        .map(Object::toString)
        .collect(Collectors.joining(""));
  }

  private static boolean mayContainHtmlTag(List<Character> chars) {
    return chars.contains('<')
        && chars.contains('>');
  }

  private int findTagOpeningChar(List<Character> chars, int startIndex) {
    int index = startIndex;
    while (!isTagOpeningChar(chars.get(index))) {
      index++;
    }
    return index;
  }

  private boolean isTagClosingChar(char c) {
    return Character.valueOf('>').equals(c);
  }

  private boolean isTagOpeningChar(char c) {
    return Character.valueOf('<').equals(c);
  }

  private void trim(List<Character> chars) {
    trimEnding(chars);
    trimBeginning(chars);
  }

  private void parallelTrim(List<Character> chars) {
//    List<Integer> indexes = Stream.of(calculateEndIndex(chars), calculateStartIndex(chars))
//        .map(CompletableFuture::join)
//        .toList();
//    if (indexes.get(0) != -1) {
//      chars.subList(indexes.get(0), chars.size()).clear();
//    }
//    if (indexes.get(1) != -1) {
//      chars.subList(0, indexes.get(1)).clear();
//    }

    CompletableFuture<Integer> endIndexCalculation = calculateEndIndex(chars);
    CompletableFuture<Integer> startIndexCalculation = calculateStartIndex(chars);
    CompletableFuture.allOf(endIndexCalculation, startIndexCalculation).join();
    Integer endIndex = endIndexCalculation.join();
    if (endIndex != -1) {
      chars.subList(endIndex, chars.size()).clear();
    }
    Integer startIndex = startIndexCalculation.join();
    if (startIndex != -1) {
      chars.subList(0, startIndex).clear();
    }
  }

  private CompletableFuture<Integer> calculateEndIndex(List<Character> chars) {
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

  private CompletableFuture<Integer> calculateStartIndex(List<Character> chars) {
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

  private void trimEnding(List<Character> chars) {
    if (isTagClosingChar(chars.get(chars.size() - 1))) {
      return;
    }
    int i = chars.size() - 1;
    while (i >= 0 && !isTagClosingChar(chars.get(i))) {
      i--;
    }
    int j = i;
    while (j >= 0 && !isTagOpeningChar(chars.get(j))) {
      j--;
    }
    if (!Character.valueOf('/').equals(chars.get(j + 1))) {
      chars.subList(j, chars.size()).clear();
    } else {
      chars.subList(i + 1, chars.size()).clear();
    }
  }

  private void trimBeginning(List<Character> chars) {
    if (isTagOpeningChar(chars.get(0))) {
      return;
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
    chars.subList(0, Math.max(i, ++j)).clear();
  }

  private List<Character> toCharList(String text) {
    List<Character> chars = new ArrayList<>();
    for (char c : text.toCharArray()) {
      chars.add(c);
    }
    return chars;
  }

  private List<Character> getWithoutTagsFound(List<Character> chars, HtmlTag opening, HtmlTag closing) {
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

  private List<Character> removeRemainingTags(List<Character> chars) {
    if (mayContainHtmlTag(chars)) {
      int index = 0;
      while (mayContainHtmlTag(chars) && index < chars.size()) {
        index = findTagOpeningChar(chars, index);
        HtmlTag tag = HtmlTag.withStartIndex(index);
        while (!isTagClosingChar(chars.get(index))) {
          index++;
        }
        tag.setEndIndex(index);
        chars.subList(tag.getStartIndex(), tag.getEndIndex() + 1).clear();
      }
    }
    return chars;
  }
}
