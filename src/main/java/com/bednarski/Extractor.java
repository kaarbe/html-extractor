package com.bednarski;

import java.util.*;
import java.util.stream.Collectors;

public class Extractor {

  public String extractPlainText(String input, boolean shouldTrim) {
    List<Character> inputChars = toCharList(input);
    if (shouldTrim) {
      trimBeginning(inputChars);
      trimEnding(inputChars);
    }
    while (containsHtmlMarks(inputChars)) {
      int index = findMarkOpeningChar(inputChars, 0);
      // save initial first mark
      HtmlMark firstMark = HtmlMark.withStartIndex(index);
      while (!isMarkClosingChar(inputChars.get(index))) {
        firstMark.append(inputChars.get(index++));
      }
      firstMark.append(inputChars.get(index));
      firstMark.setEndIndex(index);

      // skip content in-between two marks (for now)
      index = findMarkOpeningChar(inputChars, index);

      // initial second mark
      HtmlMark secondMark = HtmlMark.withStartIndex(index);
      while (!isMarkClosingChar(inputChars.get(index))) {
        secondMark.append(inputChars.get(index++));
      }
      secondMark.append(inputChars.get(index));
      secondMark.setEndIndex(index);

      // while first isn't opening and second isn't closing, keep looking
      while (!(firstMark.isOpening() && secondMark.isClosing())) {
        // move first 'pointer' to the second 'pointer'
        firstMark = secondMark;

        // skip content in-between two marks (for now)
        index = findMarkOpeningChar(inputChars, index);

        // set second 'pointer' on new html mark
        secondMark = HtmlMark.withStartIndex(index);
        while (!isMarkClosingChar(inputChars.get(index))) {
          secondMark.append(inputChars.get(index++));
        }
        secondMark.append(inputChars.get(index));
        secondMark.setEndIndex(index);
      }

      if (firstMark.isPairWith(secondMark)) {
        inputChars = getWithoutHtmlMarksFound(inputChars, firstMark, secondMark);
      } else {
        // remove marks and content
        inputChars
            .subList(firstMark.getStartIndex(), secondMark.getEndIndex() + 1)
            .clear();
      }
    }

    return inputChars
        .stream()
        .map(Object::toString)
        .collect(Collectors.joining(""));
  }

  private static boolean containsHtmlMarks(List<Character> inputChars) {
    return inputChars.contains('<')
        && inputChars.contains('>')
        && inputChars.contains('/');
  }

  private int findMarkOpeningChar(List<Character> chars, int startIndex) {
    int index = startIndex;
    while (!isMarkOpeningChar(chars.get(index))) {
      index++;
    }
    return index;
  }

  private boolean isMarkClosingChar(char c) {
    return Character.valueOf('>').equals(c);
  }

  private boolean isMarkOpeningChar(char c) {
    return Character.valueOf('<').equals(c);
  }

  private void trimBeginning(List<Character> chars) {
    if (isMarkOpeningChar(chars.get(0))) {
      return;
    }
    int i = 0;
    int j = 0;
    while (i < chars.size() && !isMarkOpeningChar(chars.get(i))) {
      i++;
      if (Character.valueOf('/').equals(chars.get(i + 1))) {
        j = i + 1;
        while (!isMarkClosingChar(chars.get(j))) {
          j++;
        }
      }
    }
    chars.subList(0, Math.max(i, ++j)).clear();
  }

  private void trimEnding(List<Character> chars) {
    if (isMarkClosingChar(chars.get(chars.size() - 1))) {
      return;
    }
    int i = chars.size() - 1;
    while (i >= 0 && !isMarkClosingChar(chars.get(i))) {
      i--;
    }
    int j = i;
    while (j >= 0 && !isMarkOpeningChar(chars.get(j))) {
      j--;
    }
    if (!Character.valueOf('/').equals(chars.get(j + 1))) {
      chars.subList(j, chars.size()).clear();
    } else {
      chars.subList(i + 1, chars.size()).clear();
    }
  }

  private List<Character> toCharList(String text) {
    List<Character> chars = new ArrayList<>();
    for (char c : text.toCharArray()) {
      chars.add(c);
    }
    return chars;
  }

  private List<Character> getWithoutHtmlMarksFound(List<Character> chars, HtmlMark opening, HtmlMark closing) {
    List<Character> beforeOpeningMark = chars.subList(0, opening.getStartIndex());
    List<Character> inBetweenMarks = chars.subList(opening.getEndIndex() + 1, closing.getStartIndex());
    List<Character> afterClosingMark = chars.subList(closing.getEndIndex() + 1, chars.size());
    List<Character> charsWithoutMarksFound =
        new ArrayList<>(beforeOpeningMark.size() + inBetweenMarks.size() + afterClosingMark.size());
    charsWithoutMarksFound.addAll(beforeOpeningMark);
    charsWithoutMarksFound.addAll(inBetweenMarks);
    charsWithoutMarksFound.addAll(afterClosingMark);
    return charsWithoutMarksFound;
  }
}
