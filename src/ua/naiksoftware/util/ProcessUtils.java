package ua.naiksoftware.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Naik
 */
public class ProcessUtils {

    public static String readFromProcess(java.lang.Process process, boolean err) {
        StringBuilder result = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(err ? process.getErrorStream() : process.getInputStream()));
        try {
            while ((line = br.readLine()) != null) {
                result.append(line).append("\n");
            }
        } catch (IOException e) {
            //Log.e("Main", "read From Process", e);
        }
        return result.toString();
    }
}
