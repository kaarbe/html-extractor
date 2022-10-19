package com.bednarski;

import java.util.*;
import java.util.stream.Collectors;

public class Extractor {

  public String extractPlainText(String input) {
    List<Character> inputChars = new ArrayList<>();
    for (char c : input.toCharArray()) {
       inputChars.add(c);
    }
    boolean isFirstRun = true;
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
    return inputChars.stream().map(Object::toString).collect(Collectors.joining(""));
  }

  private List<Character> getCharsWithoutHtmlMarksFound(
      List<Character> inputChars, HtmlMark openingMark, HtmlMark closingMark) {
    // remove marks
    List<Character> beforeOpeningMark = inputChars.subList(0, openingMark.getStartIndex());
    List<Character> inBetweenMarks = inputChars.subList(openingMark.getEndIndex() + 1, closingMark.getStartIndex());
    List<Character> afterClosingMark = inputChars.subList(closingMark.getEndIndex() + 1, inputChars.size());
    List<Character> inputCharsWithoutMarksFound =
        new ArrayList<>(beforeOpeningMark.size() + inBetweenMarks.size() + afterClosingMark.size());
    inputCharsWithoutMarksFound.addAll(beforeOpeningMark);
    inputCharsWithoutMarksFound.addAll(inBetweenMarks);
    inputCharsWithoutMarksFound.addAll(afterClosingMark);
    return inputCharsWithoutMarksFound;
  }
}
