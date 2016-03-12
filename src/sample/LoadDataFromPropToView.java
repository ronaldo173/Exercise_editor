package sample;

import java.io.*;
import java.nio.charset.Charset;
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
        try (Reader reader = new InputStreamReader(new FileInputStream(path), encoding);//with encoding
        ) {
            properties.load(reader);

        }

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

        OrderedProperties.OrderedPropertiesBuilder builder = new OrderedProperties.OrderedPropertiesBuilder();
        builder.withSuppressDateInComment(true);
        OrderedProperties properties = builder.build();

        try (Reader reader = new InputStreamReader(new FileInputStream(file), encoding)
        ) {
            properties.load(reader);
            properties.setProperty(key, newValue);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding)) {
//                properties.store(writer, null);
                fixAndWritePropertiesToFileWithBrackets(writer, properties, file);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void fixAndWritePropertiesToFileWithBrackets(Writer writer, OrderedProperties properties,
                                                                File file) throws IOException {

        if (properties.containsProperty("}")) {
            properties.removeProperty("}");
        }
        properties.store(writer, null);

        if (file != null) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                StringBuilder forStart = new StringBuilder(file.getName().substring(0, file.getName().lastIndexOf(".")));
//            String forStart = path.getName().substring(0, path.getName().lastIndexOf(".")) + "\r\n{\r\n";
                randomAccessFile.seek(0);
                randomAccessFile.write(forStart.append("\r\n{\r\n").toString().getBytes(Charset.forName(encoding)));

                randomAccessFile.seek(randomAccessFile.length());
                randomAccessFile.write("}".getBytes(Charset.forName(encoding)));
            }
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
                Reader reader = new InputStreamReader(new FileInputStream(file), encoding)//with encoding
        ) {
            OrderedProperties.OrderedPropertiesBuilder builder = new OrderedProperties.OrderedPropertiesBuilder();
            builder.withSuppressDateInComment(true);
            OrderedProperties properties = builder.build();
            properties.load(reader);

            String value = properties.getProperty(oldKey);
            properties.removeProperty(oldKey);
            properties.setProperty(newKey, value);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding)) {
                properties.store(writer, null);
                fixAndWritePropertiesToFileWithBrackets(writer, properties, file);
            }
        }
    }


    public static void main(String[] args) throws IOException {
        OrderedProperties.OrderedPropertiesBuilder builder = new OrderedProperties.OrderedPropertiesBuilder();
        builder.withSuppressDateInComment(true);
        OrderedProperties properties = builder.build();

        File path = new File("resources\\languages\\Russian.cfg");
        try (
                Reader reader = new InputStreamReader(new FileInputStream(path), encoding)) {
            properties.load(reader);
        }

        for (Map.Entry<String, String> entry : properties.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }

        properties.setProperty("menuNewExercise", "тестим новое");
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(path), encoding)) {
            fixAndWritePropertiesToFileWithBrackets(writer, properties, path);
        }
    }
}
