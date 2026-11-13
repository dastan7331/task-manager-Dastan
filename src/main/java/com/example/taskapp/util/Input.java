package com.example.taskapp.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

public final class Input {
    private static final Scanner SC = new Scanner(System.in);
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    public static final Set<String> STATUSES = Set.of("TODO", "IN_PROGRESS", "DONE");

    private Input() { }

    public static String text(String prompt, int max, boolean required) {
        while (true) {
            System.out.print(prompt);
            String s = SC.nextLine().trim();
            if (s.isEmpty() && required) { System.out.println("Value is required."); continue; }
            if (s.length() > max) { System.out.println("Too long (max " + max + ")."); continue; }
            return s;
        }
    }

    public static int integer(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int n = Integer.parseInt(SC.nextLine().trim());
                if (n >= min && n <= max) return n;
                System.out.println("Enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("That is not a number.");
            }
        }
    }

    public static Integer optionalInteger(String prompt) {
        while (true) {
            System.out.print(prompt + " (Enter to skip): ");
            String s = SC.nextLine().trim();
            if (s.isEmpty()) return null;
            try { return Integer.parseInt(s); } catch (NumberFormatException e) { System.out.println("That is not a number."); }
        }
    }

    public static String email(String prompt) {
        while (true) {
            String s = text(prompt, 150, true);
            if (EMAIL.matcher(s).matches()) return s;
            System.out.println("Invalid email format.");
        }
    }

    public static LocalDate optionalDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (yyyy-mm-dd, Enter to skip): ");
            String s = SC.nextLine().trim();
            if (s.isEmpty()) return null;
            try { return LocalDate.parse(s); } catch (DateTimeParseException e) { System.out.println("Wrong date format."); }
        }
    }

    public static String status(String prompt) {
        while (true) {
            String s = text(prompt + " (TODO / IN_PROGRESS / DONE): ", 20, true).toUpperCase();
            if (STATUSES.contains(s)) return s;
            System.out.println("Unknown status.");
        }
    }

    public static boolean confirm(String prompt) {
        System.out.print(prompt + " (y/n): ");
        return SC.nextLine().trim().equalsIgnoreCase("y");
    }
}
