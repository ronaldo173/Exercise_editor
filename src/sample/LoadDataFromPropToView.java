package sample;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Created by Santer on 20.02.2016.
 */
public class LoadDataFromPropToView {

    public static Map<String, String> getDataFromProp(String path) throws IOException {
        Map<String, String> map = new TreeMap<>();
        Properties properties = new Properties();
//        InputStream inputStream = new FileInputStream(path);
        FileReader fileReader = new FileReader(path);

        //
        properties.load(fileReader);
        fileReader.close();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey().toString().startsWith("Exercise") &&
                    !entry.getKey().toString().endsWith("Exercise")) {
                map.put(entry.getKey().toString(), (String) entry.getValue());
            }
        }
        return map;
    }

    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        String path = "resources\\languages\\s\\Russian.properties";
        Reader reader = new InputStreamReader(new FileInputStream(path), "UTF-16");
        properties.load(reader);

        String key = "Exercise0";
        Object exercise0 = properties.get(key);
        System.out.println(exercise0);



    }
}
