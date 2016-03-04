package sample;

import javafx.beans.property.SimpleStringProperty;

import java.util.List;

/**
 * Created by Developer on 25.02.2016.
 */
public class ExerciseChild extends Exercises {
    private SimpleStringProperty key;
    private SimpleStringProperty Arabic;
    private SimpleStringProperty English;
    private SimpleStringProperty Russian;
    private SimpleStringProperty Ukraine;
    private SimpleStringProperty French;
    private SimpleStringProperty Georgian;
    private SimpleStringProperty fileResult;

    public ExerciseChild(String key, String s, String s1, String s2, String s3) {
        this.key = new SimpleStringProperty(key);
        this.Arabic = new SimpleStringProperty(s);
        this.English = new SimpleStringProperty(s1);
        this.Russian = new SimpleStringProperty(s2);
        this.Ukraine = new SimpleStringProperty(s3);
    }

    public ExerciseChild(String key, List<String> entryValue, List<String> nameColumns, String resultName) {
        this.key = new SimpleStringProperty(key);


        for (int i = 0; i < nameColumns.size(); i++) {
            SimpleStringProperty temp = new SimpleStringProperty(entryValue.get(i));
            switch (nameColumns.get(i)) {
                case "Arabic":
                    this.Arabic = temp;
                    break;
                case "English":
                    this.English = temp;
                    break;
                case "Russian":
                    this.Russian = temp;
                    break;
                case "Ukraine":
                    this.Ukraine = temp;
                    break;
                case "French":
                    this.French = temp;
                    break;
                case "Georgian":
                    this.Georgian = temp;
                    break;
                default:
                    System.out.println("unknown name");
            }
        }
        this.fileResult = new SimpleStringProperty(resultName);
    }

    @Override
    public String getKey() {
        return key.get();
    }

    @Override
    public SimpleStringProperty keyProperty() {
        return key;
    }

    public String getArabic() {
        return Arabic.get();
    }

    public SimpleStringProperty arabicProperty() {
        return Arabic;
    }

    public String getEnglish() {
        return English.get();
    }

    public SimpleStringProperty englishProperty() {
        return English;
    }

    public String getRussian() {
        return Russian.get();
    }

    public SimpleStringProperty russianProperty() {
        return Russian;
    }

    public String getUkraine() {
        return Ukraine.get();
    }

    public SimpleStringProperty ukraineProperty() {
        return Ukraine;
    }

    public String getFrench() {
        return French.get();
    }

    public SimpleStringProperty frenchProperty() {
        return French;
    }

    public String getGeorgian() {
        return Georgian.get();
    }

    public SimpleStringProperty georgianProperty() {
        return Georgian;
    }

    public String getFileResult() {
        return fileResult.get();
    }

    public SimpleStringProperty fileResultProperty() {
        return fileResult;
    }
}
