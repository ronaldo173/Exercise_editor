package sample;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * Created by Santer on 20.02.2016.
 */
public class LoadDataFromPropToView {
    private static String encoding = "UTF-16";

    public static String getEncoding() {
        return encoding;
    }

    public static void setEncoding(String encoding) {
        LoadDataFromPropToView.encoding = encoding;
    }

    public static Map<String, String> getDataFromProp(String path) throws IOException {
        Map<String, String> map = new TreeMap<>();
        Properties properties = new Properties();
        Reader reader = new InputStreamReader(new FileInputStream(path), encoding);//with encoding

        properties.load(reader);
        reader.close();

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getKey().toString().startsWith("Exercise") &&
                    !entry.getKey().toString().endsWith("Exercise")) {
                map.put(entry.getKey().toString(), (String) entry.getValue());
            }
        }
        return map;
    }

    /**
     * to set value in file by key and file name
     *
     * @param file
     * @param key
     * @param newValue
     */
    public static void setPropToFile(File file, String key, String newValue) {

        Properties properties = new Properties();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), encoding)
        ) {
            properties.load(reader);
            properties.setProperty(key, newValue);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding)) {
                properties.store(writer, null);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void changeKeyInAllFiles(List<File> filesList, String oldKey, String newKey) {
        if (filesList == null || oldKey == null || newKey == null) {
            new Controller().showInformationAlert("Error", "List of files or oldKey or newKey is null");
        } else {
            for (File file : filesList) {
                try {
                    changeKeyInOneFile(file, oldKey, newKey);
                } catch (IOException e) {
                    new Controller().showInformationAlert("Error changing key",
                            e.toString());
                }
            }
        }
    }

    private static void changeKeyInOneFile(File file, String oldKey, String newKey) throws IOException {

        try (
                Reader reader = new InputStreamReader(new FileInputStream(file), encoding);//with encoding
        ) {
            Properties properties = new Properties();
            properties.load(reader);

            String value = (String) properties.get(oldKey);
            properties.remove(oldKey);
            properties.setProperty(newKey, value);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding)) {
                properties.store(writer, null);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        Properties properties = new Properties();
        String path = "resources\\languages\\s\\Russian.properties";
        Reader reader = new InputStreamReader(new FileInputStream(path), encoding);
        properties.load(reader);

        String key = "Exercise0";
        Object exercise0 = properties.get(key);
        System.out.println(exercise0);
    }
}
