package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.librarymanagmentsystem.daos.EstudanteDAO;
import org.example.librarymanagmentsystem.entidades.Estudante;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EstudanteController {

    private EstudanteDAO estudanteDAO;
    private ObservableList<Estudante> estudantesList;
    private Estudante estudanteSelecionado;

    // Componentes da interface
    private TextField txtNome, txtCurso, txtDepartamento, txtIdade, txtCodigoEstudante, txtCartaoArduino;
    private TextField txtBuscar;
    private TableView<Estudante> tabelaEstudantes;
    private TableColumn<Estudante, Integer> colId;
    private TableColumn<Estudante, String> colNome, colCurso, colDepartamento, colCodigo, colCartaoRFID;
    private Button btnSalvar, btnAtualizar, btnDeletar, btnLimpar;

    @SuppressWarnings("unchecked")
    public void inicializar(
            TextField txtNome,
            TextField txtCurso,
            TextField txtDepartamento,
            TextField txtIdade,
            TextField txtCodigoEstudante,
            TextField txtCartaoArduino,
            TextField txtBuscar,
            TableView<?> tabelaEstudantes,
            TableColumn<?, ?> colId,
            TableColumn<?, ?> colNome,
            TableColumn<?, ?> colCurso,
            TableColumn<?, ?> colDepartamento,
            TableColumn<?, ?> colCodigo,
            TableColumn<?, ?> colCartaoRFID,
            Button btnSalvar,
            Button btnAtualizar,
            Button btnDeletar,
            Button btnLimpar) {

        this.txtNome = txtNome;
        this.txtCurso = txtCurso;
        this.txtDepartamento = txtDepartamento;
        this.txtIdade = txtIdade;
        this.txtCodigoEstudante = txtCodigoEstudante;
        this.txtCartaoArduino = txtCartaoArduino;
        this.txtBuscar = txtBuscar;
        this.tabelaEstudantes = (TableView<Estudante>) tabelaEstudantes;
        this.colId = (TableColumn<Estudante, Integer>) colId;
        this.colNome = (TableColumn<Estudante, String>) colNome;
        this.colCurso = (TableColumn<Estudante, String>) colCurso;
        this.colDepartamento = (TableColumn<Estudante, String>) colDepartamento;
        this.colCodigo = (TableColumn<Estudante, String>) colCodigo;
        this.colCartaoRFID = (TableColumn<Estudante, String>) colCartaoRFID;
        this.btnSalvar = btnSalvar;
        this.btnAtualizar = btnAtualizar;
        this.btnDeletar = btnDeletar;
        this.btnLimpar = btnLimpar;

        estudanteDAO = new EstudanteDAO();
        estudantesList = FXCollections.observableArrayList();

        configurarTabela();
        configurarEventos();
        carregarDados();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCurso.setCellValueFactory(new PropertyValueFactory<>("curso"));
        colDepartamento.setCellValueFactory(new PropertyValueFactory<>("departamento"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoEstudante"));
        colCartaoRFID.setCellValueFactory(new PropertyValueFactory<>("idCartaoArduino"));

        tabelaEstudantes.setItems(estudantesList);

        tabelaEstudantes.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                estudanteSelecionado = novo;
                preencherCampos(novo);
            }
        });
    }

    private void configurarEventos() {
        txtBuscar.textProperty().addListener((obs, old, novo) -> {
            if (novo.isEmpty()) {
                carregarDados();
            } else {
                buscarEstudantes();
            }
        });

        btnSalvar.setOnAction(e -> salvarEstudante());
        btnAtualizar.setOnAction(e -> atualizarEstudante());
        btnDeletar.setOnAction(e -> deletarEstudante());
        btnLimpar.setOnAction(e -> limparCampos());
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
                Platform.runLater(() -> mostrarErro("Erro ao carregar: " + e.getMessage()));
            }
        }).start();
    }

    private void salvarEstudante() {
        if (!validarCampos()) return;

        try {
            Estudante estudante = new Estudante(
                    txtNome.getText().trim(),
                    Integer.parseInt(txtIdade.getText()),
                    txtDepartamento.getText().trim(),
                    txtCurso.getText().trim(),
                    txtCartaoArduino.getText().trim(),
                    txtCodigoEstudante.getText().trim()
            );

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

    private void atualizarEstudante() {
        if (estudanteSelecionado == null) {
            mostrarAviso("Selecione um estudante na tabela!");
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

    void deletarEstudante() {
        if (estudanteSelecionado == null) {
            mostrarAviso("Selecione um estudante na tabela!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar exclusão");
        alert.setHeaderText(null);
        alert.setContentText("Tem certeza que deseja deletar " + estudanteSelecionado.getNome() + "?");

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

    void buscarEstudantes() {
        String busca = txtBuscar.getText().trim();
        if (busca.isEmpty()) {
            carregarDados();
            return;
        }

        new Thread(() -> {
            try {
                List<Estudante> resultados = estudanteDAO.buscarPorNome(busca);
                Platform.runLater(() -> {
                    estudantesList.clear();
                    estudantesList.addAll(resultados);
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarErro("Erro na busca: " + e.getMessage()));
            }
        }).start();
    }

    private void preencherCampos(Estudante e) {
        txtNome.setText(e.getNome());
        txtCurso.setText(e.getCurso());
        txtDepartamento.setText(e.getDepartamento());
        txtIdade.setText(String.valueOf(e.getIdade()));
        txtCodigoEstudante.setText(e.getCodigoEstudante());
        txtCartaoArduino.setText(e.getIdCartaoArduino());
    }

    public void limparCampos() {
        txtNome.clear();
        txtCurso.clear();
        txtDepartamento.clear();
        txtIdade.clear();
        txtCodigoEstudante.clear();
        txtCartaoArduino.clear();
        estudanteSelecionado = null;
        tabelaEstudantes.getSelectionModel().clearSelection();
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