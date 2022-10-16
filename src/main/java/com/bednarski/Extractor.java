package com.bednarski;

import java.util.*;
import java.util.stream.Collectors;

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
    Queue<Character> inputChars = new LinkedList<>();
    for (char c : input.toCharArray()) {
      inputChars.add(c);
    }

    List<Character> validChars = new ArrayList<>();
    while (!inputChars.isEmpty()) {
      while ('<' != inputChars.element()) {
        inputChars.remove();
      }

      // skip '<'
      inputChars.remove();

      StringBuilder currentOpeningMark = new StringBuilder();
      while ('>' != inputChars.element()) {
        currentOpeningMark.append(inputChars.remove());
      }

      // skip '>'
      inputChars.remove();

      List<Character> possiblyValidChars = new ArrayList<>();
      Character first = inputChars.remove();
      Character second = inputChars.remove();
      while ('<' != first && '/' != second) {
        possiblyValidChars.add(first);
        first = second;
        second = inputChars.remove();
      }
      //  f s 0 0 0 0
      //  g < / h 1 >

      StringBuilder currentClosingMark = new StringBuilder();
      while ('>' != inputChars.element()) {
        currentClosingMark.append(inputChars.remove());
      }
      // skip '>'
      inputChars.remove();

      if (currentOpeningMark.compareTo(currentClosingMark) == 0) {
        validChars.addAll(possiblyValidChars);
      }
    }
    return validChars
        .stream()
        .map(Object::toString)
        .collect(Collectors.joining());
  }
}
