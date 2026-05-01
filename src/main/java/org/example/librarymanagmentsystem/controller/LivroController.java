package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.librarymanagmentsystem.daos.DisciplinaDAO;
import org.example.librarymanagmentsystem.daos.LivroDAO;
import org.example.librarymanagmentsystem.entidades.Disciplina;
import org.example.librarymanagmentsystem.entidades.Livro;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class LivroController {

    private LivroDAO livroDAO;
    private DisciplinaDAO disciplinaDAO;
    private ObservableList<Livro> livrosList;
    private ObservableList<Disciplina> disciplinasList;
    private Livro livroSelecionado;

    private TextField txtTitulo, txtAutor, txtISBN, txtAnoPublicacao, txtCategoria, txtUnidades;
    private TextField txtBuscar;
    private ComboBox<Disciplina> cbDisciplina;
    private ComboBox<String> cbStatus;
    private TableView<Livro> tabelaLivros;
    private TableColumn<Livro, Integer> colId;
    private TableColumn<Livro, String> colTitulo, colAutor, colStatus, colCategoria, colDisciplina;
    private TableColumn<Livro, Integer> colUnidades;
    private Button btnSalvar, btnAtualizar, btnDeletar, btnLimpar;
    private TextArea txtCitacao, txtBibliografia;

    @SuppressWarnings("unchecked")
    public void inicializar(
            TextField txtTitulo,
            TextField txtAutor,
            TextField txtISBN,
            TextField txtAnoPublicacao,
            TextField txtCategoria,
            TextField txtUnidades,
            TextField txtBuscar,
            ComboBox<?> cbDisciplina,
            ComboBox<?> cbStatus,
            TableView<?> tabelaLivros,
            TableColumn<?, ?> colId,
            TableColumn<?, ?> colTitulo,
            TableColumn<?, ?> colAutor,
            TableColumn<?, ?> colStatus,
            TableColumn<?, ?> colCategoria,
            TableColumn<?, ?> colDisciplina,
            TableColumn<?, ?> colUnidades,
            Button btnSalvar,
            Button btnAtualizar,
            Button btnDeletar,
            Button btnLimpar,
            TextArea txtCitacao,
            TextArea txtBibliografia) {

        this.txtTitulo = txtTitulo;
        this.txtAutor = txtAutor;
        this.txtISBN = txtISBN;
        this.txtAnoPublicacao = txtAnoPublicacao;
        this.txtCategoria = txtCategoria;
        this.txtUnidades = txtUnidades;
        this.txtBuscar = txtBuscar;
        this.cbDisciplina = (ComboBox<Disciplina>) cbDisciplina;
        this.cbStatus = (ComboBox<String>) cbStatus;
        this.tabelaLivros = (TableView<Livro>) tabelaLivros;
        this.colId = (TableColumn<Livro, Integer>) colId;
        this.colTitulo = (TableColumn<Livro, String>) colTitulo;
        this.colAutor = (TableColumn<Livro, String>) colAutor;
        this.colStatus = (TableColumn<Livro, String>) colStatus;
        this.colCategoria = (TableColumn<Livro, String>) colCategoria;
        this.colDisciplina = (TableColumn<Livro, String>) colDisciplina;
        this.colUnidades = (TableColumn<Livro, Integer>) colUnidades;
        this.btnSalvar = btnSalvar;
        this.btnAtualizar = btnAtualizar;
        this.btnDeletar = btnDeletar;
        this.btnLimpar = btnLimpar;
        this.txtCitacao = txtCitacao;
        this.txtBibliografia = txtBibliografia;

        livroDAO = new LivroDAO();
        disciplinaDAO = new DisciplinaDAO();
        livrosList = FXCollections.observableArrayList();
        disciplinasList = FXCollections.observableArrayList();

        configurarTabela();
        configurarCombos();
        configurarEventos();
        carregarDados();
        carregarDisciplinas();
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idLivro"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colDisciplina.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDisciplina() != null ?
                                cellData.getValue().getDisciplina().getNomeDisciplina() : ""
                )
        );
        colUnidades.setCellValueFactory(new PropertyValueFactory<>("unidades"));

        tabelaLivros.setItems(livrosList);

        tabelaLivros.getSelectionModel().selectedItemProperty().addListener((obs, old, novo) -> {
            if (novo != null) {
                livroSelecionado = novo;
                preencherCampos(novo);
                mostrarCitacaoBibliografia(novo);
            }
        });
    }

    private void configurarCombos() {
        cbStatus.setItems(FXCollections.observableArrayList("Disponível", "Emprestado", "Manutenção"));
    }

    private void carregarDisciplinas() {
        new Thread(() -> {
            try {
                List<Disciplina> disciplinas = disciplinaDAO.listarTodas();
                Platform.runLater(() -> {
                    disciplinasList.clear();
                    disciplinasList.addAll(disciplinas);
                    cbDisciplina.setItems(disciplinasList);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void configurarEventos() {
        txtBuscar.textProperty().addListener((obs, old, novo) -> {
            if (novo.isEmpty()) {
                carregarDados();
            } else {
                buscarLivros();
            }
        });

        btnSalvar.setOnAction(e -> salvarLivro());
        btnAtualizar.setOnAction(e -> atualizarLivro());
        btnDeletar.setOnAction(e -> deletarLivro());
        btnLimpar.setOnAction(e -> limparCampos());
    }

    public void carregarDados() {
        new Thread(() -> {
            try {
                List<Livro> livros = livroDAO.listarTodos();
                Platform.runLater(() -> {
                    livrosList.clear();
                    livrosList.addAll(livros);
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarErro("Erro ao carregar livros: " + e.getMessage()));
            }
        }).start();
    }

    private void salvarLivro() {
        if (!validarCampos()) return;

        try {
            Livro livro = new Livro();
            livro.setTitulo(txtTitulo.getText().trim());
            livro.setAutor(txtAutor.getText().trim());
            livro.setAnoPublicacao(Integer.parseInt(txtAnoPublicacao.getText()));
            livro.setIsbn(txtISBN.getText().trim());
            livro.setCategoria(txtCategoria.getText().trim());
            livro.setUnidades(Integer.parseInt(txtUnidades.getText()));
            livro.setStatus("Disponível");
            livro.setDisciplina(cbDisciplina.getValue());

            livroDAO.inserir(livro);
            mostrarSucesso("Livro cadastrado com sucesso!");
            limparCampos();
            carregarDados();
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                mostrarErro("ISBN já cadastrado!");
            } else {
                mostrarErro("Erro ao salvar: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            mostrarErro("Ano e unidades devem ser números válidos!");
        }
    }

    private void atualizarLivro() {
        if (livroSelecionado == null) {
            mostrarAviso("Selecione um livro!");
            return;
        }
        if (!validarCampos()) return;

        try {
            livroSelecionado.setTitulo(txtTitulo.getText().trim());
            livroSelecionado.setAutor(txtAutor.getText().trim());
            livroSelecionado.setAnoPublicacao(Integer.parseInt(txtAnoPublicacao.getText()));
            livroSelecionado.setIsbn(txtISBN.getText().trim());
            livroSelecionado.setCategoria(txtCategoria.getText().trim());
            livroSelecionado.setUnidades(Integer.parseInt(txtUnidades.getText()));
            livroSelecionado.setStatus(cbStatus.getValue());
            livroSelecionado.setDisciplina(cbDisciplina.getValue());

            livroDAO.atualizar(livroSelecionado);
            mostrarSucesso("Livro atualizado!");
            limparCampos();
            carregarDados();
        } catch (SQLException e) {
            mostrarErro("Erro ao atualizar: " + e.getMessage());
        } catch (NumberFormatException e) {
            mostrarErro("Ano e unidades devem ser números válidos!");
        }
    }

    private void deletarLivro() {
        if (livroSelecionado == null) {
            mostrarAviso("Selecione um livro!");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar exclusão");
        alert.setContentText("Deletar \"" + livroSelecionado.getTitulo() + "\"?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                livroDAO.deletar(livroSelecionado.getIdLivro());
                mostrarSucesso("Livro deletado!");
                limparCampos();
                carregarDados();
            } catch (SQLException e) {
                mostrarErro("Erro ao deletar: " + e.getMessage());
            }
        }
    }

    private void buscarLivros() {
        String busca = txtBuscar.getText().trim();
        if (busca.isEmpty()) {
            carregarDados();
            return;
        }

        new Thread(() -> {
            try {
                List<Livro> resultados = livroDAO.buscarPorTitulo(busca);
                Platform.runLater(() -> {
                    livrosList.clear();
                    livrosList.addAll(resultados);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void mostrarCitacaoBibliografia(Livro livro) {
        StringBuilder citacao = new StringBuilder();
        citacao.append(livro.getAutor().toUpperCase()).append(" (");
        citacao.append(livro.getAnoPublicacao()).append("). ");
        citacao.append(livro.getTitulo()).append(".");
        txtCitacao.setText(citacao.toString());

        StringBuilder bibliografia = new StringBuilder();
        String[] autores = livro.getAutor().split(" ");
        if (autores.length > 1) {
            bibliografia.append(autores[autores.length - 1]).append(", ");
            for (int i = 0; i < autores.length - 1; i++) {
                bibliografia.append(autores[i].charAt(0)).append(". ");
            }
        } else {
            bibliografia.append(livro.getAutor());
        }
        bibliografia.append(". ").append(livro.getTitulo()).append(". ");
        bibliografia.append(livro.getAnoPublicacao()).append(".");
        txtBibliografia.setText(bibliografia.toString());
    }

    private void preencherCampos(Livro l) {
        txtTitulo.setText(l.getTitulo());
        txtAutor.setText(l.getAutor());
        txtISBN.setText(l.getIsbn());
        txtAnoPublicacao.setText(String.valueOf(l.getAnoPublicacao()));
        txtCategoria.setText(l.getCategoria());
        txtUnidades.setText(String.valueOf(l.getUnidades()));
        cbStatus.setValue(l.getStatus());
        cbDisciplina.setValue(l.getDisciplina());
    }

    public void limparCampos() {
        txtTitulo.clear();
        txtAutor.clear();
        txtISBN.clear();
        txtAnoPublicacao.clear();
        txtCategoria.clear();
        txtUnidades.clear();
        cbStatus.setValue(null);
        cbDisciplina.setValue(null);
        txtCitacao.clear();
        txtBibliografia.clear();
        livroSelecionado = null;
        tabelaLivros.getSelectionModel().clearSelection();
    }

    private boolean validarCampos() {
        if (txtTitulo.getText().trim().isEmpty()) {
            mostrarAviso("Preencha o título!");
            return false;
        }
        if (txtAutor.getText().trim().isEmpty()) {
            mostrarAviso("Preencha o autor!");
            return false;
        }
        if (txtUnidades.getText().trim().isEmpty()) {
            mostrarAviso("Preencha as unidades!");
            return false;
        }
        return true;
    }

    private void mostrarSucesso(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg);
        alert.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg);
        alert.showAndWait();
    }

    private void mostrarAviso(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.showAndWait();
    }
}