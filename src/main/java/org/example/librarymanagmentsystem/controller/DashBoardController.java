package org.example.librarymanagmentsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class DashBoardController {

    @FXML
    private Label dasboard_btn;

    @FXML
    private AnchorPane dashboard_form;

    @FXML
    private Label estudante_form;

    @FXML
    private Label estudantes_btn;

    @FXML
    private Label livros_btn;

    @FXML
    private AnchorPane livros_form;

    @FXML
    private Label relatorio_btn;

    @FXML
    private AnchorPane relatorio_form;

    @FXML
    private AnchorPane telaEstudante_form;

    public void mudarTela(ActionEvent event){
        // 1. Identificar qual label foi clicada
        Label btnClicado = (Label) event.getSource();

        // 2. Lista de todos os formulários e botões para facilitar o reset
        AnchorPane[] formulários = {dashboard_form, livros_form, relatorio_form, telaEstudante_form};
        Label[] botoes = {dasboard_btn, estudantes_btn, livros_btn, relatorio_btn};

        // 3. Resetar estilos e esconder todas as telas
        for (int i = 0; i < formulários.length; i++) {
            formulários[i].setVisible(false);
            // Estilo In-line: Transparente e texto branco (ajuste conforme seu azul)
            botoes[i].setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
        }

        // 4. Ativar a tela correspondente e estilizar o botão (Active)
        if (btnClicado == dasboard_btn) {
            dashboard_form.setVisible(true);
        } else if (btnClicado == estudantes_btn) {
            telaEstudante_form.setVisible(true);
        } else if (btnClicado == livros_btn) {
            livros_form.setVisible(true);
        } else if (btnClicado == relatorio_btn) {
            relatorio_form.setVisible(true);
        }

        // Estilo de botão Ativo (Azul mais claro ou branco com texto escuro)
        btnClicado.setStyle("-fx-background-color: #ffffff33; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5px;");

    }
}
