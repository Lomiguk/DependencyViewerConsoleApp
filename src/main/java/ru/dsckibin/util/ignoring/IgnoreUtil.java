package ru.dsckibin.util.ignoring;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IgnoreUtil {
    public List<String> getIgnoredNamesFrom(String ignoreFileName) {
        try {
            var result = new ArrayList<String>();
            var file = new File(ignoreFileName);
            var fr = new FileReader(file);
            var br = new BufferedReader(fr);
            String line;
            while((line = br.readLine()) != null){
                if (!line.trim().isEmpty()) {
                    result.add(line.trim());
                }
            }

            return result;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
