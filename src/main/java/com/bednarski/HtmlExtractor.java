package com.bednarski;

import java.util.Scanner;

public class HtmlExtractor {

    public static void main(String[] args) {
        String input = readInput();
        String result = Extractor.extract(input, true);
        System.out.println(result);
    }

    private static String readInput() {
        String input;
        try (var scanner = new Scanner(System.in)) {
            System.out.println("Enter a html line:");
            input = scanner.nextLine();
        }
        return input;
    }
}
