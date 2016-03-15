package sample;

import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Developer on 24.02.2016.
 */
public class Exercises {
    private SimpleStringProperty key;
    private SimpleStringProperty value;

    public Exercises(String key, String value) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
    }

    public Exercises() {
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public SimpleStringProperty keyProperty() {
        return key;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    @Override
    public String toString() {
        return "Exercises{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
