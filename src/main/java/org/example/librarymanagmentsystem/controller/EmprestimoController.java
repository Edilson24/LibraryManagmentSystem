package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.librarymanagmentsystem.daos.EmprestimoDAO;
import org.example.librarymanagmentsystem.daos.EstudanteDAO;
import org.example.librarymanagmentsystem.daos.LivroDAO;
import org.example.librarymanagmentsystem.entidades.Emprestimo;
import org.example.librarymanagmentsystem.entidades.Estudante;
import org.example.librarymanagmentsystem.entidades.Livro;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmprestimoController {

    private EstudanteDAO estudanteDAO;
    private LivroDAO livroDAO;
    private EmprestimoDAO emprestimoDAO;
    private Estudante estudanteAtual;
    private Livro livroAtual;
    private ObservableList<Emprestimo> emprestimosList;

    private TextField txtRFIDLeitura, txtCodigoLivro;
    private Label lblNomeEstudante, lblCursoEstudante, lblCodigoEstudante;
    private Label lblQtdEmprestimos, lblMultaPendente;
    private Label lblTituloLivro, lblAutoresLivro, lblUnidadesDisponiveis, lblCategoriaLivro, lblDisciplinaLivro;
    private TableView<Emprestimo> tabelaEmprestimosAtivos;
    private TableColumn<Emprestimo, Integer> colEmprestimoId;
    private TableColumn<Emprestimo, String> colEmprestimoLivro, colDataSaida, colDataPrevista, colStatusEmprestimo;
    private TableColumn<Emprestimo, Double> colMultaEmprestimo;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @SuppressWarnings("unchecked")
    public void inicializar(
            TextField txtRFIDLeitura,
            TextField txtCodigoLivro,
            Label lblNomeEstudante,
            Label lblCursoEstudante,
            Label lblCodigoEstudante,
            Label lblQtdEmprestimos,
            Label lblMultaPendente,
            Label lblTituloLivro,
            Label lblAutoresLivro,
            Label lblUnidadesDisponiveis,
            Label lblCategoriaLivro,
            Label lblDisciplinaLivro,
            TableView<?> tabelaEmprestimosAtivos,
            TableColumn<?, ?> colEmprestimoId,
            TableColumn<?, ?> colEmprestimoLivro,
            TableColumn<?, ?> colDataSaida,
            TableColumn<?, ?> colDataPrevista,
            TableColumn<?, ?> colStatusEmprestimo,
            TableColumn<?, ?> colMultaEmprestimo) {

        this.txtRFIDLeitura = txtRFIDLeitura;
        this.txtCodigoLivro = txtCodigoLivro;
        this.lblNomeEstudante = lblNomeEstudante;
        this.lblCursoEstudante = lblCursoEstudante;
        this.lblCodigoEstudante = lblCodigoEstudante;
        this.lblQtdEmprestimos = lblQtdEmprestimos;
        this.lblMultaPendente = lblMultaPendente;
        this.lblTituloLivro = lblTituloLivro;
        this.lblAutoresLivro = lblAutoresLivro;
        this.lblUnidadesDisponiveis = lblUnidadesDisponiveis;
        this.lblCategoriaLivro = lblCategoriaLivro;
        this.lblDisciplinaLivro = lblDisciplinaLivro;
        this.tabelaEmprestimosAtivos = (TableView<Emprestimo>) tabelaEmprestimosAtivos;
        this.colEmprestimoId = (TableColumn<Emprestimo, Integer>) colEmprestimoId;
        this.colEmprestimoLivro = (TableColumn<Emprestimo, String>) colEmprestimoLivro;
        this.colDataSaida = (TableColumn<Emprestimo, String>) colDataSaida;
        this.colDataPrevista = (TableColumn<Emprestimo, String>) colDataPrevista;
        this.colStatusEmprestimo = (TableColumn<Emprestimo, String>) colStatusEmprestimo;
        this.colMultaEmprestimo = (TableColumn<Emprestimo, Double>) colMultaEmprestimo;

        estudanteDAO = new EstudanteDAO();
        livroDAO = new LivroDAO();
        emprestimoDAO = new EmprestimoDAO();
        emprestimosList = FXCollections.observableArrayList();

        configurarTabela();
        configurarEventos();
    }

    private void configurarTabela() {
        colEmprestimoId.setCellValueFactory(new PropertyValueFactory<>("idEmprestimo"));
        colEmprestimoLivro.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getLivro().getTitulo()
                )
        );
        colDataSaida.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDataSaida().format(dateTimeFormatter)
                )
        );
        colDataPrevista.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDataPrevistaDevolucao().format(dateFormatter)
                )
        );
        colStatusEmprestimo.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().isAtrasado() ? "⚠️ ATRASADO" : "✅ EM DIA"
                )
        );
        colMultaEmprestimo.setCellValueFactory(new PropertyValueFactory<>("valorMulta"));

        // Cor para status
        colStatusEmprestimo.setCellFactory(column -> new TableCell<Emprestimo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("ATRASADO")) {
                        setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #4caf50;");
                    }
                }
            }
        });

        tabelaEmprestimosAtivos.setItems(emprestimosList);
    }

    private void configurarEventos() {
        txtRFIDLeitura.setOnAction(e -> buscarPorRFID());
        txtCodigoLivro.setOnAction(e -> buscarLivro());
    }

    public void carregarDados() {
        limparTudo();
    }

    public void buscarPorRFID() {
        String rfid = txtRFIDLeitura.getText().trim();
        if (rfid.isEmpty()) {
            mostrarAviso("Digite ou aproxime o cartão RFID!");
            return;
        }

        lblNomeEstudante.setText("Buscando...");

        new Thread(() -> {
            try {
                Estudante estudante = estudanteDAO.buscarPorCartaoArduino(rfid);
                Platform.runLater(() -> {
                    if (estudante != null) {
                        estudanteAtual = estudante;
                        mostrarDadosEstudante(estudante);
                        carregarEmprestimosEstudante(estudante.getId());
                        txtRFIDLeitura.setStyle("-fx-border-color: #4caf50;");
                    } else {
                        mostrarErro("Estudante não encontrado!");
                        limparDadosEstudante();
                        txtRFIDLeitura.setStyle("-fx-border-color: #f44336;");
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    mostrarErro("Erro: " + e.getMessage());
                    limparDadosEstudante();
                });
            }
        }).start();
    }

    public void buscarLivro() {
        String busca = txtCodigoLivro.getText().trim();
        if (busca.isEmpty()) {
            mostrarAviso("Digite o ISBN ou título do livro!");
            return;
        }

        lblTituloLivro.setText("Buscando...");

        new Thread(() -> {
            try {
                Livro livro = null;
                if (busca.matches("\\d{9,13}")) {
                    livro = livroDAO.buscarPorIsbn(busca);
                }
                if (livro == null) {
                    List<Livro> livros = livroDAO.buscarPorTitulo(busca);
                    if (!livros.isEmpty()) livro = livros.get(0);
                }

                final Livro livroFinal = livro;
                Platform.runLater(() -> {
                    if (livroFinal != null) {
                        livroAtual = livroFinal;
                        mostrarDadosLivro(livroFinal);
                        txtCodigoLivro.setStyle("-fx-border-color: #4caf50;");
                    } else {
                        mostrarErro("Livro não encontrado!");
                        limparDadosLivro();
                        txtCodigoLivro.setStyle("-fx-border-color: #f44336;");
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    mostrarErro("Erro: " + e.getMessage());
                    limparDadosLivro();
                });
            }
        }).start();
    }

    public void realizarEmprestimo() {
        if (estudanteAtual == null) {
            mostrarAviso("Busque um estudante primeiro!");
            return;
        }
        if (livroAtual == null) {
            mostrarAviso("Busque um livro primeiro!");
            return;
        }

        try {
            if (!emprestimoDAO.podePegarLivro(estudanteAtual.getId())) {
                mostrarErro("Estudante já tem 3 empréstimos ativos!");
                return;
            }
            if (emprestimoDAO.temMultaPendente(estudanteAtual.getId())) {
                mostrarErro("Estudante tem multa pendente!");
                return;
            }
            if (!livroAtual.isDisponivel()) {
                mostrarErro("Livro não disponível!");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Empréstimo");
            confirm.setHeaderText("Confirmar empréstimo?");
            confirm.setContentText(String.format("Estudante: %s\nLivro: %s\nPeríodo: 7 dias",
                    estudanteAtual.getNome(), livroAtual.getTitulo()));

            if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                LocalDateTime dataSaida = LocalDateTime.now();
                LocalDate dataPrevista = dataSaida.plusDays(7).toLocalDate();

                Emprestimo emprestimo = new Emprestimo(estudanteAtual, livroAtual, dataSaida, dataPrevista);
                emprestimoDAO.inserir(emprestimo);

                mostrarSucesso("Empréstimo realizado!\nDevolução: " + dataPrevista.format(dateFormatter));
                carregarEmprestimosEstudante(estudanteAtual.getId());
                limparDadosLivro();
                txtCodigoLivro.clear();
                livroAtual = null;
            }
        } catch (SQLException e) {
            mostrarErro("Erro: " + e.getMessage());
        }
    }

    public void realizarDevolucao() {
        Emprestimo selecionado = tabelaEmprestimosAtivos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            mostrarAviso("Selecione um empréstimo!");
            return;
        }

        LocalDate dataDevolucao = LocalDate.now();
        double multa = emprestimoDAO.calcularMulta(selecionado, dataDevolucao);

        String msg = "Devolver \"" + selecionado.getLivro().getTitulo() + "\"?";
        if (multa > 0) msg += "\nMulta: R$ " + String.format("%.2f", multa);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                emprestimoDAO.registrarDevolucao(selecionado.getIdEmprestimo(), dataDevolucao, multa);
                mostrarSucesso("Devolução realizada!");
                if (estudanteAtual != null) {
                    carregarEmprestimosEstudante(estudanteAtual.getId());
                }
            } catch (SQLException e) {
                mostrarErro("Erro: " + e.getMessage());
            }
        }
    }

    public void limparTela() {
        limparTudo();
        txtRFIDLeitura.clear();
        txtCodigoLivro.clear();
        txtRFIDLeitura.setStyle("");
        txtCodigoLivro.setStyle("");
        txtRFIDLeitura.requestFocus();
    }

    private void mostrarDadosEstudante(Estudante e) {
        lblNomeEstudante.setText(e.getNome());
        lblCursoEstudante.setText(e.getCurso());
        lblCodigoEstudante.setText(e.getCodigoEstudante());
    }

    private void mostrarDadosLivro(Livro l) {
        lblTituloLivro.setText(l.getTitulo());
        lblAutoresLivro.setText(l.getAutor());
        lblUnidadesDisponiveis.setText(String.valueOf(l.getUnidades()));
        lblCategoriaLivro.setText(l.getCategoria());
        lblDisciplinaLivro.setText(l.getDisciplina() != null ? l.getDisciplina().getNomeDisciplina() : "-");
    }

    private void carregarEmprestimosEstudante(int idEstudante) {
        new Thread(() -> {
            try {
                List<Emprestimo> emprestimos = emprestimoDAO.listarAtivosPorEstudante(idEstudante);
                Platform.runLater(() -> {
                    emprestimosList.clear();
                    emprestimosList.addAll(emprestimos);
                    lblQtdEmprestimos.setText(String.valueOf(emprestimos.size()));

                    try {
                        boolean temMulta = emprestimoDAO.temMultaPendente(idEstudante);
                        lblMultaPendente.setText(temMulta ? "SIM" : "NÃO");
                        lblMultaPendente.setStyle(temMulta ? "-fx-text-fill: #f44336;" : "-fx-text-fill: #4caf50;");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void limparDadosEstudante() {
        lblNomeEstudante.setText("-");
        lblCursoEstudante.setText("-");
        lblCodigoEstudante.setText("-");
        lblQtdEmprestimos.setText("0");
        lblMultaPendente.setText("NÃO");
        lblMultaPendente.setStyle("");
        emprestimosList.clear();
        estudanteAtual = null;
    }

    private void limparDadosLivro() {
        lblTituloLivro.setText("-");
        lblAutoresLivro.setText("-");
        lblUnidadesDisponiveis.setText("0");
        lblCategoriaLivro.setText("-");
        lblDisciplinaLivro.setText("-");
        livroAtual = null;
    }

    private void limparTudo() {
        limparDadosEstudante();
        limparDadosLivro();
    }

    private void mostrarSucesso(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.showAndWait();
    }

    private void mostrarAviso(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}