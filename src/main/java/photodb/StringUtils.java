package photodb;


public class StringUtils {

    private StringUtils() {

    }

    public static String lpad(String valueToPad, char filler, int size) {
        char[] array = new char[size];

        int len = size - valueToPad.length();

        for (int i = 0; i < len; i++)
            array[i] = filler;

        valueToPad.getChars(0, valueToPad.length(), array, size - valueToPad.length());

        return String.valueOf(array);
    }
}
