package org.example.librarymanagmentsystem.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class DashBoardController implements Initializable {

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
    private Label emprestimos_btn;

    @FXML
    private AnchorPane livros_form;

    @FXML
    private Label relatorio_btn;

    @FXML
    private AnchorPane relatorio_form;
    
    @FXML
    private AnchorPane emprestimos_form;

    @FXML
    private AnchorPane telaEstudante_form;

    public void mudarTela(MouseEvent event) {
        // Verificação de segurança
        if (!(event.getSource() instanceof Label)) {
            System.out.println("Event source não é um Label: " + event.getSource());
            return;
        }

        Label btnClicado = (Label) event.getSource();

        // Mapa completo com TODOS os botões e telas
        Map<Label, AnchorPane> telaMap = new HashMap<>();
        telaMap.put(dasboard_btn, dashboard_form);
        telaMap.put(estudantes_btn, telaEstudante_form);
        telaMap.put(livros_btn, livros_form);
        telaMap.put(relatorio_btn, relatorio_form);
        telaMap.put(emprestimos_btn, emprestimos_form);  // ← ADICIONADO

        // Array com TODOS os botões do menu
        Label[] todosBotoes = {dasboard_btn, estudantes_btn, livros_btn, relatorio_btn, emprestimos_btn};  // ← ADICIONADO

        // Esconder todas as telas
        for (AnchorPane tela : telaMap.values()) {
            if (tela != null) {
                tela.setVisible(false);
            }
        }

        // Resetar estilo de todos os botões
        for (Label botao : todosBotoes) {
            if (botao != null) {
                botao.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
            }
        }

        // Mostrar tela selecionada e estilizar botão
        AnchorPane telaSelecionada = telaMap.get(btnClicado);
        if (telaSelecionada != null) {
            telaSelecionada.setVisible(true);
            btnClicado.setStyle("-fx-background-color: #ffffff33; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5px;");
        }
    }

    private void limparCamposEmprestimo() {
    }

    private void carregarRelatorios() {
    }

    private void carregarListaLivros() {
    }

    private void carregarListaEstudantes() {
    }


    public void salvarEstudante(ActionEvent actionEvent) {
    }

    public void buscarEstudantes(KeyEvent keyEvent) {
    }

    public void atualizarEstudante(ActionEvent actionEvent) {
    }

    public void deletarEstudante(ActionEvent actionEvent) {
    }

    public void limparCampos(ActionEvent actionEvent) {
    }

    public void salvarLivro(ActionEvent actionEvent) {
    }

    public void limparCamposLivro(ActionEvent actionEvent) {
    }



    /**
     * Called to initialize a controller after its root element has been
     * completely processed.
     *
     * @param location  The location used to resolve relative paths for the root object, or
     *                  {@code null} if the location is not known.
     * @param resources The resources used to localize the root object, or {@code null} if
     *                  the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Garantir que apenas o Dashboard está visível no início
        dashboard_form.setVisible(true);
        telaEstudante_form.setVisible(false);
        livros_form.setVisible(false);
        relatorio_form.setVisible(false);
        emprestimos_form.setVisible(false);

        // Aplicar estilo ativo no botão Dashboard
        dasboard_btn.setStyle("-fx-background-color: #ffffff33; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5px;");

        // Resetar estilo dos outros botões
        estudantes_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
        livros_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
        relatorio_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");

        // Carregar dados iniciais
        carregarCardsDashboard();
        carregarGraficoDashboard();
        carregarUltimosEmprestimos();

        // Carregar dados iniciais
        carregarCardsDashboard();
        carregarGraficoDashboard();
        carregarUltimosEmprestimos();
    }

    private void carregarGraficoDashboard() {
    }

    private void carregarCardsDashboard() {
    }

    private void carregarUltimosEmprestimos() {
    }

    public void realizarEmprestimo(ActionEvent actionEvent) {
    }

    public void realizarDevolucao(ActionEvent actionEvent) {
    }

    public void buscarPorRFID(ActionEvent actionEvent) {
    }

    public void buscarLivro(ActionEvent actionEvent) {
    }
}
