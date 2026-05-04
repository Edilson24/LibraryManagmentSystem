package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.example.librarymanagmentsystem.daos.EstudanteDAO;
import org.example.librarymanagmentsystem.entidades.*;
import org.example.librarymanagmentsystem.services.DashboardService;
import org.example.librarymanagmentsystem.controller.LivroController;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class DashBoardController implements Initializable {

    private EstudanteDAO estudanteDAO;
    private Estudante estudanteSelecionado;
    private ObservableList<Estudante> estudantesList;

    // ==================== COMPONENTES DO DASHBOARD ====================
    @FXML private Label lblTotalEstudantes;
    @FXML private Label lblLivrosDisponiveis;
    @FXML private Label lblEmprestimosAtivos;
    @FXML private Label lblMultasPendentes;
    @FXML private ListView<String> listaUltimosEmprestimos;
    @FXML private ListView<String> listaTopLivros;

    // ==================== TELAS PRINCIPAIS ====================
    @FXML private AnchorPane dashboard_form;
    @FXML private AnchorPane telaEstudante_form;
    @FXML private AnchorPane livros_form;
    @FXML private AnchorPane emprestimos_form;
    @FXML private AnchorPane relatorio_form;

    // ==================== BOTÕES DO MENU ====================
    @FXML private Label dasboard_btn;
    @FXML private Label estudantes_btn;
    @FXML private Label livros_btn;
    @FXML private Label emprestimos_btn;
    @FXML private Label relatorio_btn;

    // ==================== COMPONENTES DA TELA ESTUDANTES ====================
    @FXML private TextField txtNome, txtCurso, txtDepartamento, txtIdade, txtCodigoEstudante, txtCartaoArduino;
    @FXML private TextField txtBuscarEstudante;
    @FXML private TableView<Estudante> tabelaEstudantes;
    @FXML private TableColumn<Estudante, Integer> colId;
    @FXML private TableColumn<Estudante, String> colNome, colCurso, colDepartamento, colCodigo, colCartaoRFID;
    @FXML private Button btnSalvar, btnAtualizar, btnDeletar, btnLimpar;

    // ==================== COMPONENTES DA TELA LIVROS ====================
    @FXML private TextField txtTitulo, txtAutor, txtISBN, txtAnoPublicacao, txtCategoria, txtUnidades;
    @FXML private TextField txtBuscarLivro;
    @FXML private ComboBox<String> cbCategoria;
    @FXML private ComboBox<Disciplina> cbDisciplina;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Spinner<Integer> spUnidades;
    @FXML private TableView<Livro> tabelaLivros;
    @FXML private TableColumn<Livro, Integer> colLivroId, colUnidades;
    @FXML private TableColumn<Livro, String> colTitulo, colAutor, colStatus, colCategoria, colDisciplina;
    @FXML private Button btnSalvarLivro, btnAtualizarLivro, btnDeletarLivro, btnLimparLivro;
    @FXML private TextArea txtCitacao, txtBibliografia;

    // ==================== COMPONENTES DA TELA EMPRÉSTIMOS ====================
    @FXML private TextField txtRFIDLeitura, txtCodigoLivro;
    @FXML private Label lblNomeEstudante, lblCursoEstudante, lblCodigoEstudante;
    @FXML private Label lblQtdEmprestimos, lblMultaPendente;
    @FXML private Label lblTituloLivro, lblAutoresLivro, lblUnidadesDisponiveis, lblCategoriaLivro, lblDisciplinaLivro;
    @FXML private TableView<Emprestimo> tabelaEmprestimosAtivos;
    @FXML private TableColumn<Emprestimo, Integer> colEmprestimoId;
    @FXML private TableColumn<Emprestimo, String> colEmprestimoLivro, colDataSaida, colDataPrevista, colStatusEmprestimo;
    @FXML private TableColumn<Emprestimo, Double> colMultaEmprestimo;

    // ==================== COMPONENTES DA TELA RELATÓRIOS ====================
    @FXML private ComboBox<String> cbTipoRelatorio;
    @FXML private DatePicker dpDataInicio, dpDataFim;
    @FXML private ComboBox<String> cbStatusEmprestimo, cbCurso;
    @FXML private Label lblFiltroDataInicio, lblFiltroDataFim, lblStatus, lblCurso;
    @FXML private Label lblTotalRegistros;
    @FXML private TextField txtBuscarTabela;
    @FXML private TableView<ObservableList<String>> tabelaResultados;
    @FXML private TableColumn<ObservableList<String>, String> col1, col2, col3, col4, col5;

    // ==================== CONTROLLERS SECUNDÁRIOS ====================
    private EstudanteController estudanteController;
    private LivroController livroController;
    private EmprestimoController emprestimoController;
    private RelatorioController relatorioController;

    private DashboardService dashboardService;
    private ObservableList<String> ultimosEmprestimosList;
    private ObservableList<String> topLivrosList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dashboardService = new DashboardService();
        ultimosEmprestimosList = FXCollections.observableArrayList();
        topLivrosList = FXCollections.observableArrayList();

        listaUltimosEmprestimos.setItems(ultimosEmprestimosList);
        listaTopLivros.setItems(topLivrosList);

        inicializarControllers();
        configurarSpinner();

        dashboard_form.setVisible(true);
        telaEstudante_form.setVisible(false);
        livros_form.setVisible(false);
        emprestimos_form.setVisible(false);
        relatorio_form.setVisible(false);

        dasboard_btn.setStyle("-fx-background-color: #ffffff33; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5px;");

        carregarDadosDashboard();
    }

    private void configurarSpinner() {
        spUnidades = new Spinner<>(1, 999, 1);
    }

    private void inicializarControllers() {
        // Instanciar controllers PRIMEIRO
        estudanteController = new EstudanteController();
        livroController = new LivroController();
        emprestimoController = new EmprestimoController();
        relatorioController = new RelatorioController();

        // Depois inicializar cada um
        try {
            estudanteController.inicializar(
                    txtNome, txtCurso, txtDepartamento, txtIdade, txtCodigoEstudante, txtCartaoArduino,
                    txtBuscarEstudante, tabelaEstudantes, colId, colNome, colCurso, colDepartamento, colCodigo, colCartaoRFID,
                    btnSalvar, btnAtualizar, btnDeletar, btnLimpar
            );
            System.out.println("EstudanteController inicializado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar EstudanteController: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            livroController.inicializar(
                    txtTitulo, txtAutor, txtISBN, txtAnoPublicacao, txtCategoria, txtUnidades,
                    txtBuscarLivro, cbDisciplina, cbStatus, tabelaLivros,
                    colLivroId, colTitulo, colAutor, colStatus, colCategoria, colDisciplina, colUnidades,
                    btnSalvarLivro, btnAtualizarLivro, btnDeletarLivro, btnLimparLivro,
                    txtCitacao, txtBibliografia, spUnidades
            );
            System.out.println("LivroController inicializado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar LivroController: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            emprestimoController.inicializar(
                    txtRFIDLeitura, txtCodigoLivro,
                    lblNomeEstudante, lblCursoEstudante, lblCodigoEstudante,
                    lblQtdEmprestimos, lblMultaPendente,
                    lblTituloLivro, lblAutoresLivro, lblUnidadesDisponiveis, lblCategoriaLivro, lblDisciplinaLivro,
                    tabelaEmprestimosAtivos, colEmprestimoId, colEmprestimoLivro, colDataSaida, colDataPrevista, colStatusEmprestimo, colMultaEmprestimo
            );
            System.out.println("EmprestimoController inicializado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar EmprestimoController: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            relatorioController.inicializar(
                    cbTipoRelatorio, dpDataInicio, dpDataFim, cbStatusEmprestimo, cbCurso,
                    lblFiltroDataInicio, lblFiltroDataFim, lblStatus, lblCurso,
                    lblTotalRegistros, txtBuscarTabela, tabelaResultados, col1, col2, col3, col4, col5
            );
            System.out.println("RelatorioController inicializado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao inicializar RelatorioController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== MÉTODOS DE NAVEGAÇÃO ====================

    @FXML
    public void mudarTela(MouseEvent event) {
        Label btnClicado = (Label) event.getSource();

        dashboard_form.setVisible(false);
        telaEstudante_form.setVisible(false);
        livros_form.setVisible(false);
        emprestimos_form.setVisible(false);
        relatorio_form.setVisible(false);

        resetarEstilosBotoes();

        if (btnClicado == dasboard_btn) {
            dashboard_form.setVisible(true);
            aplicarEstiloAtivo(dasboard_btn);
            carregarDadosDashboard();
        } else if (btnClicado == estudantes_btn) {
            telaEstudante_form.setVisible(true);
            aplicarEstiloAtivo(estudantes_btn);
            estudanteController.carregarDados();
        } else if (btnClicado == livros_btn) {
            livros_form.setVisible(true);
            aplicarEstiloAtivo(livros_btn);
            livroController.carregarDados();
        } else if (btnClicado == emprestimos_btn) {
            emprestimos_form.setVisible(true);
            aplicarEstiloAtivo(emprestimos_btn);
            emprestimoController.carregarDados();
        } else if (btnClicado == relatorio_btn) {
            relatorio_form.setVisible(true);
            aplicarEstiloAtivo(relatorio_btn);
            relatorioController.carregarDados();
        }
    }

    private void resetarEstilosBotoes() {
        dasboard_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
        estudantes_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
        livros_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
        emprestimos_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
        relatorio_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
    }

    private void aplicarEstiloAtivo(Label botao) {
        botao.setStyle("-fx-background-color: #ffffff33; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5px;");
    }

    // ==================== MÉTODOS DO DASHBOARD ====================

    private void carregarDadosDashboard() {
        carregarCardsDashboard();
        carregarUltimosEmprestimos();
        carregarTopLivros();
    }

    private void carregarCardsDashboard() {
        new Thread(() -> {
            try {
                int totalEstudantes = dashboardService.getTotalEstudantes();
                int livrosDisponiveis = dashboardService.getLivrosDisponiveis();
                int emprestimosAtivos = dashboardService.getEmprestimosAtivos();
                double multasPendentes = dashboardService.getMultasPendentes();

                Platform.runLater(() -> {
                    lblTotalEstudantes.setText(String.valueOf(totalEstudantes));
                    lblLivrosDisponiveis.setText(String.valueOf(livrosDisponiveis));
                    lblEmprestimosAtivos.setText(String.valueOf(emprestimosAtivos));
                    lblMultasPendentes.setText(String.format("R$ %.2f", multasPendentes));
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void carregarUltimosEmprestimos() {
        new Thread(() -> {
            try (var rs = dashboardService.getUltimosEmprestimos()) {
                ObservableList<String> items = FXCollections.observableArrayList();
                while (rs.next()) {
                    String item = String.format("#%d | %s | %s",
                            rs.getInt("id_emprestimo"),
                            rs.getString("estudante_nome"),
                            rs.getString("livro_titulo"));
                    items.add(item);
                }
                Platform.runLater(() -> {
                    ultimosEmprestimosList.clear();
                    ultimosEmprestimosList.addAll(items);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void carregarTopLivros() {
        new Thread(() -> {
            try (var rs = dashboardService.getTopLivros()) {
                ObservableList<String> items = FXCollections.observableArrayList();
                int rank = 1;
                while (rs.next()) {
                    String item = String.format("%dº - %s (%d empréstimos)",
                            rank++, rs.getString("titulo"), rs.getInt("total_emprestimos"));
                    items.add(item);
                }
                Platform.runLater(() -> {
                    topLivrosList.clear();
                    topLivrosList.addAll(items);
                });
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ==================== MÉTODOS REDIRECIONADORES ESTUDANTES ====================

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

    @FXML
    public void salvarEstudante() {
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

    @FXML
    public void atualizarEstudante() {
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

    @FXML
    public void deletarEstudante() {
        estudanteController.deletarEstudante();
    }

    @FXML
    public void limparCamposEstudante() {
        estudanteController.limparCampos();
    }

    @FXML
    public void buscarEstudantes() {
        estudanteController.buscarEstudantes();
    }

    // ==================== MÉTODOS REDIRECIONADORES LIVROS ====================

    @FXML
    public void salvarLivro() {
        livroController.salvarLivro();
    }

    @FXML
    public void atualizarLivro() {
        livroController.atualizarLivro();
    }

    @FXML
    public void deletarLivro() {
        livroController.deletarLivro();
    }

    @FXML
    public void limparCamposLivro() {
        livroController.limparCampos();
    }

    @FXML
    public void buscarLivros() {
        livroController.buscarLivros();
    }

    // ==================== MÉTODOS REDIRECIONADORES EMPRÉSTIMOS ====================

    @FXML
    public void buscarPorRFID() {
        emprestimoController.buscarPorRFID();
    }

    @FXML
    public void buscarLivro() {
        emprestimoController.buscarLivro();
    }

    @FXML
    public void realizarEmprestimo() {
        emprestimoController.realizarEmprestimo();
    }

    @FXML
    public void realizarDevolucao() {
        emprestimoController.realizarDevolucao();
    }

    @FXML
    public void limparTelaEmprestimo() {
        emprestimoController.limparTela();
    }

    // ==================== MÉTODOS REDIRECIONADORES RELATÓRIOS ====================

    @FXML
    public void gerarRelatorio() {
        relatorioController.gerarRelatorio();
    }

    @FXML
    public void imprimirPDF() {
        relatorioController.imprimirPDF();
    }

    @FXML
    public void exportarExcel() {
        relatorioController.exportarExcel();
    }

    @FXML
    public void buscarNaTabela() {
        relatorioController.filtrarTabela();
    }
}