package test;


import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class ListOrganizer extends Application {
    private static final String PREFIX =
            "http://icons.iconarchive.com/icons/jozef89/origami-birds/72/bird";

    private static final String SUFFIX =
            "-icon.png";

    private static final ObservableList<String> birds = FXCollections.observableArrayList(
            "-black",
            "-blue",
            "-red",
            "-red-2",
            "-yellow",
            "s-green",
            "s-green-2"
    );

    private static final ObservableList<Image> birdImages = FXCollections.observableArrayList();

    public static void main(String[] args) {
        launch(ListOrganizer.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        birds.forEach(bird -> birdImages.add(new Image(PREFIX + bird + SUFFIX)));

        ListView<String> birdList = new ListView<>(birds);
        birdList.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new BirdCell();
            }
        });
        birdList.setPrefWidth(180);

        VBox layout = new VBox(birdList);
        layout.setPadding(new Insets(10));

        stage.setScene(new Scene(layout));
        stage.show();
    }

    private class BirdCell extends ListCell<String> {
        private final ImageView imageView = new ImageView();

        public BirdCell() {
            ListCell thisCell = this;

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);

            setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (BirdCell.this.getItem() == null) {
                        return;
                    }

                    ObservableList<String> items = BirdCell.this.getListView().getItems();

                    Dragboard dragboard = BirdCell.this.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(BirdCell.this.getItem());
                    dragboard.setDragView(
                            birdImages.get(
                                    items.indexOf(
                                            BirdCell.this.getItem()
                                    )
                            )
                    );
                    dragboard.setContent(content);

                    event.consume();
                }
            });

            setOnDragOver(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    if (event.getGestureSource() != thisCell &&
                            event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }

                    event.consume();
                }
            });

            setOnDragEntered(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    if (event.getGestureSource() != thisCell &&
                            event.getDragboard().hasString()) {
                        BirdCell.this.setOpacity(0.3);
                    }
                }
            });

            setOnDragExited(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    if (event.getGestureSource() != thisCell &&
                            event.getDragboard().hasString()) {
                        BirdCell.this.setOpacity(1);
                    }
                }
            });

            setOnDragDropped(new EventHandler<DragEvent>() {
                @Override
                public void handle(DragEvent event) {
                    if (BirdCell.this.getItem() == null) {
                        return;
                    }

                    Dragboard db = event.getDragboard();
                    boolean success = false;

                    if (db.hasString()) {
                        ObservableList<String> items = BirdCell.this.getListView().getItems();
                        int draggedIdx = items.indexOf(db.getString());
                        int thisIdx = items.indexOf(BirdCell.this.getItem());

                        Image temp = birdImages.get(draggedIdx);
                        birdImages.set(draggedIdx, birdImages.get(thisIdx));
                        birdImages.set(thisIdx, temp);

                        items.set(draggedIdx, BirdCell.this.getItem());
                        items.set(thisIdx, db.getString());

                        List<String> itemscopy = new ArrayList<>(BirdCell.this.getListView().getItems());
                        BirdCell.this.getListView().getItems().setAll(itemscopy);

                        success = true;
                    }
                    event.setDropCompleted(success);

                    event.consume();
                }
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
            } else {
                imageView.setImage(
                        birdImages.get(
                                getListView().getItems().indexOf(item)
                        )
                );
                setGraphic(imageView);
            }
        }
    }

    // Iconset Homepage: http://jozef89.deviantart.com/art/Origami-Birds-400642253
    // License: CC Attribution-Noncommercial-No Derivate 3.0
    // Commercial usage: Not allowed

}