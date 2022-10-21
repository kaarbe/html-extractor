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
      // find mark opening char
      int i = 0;
      while (!Character.valueOf('<').equals(inputChars.get(i))) {
        i++;
      }

      // save initial first mark
      HtmlMark firstMark = HtmlMark.withStartIndex(i);
      while (!Character.valueOf('>').equals(inputChars.get(i))) {
        firstMark.append(inputChars.get(i++));
      }
      firstMark.append(inputChars.get(i));
      firstMark.setEndIndex(i);

      // skip content in-between two marks (for now)
      while (!Character.valueOf('<').equals(inputChars.get(i))) {
        i++;
      }

      // initial second mark
      HtmlMark secondMark = HtmlMark.withStartIndex(i);
      while (!Character.valueOf('>').equals(inputChars.get(i))) {
        secondMark.append(inputChars.get(i++));
      }
      secondMark.append(inputChars.get(i));
      secondMark.setEndIndex(i);

      // while first isn't opening and second isn't closing, keep looking
      while (!(firstMark.isOpening() && secondMark.isClosing())) {
        // move first 'pointer' to the second 'pointer'
        firstMark = secondMark;

        // skip content in-between two marks (for now)
        while (!Character.valueOf('<').equals(inputChars.get(i))) {
          i++;
        }

        // set second 'pointer' on new html mark
        secondMark = HtmlMark.withStartIndex(i);
        while (!Character.valueOf('>').equals(inputChars.get(i))) {
          secondMark.append(inputChars.get(i++));
        }
        secondMark.append(inputChars.get(i));
        secondMark.setEndIndex(i);
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

  private void trimBeginning(List<Character> chars) {
    if (Character.valueOf('<').equals(chars.get(0))) {
      return;
    }
    int i = 0;
    int j = 0;
    while (i < chars.size() && !Character.valueOf('<').equals(chars.get(i))) {
      i++;
      if (Character.valueOf('/').equals(chars.get(i + 1))) {
        j = i + 1;
        while (!Character.valueOf('>').equals(chars.get(j))) {
          j++;
        }
      }
    }
    chars.subList(0, Math.max(i, ++j)).clear();
  }


  private void trimEnding(List<Character> chars) {
    if (Character.valueOf('>').equals(chars.get(chars.size() - 1))) {
      return;
    }
    int i = chars.size() - 1;
    while (i >= 0 && !Character.valueOf('>').equals(chars.get(i))) {
      i--;
    }
    int j = i;
    while (j >= 0 && !Character.valueOf('<').equals(chars.get(j))) {
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
