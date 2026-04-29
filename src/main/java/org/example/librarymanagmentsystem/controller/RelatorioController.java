package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.example.librarymanagmentsystem.services.RelatorioService;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class RelatorioController implements Initializable {

    @FXML private ComboBox<String> cbTipoRelatorio;
    @FXML private DatePicker dpDataInicio, dpDataFim;
    @FXML private ComboBox<String> cbStatusEmprestimo, cbCurso;
    @FXML private Label lblFiltroDataInicio, lblFiltroDataFim, lblStatus, lblCurso;
    @FXML private Label lblTotalRegistros;
    @FXML private TextField txtBuscarTabela;
    @FXML private TableView<ObservableList<String>> tabelaResultados;
    @FXML private TableColumn<ObservableList<String>, String> col1, col2, col3, col4, col5;

    private RelatorioService relatorioService;
    private ObservableList<ObservableList<String>> data;
    private String tipoRelatorioAtual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            relatorioService = new RelatorioService();
            data = FXCollections.observableArrayList();
            configurarTabela();
            configurarListeners();
            carregarCursos();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarErro("Erro ao inicializar: " + e.getMessage());
        }
    }

    private void configurarTabela() {
        col1.setCellValueFactory(new PropertyValueFactory<>("0"));
        col2.setCellValueFactory(new PropertyValueFactory<>("1"));
        col3.setCellValueFactory(new PropertyValueFactory<>("2"));
        col4.setCellValueFactory(new PropertyValueFactory<>("3"));
        col5.setCellValueFactory(new PropertyValueFactory<>("4"));

        tabelaResultados.setItems(data);

        // Double-click na tabela para ver detalhes
        tabelaResultados.setOnMouseClicked(this::aoClicarNaTabela);
    }

    private void configurarListeners() {
        cbTipoRelatorio.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                ajustarFiltrosPorTipo(newVal);
                carregarDados();
            }
        });

        dpDataInicio.valueProperty().addListener((obs, oldVal, newVal) -> carregarDados());
        dpDataFim.valueProperty().addListener((obs, oldVal, newVal) -> carregarDados());
        cbStatusEmprestimo.valueProperty().addListener((obs, oldVal, newVal) -> carregarDados());
        cbCurso.valueProperty().addListener((obs, oldVal, newVal) -> carregarDados());
    }

    private void ajustarFiltrosPorTipo(String tipo) {
        // Esconder todos os filtros primeiro
        lblFiltroDataInicio.setVisible(false);
        lblFiltroDataFim.setVisible(false);
        lblStatus.setVisible(false);
        lblCurso.setVisible(false);
        dpDataInicio.setVisible(false);
        dpDataFim.setVisible(false);
        cbStatusEmprestimo.setVisible(false);
        cbCurso.setVisible(false);

        // Mostrar filtros conforme o tipo
        if (tipo.contains("Empréstimos")) {
            lblFiltroDataInicio.setVisible(true);
            lblFiltroDataFim.setVisible(true);
            lblStatus.setVisible(true);
            dpDataInicio.setVisible(true);
            dpDataFim.setVisible(true);
            cbStatusEmprestimo.setVisible(true);
            dpDataInicio.setValue(LocalDate.now().minusMonths(1));
            dpDataFim.setValue(LocalDate.now());
            cbStatusEmprestimo.setValue("Todos");
        } else if (tipo.contains("Estudantes")) {
            lblCurso.setVisible(true);
            cbCurso.setVisible(true);
            cbCurso.setValue("Todos");
        }
    }

    private void carregarCursos() {
        try (ResultSet rs = relatorioService.getCursos()) {
            ObservableList<String> cursos = FXCollections.observableArrayList();
            cursos.add("Todos");
            while (rs.next()) {
                cursos.add(rs.getString("curso"));
            }
            cbCurso.setItems(cursos);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void gerarRelatorio() {
        carregarDados();
    }

    private void carregarDados() {
        String tipo = cbTipoRelatorio.getValue();
        if (tipo == null) return;

        tipoRelatorioAtual = tipo;
        data.clear();

        new Thread(() -> {
            try {
                ResultSet rs = null;

                if (tipo.contains("Livros")) {
                    rs = relatorioService.getRelatorioLivros();
                    carregarDadosLivros(rs);
                } else if (tipo.contains("Estudantes")) {
                    String curso = cbCurso.getValue();
                    if (curso != null && curso.equals("Todos")) curso = null;
                    rs = relatorioService.getRelatorioEstudantes(curso);
                    carregarDadosEstudantes(rs);
                } else if (tipo.contains("Empréstimos")) {
                    rs = relatorioService.getRelatorioEmprestimos(
                            dpDataInicio.getValue(),
                            dpDataFim.getValue(),
                            cbStatusEmprestimo.getValue()
                    );
                    carregarDadosEmprestimos(rs);
                } else if (tipo.contains("Multas")) {
                    rs = relatorioService.getRelatorioMultas();
                    carregarDadosMultas(rs);
                } else if (tipo.contains("Estatísticas")) {
                    rs = relatorioService.getEstatisticas();
                    carregarDadosEstatisticas(rs);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarErro("Erro ao carregar dados: " + e.getMessage()));
            }
        }).start();
    }

    private void carregarDadosLivros(ResultSet rs) throws SQLException {
        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
        int total = 0;

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(String.valueOf(rs.getInt("id_livro")));
            row.add(rs.getString("titulo"));
            row.add(rs.getString("autor"));
            row.add(String.valueOf(rs.getInt("ano_publicacao")));
            row.add(rs.getString("status"));
            items.add(row);
            total++;
        }

        final int finalTotal = total;
        Platform.runLater(() -> {
            data.setAll(items);
            lblTotalRegistros.setText("Total: " + finalTotal + " livros");
        });
    }

    private void carregarDadosEstudantes(ResultSet rs) throws SQLException {
        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
        int total = 0;

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(String.valueOf(rs.getInt("id_estudante")));
            row.add(rs.getString("nome"));
            row.add(rs.getString("curso"));
            row.add(rs.getString("codigo_estudante"));
            row.add(rs.getString("departamento"));
            items.add(row);
            total++;
        }

        final int finalTotal = total;
        Platform.runLater(() -> {
            data.setAll(items);
            lblTotalRegistros.setText("Total: " + finalTotal + " estudantes");
        });
    }

    private void carregarDadosEmprestimos(ResultSet rs) throws SQLException {
        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
        int total = 0;

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(String.valueOf(rs.getInt("id_emprestimo")));
            row.add(rs.getString("estudante_nome"));
            row.add(rs.getString("livro_titulo"));
            row.add(rs.getDate("data_saida") != null ? rs.getDate("data_saida").toString() : "");
            String status = rs.getDate("data_devolucao_real") == null ? "Ativo" : "Finalizado";
            row.add(status);
            items.add(row);
            total++;
        }

        final int finalTotal = total;
        Platform.runLater(() -> {
            data.setAll(items);
            lblTotalRegistros.setText("Total: " + finalTotal + " empréstimos");
        });
    }

    private void carregarDadosMultas(ResultSet rs) throws SQLException {
        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();
        int total = 0;

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add(String.valueOf(rs.getInt("id_emprestimo")));
            row.add(rs.getString("estudante_nome"));
            row.add(rs.getString("livro_titulo"));
            row.add(String.format("R$ %.2f", rs.getDouble("valor_multa")));
            row.add(rs.getBoolean("pago") ? "Pago" : "Pendente");
            items.add(row);
            total++;
        }

        final int finalTotal = total;
        Platform.runLater(() -> {
            data.setAll(items);
            lblTotalRegistros.setText("Total: " + finalTotal + " multas | Valor total: R$ " + calcularTotalMultas(items));
        });
    }

    private void carregarDadosEstatisticas(ResultSet rs) throws SQLException {
        ObservableList<ObservableList<String>> items = FXCollections.observableArrayList();

        while (rs.next()) {
            ObservableList<String> row = FXCollections.observableArrayList();
            row.add("");
            row.add(rs.getString("indicador"));
            row.add(rs.getString("valor"));
            row.add("");
            row.add("");
            items.add(row);
        }

        Platform.runLater(() -> data.setAll(items));
    }

    @FXML
    private void imprimirPDF() {
        if (tipoRelatorioAtual == null || data.isEmpty()) {
            mostrarAviso("Nenhum dado para gerar PDF. Execute uma consulta primeiro.");
            return;
        }

        new Thread(() -> {
            try {
                ResultSet rs = obterResultSetAtual();
                String filtros = montarFiltrosParaPDF();
                relatorioService.gerarPDF(tipoRelatorioAtual, rs, filtros);

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Sucesso");
                    alert.setHeaderText(null);
                    alert.setContentText("PDF gerado com sucesso! O arquivo será aberto automaticamente.");
                    alert.showAndWait();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarErro("Erro ao gerar PDF: " + e.getMessage()));
            }
        }).start();
    }

    private ResultSet obterResultSetAtual() throws SQLException {
        if (tipoRelatorioAtual.contains("Livros")) {
            return relatorioService.getRelatorioLivros();
        } else if (tipoRelatorioAtual.contains("Estudantes")) {
            String curso = cbCurso.getValue();
            if (curso != null && curso.equals("Todos")) curso = null;
            return relatorioService.getRelatorioEstudantes(curso);
        } else if (tipoRelatorioAtual.contains("Empréstimos")) {
            return relatorioService.getRelatorioEmprestimos(
                    dpDataInicio.getValue(), dpDataFim.getValue(), cbStatusEmprestimo.getValue()
            );
        }
        return null;
    }

    private String montarFiltrosParaPDF() {
        StringBuilder filtros = new StringBuilder();
        if (tipoRelatorioAtual.contains("Empréstimos")) {
            filtros.append("Período: ").append(dpDataInicio.getValue()).append(" a ").append(dpDataFim.getValue());
            filtros.append(" | Status: ").append(cbStatusEmprestimo.getValue());
        } else if (tipoRelatorioAtual.contains("Estudantes")) {
            filtros.append("Curso: ").append(cbCurso.getValue());
        } else {
            filtros.append("Todos os registros");
        }
        return filtros.toString();
    }

    @FXML
    private void exportarExcel() {
        // Implementar exportação para Excel se necessário
        mostrarInfo("Funcionalidade de exportação Excel será implementada em breve.");
    }

    @FXML
    private void buscarNaTabela() {
        String busca = txtBuscarTabela.getText().toLowerCase();
        if (busca.isEmpty()) {
            carregarDados();
            return;
        }

        ObservableList<ObservableList<String>> filtrados = FXCollections.observableArrayList();
        for (ObservableList<String> row : data) {
            for (String cell : row) {
                if (cell.toLowerCase().contains(busca)) {
                    filtrados.add(row);
                    break;
                }
            }
        }

        tabelaResultados.setItems(filtrados);
        lblTotalRegistros.setText("Exibindo: " + filtrados.size() + " de " + data.size() + " registros");
    }

    private void aoClicarNaTabela(MouseEvent event) {
        if (event.getClickCount() == 2) {
            ObservableList<String> selected = tabelaResultados.getSelectionModel().getSelectedItem();
            if (selected != null) {
                abrirPopupDetalhes(selected);
            }
        }
    }

    private void abrirPopupDetalhes(ObservableList<String> dados) {
        Stage popupStage = new Stage();
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Detalhes do Registro");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: white;");

        Label titulo = new Label("📄 INFORMAÇÕES DO REGISTRO");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1b5e90;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        // Adicionar campos baseado no tipo
        if (tipoRelatorioAtual.contains("Livros")) {
            grid.add(new Label("ID:"), 0, 0);
            grid.add(new Label(dados.get(0)), 1, 0);
            grid.add(new Label("Título:"), 0, 1);
            grid.add(new Label(dados.get(1)), 1, 1);
            grid.add(new Label("Autor:"), 0, 2);
            grid.add(new Label(dados.get(2)), 1, 2);
            grid.add(new Label("Ano:"), 0, 3);
            grid.add(new Label(dados.get(3)), 1, 3);
            grid.add(new Label("Status:"), 0, 4);
            grid.add(new Label(dados.get(4)), 1, 4);
        } else if (tipoRelatorioAtual.contains("Estudantes")) {
            grid.add(new Label("ID:"), 0, 0);
            grid.add(new Label(dados.get(0)), 1, 0);
            grid.add(new Label("Nome:"), 0, 1);
            grid.add(new Label(dados.get(1)), 1, 1);
            grid.add(new Label("Curso:"), 0, 2);
            grid.add(new Label(dados.get(2)), 1, 2);
            grid.add(new Label("Código:"), 0, 3);
            grid.add(new Label(dados.get(3)), 1, 3);
            grid.add(new Label("Departamento:"), 0, 4);
            grid.add(new Label(dados.get(4)), 1, 4);
        } else if (tipoRelatorioAtual.contains("Empréstimos")) {
            grid.add(new Label("ID Empréstimo:"), 0, 0);
            grid.add(new Label(dados.get(0)), 1, 0);
            grid.add(new Label("Estudante:"), 0, 1);
            grid.add(new Label(dados.get(1)), 1, 1);
            grid.add(new Label("Livro:"), 0, 2);
            grid.add(new Label(dados.get(2)), 1, 2);
            grid.add(new Label("Data Saída:"), 0, 3);
            grid.add(new Label(dados.get(3)), 1, 3);
            grid.add(new Label("Status:"), 0, 4);
            grid.add(new Label(dados.get(4)), 1, 4);
        }

        HBox buttons = new HBox(10);
        Button btnCancelar = new Button("Cancelar");
        Button btnImprimir = new Button("Imprimir PDF");

        btnCancelar.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white; -fx-padding: 10 20;");
        btnImprimir.setStyle("-fx-background-color: #1b5e90; -fx-text-fill: white; -fx-padding: 10 20;");

        btnCancelar.setOnAction(e -> popupStage.close());
        btnImprimir.setOnAction(e -> {
            imprimirRegistroEspecifico(dados);
            popupStage.close();
        });

        buttons.getChildren().addAll(btnCancelar, btnImprimir);

        vbox.getChildren().addAll(titulo, grid, buttons);
        Scene scene = new Scene(vbox, 500, 350);
        popupStage.setScene(scene);
        popupStage.showAndWait();
    }

    private void imprimirRegistroEspecifico(ObservableList<String> dados) {
        mostrarInfo("Gerando PDF para o registro selecionado...");
        // Implementar geração de PDF para um registro específico
    }

    private String calcularTotalMultas(ObservableList<ObservableList<String>> items) {
        double total = 0;
        for (ObservableList<String> row : items) {
            String valorStr = row.get(3).replace("R$ ", "").replace(",", ".");
            try {
                total += Double.parseDouble(valorStr);
            } catch (NumberFormatException e) {}
        }
        return String.format("%.2f", total);
    }

    private void mostrarErro(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarAviso(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Aviso");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void mostrarInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informação");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}