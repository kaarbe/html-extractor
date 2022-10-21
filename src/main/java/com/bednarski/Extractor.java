package com.bednarski;

import java.util.*;
import java.util.stream.Collectors;

public class Extractor {

  public String extractPlainText(String input) {
    List<Character> inputChars = toCharList(input);
    boolean isFirstRun = true;
//    trimBeginning(inputChars);
//    trimEnding(inputChars);
    while (inputChars.contains('<') && inputChars.contains('>') && inputChars.contains('/')) {
      // find opening mark and save it
      int i = 0;
      while (!Character.valueOf('<').equals(inputChars.get(i))) {
        i++;
      }

      // trim chars before the first html opening mark
      if (isFirstRun && i > 0) {
        if (Character.valueOf('/').equals(inputChars.get(i + 1))) {
          int j = i + 1;
          while (!Character.valueOf('>').equals(inputChars.get(j))) {
            j++;
          }
          inputChars = inputChars.subList(++j, inputChars.size());
        } else {
          inputChars = inputChars.subList(i, inputChars.size());
        }
        i = 0;
      }

      if (isFirstRun) {
        isFirstRun = false;
      }

      // initial first mark
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
        inputChars = getCharsWithoutHtmlMarksFound(inputChars, firstMark, secondMark);
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

  private void trimBeginning(List<Character> chars) {
    int i = 0;
    while (i < chars.size() && !Character.valueOf('<').equals(chars.get(i))) {
      i++;
    }
    chars.subList(0, i).clear();
  }


  private void trimEnding(List<Character> chars) {
    int i = chars.size() - 1;
    while (i >= 0 && !Character.valueOf('>').equals(chars.get(i))) {
      i--;
    }
    chars.subList(i + 1, chars.size()).clear();
  }

  private List<Character> toCharList(String text) {
    List<Character> chars = new ArrayList<>();
    for (char c : text.toCharArray()) {
      chars.add(c);
    }
    return chars;
  }

  private List<Character> getCharsWithoutHtmlMarksFound(List<Character> chars, HtmlMark opening, HtmlMark closing) {
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
