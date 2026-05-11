package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.librarymanagmentsystem.daos.UsuarioDAO;
import org.example.librarymanagmentsystem.entidades.Usuario;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Button close;
    @FXML private AnchorPane main_form;

    private UsuarioDAO usuarioDAO;
    private Usuario usuarioLogado;

    private double x = 0;
    private double y = 0;
    @Override
    public void initialize(URL location, ResourceBundle resources) {



        usuarioDAO = new UsuarioDAO();

        // Permitir login com Enter
        txtPassword.setOnKeyPressed(this::handleEnterKey);
        txtUsuario.setOnKeyPressed(this::handleEnterKey);
    }

    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            loginAdmin();
        }
    }

    @FXML
    private void loginAdmin() {
        String usuario = txtUsuario.getText().trim();
        String senha = txtPassword.getText().trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            mostrarAlerta("Campos vazios", "Preencha usuário e senha para continuar!");
            return;
        }

        // Desabilitar botão durante autenticação
        btnLogin.setDisable(true);
        btnLogin.setText("Autenticando...");

        new Thread(() -> {
            try {
                Usuario usuarioAutenticado = usuarioDAO.autenticar(usuario, senha);

                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    btnLogin.setText("entrar");

                    if (usuarioAutenticado != null) {
                        usuarioLogado = usuarioAutenticado;
                        abrirDashboard(usuarioAutenticado);
                    } else {
                        mostrarAlerta("Erro de autenticação",
                                "Usuário ou senha incorretos!");
                        txtPassword.clear();
                        txtPassword.requestFocus();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    btnLogin.setDisable(false);
                    btnLogin.setText("entrar");
                    mostrarAlerta("Erro", "Erro ao conectar ao banco de dados: " + e.getMessage());
                });
            }
        }).start();
    }

    private void abrirDashboard(Usuario usuario) {
        try {
            // Carregar a tela principal (Dashboard)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/LibraryManagmentSystem/view/dashboard/Dashboard.fxml"));
            Parent root = loader.load();

            // Configurar a nova cena
            Stage stage = new Stage();
            Scene scene = new Scene(root);

            // Obter o controller do dashboard
            DashBoardController dashboardController = loader.getController();

            // Passar o usuário logado para o dashboard
            dashboardController.setUsuarioLogado(usuario);



            // Carregar CSS
            scene.getStylesheets().add(getClass().getResource("/org/example/LibraryManagmentSystem/css/dashboardStyle.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("SISMON - Sistema de Gestão Bibliotecária");
            stage.setMaximized(true);
            stage.show();

            stage.initStyle(StageStyle.TRANSPARENT);

            root.setOnMousePressed((MouseEvent event) ->{
                x = event.getSceneX();
                y = event.getSceneY();
            });

            root.setOnMouseDragged((MouseEvent event) ->{
                stage.setX(event.getScreenX() - x);
                stage.setY(event.getScreenY() - y);

                stage.setOpacity(.8);
            });

            root.setOnMouseReleased((MouseEvent event) ->{
                stage.setOpacity(1);
            });

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível carregar o sistema: " + e.getMessage());
        }
    }

    @FXML
    private void close() {
        Stage stage = (Stage) close.getScene().getWindow();
        stage.close();
        Platform.exit();
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}