package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import org.example.librarymanagmentsystem.services.DashboardService;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class DashBoardController implements Initializable {

    // ==================== COMPONENTES DO DASHBOARD ====================
    @FXML private Label lblTotalEstudantes;
    @FXML private Label lblLivrosDisponiveis;
    @FXML private Label lblEmprestimosAtivos;
    @FXML private Label lblMultasPendentes;
    @FXML private ListView<String> listaUltimosEmprestimos;
    @FXML private ListView<String> listaTopLivros;
    @FXML private BarChart<String, Number> graficoEmprestimos;

    // ==================== TELAS PRINCIPAIS (AnchorPanes) ====================
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
    @FXML private TableView<?> tabelaEstudantes;
    @FXML private TableColumn<?, ?> colId, colNome, colCurso, colDepartamento, colCodigo, colCartaoRFID;
    @FXML private Button btnSalvarEstudante, btnAtualizarEstudante, btnDeletarEstudante, btnLimparEstudante;

    // ==================== COMPONENTES DA TELA LIVROS ====================
    @FXML private TextField txtTitulo, txtAutor, txtISBN, txtAnoPublicacao, txtCategoria, txtUnidades;
    @FXML private TextField txtBuscarLivro;
    @FXML private ComboBox<?> cbDisciplina, cbStatus;
    @FXML private TableView<?> tabelaLivros;
    @FXML private TableColumn<?, ?> colLivroId, colTitulo, colAutor, colStatus, colCategoria, colDisciplina, colUnidades;
    @FXML private Button btnSalvarLivro, btnAtualizarLivro, btnDeletarLivro, btnLimparLivro;
    @FXML private TextArea txtCitacao, txtBibliografia;

    // ==================== COMPONENTES DA TELA EMPRÉSTIMOS ====================
    @FXML private TextField txtRFIDLeitura, txtCodigoLivro;
    @FXML private Label lblNomeEstudante, lblCursoEstudante, lblCodigoEstudante;
    @FXML private Label lblQtdEmprestimos, lblMultaPendente;
    @FXML private Label lblTituloLivro, lblAutoresLivro, lblUnidadesDisponiveis, lblCategoriaLivro, lblDisciplinaLivro;
    @FXML private TableView<?> tabelaEmprestimosAtivos;
    @FXML private TableColumn<?, ?> colEmprestimoId, colEmprestimoLivro, colDataSaida, colDataPrevista, colStatusEmprestimo, colMultaEmprestimo;

    // ==================== COMPONENTES DA TELA RELATÓRIOS ====================
    @FXML private ComboBox<String> cbTipoRelatorio;
    @FXML private DatePicker dpDataInicio, dpDataFim;
    @FXML private ComboBox<String> cbStatusEmprestimo, cbCurso;
    @FXML private Label lblFiltroDataInicio, lblFiltroDataFim, lblStatus, lblCurso;
    @FXML private Label lblTotalRegistros;
    @FXML private TextField txtBuscarTabela;
    @FXML private TableView<?> tabelaResultados;
    @FXML private TableColumn<?, ?> col1, col2, col3, col4, col5;

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
        try {
            dashboardService = new DashboardService();
            ultimosEmprestimosList = FXCollections.observableArrayList();
            topLivrosList = FXCollections.observableArrayList();

            // Configurar ListViews do Dashboard
            listaUltimosEmprestimos.setItems(ultimosEmprestimosList);
            listaTopLivros.setItems(topLivrosList);

            // INICIALIZAR OS CONTROLLERS SECUNDÁRIOS
            inicializarControllers();

            // Configurar tela inicial
            dashboard_form.setVisible(true);
            telaEstudante_form.setVisible(false);
            livros_form.setVisible(false);
            emprestimos_form.setVisible(false);
            relatorio_form.setVisible(false);

            // Estilo do botão Dashboard
            dasboard_btn.setStyle("-fx-background-color: #ffffff33; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5px;");

            // Carregar dados do dashboard
            carregarDadosDashboard();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao inicializar: " + e.getMessage());
        }
    }

    /**
     * Inicializa todos os controllers secundários passando os componentes FXML
     */
    private void inicializarControllers() {
        // Inicializar EstudanteController
        estudanteController = new EstudanteController();
        estudanteController.inicializar(
                txtNome, txtCurso, txtDepartamento, txtIdade, txtCodigoEstudante, txtCartaoArduino,
                txtBuscarEstudante, tabelaEstudantes, colId, colNome, colCurso, colDepartamento, colCodigo, colCartaoRFID,
                btnSalvarEstudante, btnAtualizarEstudante, btnDeletarEstudante, btnLimparEstudante
        );

        // Inicializar LivroController
        livroController = new LivroController();
        livroController.inicializar(
                txtTitulo, txtAutor, txtISBN, txtAnoPublicacao, txtCategoria, txtUnidades,
                txtBuscarLivro, cbDisciplina, cbStatus, tabelaLivros,
                colLivroId, colTitulo, colAutor, colStatus, colCategoria, colDisciplina, colUnidades,
                btnSalvarLivro, btnAtualizarLivro, btnDeletarLivro, btnLimparLivro,
                txtCitacao, txtBibliografia
        );

        // Inicializar EmprestimoController
        emprestimoController = new EmprestimoController();
        emprestimoController.inicializar(
                txtRFIDLeitura, txtCodigoLivro,
                lblNomeEstudante, lblCursoEstudante, lblCodigoEstudante,
                lblQtdEmprestimos, lblMultaPendente,
                lblTituloLivro, lblAutoresLivro, lblUnidadesDisponiveis, lblCategoriaLivro, lblDisciplinaLivro,
                tabelaEmprestimosAtivos, colEmprestimoId, colEmprestimoLivro, colDataSaida, colDataPrevista, colStatusEmprestimo, colMultaEmprestimo
        );

        // Inicializar RelatorioController
        relatorioController = new RelatorioController();
        relatorioController.inicializar(
                cbTipoRelatorio, dpDataInicio, dpDataFim, cbStatusEmprestimo, cbCurso,
                lblFiltroDataInicio, lblFiltroDataFim, lblStatus, lblCurso,
                lblTotalRegistros, txtBuscarTabela, tabelaResultados, col1, col2, col3, col4, col5
        );
    }

    // ==================== MÉTODOS DE NAVEGAÇÃO ====================

    @FXML
    public void mudarTela(MouseEvent event) {
        Label btnClicado = (Label) event.getSource();

        // Esconder todas as telas
        dashboard_form.setVisible(false);
        telaEstudante_form.setVisible(false);
        livros_form.setVisible(false);
        emprestimos_form.setVisible(false);
        relatorio_form.setVisible(false);

        // Resetar estilos dos botões
        resetarEstilosBotoes();

        // Ativar tela selecionada
        if (btnClicado == dasboard_btn) {
            dashboard_form.setVisible(true);
            aplicarEstiloAtivo(dasboard_btn);
            carregarDadosDashboard();

        } else if (btnClicado == estudantes_btn) {
            telaEstudante_form.setVisible(true);
            aplicarEstiloAtivo(estudantes_btn);
            estudanteController.carregarDados();  // ← CHAMA O MÉTODO DO CONTROLLER

        } else if (btnClicado == livros_btn) {
            livros_form.setVisible(true);
            aplicarEstiloAtivo(livros_btn);
            livroController.carregarDados();  // ← CHAMA O MÉTODO DO CONTROLLER

        } else if (btnClicado == emprestimos_btn) {
            emprestimos_form.setVisible(true);
            aplicarEstiloAtivo(emprestimos_btn);
            emprestimoController.carregarDados();  // ← CHAMA O MÉTODO DO CONTROLLER

        } else if (btnClicado == relatorio_btn) {
            relatorio_form.setVisible(true);
            aplicarEstiloAtivo(relatorio_btn);
            relatorioController.carregarDados();  // ← CHAMA O MÉTODO DO CONTROLLER
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
        carregarGraficoDashboard();
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
            try (ResultSet rs = dashboardService.getUltimosEmprestimos()) {
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
            try (ResultSet rs = dashboardService.getTopLivros()) {
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

    private void carregarGraficoDashboard() {
        // Implementar gráfico depois
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}