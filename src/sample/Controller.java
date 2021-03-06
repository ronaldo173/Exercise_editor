package sample;

import com.sun.javafx.scene.control.skin.TableViewSkin;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import org.controlsfx.dialog.FontSelectorDialog;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Controller implements Initializable {

    private final IntegerProperty dragFromIndex = new SimpleIntegerProperty(-1);
    private final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    List<String> nameColList = new ArrayList<>();
    @FXML
    private TableView<Exercises> tableView;
    @FXML
    private HBox hBoxForRBut;
    @FXML
    private ToggleGroup toggleGroup;
    @FXML
    private ToggleGroup toggleGroupMenuCheckEncode;
    @FXML
    private RadioMenuItem rButtEncodeUTF8;
    private File chosenDirectory;
    private File standartPathTODir;
    private File[] files;
    private TableColumn<Exercises, String> valueColumn;
    private TableColumn<Exercises, String> keyColumn = new TableColumn<>("Key");
    private List<TableColumn<Exercises, String>> tableColumnList = new ArrayList<>();
    private String pathToLessonsStandart = "C:\\Users\\Developer\\AppData\\Roaming\\KhMBDB\\BTR4E\\IWP\\Lessons\\";
    private String pathToLessons = pathToLessonsStandart;
    private String typeOfFile = "cfg";
    private Logger log = Logger.getLogger(Controller.class.getName());
    private Comparator<String> comparatorNaturalOrder;
    private File currentFileForTable = null;
    private Timeline scrolltimeline = new Timeline();
    private double scrollDirection = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        standartPathTODir = new File(new File(".").getAbsolutePath() + "\\resources\\languages");
        //make logger save to file

        try {
            FileHandler handler = new FileHandler("logFile.log");
            log.addHandler(handler);
            SimpleFormatter formatter = new SimpleFormatter();
            handler.setFormatter(formatter);

            log.info("First log.");
        } catch (IOException e) {
            e.printStackTrace();
        }


        getFileNames(standartPathTODir);
        initRadioBottoms();
        try {
            initTable("Ukraine");
            setOnEditTableCol("Ukraine");
            currentFileForTable = initCurrentOpenFile("Ukraine");
        } catch (Exception e) {
            log.info("initTable try choose Ukraine lang, no Ukraine");
        }

        toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {

                String nameOfRButt = (String) newValue.getUserData();
                System.out.println(nameOfRButt);
                initTable(nameOfRButt);
                setOnEditTableCol(nameOfRButt);
                currentFileForTable = initCurrentOpenFile(nameOfRButt);
            }
        });

        toggleGroupMenuCheckEncode.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (toggleGroupMenuCheckEncode.getSelectedToggle() != null) {

                    if (newValue == rButtEncodeUTF8) {
                        LoadDataFromToProperties.setEncoding("UTF-8");
                    } else {
                        LoadDataFromToProperties.setEncoding("UTF-16");
                    }
                    log.info("Changed encoding to: " + LoadDataFromToProperties.getEncoding());
                }
            }
        });

        makeDragDropTableWithSwapFirstCol();
        setupScrolling();

    }


    private void setupScrolling() {
        scrolltimeline.setCycleCount(Timeline.INDEFINITE);
        scrolltimeline.getKeyFrames().add(new KeyFrame(Duration.millis(20), "Scoll", new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent ActionEvent) {
                dragScroll();
            }
        }));
        tableView.setOnDragExited(event -> {

            //fixed with getting visible rows
            double directionScroll = event.getY() > 0 ? 1.0 : -1.0;
            scrollDirection = directionScroll / (getNumberOfVisibleRows() * 5);
            scrolltimeline.play();
        });
        tableView.setOnDragEntered(event -> scrolltimeline.stop());
        tableView.setOnDragDone(event -> scrolltimeline.stop());

    }

    private void dragScroll() {
        ScrollBar sb = getVerticalScrollbar();
        if (sb != null) {
            double newValue = sb.getValue() + scrollDirection;
            newValue = Math.min(newValue, 1.0);
            newValue = Math.max(newValue, 0.0);
            sb.setValue(newValue);
        }
    }

    private ScrollBar getVerticalScrollbar() {
        ScrollBar result = null;
        for (Node n : tableView.lookupAll(".scroll-bar")) {
            if (n instanceof ScrollBar) {
                ScrollBar bar = (ScrollBar) n;
                if (bar.getOrientation().equals(Orientation.VERTICAL)) {
                    result = bar;
                }
            }
        }
        return result;
    }


    private File initCurrentOpenFile(String nameOfLanguage) {

        for (File file : files) {
            if (file.getName().startsWith(nameOfLanguage)) {
                return file;
            }
        }

        return null;
    }

    private void makeDragDropTableWithSwapFirstCol() {
        tableView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    Exercises selected = tableView.getSelectionModel().getSelectedItem();

                    if (selected != null) {
                        System.out.println(selected);
                    } else {
                        System.out.println("no selected");
                    }
                }
            }
        });

        tableView.setRowFactory(par -> {
            TableRow<Exercises> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    dragFromIndex.set(index);

                    Dragboard dragboard = row.startDragAndDrop(TransferMode.MOVE);
                    dragboard.setDragView(row.snapshot(null, null));
                    ClipboardContent clipboardContent = new ClipboardContent();

                    clipboardContent.put(SERIALIZED_MIME_TYPE, index);
                    dragboard.setContent(clipboardContent);
                    event.consume();
                }
            });

            row.setOnDragEntered(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    if (dragFromIndex.get() >= 0 && dragFromIndex.get() != row.getIndex()) {
                        row.setStyle("-fx-background-color: gold");
                    }
                }
            });

            row.setOnDragExited(event -> row.setStyle(""));


            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    if (row.getIndex() != ((Integer) db.getContent(SERIALIZED_MIME_TYPE)).intValue()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(SERIALIZED_MIME_TYPE)) {
                    int draggedIndex = (Integer) db.getContent(SERIALIZED_MIME_TYPE);
                    int dropIndex;

                    if (row.isEmpty()) {
                        dropIndex = tableView.getItems().size();
                    } else {
                        dropIndex = row.getIndex();

                        Exercises personDragged = tableView.getItems().get(draggedIndex);
                        Exercises personResult = tableView.getItems().get(dropIndex);
                        log.info("Swap:\n" +
                                personDragged + "\n" + personResult);

                        swapExercisesFirstColumn(personDragged, personResult);
                    }

                    event.setDropCompleted(true);
                    tableView.getSelectionModel().select(dropIndex);
                    tableView.getFocusModel().focus(draggedIndex);
                    event.consume();
                }
            });
