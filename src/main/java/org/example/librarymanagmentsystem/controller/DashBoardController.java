package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.example.librarymanagmentsystem.aplication.HelloApplication;
import org.example.librarymanagmentsystem.daos.EstudanteDAO;
import org.example.librarymanagmentsystem.daos.UsuarioDAO;
import org.example.librarymanagmentsystem.entidades.*;
import org.example.librarymanagmentsystem.services.DashboardService;
import org.example.librarymanagmentsystem.controller.LivroController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;

public class DashBoardController implements Initializable {

    private Usuario usuarioLogado;
    @FXML private Button close;

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
    @FXML private TextField txtNome, txtDepartamento, txtIdade, txtCodigoEstudante, txtCartaoArduino;
    @FXML private ComboBox<String> cbCursoEstudante;
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
    @FXML private ObservableList<Emprestimo> emprestimosList;

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
        atualizarTabelaEmprestimo();
        configurarTabelaEmprestimos();
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
                    txtNome, cbCursoEstudante, txtDepartamento, txtIdade, txtCodigoEstudante, txtCartaoArduino,
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
                    txtTitulo, txtAutor, txtISBN, txtAnoPublicacao, cbCategoria,
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
            atualizarTabelaEmprestimo();
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
        if (cbCursoEstudante.getValue() == null) {
            mostrarAviso("Preencha o curso!");
            cbCursoEstudante.requestFocus();
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
        cbCursoEstudante.getSelectionModel().clearSelection();
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
                    cbCursoEstudante.getValue(),
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
            estudanteSelecionado.setCurso(cbCursoEstudante.getValue());
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

    public void atualizarTabelaEmprestimo(){
        tabelaEmprestimosAtivos.setItems(emprestimosList);
    }

    //MTODOS PARA O LOGIN
    public void setUsuarioLogado(Usuario usuario) {
        this.usuarioLogado = usuario;
        atualizarInterfaceUsuario();
    }

    private void atualizarInterfaceUsuario() {
        if (usuarioLogado != null) {
            // Atualizar o nome do usuário na tela
            Label lblNomeUsuario = (Label) dashboard_form.lookup("#lblNomeUsuario");
            if (lblNomeUsuario != null) {
                lblNomeUsuario.setText(usuarioLogado.getNome());
            }

            // Atualizar o tipo/permissão
            Label lblTipoUsuario = (Label) dashboard_form.lookup("#lblTipoUsuario");
            if (lblTipoUsuario != null) {
                lblTipoUsuario.setText(usuarioLogado.getTipo());

                // Aplicar estilo baseado no tipo
                if (usuarioLogado.isAdministrador()) {
                    lblTipoUsuario.setStyle("-fx-text-fill: #ffd700; -fx-font-weight: bold;");
                } else {
                    lblTipoUsuario.setStyle("-fx-text-fill: #90caf9;");
                }
            }

            // Carregar foto do usuário no Circle
            carregarFotoUsuario();

            // Aplicar restrições de acesso baseadas no tipo
            aplicarRestricoesAcesso();
        }
    }

    private void carregarFotoUsuario() {
        // Buscar o Circle do FXML
        Circle fotoPerfil = (Circle) dashboard_form.lookup("#fotoPerfil");

        if (fotoPerfil != null && usuarioLogado != null) {
            String caminhoFoto = usuarioLogado.getFoto();

            if (caminhoFoto != null && !caminhoFoto.isEmpty()) {
                try {
                    Image image;

                    // Verificar se o caminho é uma URL ou arquivo local
                    if (caminhoFoto.startsWith("http")) {
                        // Imagem da internet
                        image = new Image(caminhoFoto, true);
                    } else {
                        // Imagem local - tenta diferentes formas de carregar
                        image = carregarImagemLocal(caminhoFoto);
                    }

                    // Aplicar a imagem ao Circle
                    if (!image.isError()) {
                        fotoPerfil.setFill(new ImagePattern(image));
                        System.out.println("✅ Foto carregada com sucesso: " + caminhoFoto);
                    } else {
                        usarImagemPadrao(fotoPerfil);
                        System.err.println("⚠️ Erro ao carregar foto: " + caminhoFoto);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    usarImagemPadrao(fotoPerfil);
                }
            } else {
                // Usuário não tem foto, usar imagem padrão
                usarImagemPadrao(fotoPerfil);
            }
        }
    }

    private Image carregarImagemLocal(String caminhoFoto) {
        // Tenta carregar de diferentes locais

        // 1. Tenta como recurso do classpath
        java.net.URL resourceUrl = getClass().getResource(caminhoFoto);
        if (resourceUrl != null) {
            return new Image(resourceUrl.toExternalForm());
        }

        // 2. Tenta como arquivo do sistema
        java.io.File file = new java.io.File(caminhoFoto);
        if (file.exists()) {
            return new Image(file.toURI().toString());
        }

        // 3. Tenta como caminho relativo ao projeto
        String projectPath = System.getProperty("user.dir");
        java.io.File projectFile = new java.io.File(projectPath + caminhoFoto);
        if (projectFile.exists()) {
            return new Image(projectFile.toURI().toString());
        }

        // 4. Tenta como caminho da classe
        resourceUrl = getClass().getResource("/" + caminhoFoto);
        if (resourceUrl != null) {
            return new Image(resourceUrl.toExternalForm());
        }

        // Se não encontrou, retorna imagem de erro
        return new Image(getClass().getResourceAsStream("/imagens/default-user.png"));
    }

    private void usarImagemPadrao(Circle circle) {
        try {
            // Tenta carregar imagem padrão
            java.net.URL defaultImageUrl = getClass().getResource("/imagens/default-user.png");
            if (defaultImageUrl != null) {
                Image defaultImage = new Image(defaultImageUrl.toExternalForm());
                circle.setFill(new ImagePattern(defaultImage));
            } else {
                // Se não tiver imagem padrão, usa cor sólida com iniciais
                circle.setFill(javafx.scene.paint.Color.web("#1b5e90"));

                // Opcional: Adicionar texto com iniciais
                String iniciais = obterIniciais(usuarioLogado.getNome());
                Label lblIniciais = new Label(iniciais);
                lblIniciais.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
                lblIniciais.setLayoutX(circle.getLayoutX() - 10);
                lblIniciais.setLayoutY(circle.getLayoutY() - 8);

                // Adicionar ao AnchorPane (isso pode precisar de ajuste)
                AnchorPane parent = (AnchorPane) circle.getParent();
                if (parent != null && !parent.getChildren().contains(lblIniciais)) {
                    parent.getChildren().add(lblIniciais);
                }
            }
        } catch (Exception e) {
            // Fallback: cor sólida
            circle.setFill(javafx.scene.paint.Color.web("#1b5e90"));
        }
    }

    private String obterIniciais(String nome) {
        if (nome == null || nome.isEmpty()) return "?";
        String[] partes = nome.trim().split(" ");
        if (partes.length == 1) {
            return String.valueOf(partes[0].charAt(0)).toUpperCase();
        }
        return String.valueOf(partes[0].charAt(0)).toUpperCase() +
                String.valueOf(partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    private void aplicarRestricoesAcesso() {
        // Se for funcionário, desabilitar algumas funcionalidades
        if (!usuarioLogado.isAdministrador()) {
            // Desabilitar botões de deletar
            if (btnDeletar != null) btnDeletar.setDisable(true);
            if (btnDeletarLivro != null) btnDeletarLivro.setDisable(true);

            // Desabilitar botões de cadastro (apenas leitura)
            if (btnSalvar != null) btnSalvar.setDisable(true);
            if (btnSalvarLivro != null) btnSalvarLivro.setDisable(true);

            // Mostrar alerta de acesso limitado
            mostrarInfo("Acesso Limitado",
                    "Você está logado como FUNCIONÁRIO.\n" +
                            "Você pode apenas visualizar e realizar empréstimos/devoluções.\n" +
                            "Para cadastros e exclusões, acesse como Administrador.");
        }
    }

    private void mostrarInfo(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }


    @FXML
    private void alterarFotoPerfil() {
        if (!usuarioLogado.isAdministrador()) {
            mostrarInfo("Acesso negado", "Apenas administradores podem alterar a foto de perfil.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Foto de Perfil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File arquivo = fileChooser.showOpenDialog(null);
        if (arquivo != null) {
            // Copiar arquivo para a pasta do projeto
            String destino = "src/main/resources/imagens/usuarios/" +
                    usuarioLogado.getUsuario() + "_" + System.currentTimeMillis() + ".jpg";

            try {
                java.nio.file.Files.copy(arquivo.toPath(),
                        new java.io.File(destino).toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);

                // Atualizar caminho no banco
                usuarioLogado.setFoto(destino);
                new UsuarioDAO().atualizar(usuarioLogado);

                // Recarregar foto na tela
                carregarFotoUsuario();

                mostrarInfo("Foto atualizada", "Sua foto de perfil foi atualizada com sucesso!");

            } catch (Exception e) {
                e.printStackTrace();
                mostrarInfo("Erro", "Não foi possível salvar a foto: " + e.getMessage());
            }
        }
    }

    public void close(){
        Stage stage = (Stage) close.getScene().getWindow();
        stage.close();
        Platform.exit();
    }

    private double x = 0;
    private double y = 0;

    public void logout() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("/org/example/librarymanagmentsystem/view/login/login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        Stage stage = new Stage();

        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setTitle("CANDIDATURAS!");


        root.setOnMousePressed((MouseEvent event) ->{
            x = event.getSceneX();
            y = event.getSceneY();
        });

        root.setOnMouseDragged((MouseEvent event) ->{
            stage.setX(event.getScreenX() - x);
            stage.setY(event.getScreenY() - y);

            stage.setOpacity(.8);
        });

        root.setOnMouseReleased((MouseEvent event) ->{
            stage.setOpacity(1);
        });

        stage.setScene(scene);
        stage.show();


        // Fecha a janela de dashboards
        Stage loginStage = (Stage) dashboard_form.getScene().getWindow();
        loginStage.close();
    }


    @FXML
    public void configurarTabelaEmprestimos() {
        emprestimoController.configurarTabela();
    }




}