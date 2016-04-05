package sample;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
//                System.out.println("old key: " + oldKey + ". new key: " + newKey + "\nold color key: " + colorOldKey
//                        + " .new color key: " + colorNewKey);
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


    public static void loadLastPathToLessons(String pathToLessons) {
        if (pathToLessons == null) {
            return;
        }
        Properties properties = new Properties();
        File file = new File("my_config.properties");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (OutputStream stream = new FileOutputStream(file);) {
            properties.put("lastLessPath", pathToLessons);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void swapKeyInFileAndSwapLessonsWithResults(File[] currentFileForTable, String pathToLessons,
                                                              Exercises exerciseDragged, Exercises exerciseResult) {

        System.out.println("\n swap keys: " + exerciseDragged.getKey());
        System.out.println("\n swap keys: " + exerciseResult.getKey());
        for (File file : currentFileForTable) {


//            setPropToFile(file, exerciseDragged.getKey(), exerciseResult.getValue());
//            setPropToFile(file, exerciseResult.getKey(), exerciseDragged.getValue());

            try {
                swapValueInFile(file, exerciseDragged.getKey(), exerciseResult.getKey());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        try {
            swapLessonFilesAndResultIfExists(pathToLessons, exerciseDragged.getKey(), exerciseResult.getKey());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private static void swapValueInFile(File file, String key1, String key2) throws Exception {
        try (
                Reader reader = new InputStreamReader(new FileInputStream(file), encoding)//with encoding
        ) {
            OrderedProperties.OrderedPropertiesBuilder builder = new OrderedProperties.OrderedPropertiesBuilder();
            builder.withSuppressDateInComment(true);
            OrderedProperties properties = builder.build();
            properties.load(reader);

            String value1 = properties.getProperty(key1);
            String value2 = properties.getProperty(key2);
            properties.setProperty(key1, value2);
            properties.setProperty(key2, value1);

            //and changeKey Color+key if exists
            String color = "Color";
            String colorKey1 = color + key1;
            String colorKey2 = color + key2;
            String colorValue1 = properties.getProperty(colorKey1);
            String colorValue2 = properties.getProperty(colorKey2);

            if (properties.containsProperty(colorKey1) && properties.containsProperty(colorKey2)) {
                properties.setProperty(colorKey1, colorValue2);
                properties.setProperty(colorKey2, colorValue1);
            }

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), encoding)) {
                fixAndWritePropertiesToFileWithBrackets(writer, properties, file);
            }
        }

    }

    private static void swapLessonFilesAndResultIfExists(String pathToLessons, String oldKey, String newKey) throws IOException {

        File[] lessonFilesArray = (new File(pathToLessons)).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith(oldKey + ".") ||
                        pathname.getName().startsWith(newKey + ".")) {
                    return true;
                }
                return false;
            }
        });
        List<File> fileList = new ArrayList<>(Arrays.asList(lessonFilesArray));

        for (File file : fileList) {
            String typeOfLessonFile = file.getName().substring(file.getName().lastIndexOf("."));
            String newFileName = "temp" + typeOfLessonFile;
            if (file.getName().startsWith(oldKey)) {
                Files.move(file.toPath(), file.toPath().resolveSibling(newFileName), StandardCopyOption.REPLACE_EXISTING);
            } else {
                newFileName = oldKey + typeOfLessonFile;
                Files.move(file.toPath(), file.toPath().resolveSibling(newFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        }


        File[] tempFilesRename = (new File(pathToLessons)).listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().startsWith("temp" + ".")) {
                    return true;
                }
                return false;
            }
        });

        for (File file : tempFilesRename) {
            String typeOfLessonFile = file.getName().substring(file.getName().lastIndexOf("."));
            String newFileName = newKey + typeOfLessonFile;
            Files.move(file.toPath(), file.toPath().resolveSibling(newFileName), StandardCopyOption.REPLACE_EXISTING);
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