//            initTable("all");

            return row;
        });
    }

    private void swapExercisesFirstColumn(Exercises exerciseDragged, Exercises exerciseResult) {


        //TODO make swap logic in files

//        LoadDataFromToProperties.swapInFilesKeys(files, keyDragged, keyResult);

        if (exerciseDragged.mySize() > 2) {
            showInformationAlert("Swap alert, do it not in 'ALL' column!", "!");
        } else {
            String valueDragged = exerciseDragged.getValue();

            //edited to change make change in all files
            LoadDataFromToProperties.swapKeyInFileAndSwapLessonsWithResults(files, pathToLessons,
                    exerciseDragged, exerciseResult);

            initTable(toggleGroup.getSelectedToggle().getUserData().toString());
        }

    }

    /**
     * return comparator for String with natural order ignore case
     *
     * @return
     */
    private Comparator<String> getComparatorNaturalOrder() {
        if (this.comparatorNaturalOrder != null) {
            return this.comparatorNaturalOrder;
        } else {
            this.comparatorNaturalOrder = new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    String s1 = o1.toLowerCase();
                    String s2 = o2.toLowerCase();

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
            return this.comparatorNaturalOrder;
        }
    }

    private void setOnEditTableCol(String nameLang) {
        int numOfCol = nameLang.equalsIgnoreCase("all") ? 0 : 1;
        log.info("nameLang..." + nameLang + "numOfCol for edit..." + numOfCol);
        TableColumn<Exercises, String> exercisesTableColumn = (TableColumn<Exercises, String>) tableView.getColumns().get(numOfCol);
        exercisesTableColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Exercises, String>>() {

            @Override
            public void handle(TableColumn.CellEditEvent<Exercises, String> string) {

                if (numOfCol == 1) {
                    (string.getTableView().getItems().get(string.getTablePosition().getRow())).setValue(string.getNewValue());
                    String key = string.getRowValue().getKey();
                    String newValue = string.getNewValue();
                    log.info("New value: " + newValue + "...For key: " + key);
                    LoadDataFromToProperties.setPropToFile(currentFileForTable, key, newValue);

                } else {
                    String oldKey = string.getOldValue();
                    String newKey = string.getNewValue();

                    if (oldKey.equals(newKey)) {
                        showInformationAlert("Rename info", "Old value = new value, no rename");
                    } else {


                        log.info("New key: " + newKey + "...old key: " + oldKey);
                        List<File> tempFilesList = new ArrayList<>(Arrays.asList(files));
                        List<File> tempLessonListToRename = new ArrayList<>();


                        //files for rename
                        File[] lessonFiles = (new File(pathToLessonsStandart)).listFiles(new FileFilter() {
                            @Override
                            public boolean accept(File pathname) {
                                if (pathname.getName().startsWith(oldKey + ".")) {
                                    return true;
                                }
                                return false;
                            }
                        });

                        //files for change key
                        for (File lessonFile : lessonFiles) {
                            String typeOfLessonFile = lessonFile.getName().substring(lessonFile.getName().lastIndexOf("."));
                            String newFileName = newKey.toString() + typeOfLessonFile;
                            log.info("rename:\n" + "old-" + lessonFile + "\nnew-" + newFileName);
//
                            try {
                                Files.move(lessonFile.toPath(), lessonFile.toPath().resolveSibling(newFileName));
                            } catch (IOException e) {
                                showInformationAlert("Can't rename", "file: " + lessonFile + "\n to:\n" + newKey);
                                continue;
                            }
                            tempLessonListToRename.add(lessonFile);
                        }
                        //set text info about renaime
                        String alertInfoTextWereRenaimed = tempLessonListToRename.size() == 0 ? "No files were renaimed" :
                                "Were renamed: " + tempLessonListToRename;
                        showInformationAlert("Renamed:", alertInfoTextWereRenaimed);

                        //change key in all prop files
                        LoadDataFromToProperties.changeKeyInAllFiles(tempFilesList, oldKey, newKey);
//                     re init
                        initTable("all");
                    }
                }
            }
        });
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
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));


        if (!langName.equalsIgnoreCase("all")) {
            printColNames(langName);

            valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
            valueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            data = getExercisesOneFile(langName);
        } else {
            printColNames("all");
            setColumns(tableColumnList);
            data = getExercisesFewFile(tableColumnList);

            //now make first column editable with saving to all files
            keyColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        }
        tableView.setItems(data);
        tableView.getSortOrder().add(tableView.getColumns().get(0));

        /**
         *natural sort of table
         */
        for (TableColumn<Exercises, ?> column : tableView.getColumns()) {
            TableColumn<Exercises, String> temp = (TableColumn<Exercises, String>) column;
            temp.setComparator(getComparatorNaturalOrder());
        }

        keyColumn.setSortType(TableColumn.SortType.ASCENDING);
        tableView.getSortOrder().addAll(keyColumn);
        Main.getStage().setTitle("Editor. Path to lessons: " + pathToLessons);

    }


    private void setColumns(List<TableColumn<Exercises, String>> tableColumnList) {
        nameColList.clear();
        for (int i = 0; i < tableColumnList.size(); i++) {
            TableColumn<Exercises, String> tableColumn = tableColumnList.get(i);
            String nameCol = tableColumn.getUserData().toString();

            if (i != tableColumnList.size() - 1) {
                nameCol = nameCol.substring(nameCol.lastIndexOf("\\") + 1, nameCol.lastIndexOf("."));
                nameColList.add(nameCol);
            }
            tableColumn.setCellValueFactory(new PropertyValueFactory<>(nameCol));
        }

    }

    private ObservableList<Exercises> getExercisesFewFile(List<TableColumn<Exercises, String>> tableColumnList) {

        ObservableList<Exercises> exercises = FXCollections.observableArrayList();
        Map<String, List<String>> mapAll = new HashMap<>();
        setColumns(tableColumnList);

        for (int i = 0; i < tableColumnList.size() - 1; i++) {
            TableColumn<Exercises, String> column = tableColumnList.get(i);
            String fileName = (String) column.getUserData();
            Map<String, String> dataFromProp = null;

            try {
                dataFromProp = LoadDataFromToProperties.getDataFromProp(fileName);
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

        // map with exercise name - file result

        Map<String, File> resultFilesMap = getResultFiles(mapAll.keySet(), pathToLessons);

        for (Map.Entry<String, List<String>> entry : mapAll.entrySet()) {
            String resultName = "-";
            if (resultFilesMap.containsKey(entry.getKey())) {
                resultName = resultFilesMap.get(entry.getKey()).getName();
            }
            if (entry.getValue().size() != nameColList.size()) {
                LoadDataFromToProperties.checkContainIfNoAddKeyToAllFiles(files, entry.getKey(), entry.getValue(), "-");

            }

            exercises.add(new ExerciseChild(entry.getKey(), entry.getValue(), nameColList, resultName));

        }
        System.out.println("cols: " + nameColList);
        return exercises;
    }

    private Map<String, File> getResultFiles(Set<String> keys, String path) {

        Map map = new HashMap<>();
        File file = new File(path);
        FileFilter fileFilter = getFileFilter("rtg");

        while (file == null || !file.exists() || !file.isDirectory()
                || file.listFiles(fileFilter).length == 0) {
            showInformationAlert("Error", "Need to choose directory with RESULT files: ...Lessons");

            file = getChosenDirectory();
            pathToLessons = file.getAbsolutePath();
            log.info("Choose dir: " + file);
        }

        File[] files = file.listFiles(fileFilter);
        List<File> filesRes = new ArrayList<>(Arrays.asList(files));

        for (File fileWithRes : filesRes) {
            String fileName = fileWithRes.getName();
            String substringWithoutType = fileName.substring(0, fileName.lastIndexOf("."));

            if (keys.contains(substringWithoutType)) {
                map.put(substringWithoutType, fileWithRes);
            }
        }

        return map;
    }

    private ObservableList<Exercises> getExercisesOneFile(String langName) {
        ObservableList<Exercises> exercises = FXCollections.observableArrayList();
        Map<String, String> dataFromProp = null;
        File fileForLoad = null;
        for (File file : files) {
            if (file.getName().startsWith(langName)) {
                fileForLoad = file;
            }
        }
        try {
            dataFromProp = LoadDataFromToProperties.getDataFromProp(fileForLoad.toString());
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

    /**
     * set columns depend on language
     *
     * @param lang
     */
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
            TableColumn<Exercises, String> myLastCol = new TableColumn<>(pathToLessons);
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

    private File getChosenDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setInitialDirectory(standartPathTODir);
        return chooser.showDialog(null);
    }

    public void onClickChooseDirLessons(ActionEvent actionEvent) {

        File chosenDirectory = getChosenDirectory();
        if (chosenDirectory == null) {
            showInformationAlert("Alert", "Don't choose directory for lessons, work with standart.");
            log.log(Level.WARNING, "No directory chosen");
        } else {
            pathToLessons = chosenDirectory.getAbsolutePath();
            LoadDataFromToProperties.loadLastPathToLessons(pathToLessons);
            initNewFiles();
        }
    }

    @FXML
    public void onClickChooseDir() {
        chosenDirectory = getChosenDirectory();
        log.info("Chosen dir: " + chosenDirectory);

        if (chosenDirectory == null) {

            showInformationAlert("Информация о выбранной папке", "Работаем с текущими файлами");
        } else {
            this.files = chosenDirectory.listFiles(getFileFilter(typeOfFile));
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

        List<File> filesChosen = fileChooser.showOpenMultipleDialog(null);
        log.info("Chosen files: " + filesChosen);

        if (filesChosen == null) {
            showInformationAlert("Файлы не выбраны", "Работаю со старыми файлами");
        } else {

            this.files = filesChosen.toArray(new File[filesChosen.size()]);
            initNewFiles();
        }
    }

    /**
     * To show information dialog, with picture ^)
     *
     * @param title
     * @param text
     */
    public void showInformationAlert(String title, String text) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);

        try {
            File file = new File("resources/main2.png");
            Image image = new Image(new FileInputStream(file));
            ((Stage) alert.getDialogPane().getScene().getWindow()).getIcons().add(image);
        } catch (FileNotFoundException e) {
            System.out.println("no icon");
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(text);

        alert.showAndWait();
    }

    /**
     * To show dialog for choosing font
     */
    @FXML
    public void onClickChoseFontDialog() {

        FontSelectorDialog fontSelectorDialog = new FontSelectorDialog(Font.font(14));
        fontSelectorDialog.showAndWait();
        Font resultFont = fontSelectorDialog.getResult();

        if (resultFont != null) {
            double size = resultFont.getSize();
            String family = resultFont.getFamily();
            System.out.println(resultFont);
            tableView.setStyle("-fx-font-size: " + size);
            for (TableColumn<Exercises, ?> column : tableView.getColumns()) {
                column.setStyle("-fx-font-family: " + "'" + family + "'");
            }
        }
    }

    @FXML
    public void onClickAbout() {
        showInformationAlert("About", "Создано на ХКБМ МОРОЗОВА");
    }

    @FXML
    public void onClickExit() {
        log.info("Exit");
        ((EventHandler<ActionEvent>) event -> Platform.exit()).handle(null);

    }

    /**
     * To add files from standartPathTODir to array files
     * with using FileFilter
     *
     * @param standartPathTODir
     */
    private void getFileNames(File standartPathTODir) {
        FileFilter fileFilter = getFileFilter(typeOfFile);

        try {
            files = standartPathTODir.listFiles(fileFilter);
            for (File file : files) {
                System.out.println(file);
            }
            System.out.println("\n");
        } catch (Exception e) {
            onClickChooseDir();
            standartPathTODir = chosenDirectory;
            getFileNames(standartPathTODir);
        }
    }

    /**
     * return FileFilter by type of file
     *
     * @param type
     * @return
     */
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

    /**
     * initialize RadioBottoms, depends on contest of array files
     *
     * @return
     */
    private List<RadioButton> initRadioBottoms() {
        List<RadioButton> buttonList = new ArrayList<>();
        hBoxForRBut.setAlignment(Pos.CENTER);

        for (File file : files) {
            String name = file.getName().substring(0, file.getName().lastIndexOf("."));
            RadioButton radioButton = new RadioButton(name);
            radioButton.setUserData(name);
            buttonList.add(radioButton);
            hBoxForRBut.getChildren().add(radioButton);

            if (name.equalsIgnoreCase("Ukraine")) {
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

    public void onClickAddExerciseMenu(ActionEvent actionEvent) {
        initTable("all");
        toggleGroup.selectToggle(toggleGroup.getToggles().get(toggleGroup.getToggles().size() - 1));

        System.out.println("add..pressed");
        showInformationAlert("it will be soon", "visible rows: " + getNumberOfVisibleRows());
    }

    private int getNumberOfVisibleRows() {
        VirtualFlow<?> vf = loadVirtualFlow();
        return vf.getLastVisibleCell().getIndex() - vf.getFirstVisibleCell().getIndex();
    }


    private VirtualFlow<?> loadVirtualFlow() {
        return (VirtualFlow<?>) ((TableViewSkin<?>) tableView.getSkin()).getChildren().get(1);
    }

}
