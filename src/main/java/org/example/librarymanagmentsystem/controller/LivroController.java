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

public class LivroController {

    private LivroDAO livroDAO;
    private DisciplinaDAO disciplinaDAO;
    private ObservableList<Livro> livrosList;
    private ObservableList<Disciplina> disciplinasList;
    private Livro livroSelecionado;

    private TextField txtTitulo, txtAutor, txtISBN, txtAnoPublicacao;
    private Spinner<Integer> spUnidades;
    private ComboBox<String> cbCategoria;
    private TextField txtBuscarLivro;
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
            ComboBox<String> cbCategoria,
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
            TextArea txtBibliografia,
            Spinner<Integer> spUnidades) {

        this.txtTitulo = txtTitulo;
        this.txtAutor = txtAutor;
        this.txtISBN = txtISBN;
        this.txtAnoPublicacao = txtAnoPublicacao;
        this.cbCategoria = cbCategoria;
        this.txtBuscarLivro = txtBuscar;
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
        this.spUnidades = spUnidades;

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

    private void preencherCampos(Livro l) {
        txtTitulo.setText(l.getTitulo());
        txtAutor.setText(l.getAutor());
        txtISBN.setText(l.getIsbn());
        txtAnoPublicacao.setText(String.valueOf(l.getAnoPublicacao()));
        cbCategoria.setValue(l.getCategoria());
        //spUnidades.getValueFactory().setValue(l.getUnidades());
        cbStatus.setValue(l.getStatus());
        cbDisciplina.setValue(l.getDisciplina());
    }

    private void configurarCombos() {
        cbStatus.setItems(FXCollections.observableArrayList("Disponível", "Emprestado", "Manutenção"));

        // Configurar o Spinner de unidades
        if (spUnidades != null) {
            SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1);
            spUnidades.setValueFactory(valueFactory);
            spUnidades.setEditable(true);  // Permitir edição manual
            System.out.println("✅ Spinner configurado com sucesso!");
        } else {
            System.err.println("⚠️ spUnidades é NULL!");
        }
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
        // Verificação segura para txtBuscarLivro
        if (txtBuscarLivro != null) {
            txtBuscarLivro.textProperty().addListener((obs, old, novo) -> {
                if (novo.isEmpty()) {
                    carregarDados();
                } else {
                    buscarLivros();
                }
            });
            System.out.println("✅ txtBuscarLivro configurado com sucesso!");
        } else {
            System.err.println("⚠️ ATENÇÃO: txtBuscarLivro é NULL! Verifique o fx:id no FXML.");
        }

        if (btnSalvar != null) {
            btnSalvar.setOnAction(e -> salvarLivro());
            System.out.println("✅ btnSalvar configurado");
        } else {
            System.err.println("⚠️ btnSalvar é NULL");
        }

        if (btnAtualizar != null) {
            btnAtualizar.setOnAction(e -> atualizarLivro());
            System.out.println("✅ btnAtualizar configurado");
        } else {
            System.err.println("⚠️ btnAtualizar é NULL");
        }

        if (btnDeletar != null) {
            btnDeletar.setOnAction(e -> deletarLivro());
            System.out.println("✅ btnDeletar configurado");
        } else {
            System.err.println("⚠️ btnDeletar é NULL");
        }

        if (btnLimpar != null) {
            btnLimpar.setOnAction(e -> limparCampos());
            System.out.println("✅ btnLimpar configurado");
        } else {
            System.err.println("⚠️ btnLimpar é NULL");
        }
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

    void salvarLivro() {
        String categoria = cbCategoria.getValue();
        if (!validarCampos()) return;

        try {
            Livro livro = new Livro();
            livro.setTitulo(txtTitulo.getText().trim());
            livro.setAutor(txtAutor.getText().trim());
            livro.setAnoPublicacao(Integer.parseInt(txtAnoPublicacao.getText()));
            livro.setIsbn(txtISBN.getText().trim());
            livro.setCategoria(categoria);
            livro.setUnidades(spUnidades.getValue());
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

    void atualizarLivro() {
        String categoria = cbCategoria.getValue();
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
            livroSelecionado.setCategoria(categoria);
            livroSelecionado.setUnidades(spUnidades.getValue());
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

    void deletarLivro() {
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

    void buscarLivros() {
        String busca = txtBuscarLivro.getText().trim();
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
        String[] autores = livro.getAutor().split(" ");
        String sobrenome = autores[autores.length - 1];

        // ========== CITAÇÃO APA (no texto) ==========
        // Formato: (Silva, 2020) ou Silva (2020) para citação narrativa
        StringBuilder citacaoParentetica = new StringBuilder();
        citacaoParentetica.append("(").append(sobrenome).append(", ").append(livro.getAnoPublicacao()).append(")");

        StringBuilder citacaoNarrativa = new StringBuilder();
        citacaoNarrativa.append(sobrenome).append(" (").append(livro.getAnoPublicacao()).append(")");

        // Mostrar ambas as formas de citação
        txtCitacao.setText("Citação parentética: " + citacaoParentetica.toString() +
                "\nCitação narrativa: " + citacaoNarrativa.toString());

        // ========== REFERÊNCIA BIBLIOGRÁFICA APA 6ª edição ==========
        // Formato completo:
        // SOBRENOME, Iniciais. (Ano). Título do livro em itálico. (Edição). Cidade: Editora.
        StringBuilder referencia = new StringBuilder();

        // 1. Autor(es) - formato APA
        if (autores.length == 1) {
            // Um autor: SILVA, J.
            referencia.append(autores[0].toUpperCase());
            if (autores[0].length() > 1) {
                // Adiciona iniciais se houver nome completo
                referencia.append(", ").append(autores[0].charAt(0)).append(".");
            }
        } else if (autores.length == 2) {
            // Dois autores: SILVA, J., & SANTOS, M.
            referencia.append(autores[1].toUpperCase()).append(", ");
            referencia.append(autores[0].charAt(0)).append("., & ");
            referencia.append(autores[1].toUpperCase()).append(", ");
            referencia.append(autores[1].charAt(0)).append(".");
        } else {
            // Três ou mais autores: SILVA, J., et al.
            referencia.append(autores[autores.length - 1].toUpperCase()).append(", ");
            referencia.append(autores[autores.length - 1].charAt(0)).append("., et al.");
        }

        referencia.append(" (").append(livro.getAnoPublicacao()).append("). ");

        // 2. Título do livro (em itálico) - só primeira letra maiúscula
        String tituloAPA = livro.getTitulo().substring(0, 1).toUpperCase() +
                livro.getTitulo().substring(1).toLowerCase();
        referencia.append("*").append(tituloAPA).append("*");

        // 3. Categoria/Edição (se houver)
        if (livro.getCategoria() != null && !livro.getCategoria().isEmpty()) {
            referencia.append(" (").append(livro.getCategoria().toLowerCase()).append(" ed.)");
        }

        referencia.append(". ");

        // 4. Disciplina (como coleção ou área)
        if (livro.getDisciplina() != null && livro.getDisciplina().getNomeDisciplina() != null) {
            referencia.append(livro.getDisciplina().getNomeDisciplina()).append(". ");
        }

        // 5. ISBN
        if (livro.getIsbn() != null && !livro.getIsbn().isEmpty()) {
            referencia.append("ISBN ").append(livro.getIsbn()).append(". ");
        }

        txtBibliografia.setText(referencia.toString());
    }



    public void limparCampos() {
        txtTitulo.clear();
        txtAutor.clear();
        txtISBN.clear();
        txtAnoPublicacao.clear();
        cbCategoria.setValue(null);
        spUnidades.getValueFactory().setValue(0);
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
        if (spUnidades.getValue()== null) {
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