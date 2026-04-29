package org.example.librarymanagmentsystem.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.example.librarymanagmentsystem.services.DashboardService;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public class DashBoardController implements Initializable {

    @FXML private Label lblTotalEstudantes;
    @FXML private Label lblLivrosDisponiveis;
    @FXML private Label lblEmprestimosAtivos;
    @FXML private Label lblMultasPendentes;
    @FXML private ListView<String> listaUltimosEmprestimos;
    @FXML private ListView<String> listaTopLivros;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;


    private DashboardService dashboardService;
    private ObservableList<String> ultimosEmprestimosList;
    private ObservableList<String> topLivrosList;

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
        dashboardService = new DashboardService();
        ultimosEmprestimosList = FXCollections.observableArrayList();
        topLivrosList = FXCollections.observableArrayList();

        // Configurar ListViews
        listaUltimosEmprestimos.setItems(ultimosEmprestimosList);
        listaTopLivros.setItems(topLivrosList);

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

        // Carregar todos os dados
        carregarDadosDashboard();
    }

    private void carregarDadosDashboard() {
        carregarCardsDashboard();
        carregarUltimosEmprestimos();
        carregarTopLivros();

    }

    private void carregarTopLivros() {
        try (ResultSet rs = dashboardService.getTopLivros()) {
            topLivrosList.clear();
            int rank = 1;

            while (rs.next()) {
                String titulo = rs.getString("titulo");
                int total = rs.getInt("total_emprestimos");

                String medalha = rank == 1 ? "🥇" : (rank == 2 ? "🥈" : (rank == 3 ? "🥉" : "📚"));
                String item = String.format("%s %dº | %s | %d empréstimos", medalha, rank, titulo, total);

                topLivrosList.add(item);
                rank++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            topLivrosList.add("Erro ao carregar top livros");
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



    public void atualizarDashboard() {
        carregarDadosDashboard();
    }

    private void carregarCardsDashboard() {
        // Verificação de segurança
        if (dashboardService == null) {
            System.err.println("DashboardService não foi inicializado!");
            dashboardService = new DashboardService();
        }

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
            mostrarErro("Erro ao carregar cards: " + e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    // Método para verificar alertas de livros com poucas unidades
    private void verificarAlertas() {
        try (ResultSet rs = dashboardService.getLivrosPoucasUnidades(3)) {
            StringBuilder alerta = new StringBuilder("⚠️ Livros com poucas unidades:\n");
            boolean temAlerta = false;

            while (rs.next()) {
                String titulo = rs.getString("titulo");
                int unidades = rs.getInt("unidades");
                alerta.append(String.format("• %s: %d unidade(s)\n", titulo, unidades));
                temAlerta = true;
            }

            if (temAlerta) {
                mostrarAlerta(alerta.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método auxiliar para mostrar alerta
    private void mostrarAlerta(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Atenção");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void carregarUltimosEmprestimos() {
        try (ResultSet rs = dashboardService.getUltimosEmprestimos()) {
            ultimosEmprestimosList.clear();

            while (rs.next()) {
                int id = rs.getInt("id_emprestimo");
                String estudante = rs.getString("estudante_nome");
                String livro = rs.getString("livro_titulo");
                Date dataSaida = rs.getDate("data_saida");
                Date dataPrevista = rs.getDate("data_prevista_devolucao");
                Date dataDevolucao = rs.getDate("data_devolucao_real");

                String status = dataDevolucao == null ? "🔴 Em andamento" : "✅ Finalizado";
                String item = String.format("#%d | %s | %s | %s | %s",
                        id, estudante, livro, dataSaida.toString(), status);

                ultimosEmprestimosList.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            ultimosEmprestimosList.add("Erro ao carregar empréstimos");
        }
    }

    // Método público para atualizar apenas os cards
    public void atualizarCards() {
        carregarCardsDashboard();
    }

    // No método initialize, adicionar um timer para atualizar a cada 30 segundos
    private void iniciarAtualizacaoAutomatica() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            carregarDadosDashboard();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void realizarEmprestimo(ActionEvent actionEvent) {
    }

    public void realizarDevolucao(ActionEvent actionEvent) {
    }

    public void buscarPorRFID(ActionEvent actionEvent) {
    }

    public void buscarLivro(ActionEvent actionEvent) {
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

        try {
            // Inicializar serviços e listas PRIMEIRO
            dashboardService = new DashboardService();
            ultimosEmprestimosList = FXCollections.observableArrayList();
            topLivrosList = FXCollections.observableArrayList();

            // Configurar ListViews
            listaUltimosEmprestimos.setItems(ultimosEmprestimosList);
            listaTopLivros.setItems(topLivrosList);

            // Configurar telas
            dashboard_form.setVisible(true);
            telaEstudante_form.setVisible(false);
            livros_form.setVisible(false);
            relatorio_form.setVisible(false);
            if (emprestimos_form != null) {
                emprestimos_form.setVisible(false);
            }

            // Estilo do botão Dashboard
            dasboard_btn.setStyle("-fx-background-color: #ffffff33; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10px; -fx-background-radius: 5px;");

            // Resetar estilo dos outros botões
            estudantes_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
            livros_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
            relatorio_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
            if (emprestimos_btn != null) {
                emprestimos_btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 10px;");
            }

            // Carregar dados do dashboard
            carregarDadosDashboard();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao inicializar dashboard: " + e.getMessage());
        }
    }

    public void exportarExcel(ActionEvent actionEvent) {
    }

    public void imprimirPDF(ActionEvent actionEvent) {
    }

    public void gerarRelatorio(ActionEvent actionEvent) {
    }

    public void buscarNaTabela(KeyEvent keyEvent) {
    }
}
