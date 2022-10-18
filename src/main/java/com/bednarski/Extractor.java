package com.bednarski;

import java.util.*;

public class Extractor {

  public String extract(String input) {
    List<Character> inputChars = new ArrayList<>();
    for (char c : input.toCharArray()) {
      inputChars.add(c);
    }

    List<Character> validCharacters = new ArrayList<>();
    while (!inputChars.isEmpty()) {

      char[] lineCharacters = input.toCharArray();

      // find '<' character
      int i = 0;
      while ('<' != lineCharacters[i]) {
        i++;
      }

      // skip '<' sign
      i++;

      // collect all chars inbetween until '>' is found and save it as the current opening mark
      StringBuilder currentOpeningMark = new StringBuilder();
      while ('>' != lineCharacters[i]) {
        currentOpeningMark.append(lineCharacters[i++]);
      }

      // skip '>' sign
      i++;

      List<Character> possiblyValidCharacters = new ArrayList<>();
      while ('<' != lineCharacters[i] && '/' != lineCharacters[i + 1]) {
        possiblyValidCharacters.add(lineCharacters[i]);
        i++;
      }

      // skip '<' and '/' signs
      i += 2;

      // collect all chars inbetween until '>' is found and save it as the current closing mark
      StringBuilder currentClosingMark = new StringBuilder();
      while ('>' != lineCharacters[i]) {
        currentClosingMark.append(lineCharacters[i++]);
      }

      if (currentOpeningMark.compareTo(currentClosingMark) == 0) {
        validCharacters.addAll(possiblyValidCharacters);
      }
    }
    return input;
  }

  public String extractTwo(String input) {
    List<Character> inputChars = new ArrayList<>();
    for (char c : input.toCharArray()) {
       inputChars.add(c);
    }
    // find opening mark and save it
    int i = 0;
    while (!Character.valueOf('<').equals(inputChars.get(i))) {
      i++;
    }

    // trim chars before the first html opening mark
    if (i > 0) {
      inputChars = inputChars.subList(i, inputChars.size());
      i = 0;
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
      inputChars.subList(firstMark.getStartIndex(), secondMark.getEndIndex() + 1).clear();
    }

    return "";
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
