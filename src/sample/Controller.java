package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    List<String> nameColList = new ArrayList<>();
    @FXML
    private TableView<Exercises> tableView;
    @FXML
    private HBox hBoxForRBut;
    @FXML
    private ToggleGroup toggleGroup;
    private File choosenDirectory;
    private File standartPathTODir;
    private File[] files;
    private TableColumn<Exercises, String> valueColumn;
    private TableColumn<Exercises, String> keyColumn = new TableColumn<>("Key");
    private List<TableColumn<Exercises, String>> tableColumnList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        standartPathTODir = new File(new File(".").getAbsolutePath() + "\\resources\\languages");

        getFileNames();
        List<RadioButton> rButtonList = initRadioBottoms();
        initTable("Ukraine");
        setOnEditTableCol("Ukraine");

        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {

                String nameOfRButt = (String) newValue.getUserData();
                System.out.println(nameOfRButt);
                initTable(nameOfRButt);
                setOnEditTableCol(nameOfRButt);
            }
        });
    }

    private void setOnEditTableCol(String nameLang) {
        if (true) {
            TableColumn<Exercises, String> exercisesTableColumn = (TableColumn<Exercises, String>) tableView.getColumns().get(1);
            exercisesTableColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Exercises, String>>() {
                @Override
                public void handle(TableColumn.CellEditEvent<Exercises, String> string) {
                    (string.getTableView().getItems().get(string.getTablePosition().getRow())).setValue(string.getNewValue());
                    String key = string.getRowValue().getKey();
                    String newValue = string.getNewValue();
                    System.out.println("new val is: " + newValue + " . key is: " + key);

                    for (File file : files) {
                        if (file.getName().startsWith(nameLang)) {
                            setPropToFile(file, key, newValue);
                        }
                    }
                }
            });
        }
    }

    /**
     * to set value in file by key and file name
     *
     * @param file
     * @param key
     * @param newValue
     */
    private void setPropToFile(File file, String key, String newValue) {

        Properties properties = new Properties();
        try (FileReader reader = new FileReader(file);
        ) {

            properties.load(reader);
            properties.setProperty(key, newValue);

            try (FileWriter writer = new FileWriter(file)) {
                properties.store(writer, null);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * show table by name of language or 'all' languages
     *
     * @param langName
     */
    private void initTable(String langName) {
        ObservableList<Exercises> data;
        clearTable();
        tableColumnList.clear();
        keyColumn.setCellValueFactory(new PropertyValueFactory<Exercises, String>("key"));

        /**
         *natural sort of table
         */
        tableView.sortPolicyProperty().set(param -> {
            Comparator<Exercises> comparator = new Comparator<Exercises>() {
                @Override
                public int compare(Exercises o1, Exercises o2) {
                    String s1 = o1.getKey();
                    String s2 = o2.getKey();

                    String[] s1Parts = s1.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                    String[] s2Parts = s2.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

                    int i = 0;
                    while (i < s1Parts.length && i < s2Parts.length) {

                        if (s1Parts[i].compareTo(s2Parts[i]) == 0) {
                            ++i;
                        } else {
                            try {
                                int intS1 = Integer.parseInt(s1Parts[i]);
                                int intS2 = Integer.parseInt(s2Parts[i]);
                                int diff = intS1 - intS2;
                                if (diff == 0) {
                                    ++i;
                                } else {
                                    return diff;
                                }
                            } catch (Exception ex) {
                                return s1.compareTo(s2);
                            }
                        }//end else
                    }//end while
                    if (s1.length() < s2.length()) {
                        return -1;
                    } else if (s1.length() > s2.length()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };
            FXCollections.sort(tableView.getItems(), comparator);
            return true;
        });
        tableView.getColumns().get(0).setSortType(TableColumn.SortType.ASCENDING);


        if (!langName.equalsIgnoreCase("all")) {
            printColNames(langName);

            valueColumn.setCellValueFactory(new PropertyValueFactory<Exercises, String>("value"));
            valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            data = getExercicesOneFile(langName);
            tableView.setItems(data);
        } else {
            printColNames("all");
            setColumns(tableColumnList);
            data = getExercicesFewFile(tableColumnList);

            tableView.setItems(data);

        }
        tableView.getSortOrder().add(tableView.getColumns().get(0));
        for (TableColumn<Exercises, ?> column : tableView.getColumns()) {
            column.setSortable(false);
        }
    }

    private void setColumns(List<TableColumn<Exercises, String>> tableColumnList) {
        nameColList.clear();
        for (TableColumn<Exercises, String> column : tableColumnList) {
            String nameCol = column.getUserData().toString();
            nameCol = nameCol.substring(nameCol.lastIndexOf("\\") + 1, nameCol.lastIndexOf("."));
            nameColList.add(nameCol);
            column.setCellValueFactory(new PropertyValueFactory<Exercises, String>(nameCol));
        }
    }

    private ObservableList<Exercises> getExercicesFewFile(List<TableColumn<Exercises, String>> tableColumnList) {

        ObservableList<Exercises> exercises = FXCollections.observableArrayList();
        Map<String, List<String>> mapAll = new HashMap<>();
        setColumns(tableColumnList);


        for (TableColumn<Exercises, String> column : tableColumnList) {
            String fileName = (String) column.getUserData();
            Map<String, String> dataFromProp = null;

            try {
                dataFromProp = LoadDataFromPropToView.getDataFromProp(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Map.Entry<String, String> entry : dataFromProp.entrySet()) {
                if (!mapAll.containsKey(entry.getKey())) {

                    mapAll.put(entry.getKey(), new ArrayList<String>() {{
                        add(entry.getValue());
                    }});
                } else {
                    mapAll.get(entry.getKey()).add(entry.getValue());
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : mapAll.entrySet()) {

//            exercises.add(new ExerciseChild(entry.getKey(), entry.getValue().get(0),
//                    entry.getValue().get(1), entry.getValue().get(2), entry.getValue().get(3)));
            exercises.add(new ExerciseChild(entry.getKey(), entry.getValue(), nameColList));
        }
        System.out.println("cols: " + nameColList);
        System.out.println("cols: " + mapAll.get("Exercise0"));
        return exercises;
    }

    private ObservableList<Exercises> getExercicesOneFile(String langName) {
        ObservableList<Exercises> exercises = FXCollections.observableArrayList();
        Map<String, String> dataFromProp = null;
        File fileForLoad = null;
        for (File file : files) {
            if (file.getName().startsWith(langName)) {
                fileForLoad = file;
            }
        }
        try {
            dataFromProp = LoadDataFromPropToView.getDataFromProp(fileForLoad.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.showAndWait();
        }

        for (Map.Entry<String, String> entry : dataFromProp.entrySet()) {
            exercises.add(new Exercises(entry.getKey(), entry.getValue()));
        }
        return exercises;
    }


    private void printColNames(String lang) {
        tableView.getColumns().clear();
        tableView.getColumns().add(keyColumn);

        if (!lang.equalsIgnoreCase("all")) {
            valueColumn = new TableColumn<>(lang);
            tableView.getColumns().add(valueColumn);
        } else {

            for (File file : files) {
                TableColumn<Exercises, String> column = new TableColumn<>(file.getName());
                column.setUserData(file.getAbsolutePath());
                tableColumnList.add(column);
            }
            for (TableColumn<Exercises, String> column : tableColumnList) {
                tableView.getColumns().add(column);
            }
        }
    }

    private void clearTable() {
        tableView.getItems().clear();
    }

    @FXML
    public void onClickChooseDir() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(standartPathTODir);

        choosenDirectory = chooser.showDialog(null);
        System.out.println("Choosen dir: " + choosenDirectory);

        if (choosenDirectory == null || !choosenDirectory.equals(standartPathTODir)) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Информация о выбранной папке");
            alert.setHeaderText(null);
            alert.setContentText("Работаем с файлами из текущей директории проекта");

            alert.showAndWait();
        }
    }

    @FXML
    public void onClickChooseFile() {
        System.out.println("file choose");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose files");

        List<File> files = fileChooser.showOpenMultipleDialog(null);
        System.out.println(files);
        if (files!=null) {
            for (File file : files) {
                System.out.println(file);
            }
        }
    }

    @FXML
    public void onClickChoseFontDialog() {
        FontChooser2 fontChooser2 = new FontChooser2(null);
        fontChooser2.show();

    }

    @FXML
    public void onClickAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        try {
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(new Image(new FileInputStream(new File("resources/main2.png"))));
        } catch (FileNotFoundException e) {
            System.out.println("No icon for alert");
        }

        alert.setTitle("About");
        alert.setHeaderText("Создано на ХКБМ МОРОЗОВА");
        alert.setContentText("Приложения для работы с интернационализацией тренажера");

        alert.showAndWait();
    }

    @FXML
    public void onClickExit() {
        System.out.println("exit");
        ((EventHandler<ActionEvent>) event -> Platform.exit()).handle(null);

    }

    private void getFileNames() {
        FileFilter fileFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (!pathname.isDirectory() && pathname.toString().endsWith(".properties")) {
                    return true;
                }
                return false;
            }
        };

        try {
            files = standartPathTODir.listFiles(fileFilter);
            for (File file : files) {
                System.out.println(file);
            }
            System.out.println("\n");
        } catch (Exception e) {
            onClickChooseDir();
            standartPathTODir = choosenDirectory;
            getFileNames();
        }
    }

    private List<RadioButton> initRadioBottoms() {
        List<RadioButton> buttonList = new ArrayList<>();
        hBoxForRBut.setAlignment(Pos.CENTER);

        for (File file : files) {
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            RadioButton radioButton = new RadioButton(name);
            radioButton.setUserData(name);
            buttonList.add(radioButton);
            hBoxForRBut.getChildren().add(radioButton);

            if (name.equals("Ukraine")) {
                radioButton.setSelected(true);
            }
        }
        RadioButton radioButtonAll = new RadioButton("All");
        radioButtonAll.setUserData("All");
        buttonList.add(radioButtonAll);
        hBoxForRBut.getChildren().add(radioButtonAll);

        for (RadioButton button : buttonList) {
            button.setPadding(new Insets(10, 30, 10, 0));
            button.setFont(Font.font(16));
            button.setToggleGroup(toggleGroup);
        }
        return buttonList;
    }
}