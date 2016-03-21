package sample;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Santer on 20.02.2016.
 */
public class LoadDataFromToProperties {
    private static String encoding = "UTF-16";

    public static String getEncoding() {
        return encoding;
    }

    public static void setEncoding(String encoding) {
        LoadDataFromToProperties.encoding = encoding;
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

        System.out.println(file);
        if (properties.containsProperty("}")) {
            properties.removeProperty("}");
        }
        properties.store(writer, null);

        if (file != null) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                StringBuilder forStart = new StringBuilder(file.getName().substring(0, file.getName().lastIndexOf(".")));
                randomAccessFile.seek(0);
                randomAccessFile.write(forStart.append("\r\n{\r\n").toString().getBytes(Charset.forName(encoding)));

                randomAccessFile.seek(randomAccessFile.length());

                String tempEncoding = encoding.equalsIgnoreCase("utf-16") ? "UTF-16BE" : "UTF-8";
                randomAccessFile.write("}".getBytes(tempEncoding));
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

            //and changeKey Color+key if exists
            String colorOldKey = "Color" + oldKey;
            String colorNewKey = null;
            String colorValue = null;
            if (properties.containsProperty(colorOldKey)) {
                colorValue = properties.getProperty(colorOldKey);
                colorNewKey = "Color" + newKey;
                properties.removeProperty(colorOldKey);
                properties.setProperty(colorNewKey, colorValue);
                System.out.println("old key: " + oldKey + ". new key: " + newKey + "\nold color key: " + colorOldKey
                        + " .new color key: " + colorNewKey);
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding)) {
                fixAndWritePropertiesToFileWithBrackets(writer, properties, file);
            }
        }
    }


    public static void swapInFilesKeys(File[] files, String keyDragged, String keyResult) {
        List<File> fileList = new ArrayList<>(Arrays.asList(files));

        changeKeyInAllFiles(fileList, keyResult, "temp_temp");
        changeKeyInAllFiles(fileList, keyDragged, keyResult);
        changeKeyInAllFiles(fileList, "temp_temp", keyDragged);
    }


    public static void checkContainIfNoAddKeyToAllFiles(File[] files, String key, List<String> valueList, String newValue) {
        valueList.clear();

        for (File file : files) {
            try {
                addKeyValueToFileIfNoExists(file, valueList, key, newValue);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addKeyValueToFileIfNoExists(File file, List<String> valueList, String key, String newValue) throws IOException {

        try (
                Reader reader = new InputStreamReader(new FileInputStream(file), encoding)//with encoding
        ) {
            OrderedProperties.OrderedPropertiesBuilder builder = new OrderedProperties.OrderedPropertiesBuilder();
            builder.withSuppressDateInComment(true);
            OrderedProperties properties = builder.build();
            properties.load(reader);

            if (properties.containsProperty(key)) {
                valueList.add(properties.getProperty(key));
            } else {
                properties.setProperty(key, newValue);
                valueList.add(newValue);

                try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding)) {
                    fixAndWritePropertiesToFileWithBrackets(writer, properties, file);
                }
            }
        }
    }


    public static void main(String[] args) throws IOException {
        OrderedProperties.OrderedPropertiesBuilder builder = new OrderedProperties.OrderedPropertiesBuilder();
        builder.withSuppressDateInComment(true);
        OrderedProperties properties = builder.build();

        File path = new File("resources\\languages\\Russian.cfg");

        changeKeyInOneFile(path, "Exercise3", "Exercise3333");
    }
}
