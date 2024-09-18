package fx;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import mj.impl.Exceptions.ControlFlowException;
import mj.impl.Node;
import mj.impl.Obj;
import mj.run.AbstractSyntaxTree;
import mj.run.Interpreter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class ASTViewerController {

    private boolean ctrlPressed = false;
    private final Object lock = new Object();
    Stage stage;
    File selectedFile;
    double oldZoom;
    String oldScroll;
    AbstractSyntaxTree ast;
    @FXML
    public WebView webView;
    @FXML
    public TreeView<Node> treeView;
    @FXML
    public ListView<String> listView;
    @FXML
    public MenuItem openFile;
    @FXML
    public Button compileButton;
    @FXML
    public Button runButton;
    @FXML
    public Button debugButton;
    @FXML
    public Button stepButton;
    @FXML
    public TableView<TabItem> globalSymTab;
    @FXML
    public TableView<TabItem> localSymTab;
    @FXML
    public Label locVarLabel;
    @FXML
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    @FXML
    public void initialize() {
        setSymTabCellFactory(globalSymTab);
        setSymTabCellFactory(localSymTab);

        Thread watcherThread = new Thread(() -> {
            try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
                Path svgFile = Paths.get(".");
                svgFile.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
                WatchKey key;
                while (true) {
                    try {
                        key = watchService.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    for (WatchEvent<?> evt : key.pollEvents()) {
                        WatchEvent<Path> pathEvent = (WatchEvent<Path>) evt;
                        Path fileName = pathEvent.context();
                        if (fileName.toString().equals(AbstractSyntaxTree.SVG_FILENAME)) {
                            Platform.runLater(this::loadSVG);
                        }
                    }
                    key.reset();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        watcherThread.start();

        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> listView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            if (ast != null && ast.getInterpreter() != null && getIndex() == ast.getInterpreter().getLineOfExecution() - 1) {
                                setStyle("-fx-background-color: red;");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                };
            }
        });

        treeView.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<Node> call(TreeView<Node> param) {
                return new TreeCell<>() {
                    @Override
                    protected void updateItem(Node item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item.toString());
                            if (item.isCurrentBreakpoint()) {
                                setStyle("-fx-background-color: red;");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                };
            }
        });

        webView.getEngine().getLoadWorker().stateProperty().addListener((_, _, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                webView.setZoom(oldZoom);
                webView.getEngine().executeScript("window.scrollTo(%s);".formatted(oldScroll));
            }
        });


    }
    private void setSymTabCellFactory(TableView<TabItem> tableView) {
        tableView.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("address"));
        tableView.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tableView.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("type"));
        tableView.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("value"));
    }
    @FXML
    public void step() {
        synchronized (lock) {
            lock.notify();
        }
        treeView.refresh();
        listView.refresh();
        fillSymTab();
    }
    @FXML
    public void debug() {
        ast.setDebug(true, lock);
        compileButton.setDisable(true);
        runButton.setDisable(true);
        debugButton.setDisable(true);
        stepButton.setDisable(false);
        run();
    }
    @FXML
    public void compile() {
        ast = new AbstractSyntaxTree(selectedFile.getAbsolutePath());

        if (ast.isCompiled()) {
            TreeItem<Node> item = ast.getRoot().buildTreeView();
            treeView.setRoot(item);
            runButton.setDisable(false);
            debugButton.setDisable(false);
            fillSymTab();
        }
    }
    public void fillSymTab() {
        fillGlobalSymTab();
        fillLocalSymTab();
    }
    public void fillGlobalSymTab() {
        globalSymTab.getItems().clear();

        Interpreter interpreter = ast.getInterpreter();
        Obj program = (Obj)interpreter.getRoot();

        for (Obj obj : program.locals.values()) {
            if (obj.kind == Obj.Kind.Var) {
                int value = interpreter.getData(obj.adr);
                globalSymTab.getItems().add(new TabItem(obj, value));
            }
        }
        globalSymTab.refresh();
    }
    public void fillLocalSymTab() {

        localSymTab.getItems().clear();

        Interpreter interpreter = ast.getInterpreter();
        Obj curMethod = interpreter.getCurMethod();

        if (curMethod != null) {
            locVarLabel.setText("Local Variables (%s)".formatted(curMethod.name));
            for (Obj obj : curMethod.locals.values()) {
                int value = interpreter.getLocal(obj.adr);
                localSymTab.getItems().add(new TabItem(obj, value));
            }
        } else {
            locVarLabel.setText("Local Variables");
        }
        localSymTab.refresh();
    }
    private void loadSVG() {
        oldZoom = webView.getZoom();
        oldScroll = (String) webView.getEngine().executeScript("window.scrollX + ',' + window.scrollY");
        String svgContent;
        try {
            svgContent = new String(Files.readAllBytes(Paths.get(AbstractSyntaxTree.SVG_FILENAME)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String svg = "<html><body>%s</body></html>".formatted(svgContent);
        webView.getEngine().loadContent(svg);
    }
    @FXML
    public void run() {
        Thread thread = new Thread(() -> {
            try {
                ast.run();
                ast.setDebug(false, null);
                compileButton.setDisable(false);
                runButton.setDisable(false);
                debugButton.setDisable(false);
                stepButton.setDisable(true);
            } catch (ControlFlowException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();
    }
    @FXML
    public void zoom(ScrollEvent event) {
        if (ctrlPressed) {
            double newZoom = Math.max(webView.getZoom() + event.getDeltaY() * 0.001, 0.05);
            webView.setZoom(newZoom);
        }
    }
    @FXML
    public void ctrlPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) {
            ctrlPressed = true;
        }
    }
    @FXML
    public void ctrlReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.CONTROL) {
            ctrlPressed = false;
        }
    }
    @FXML
    private void openFile() {
        // Create a FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");

        // Set extension filters (optional)
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("MicroJava Files", "*.mj")
        );
        // Show the open file dialog
        selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {

            listView.getItems().clear();
            treeView.setRoot(null);
            globalSymTab.getItems().clear();
            localSymTab.getItems().clear();

            Scanner scanner;
            try {
                scanner = new Scanner(selectedFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            List<String> sourceCode = new ArrayList<>();
            while (scanner.hasNextLine()) {
                sourceCode.add(scanner.nextLine());
            }
            int width = (int)Math.log10(sourceCode.size()) + 1;
            String formatString = STR."%\{width}d:  %s";
            for (int i = 0; i < sourceCode.size(); i++) {
                listView.getItems().add(formatString.formatted(i+1, sourceCode.get(i)));
            }
            //enable compile button
            compileButton.setDisable(false);
            runButton.setDisable(true);
            debugButton.setDisable(true);
            stepButton.setDisable(true);

        } else {
            System.out.println("File selection cancelled.");
        }
    }
}