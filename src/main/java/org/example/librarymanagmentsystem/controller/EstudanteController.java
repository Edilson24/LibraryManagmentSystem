package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import org.example.librarymanagmentsystem.daos.EstudanteDAO;
import org.example.librarymanagmentsystem.entidades.Estudante;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EstudanteController implements Initializable {

    @FXML private TextField txtNome, txtCurso, txtDepartamento, txtIdade, txtCodigoEstudante, txtCartaoArduino;
    @FXML private TextField txtBuscar;
    @FXML private TableView<Estudante> tabelaEstudantes;
    @FXML private TableColumn<Estudante, Integer> colId;
    @FXML private TableColumn<Estudante, String> colNome, colCurso, colDepartamento, colCodigo, colCartaoRFID;
    @FXML private Button btnSalvar, btnAtualizar, btnDeletar, btnLimpar;

    private EstudanteDAO estudanteDAO;
    private ObservableList<Estudante> estudantesList;
    private Estudante estudanteSelecionado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        estudanteDAO = new EstudanteDAO();
        estudantesList = FXCollections.observableArrayList();

        configurarTabela();
        carregarDados();
        configurarListeners();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEstudante"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
        colDepartamento.setCellValueFactory(new PropertyValueFactory<>("departamento"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoEstudante"));
        colCartaoRFID.setCellValueFactory(new PropertyValueFactory<>("idCartaoArduino"));

        tabelaEstudantes.setItems(estudantesList);

        // Selecionar linha da tabela
        tabelaEstudantes.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                estudanteSelecionado = novo;
                preencherCampos(novo);
            }
        });
    }

    private void configurarListeners() {
        // Busca em tempo real
        txtBuscar.textProperty().addListener((obs, old, novo) -> {
            if (novo.isEmpty()) {
                carregarDados();
            } else {
                buscarEstudantes();
            }
        });
    }

    public void carregarDados() {
        new Thread(() -> {
            try {
                List<Estudante> estudantes = estudanteDAO.listarTodos();
                Platform.runLater(() -> {
                    estudantesList.clear();
                    estudantesList.addAll(estudantes);
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarErro("Erro ao carregar estudantes: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void salvarEstudante() {
        if (!validarCampos()) return;

        try {
            Estudante estudante = new Estudante();
            estudante.setNome(txtNome.getText().trim());
            estudante.setCurso(txtCurso.getText().trim());
            estudante.setDepartamento(txtDepartamento.getText().trim());
            estudante.setIdade(Integer.parseInt(txtIdade.getText()));
            estudante.setCodigoEstudante(txtCodigoEstudante.getText().trim());
            estudante.setIdCartaoArduino(txtCartaoArduino.getText().trim());

            estudanteDAO.inserir(estudante);
            mostrarSucesso("Estudante cadastrado com sucesso!");
            limparCampos();
            carregarDados();

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                mostrarErro("Código de estudante ou cartão RFID já existe!");
            } else {
                mostrarErro("Erro ao salvar: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            mostrarErro("Idade deve ser um número válido!");
        }
    }

    @FXML
    private void atualizarEstudante() {
        if (estudanteSelecionado == null) {
            mostrarAviso("Selecione um estudante na tabela para atualizar!");
            return;
        }

        if (!validarCampos()) return;

        try {
            estudanteSelecionado.setNome(txtNome.getText().trim());
            estudanteSelecionado.setCurso(txtCurso.getText().trim());
            estudanteSelecionado.setDepartamento(txtDepartamento.getText().trim());
            estudanteSelecionado.setIdade(Integer.parseInt(txtIdade.getText()));
            estudanteSelecionado.setCodigoEstudante(txtCodigoEstudante.getText().trim());
            estudanteSelecionado.setIdCartaoArduino(txtCartaoArduino.getText().trim());

            estudanteDAO.atualizar(estudanteSelecionado);
            mostrarSucesso("Estudante atualizado com sucesso!");
            limparCampos();
            carregarDados();

        } catch (SQLException e) {
            mostrarErro("Erro ao atualizar: " + e.getMessage());
        } catch (NumberFormatException e) {
            mostrarErro("Idade deve ser um número válido!");
        }
    }

    @FXML
    private void deletarEstudante() {
        if (estudanteSelecionado == null) {
            mostrarAviso("Selecione um estudante na tabela para deletar!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText(null);
        alert.setContentText("Tem certeza que deseja deletar o estudante " +
                estudanteSelecionado.getNome() + "?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                estudanteDAO.deletar(estudanteSelecionado.getId());
                mostrarSucesso("Estudante deletado com sucesso!");
                limparCampos();
                carregarDados();
            } catch (SQLException e) {
                mostrarErro("Erro ao deletar: " + e.getMessage());
            }
        }
    }

    @FXML
    private void limparCampos() {
        txtNome.clear();
        txtCurso.clear();
        txtDepartamento.clear();
        txtIdade.clear();
        txtCodigoEstudante.clear();
        txtCartaoArduino.clear();
        estudanteSelecionado = null;
        tabelaEstudantes.getSelectionModel().clearSelection();
    }

    @FXML
    private void buscarEstudantes() {
        /*String busca = txtBuscar.getText().trim();
        if (busca.isEmpty()) {
            carregarDados();
            return;
        }

        new Thread(() -> {
            List<Estudante> resultados = estudanteDAO.buscarPorNome(busca);
            Platform.runLater(() -> {
                estudantesList.clear();
                estudantesList.addAll(resultados);
            });
        }).start();*/
    }

    private void preencherCampos(Estudante e) {
        txtNome.setText(e.getNome());
        txtCurso.setText(e.getCurso());
        txtDepartamento.setText(e.getDepartamento());
        txtIdade.setText(String.valueOf(e.getIdade()));
        txtCodigoEstudante.setText(e.getCodigoEstudante());
        txtCartaoArduino.setText(e.getIdCartaoArduino());
    }

    private boolean validarCampos() {
        if (txtNome.getText().trim().isEmpty()) {
            mostrarAviso("Preencha o nome do estudante!");
            txtNome.requestFocus();
            return false;
        }
        if (txtCurso.getText().trim().isEmpty()) {
            mostrarAviso("Preencha o curso!");
            txtCurso.requestFocus();
            return false;
        }
        if (txtCodigoEstudante.getText().trim().isEmpty()) {
            mostrarAviso("Preencha o código do estudante!");
            txtCodigoEstudante.requestFocus();
            return false;
        }
        if (txtCartaoArduino.getText().trim().isEmpty()) {
            mostrarAviso("Preencha o ID do cartão RFID!");
            txtCartaoArduino.requestFocus();
            return false;
        }
        if (txtIdade.getText().trim().isEmpty()) {
            mostrarAviso("Preencha a idade!");
            txtIdade.requestFocus();
            return false;
        }
        return true;
    }

    private void mostrarSucesso(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarAviso(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}