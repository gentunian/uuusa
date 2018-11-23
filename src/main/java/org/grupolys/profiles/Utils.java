package org.grupolys.profiles;

/**
 * Utils
 */
public class Utils {

    public static Float getWeight(String[] line) {
        try {
            return Float.parseFloat(line[1]);
        } catch (Exception e) {
            return new Float(0);
        }
    }

    public static String getWord(String[] line) {
        return line[0];
    }

    public static String[] split(String line) {
        return line.split("\t");
    }

    public static boolean notEmpty(String line) {
        return !line.isEmpty();
    }

    public static Float noDuplicates(Float a, Float b) {
        System.out.println("Duplicate key found, keeping first value.");
        return a;
    }
}
