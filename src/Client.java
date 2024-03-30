import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class Client extends Application 
{
    private static final String SERVER_ADDRESS = "172.17.8.225";
    //IPaddress(Laptop) server oda address vechu access panrom
    private static final int PORT_NUMBER = 5001;

    private PrintWriter out;
    private BufferedReader in;
    private TextField userInputField;
    private VBox chatContainer;
    private String clientName;
    private ScrollPane scrollPane;
    private int currentMatchIndex = -1;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Image icon = new Image(getClass().getResourceAsStream("ICON.png"));
        primaryStage.getIcons().add(icon);
        showLoginPage();
        setupUI(primaryStage);
        connectToServer();
    }

    private void showLoginPage() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Sri Eshwar Chat Application");
        dialog.setHeaderText(null);

        Image icon = new Image(getClass().getResourceAsStream("ICON.png"));
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(icon);
        stage.setResizable(true);
        //Window default ah resize panikalan and icon um varuthu 

        VBox content = new VBox();
        content.setSpacing(10);
        content.setAlignment(Pos.CENTER);

        content.setStyle("-fx-background-image: url('file:///C://Users//usppc//Desktop//New.jpg/'); -fx-background-size: cover; -fx-padding: 20;");
        VBox squareContainer = new VBox();
        squareContainer.setAlignment(Pos.CENTER);
        squareContainer.setMinWidth(400);

        Label titleLabel = new Label("LOGIN");
        titleLabel.setTranslateY(-30);
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");
        
        TextField nameTextField = new TextField();
        nameTextField.setPromptText("Enter Your Name");
        nameTextField.getStyleClass().add("login-text-field");
        nameTextField.setMaxWidth(130);
        nameTextField.setMaxHeight(20);
        nameTextField.setStyle("-fx-text-fill: black;");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Your Password");
        passwordField.getStyleClass().add("login-text-field");
        passwordField.setMaxWidth(130);
        passwordField.setMaxHeight(20); 
        passwordField.setStyle("-fx-text-fill: black;");

        Region space = new Region();
        space.setPrefHeight(10);

        squareContainer.setStyle("-fx-background-color: black; -fx-padding: 10; -fx-border-color: white; -fx-border-width: 1; -fx-border-radius: 5;");
        squareContainer.getChildren().addAll(titleLabel, nameTextField, space, passwordField);
        content.getChildren().add(squareContainer);

        squareContainer.setPrefSize(200,200); 
        squareContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        squareContainer.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        dialog.getDialogPane().setContent(content);

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(loginButtonType);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                String name = nameTextField.getText();
                String password = passwordField.getText();
                if (name.isEmpty() || password.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    //Error message varum pass podalena
                    alert.setTitle("Error");
                    alert.setHeaderText(null);
                    alert.setContentText("Please enter both name and password.");
                    alert.showAndWait();
                    return null;
                } else {
                    return name;
                }
            }
            return null;
        });

        dialog.getDialogPane().setPrefWidth(600); 
        dialog.getDialogPane().setPrefHeight(400); 

        dialog.getDialogPane().getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            clientName = name;
        });
    }
    //RISHI - (LOGIN UI)
    
    private void setupUI(Stage primaryStage) {
        if (clientName == null || clientName.trim().isEmpty()) {
            showLoginPage();
            return;
        }
    
        Label titleLabel = new Label("\u2B50  SECE CHAT APP  \u2B50");
        titleLabel.getStyleClass().add("title-label");
    
        HBox titleBar = new HBox(titleLabel);
        titleBar.getStyleClass().add("title-bar");
        titleBar.setAlignment(Pos.CENTER);
        titleBar.setMinHeight(40); // Adjust the height as needed
    
        userInputField = new TextField();
        userInputField.setPromptText("Type your message here...");
        userInputField.getStyleClass().add("input-field");
        chatContainer = new VBox();
        chatContainer.setPadding(new Insets(10));
        chatContainer.getStyleClass().add("chat-container");
        chatContainer.setStyle("-fx-background-color: black;");
    
        scrollPane = new ScrollPane(chatContainer);
        scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
    
        Button sendButton = new Button("         \uD83D\uDCE7         ");
        sendButton.setOnAction(e -> sendMessage());
        sendButton.getStyleClass().add("send-button");
    
        HBox bottomBox = new HBox(userInputField, sendButton);
        HBox.setHgrow(userInputField, Priority.ALWAYS);
        bottomBox.setAlignment(Pos.CENTER);
    
        TextField searchField = new TextField();
        searchField.setPromptText("Search messages");
        searchField.getStyleClass().add("search-field");
        searchField.setMaxWidth(200);
    
        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> searchMessages(searchField.getText()));
        searchButton.getStyleClass().add("search-button");
    
        HBox searchBox = new HBox(searchField, searchButton);
        searchBox.setAlignment(Pos.CENTER_RIGHT);
        searchBox.setSpacing(10);
    
        VBox mainBox = new VBox(titleBar, scrollPane, bottomBox, searchBox);
        mainBox.setVgrow(scrollPane, Priority.ALWAYS);
    
        BorderPane root = new BorderPane();
        root.setCenter(mainBox);
    
        Scene scene = new Scene(root, 400, 300);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setTitle("SECE Chat");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setOnCloseRequest(e -> closeConnection());
        primaryStage.show();
    
        scene.getRoot().setStyle("-fx-background-color: black;");
    
        userInputField.setOnAction(e -> sendMessage());
        sendButton.setOnAction(e -> sendMessage());
    
        chatContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            scrollPane.setVvalue(1.0);
        });
    }
    //PREDEEP (CHAT BOX - UI)
    
    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, PORT_NUMBER);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println(clientName);

            Thread incomingMessages = new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse = in.readLine()) != null) {
                        if (serverResponse.startsWith("/previousMessage:")) {
                            String message = serverResponse.substring("/previousMessage:".length());
                            appendToChatArea(message);
                        } else {
                            appendToChatArea(serverResponse);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            incomingMessages.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Connection to the db and server (SARATH)

    private void searchMessages(String query) {
        if (query.isEmpty()) {
            chatContainer.getChildren().forEach(node -> {
                if (node instanceof Label) {
                    Label label = (Label) node;
                    label.setStyle(""); 
                }
            });
            currentMatchIndex = -1;
            return;
        }

        boolean found = false;
        int totalChildren = chatContainer.getChildren().size();
        int startIndex = currentMatchIndex + 1;

        for (int i = startIndex; i < totalChildren; i++) {
            javafx.scene.Node node = chatContainer.getChildren().get(i);
            if (node instanceof Label) {
                Label label = (Label) node;
                if (label.getText().toLowerCase().contains(query.toLowerCase())) {
                    found = true;
                    if (currentMatchIndex != -1 && currentMatchIndex < totalChildren) {
                        Label previousMatch = (Label) chatContainer.getChildren().get(currentMatchIndex);
                        previousMatch.setStyle("");
                    }
                    label.setStyle("-fx-background-color: magenta; -fx-font-weight: bold;");
                    scrollPane.setVvalue((i * 1.0) / totalChildren);
                    currentMatchIndex = i;
                    break;
                }
            }
        }
        if (!found) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Search Result");
            alert.setHeaderText(null);
            alert.setContentText("Message not found!");
            alert.showAndWait();
            currentMatchIndex = -1; 
        }
    } 

    private void sendMessage() {
        String message = userInputField.getText();
        out.println(message);
        userInputField.clear();
        appendToChatArea("You: " + message);
    }

    private void appendToChatArea(String message) {
        final StringBuilder finalMessage = new StringBuilder(message);
        Platform.runLater(() -> {
            Label label = new Label(finalMessage.toString());
            label.getStyleClass().add("chat-label");
    
            if (finalMessage.toString().startsWith("You")) {
                label.getStyleClass().add("user-message");
                //Try pandrom to assign the message in right and left for opposite users
                label.setAlignment(Pos.CENTER_RIGHT);
            } else {
                String[] parts = finalMessage.toString().split(": ", 2);
                if (parts.length == 2) {
                    String senderName = parts[0];
                    finalMessage.setLength(0);
                    finalMessage.append(senderName.equals(clientName) ? "You: " + parts[1] : message);
                }
                label.getStyleClass().add("other-message");
            }
            VBox.setMargin(label, new Insets(0, 0, 10, 0));
            //size maathurom
            chatContainer.getChildren().add(label);
        });
    }

    private void closeConnection() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}