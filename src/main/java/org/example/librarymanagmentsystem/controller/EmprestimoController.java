package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.librarymanagmentsystem.daos.EmprestimoDAO;
import org.example.librarymanagmentsystem.daos.EstudanteDAO;
import org.example.librarymanagmentsystem.daos.LivroDAO;
import org.example.librarymanagmentsystem.entidades.Emprestimo;
import org.example.librarymanagmentsystem.entidades.Estudante;
import org.example.librarymanagmentsystem.entidades.Livro;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class EmprestimoController implements Initializable {

    @FXML private TextField txtRFIDLeitura, txtCodigoLivro;
    @FXML private Label lblNomeEstudante, lblCursoEstudante, lblCodigoEstudante;
    @FXML private Label lblQtdEmprestimos, lblMultaPendente;
    @FXML private Label lblTituloLivro, lblAutoresLivro, lblUnidadesDisponiveis, lblCategoriaLivro, lblDisciplinaLivro;
    @FXML private TableView<Emprestimo> tabelaEmprestimosAtivos;
    @FXML private TableColumn<Emprestimo, Integer> colEmprestimoId;
    @FXML private TableColumn<Emprestimo, String> colEmprestimoLivro, colDataSaida, colDataPrevista, colStatusEmprestimo;
    @FXML private TableColumn<Emprestimo, Double> colMultaEmprestimo;

    private EstudanteDAO estudanteDAO;
    private LivroDAO livroDAO;
    private EmprestimoDAO emprestimoDAO;
    private Estudante estudanteAtual;
    private Livro livroAtual;
    private ObservableList<Emprestimo> emprestimosList;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
                        cellData.getValue().isAtrasado() ? "⚠️ ATRASADO" : "✅ Em dia"
                )
        );

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

        colMultaEmprestimo.setCellValueFactory(new PropertyValueFactory<>("valorMulta"));
        colMultaEmprestimo.setCellFactory(column -> new TableCell<Emprestimo, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("R$ %.2f", item));
                    if (item > 0) {
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
        txtRFIDLeitura.setOnAction(event -> buscarPorRFID());
        txtCodigoLivro.setOnAction(event -> buscarLivro());
    }

    public void carregarDados() {
        limparTudo();
    }

    @FXML
    private void buscarPorRFID() {
        String rfid = txtRFIDLeitura.getText().trim();
        if (rfid.isEmpty()) {
            mostrarAviso("Digite ou aproxime o cartão RFID do leitor!");
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
                        // CORRIGIDO: usar getId() em vez de getIdEstudante()
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
                    mostrarErro("Erro na busca: " + e.getMessage());
                    limparDadosEstudante();
                });
            }
        }).start();
    }

    @FXML
    private void buscarLivro() {
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
                    if (!livros.isEmpty()) {
                        livro = livros.get(0);
                    }
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
                    mostrarErro("Erro na busca: " + e.getMessage());
                    limparDadosLivro();
                });
            }
        }).start();
    }

    @FXML
    private void realizarEmprestimo() {
        if (estudanteAtual == null) {
            mostrarAviso("⚠️ Primeiro, busque um estudante pelo cartão RFID!");
            txtRFIDLeitura.requestFocus();
            return;
        }

        if (livroAtual == null) {
            mostrarAviso("⚠️ Primeiro, busque um livro!");
            txtCodigoLivro.requestFocus();
            return;
        }

        try {
            // CORRIGIDO: usar getId() em vez de getIdEstudante()
            if (!emprestimoDAO.podePegarLivro(estudanteAtual.getId())) {
                mostrarErro("❌ Estudante já possui 3 empréstimos ativos!");
                return;
            }

            // CORRIGIDO: usar getId() em vez de getIdEstudante()
            if (emprestimoDAO.temMultaPendente(estudanteAtual.getId())) {
                mostrarErro("❌ Estudante possui multa pendente!");
                return;
            }

            if (!livroAtual.isDisponivel()) {
                mostrarErro("❌ Livro não está disponível!");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmar Empréstimo");
            confirm.setHeaderText("📚 Confirmar empréstimo?");
            confirm.setContentText(String.format(
                    "Estudante: %s\nLivro: %s\nPeríodo: 7 dias",
                    estudanteAtual.getNome(), livroAtual.getTitulo()
            ));

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                LocalDateTime dataSaida = LocalDateTime.now();
                LocalDate dataPrevista = dataSaida.plusDays(7).toLocalDate();

                Emprestimo emprestimo = new Emprestimo(estudanteAtual, livroAtual, dataSaida, dataPrevista);
                emprestimoDAO.inserir(emprestimo);

                mostrarSucesso("✅ EMPRÉSTIMO REALIZADO!\nData devolução: " + dataPrevista.format(dateFormatter));

                // CORRIGIDO: usar getId() em vez de getIdEstudante()
                carregarEmprestimosEstudante(estudanteAtual.getId());
                limparDadosLivro();
                txtCodigoLivro.clear();
                livroAtual = null;
                txtCodigoLivro.requestFocus();
            }

        } catch (SQLException e) {
            mostrarErro("Erro: " + e.getMessage());
        }
    }

    @FXML
    private void realizarDevolucao() {
        Emprestimo emprestimoSelecionado = tabelaEmprestimosAtivos.getSelectionModel().getSelectedItem();

        if (emprestimoSelecionado == null) {
            mostrarAviso("⚠️ Selecione um empréstimo na tabela!");
            return;
        }

        LocalDate dataDevolucao = LocalDate.now();
        double multa = emprestimoDAO.calcularMulta(emprestimoSelecionado, dataDevolucao);

        StringBuilder msg = new StringBuilder();
        msg.append("Confirmar devolução:\n\n");
        msg.append("📖 Livro: ").append(emprestimoSelecionado.getLivro().getTitulo()).append("\n");
        msg.append("👨‍🎓 Estudante: ").append(emprestimoSelecionado.getEstudante().getNome()).append("\n");

        if (multa > 0) {
            msg.append(String.format("\n⚠️ MULTA: R$ %.2f", multa));
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Devolução");
        confirm.setHeaderText("📚 Confirmar devolução?");
        confirm.setContentText(msg.toString());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                emprestimoDAO.registrarDevolucao(emprestimoSelecionado.getIdEmprestimo(), dataDevolucao, multa);

                if (multa > 0) {
                    mostrarAviso(String.format("💰 Multa gerada: R$ %.2f", multa));
                } else {
                    mostrarSucesso("✅ DEVOLUÇÃO REALIZADA!");
                }

                // CORRIGIDO: usar getId() em vez de getIdEstudante()
                if (estudanteAtual != null) {
                    carregarEmprestimosEstudante(estudanteAtual.getId());
                }

                if (livroAtual != null && livroAtual.getIdLivro() == emprestimoSelecionado.getLivro().getIdLivro()) {
                    limparDadosLivro();
                    txtCodigoLivro.clear();
                    livroAtual = null;
                }

            } catch (SQLException e) {
                mostrarErro("Erro: " + e.getMessage());
            }
        }
    }

    @FXML
    private void limparTela() {
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
        lblDisciplinaLivro.setText(l.getDisciplina() != null ? l.getDisciplina().getNomeDisciplina() : "N/A");

        if (l.getUnidades() > 0) {
            lblUnidadesDisponiveis.setStyle("-fx-text-fill: #4caf50; -fx-font-weight: bold;");
        } else {
            lblUnidadesDisponiveis.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
        }
    }

    // CORRIGIDO: método recebe int diretamente
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
                        lblMultaPendente.setText(temMulta ? "SIM ⚠️" : "NÃO ✅");
                        lblMultaPendente.setStyle(temMulta ? "-fx-text-fill: #f44336; -fx-font-weight: bold;" : "-fx-text-fill: #4caf50;");
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
        lblMultaPendente.setText("NÃO ✅");
        lblMultaPendente.setStyle("-fx-text-fill: #4caf50;");
        emprestimosList.clear();
        estudanteAtual = null;
    }

    private void limparDadosLivro() {
        lblTituloLivro.setText("-");
        lblAutoresLivro.setText("-");
        lblUnidadesDisponiveis.setText("0");
        lblCategoriaLivro.setText("-");
        lblDisciplinaLivro.setText("-");
        lblUnidadesDisponiveis.setStyle("");
    }

    private void limparTudo() {
        limparDadosEstudante();
        limparDadosLivro();
        estudanteAtual = null;
        livroAtual = null;
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