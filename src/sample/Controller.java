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
import org.controlsfx.dialog.FontSelectorDialog;

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
    @FXML
    private ToggleGroup toggleGroupMenuCheckEncode;
    private File choosenDirectory;
    private File standartPathTODir;
    private File[] files;
    private TableColumn<Exercises, String> valueColumn;
    private TableColumn<Exercises, String> keyColumn = new TableColumn<>("Key");
    private List<TableColumn<Exercises, String>> tableColumnList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        standartPathTODir = new File(new File(".").getAbsolutePath() + "\\resources\\languages");

        getFileNames(standartPathTODir);
        initRadioBottoms();
        try {
            initTable("Ukraine");
            setOnEditTableCol("Ukraine");
        } catch (Exception e) {
            System.out.println("no Ukraine file");
        }

        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {

                String nameOfRButt = (String) newValue.getUserData();
                System.out.println(nameOfRButt);
                initTable(nameOfRButt);
                setOnEditTableCol(nameOfRButt);
            }
        });

        toggleGroupMenuCheckEncode.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (toggleGroupMenuCheckEncode.getSelectedToggle() != null) {
                    System.out.println(toggleGroupMenuCheckEncode.getSelectedToggle().toString());
                }
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
        try (FileReader reader = new FileReader(file)
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
        for (int i = 0; i < tableColumnList.size(); i++) {
            TableColumn<Exercises, String> tableColumn = tableColumnList.get(i);
            String nameCol = tableColumn.getUserData().toString();

            if (i != tableColumnList.size() - 1) {
                nameCol = nameCol.substring(nameCol.lastIndexOf("\\") + 1, nameCol.lastIndexOf("."));
                nameColList.add(nameCol);
            } else {
                //TODO FOR LAST COL
//                nameColList.add(nameCol);
            }
            tableColumn.setCellValueFactory(new PropertyValueFactory<Exercises, String>(nameCol));
        }

    }

    private ObservableList<Exercises> getExercicesFewFile(List<TableColumn<Exercises, String>> tableColumnList) {

        ObservableList<Exercises> exercises = FXCollections.observableArrayList();
        Map<String, List<String>> mapAll = new HashMap<>();
        setColumns(tableColumnList);


        for (int i = 0; i < tableColumnList.size() - 1; i++) {
            TableColumn<Exercises, String> column = tableColumnList.get(i);
            String fileName = (String) column.getUserData();
            Map<String, String> dataFromProp = null;

            try {
                dataFromProp = LoadDataFromPropToView.getDataFromProp(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (Map.Entry<String, String> entry : dataFromProp.entrySet()) {
                String key = entry.getKey();

                if (!mapAll.containsKey(key)) {
                    mapAll.put(key, new ArrayList<String>() {{
                        add(entry.getValue());
                    }});
                } else {
                    mapAll.get(key).add(entry.getValue());
                }
            }
        }
        //TODO add file results if exists

        for (Map.Entry<String, List<String>> entry : mapAll.entrySet()) {
            String resultName = "-";
            exercises.add(new ExerciseChild(entry.getKey(), entry.getValue(), nameColList, resultName));
        }
        System.out.println("cols: " + nameColList);
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
            for (File file : files) {
                if (file.toString().substring(0, file.toString().lastIndexOf(".")).endsWith(lang)) {
                    valueColumn.setText(file.getAbsolutePath());
                }
            }
            tableView.getColumns().add(valueColumn);

        } else {

            for (File file : files) {
                TableColumn<Exercises, String> column = new TableColumn<>(file.getName());
                column.setUserData(file.getAbsolutePath());
                tableColumnList.add(column);
            }
            TableColumn<Exercises, String> myLastCol = new TableColumn<>("file Result");
            myLastCol.setUserData("fileResult");
            tableColumnList.add(myLastCol); //add tablecolumn for result files

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

        if (choosenDirectory == null) {

            showInformationAlert("Информация о выбранной папке", "Работаем с текущими файлами");
        } else {
            this.files = choosenDirectory.listFiles(getFileFilter("properties"));
            initNewFiles();

        }
    }

    private void initNewFiles() {
        hBoxForRBut.getChildren().clear();
        List<RadioButton> buttonList = initRadioBottoms();
        buttonList.get(0).setSelected(true);
    }

    @FXML
    public void onClickChooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose files");

        List<File> filesChoosen = fileChooser.showOpenMultipleDialog(null);
        System.out.println(filesChoosen);

        if (filesChoosen == null) {
            showInformationAlert("Файлы не выбраны", "Работаю со старыми файлами");
        } else {

            this.files = filesChoosen.toArray(new File[filesChoosen.size()]);
            initNewFiles();
        }
    }

    private void showInformationAlert(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }

    @FXML
    public void onClickChoseFontDialog() {

        FontSelectorDialog fontSelectorDialog = new FontSelectorDialog(Font.font(14));
        fontSelectorDialog.showAndWait();
        Font resultFont = fontSelectorDialog.getResult();

        if (resultFont != null) {
            double size = resultFont.getSize();
            String family = resultFont.getFamily();
            System.out.println(resultFont);
//            tableView.setStyle("-fx-font-style: " +  style );
            tableView.setStyle("-fx-font-size: " + size);
            for (TableColumn<Exercises, ?> column : tableView.getColumns()) {
                column.setStyle("-fx-font-family: " + "'" + family + "'");
            }
        }
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

    private void getFileNames(File standartPathTODir) {
        String typeOfFile = "properties";
        FileFilter fileFilter = getFileFilter(typeOfFile);

        try {
            files = standartPathTODir.listFiles(fileFilter);
            for (File file : files) {
                System.out.println(file);
            }
            System.out.println("\n");
        } catch (Exception e) {
            onClickChooseDir();
            standartPathTODir = choosenDirectory;
            getFileNames(standartPathTODir);
        }
    }

    private FileFilter getFileFilter(String type) {
        return new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (!pathname.isDirectory() && pathname.toString().endsWith("." + type)) {
                    return true;
                }
                return false;
            }
        };
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

        if (files.length > 1) {
            RadioButton radioButtonAll = new RadioButton("All");
            radioButtonAll.setUserData("All");
            buttonList.add(radioButtonAll);
            hBoxForRBut.getChildren().add(radioButtonAll);
        }

        for (RadioButton button : buttonList) {
            button.setPadding(new Insets(10, 30, 10, 0));
            button.setFont(Font.font(16));
            button.setToggleGroup(toggleGroup);
        }
        return buttonList;
    }
}
