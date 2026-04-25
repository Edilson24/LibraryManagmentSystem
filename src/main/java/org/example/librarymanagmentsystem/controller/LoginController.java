package org.example.librarymanagmentsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class LoginController {

    @FXML
    private Button btnLogin;

    @FXML
    private Button close;

    @FXML
    private AnchorPane main_form;

    @FXML
    private PasswordField txtPassword;

    @FXML
    private TextField txtUsuario;

    private double x = 0;
    private double y = 0;
    Alert alert;

    public void loginAdmin() {
        try {
            alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setHeaderText(null);
            alert.setContentText("ACESSO CONCEDIDO AO ADMINISTRADOR");
            alert.showAndWait();

            //ABRIR DASHBOARD DO ADMINISTRADOR
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/LibraryManagmentSystem/view/dashboard/Dashboard.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            Scene scene = new Scene(root);

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

            stage.setScene(scene);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("Painel de Controle - Sistema de Candidaturas");
            stage.setResizable(false);
            stage.show();

            // Fecha a janela de login
            Stage loginStage = (Stage) main_form.getScene().getWindow();
            loginStage.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void close() {

    }


}
