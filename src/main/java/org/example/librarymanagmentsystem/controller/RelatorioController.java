package org.example.librarymanagmentsystem.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import org.example.librarymanagmentsystem.services.RelatorioService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class RelatorioController {

    private RelatorioService relatorioService;
    private ObservableList<ObservableList<String>> dadosRelatorio;

    private ComboBox<String> cbTipoRelatorio;
    private DatePicker dpDataInicio, dpDataFim;
    private ComboBox<String> cbStatusEmprestimo, cbCurso;
    private Label lblFiltroDataInicio, lblFiltroDataFim, lblStatus, lblCurso;
    private Label lblTotalRegistros;
    private TextField txtBuscarTabela;
    private TableView<ObservableList<String>> tabelaResultados;
    private TableColumn<ObservableList<String>, String> col1, col2, col3, col4, col5;

    @SuppressWarnings("unchecked")
    public void inicializar(
            ComboBox<String> cbTipoRelatorio,
            DatePicker dpDataInicio,
            DatePicker dpDataFim,
            ComboBox<String> cbStatusEmprestimo,
            ComboBox<String> cbCurso,
            Label lblFiltroDataInicio,
            Label lblFiltroDataFim,
            Label lblStatus,
            Label lblCurso,
            Label lblTotalRegistros,
            TextField txtBuscarTabela,
            TableView<?> tabelaResultados,
            TableColumn<?, ?> col1,
            TableColumn<?, ?> col2,
            TableColumn<?, ?> col3,
            TableColumn<?, ?> col4,
            TableColumn<?, ?> col5) {

        this.cbTipoRelatorio = cbTipoRelatorio;
        this.dpDataInicio = dpDataInicio;
        this.dpDataFim = dpDataFim;
        this.cbStatusEmprestimo = cbStatusEmprestimo;
        this.cbCurso = cbCurso;
        this.lblFiltroDataInicio = lblFiltroDataInicio;
        this.lblFiltroDataFim = lblFiltroDataFim;
        this.lblStatus = lblStatus;
        this.lblCurso = lblCurso;
        this.lblTotalRegistros = lblTotalRegistros;
        this.txtBuscarTabela = txtBuscarTabela;
        this.tabelaResultados = (TableView<ObservableList<String>>) tabelaResultados;
        this.col1 = (TableColumn<ObservableList<String>, String>) col1;
        this.col2 = (TableColumn<ObservableList<String>, String>) col2;
        this.col3 = (TableColumn<ObservableList<String>, String>) col3;
        this.col4 = (TableColumn<ObservableList<String>, String>) col4;
        this.col5 = (TableColumn<ObservableList<String>, String>) col5;

        try {
            relatorioService = new RelatorioService();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        dadosRelatorio = FXCollections.observableArrayList();

        configurarTabela();
        configurarEventos();
        carregarDados();
    }

    private void configurarTabela() {
        /*col1.setCellValueFactory(cellData -> cellData.getValue()[0]);
        col2.setCellValueFactory(cellData -> cellData.getValue()[1]);
        col3.setCellValueFactory(cellData -> cellData.getValue()[2]);
        col4.setCellValueFactory(cellData -> cellData.getValue()[3]);
        col5.setCellValueFactory(cellData -> cellData.getValue()[4]);

        tabelaResultados.setItems(dadosRelatorio);*/
    }

    private void configurarEventos() {
        // Verificações de null para evitar erro
        if (cbTipoRelatorio != null) {
            cbTipoRelatorio.setOnAction(e -> carregarRelatorio());
            System.out.println("✅ cbTipoRelatorio configurado");
        } else {
            System.err.println("⚠️ ATENÇÃO: cbTipoRelatorio é NULL! Verifique o fx:id no FXML.");
        }

        if (dpDataInicio != null) {
            dpDataInicio.setOnAction(e -> carregarRelatorio());
            System.out.println("✅ dpDataInicio configurado");
        } else {
            System.err.println("⚠️ dpDataInicio é NULL");
        }

        if (dpDataFim != null) {
            dpDataFim.setOnAction(e -> carregarRelatorio());
            System.out.println("✅ dpDataFim configurado");
        } else {
            System.err.println("⚠️ dpDataFim é NULL");
        }

        if (cbStatusEmprestimo != null) {
            cbStatusEmprestimo.setOnAction(e -> carregarRelatorio());
            System.out.println("✅ cbStatusEmprestimo configurado");
        } else {
            System.err.println("⚠️ ATENÇÃO: cbStatusEmprestimo é NULL! Verifique o fx:id no FXML.");
        }

        if (cbCurso != null) {
            cbCurso.setOnAction(e -> carregarRelatorio());
            System.out.println("✅ cbCurso configurado");
        } else {
            System.err.println("⚠️ cbCurso é NULL");
        }

        if (txtBuscarTabela != null) {
            txtBuscarTabela.textProperty().addListener((obs, old, novo) -> filtrarTabela());
            System.out.println("✅ txtBuscarTabela configurado");
        } else {
            System.err.println("⚠️ txtBuscarTabela é NULL");
        }
    }

    public void carregarDados() {
        if (cbTipoRelatorio != null) {
            cbTipoRelatorio.setItems(FXCollections.observableArrayList(
                    "📚 Livros", "👨‍🎓 Estudantes", "🔄 Empréstimos", "💰 Multas", "📈 Estatísticas"
            ));
            cbTipoRelatorio.setValue("📚 Livros");
        } else {
            System.err.println("⚠️ cbTipoRelatorio é NULL");
        }

        if (cbStatusEmprestimo != null) {
            cbStatusEmprestimo.setItems(FXCollections.observableArrayList("Todos", "Ativos", "Finalizados"));
            System.out.println("✅ cbStatusEmprestimo configurado");
        } else {
            System.err.println("⚠️ ATENÇÃO: cbStatusEmprestimo é NULL! Verifique o fx:id no FXML.");
        }

        if (cbCurso != null) {
            cbCurso.setItems(FXCollections.observableArrayList("Todos", "Gestão", "Informática", "Administração", "Contabilidade", "Medicina", "Direito", "Arquitetura"));
            cbCurso.setValue("Todos");
            System.out.println("✅ cbCurso configurado");
        } else {
            System.err.println("⚠️ ATENÇÃO: cbCurso é NULL! Verifique o fx:id no FXML.");
        }



        // Esconder filtros inicialmente
        if (lblFiltroDataInicio != null) lblFiltroDataInicio.setVisible(false);
        if (lblFiltroDataFim != null) lblFiltroDataFim.setVisible(false);
        if (lblStatus != null) lblStatus.setVisible(false);
        if (lblCurso != null) lblCurso.setVisible(false);
        if (dpDataInicio != null) dpDataInicio.setVisible(false);
        if (dpDataFim != null) dpDataFim.setVisible(false);
        if (cbStatusEmprestimo != null) cbStatusEmprestimo.setVisible(false);
        if (cbCurso != null) cbCurso.setVisible(false);
    }

    private void carregarRelatorio() {
        String tipo = cbTipoRelatorio.getValue();
        if (tipo == null) return;

        // Ajustar visibilidade dos filtros
        ajustarFiltros(tipo);

        dadosRelatorio.clear();

        new Thread(() -> {
            try {
                if (tipo.contains("Livros")) {
                    carregarLivros();
                } else if (tipo.contains("Estudantes")) {
                    carregarEstudantes();
                } else if (tipo.contains("Empréstimos")) {
                    carregarEmprestimos();
                } else if (tipo.contains("Multas")) {
                    carregarMultas();
                } else if (tipo.contains("Estatísticas")) {
                    carregarEstatisticas();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> mostrarErro("Erro ao carregar: " + e.getMessage()));
            }
        }).start();
    }

    private void ajustarFiltros(String tipo) {
        boolean isEmprestimo = tipo.contains("Empréstimos");
        boolean isEstudante = tipo.contains("Estudantes");

        lblFiltroDataInicio.setVisible(isEmprestimo);
        lblFiltroDataFim.setVisible(isEmprestimo);
        lblStatus.setVisible(isEmprestimo);
        dpDataInicio.setVisible(isEmprestimo);
        dpDataFim.setVisible(isEmprestimo);
        cbStatusEmprestimo.setVisible(isEmprestimo);

        lblCurso.setVisible(isEstudante);
        cbCurso.setVisible(isEstudante);

        if (isEmprestimo) {
            if (dpDataInicio.getValue() == null) dpDataInicio.setValue(java.time.LocalDate.now().minusMonths(1));
            if (dpDataFim.getValue() == null) dpDataFim.setValue(java.time.LocalDate.now());
            if (cbStatusEmprestimo.getValue() == null) cbStatusEmprestimo.setValue("Todos");
        }

        if (isEstudante && cbCurso.getValue() == null) {
            cbCurso.setItems(FXCollections.observableArrayList("Todos", "Computação", "Engenharia", "Administração", "Direito"));
            cbCurso.setValue("Todos");
        }
    }

    private void carregarLivros() throws SQLException {
        try (ResultSet rs = relatorioService.getRelatorioLivros()) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_livro")));
                row.add(rs.getString("titulo"));
                row.add(rs.getString("autor"));
                row.add(String.valueOf(rs.getInt("ano_publicacao")));
                row.add(rs.getString("status"));
                dadosRelatorio.add(row);
            }
            Platform.runLater(() -> lblTotalRegistros.setText("Total: " + dadosRelatorio.size() + " livros"));
        }
    }

    private void carregarEstudantes() throws SQLException {
        String curso = cbCurso.getValue();
        if (curso != null && curso.equals("Todos")) curso = null;

        try (ResultSet rs = relatorioService.getRelatorioEstudantes(curso)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_estudante")));
                row.add(rs.getString("nome"));
                row.add(rs.getString("curso"));
                row.add(rs.getString("codigo_estudante"));
                row.add(rs.getString("departamento"));
                dadosRelatorio.add(row);
            }
            Platform.runLater(() -> lblTotalRegistros.setText("Total: " + dadosRelatorio.size() + " estudantes"));
        }
    }

    private void carregarEmprestimos() throws SQLException {
        java.time.LocalDate dataInicio = dpDataInicio.getValue();
        java.time.LocalDate dataFim = dpDataFim.getValue();
        String status = cbStatusEmprestimo.getValue();

        try (ResultSet rs = relatorioService.getRelatorioEmprestimos(dataInicio, dataFim, status)) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_emprestimo")));
                row.add(rs.getString("estudante_nome"));
                row.add(rs.getString("livro_titulo"));
                row.add(rs.getDate("data_saida") != null ? rs.getDate("data_saida").toString() : "");
                String statusEmprestimo = rs.getDate("data_devolucao_real") == null ? "Ativo" : "Finalizado";
                row.add(statusEmprestimo);
                dadosRelatorio.add(row);
            }
            Platform.runLater(() -> lblTotalRegistros.setText("Total: " + dadosRelatorio.size() + " empréstimos"));
        }
    }

    private void carregarMultas() throws SQLException {
        try (ResultSet rs = relatorioService.getRelatorioMultas()) {
            double totalMultas = 0;
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add(String.valueOf(rs.getInt("id_emprestimo")));
                row.add(rs.getString("estudante_nome"));
                row.add(rs.getString("livro_titulo"));
                row.add(String.format("R$ %.2f", rs.getDouble("valor_multa")));
                row.add(rs.getBoolean("pago") ? "Pago" : "Pendente");
                dadosRelatorio.add(row);
                totalMultas += rs.getDouble("valor_multa");
            }
            final double total = totalMultas;
            Platform.runLater(() -> lblTotalRegistros.setText(String.format("Total: %d multas | Valor: R$ %.2f", dadosRelatorio.size(), total)));
        }
    }

    private void carregarEstatisticas() throws SQLException {
        try (ResultSet rs = relatorioService.getEstatisticas()) {
            while (rs.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                row.add("");
                row.add(rs.getString("indicador"));
                row.add(rs.getString("valor"));
                row.add("");
                row.add("");
                dadosRelatorio.add(row);
            }
            Platform.runLater(() -> lblTotalRegistros.setText("Total: " + dadosRelatorio.size() + " indicadores"));
        }
    }

    void filtrarTabela() {
        String filtro = txtBuscarTabela.getText().toLowerCase();
        if (filtro.isEmpty()) {
            carregarRelatorio();
            return;
        }

        ObservableList<ObservableList<String>> filtrados = FXCollections.observableArrayList();
        for (ObservableList<String> row : dadosRelatorio) {
            for (String cell : row) {
                if (cell.toLowerCase().contains(filtro)) {
                    filtrados.add(row);
                    break;
                }
            }
        }
        tabelaResultados.setItems(filtrados);
        lblTotalRegistros.setText("Exibindo: " + filtrados.size() + " de " + dadosRelatorio.size());
    }

    public void gerarRelatorio() {
        carregarRelatorio();
    }

    public void imprimirPDF() {
        if (dadosRelatorio.isEmpty()) {
            mostrarAviso("Nenhum dado para gerar PDF. Execute uma consulta primeiro.");
            return;
        }

        try {
            // Determinar o tipo de relatório atual
            String tipo = cbTipoRelatorio.getValue();
            if (tipo == null) {
                mostrarAviso("Selecione um tipo de relatório primeiro!");
                return;
            }

            // Obter os dados do relatório atual (ResultSet)
            ResultSet rs = obterResultSetAtual();

            if (rs == null) {
                mostrarErro("Não foi possível obter os dados do relatório.");
                return;
            }

            // Montar string com os filtros aplicados
            String filtros = montarFiltrosString();

            // Chamar o serviço para gerar PDF
            relatorioService.gerarPDF(tipo, rs, filtros);

            mostrarSucesso("PDF gerado com sucesso! O arquivo será aberto automaticamente.");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro ao gerar PDF: " + e.getMessage());
        }
    }

    public void exportarExcel() {
        if (dadosRelatorio.isEmpty()) {
            mostrarAviso("Nenhum dado para exportar.");
            return;
        }
        mostrarInfo("Funcionalidade de Excel em desenvolvimento.");
    }

    private void mostrarInfo(String msg) {
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

    private ResultSet obterResultSetAtual() throws SQLException {
        String tipo = cbTipoRelatorio.getValue();

        if (tipo.contains("Livros")) {
            return relatorioService.getRelatorioLivros();
        } else if (tipo.contains("Estudantes")) {
            String curso = (cbCurso != null && cbCurso.getValue() != null && !cbCurso.getValue().equals("Todos"))
                    ? cbCurso.getValue() : null;
            return relatorioService.getRelatorioEstudantes(curso);
        } else if (tipo.contains("Empréstimos")) {
            java.time.LocalDate dataInicio = dpDataInicio != null ? dpDataInicio.getValue() : null;
            java.time.LocalDate dataFim = dpDataFim != null ? dpDataFim.getValue() : null;
            String status = cbStatusEmprestimo != null ? cbStatusEmprestimo.getValue() : "Todos";
            return relatorioService.getRelatorioEmprestimos(dataInicio, dataFim, status);
        } else if (tipo.contains("Multas")) {
            return relatorioService.getRelatorioMultas();
        } else if (tipo.contains("Estatísticas")) {
            return relatorioService.getEstatisticas();
        }

        return null;
    }

    private String montarFiltrosString() {
        StringBuilder filtros = new StringBuilder();
        String tipo = cbTipoRelatorio.getValue();

        if (tipo.contains("Empréstimos")) {
            filtros.append("Período: ");
            if (dpDataInicio.getValue() != null) {
                filtros.append(dpDataInicio.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                filtros.append("início não definido");
            }
            filtros.append(" a ");
            if (dpDataFim.getValue() != null) {
                filtros.append(dpDataFim.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            } else {
                filtros.append("fim não definido");
            }
            filtros.append(" | Status: ");
            filtros.append(cbStatusEmprestimo != null ? cbStatusEmprestimo.getValue() : "Todos");

        } else if (tipo.contains("Estudantes")) {
            filtros.append("Curso: ");
            filtros.append(cbCurso != null && cbCurso.getValue() != null ? cbCurso.getValue() : "Todos");

        } else if (tipo.contains("Livros")) {
            filtros.append("Todos os livros cadastrados");

        } else if (tipo.contains("Multas")) {
            filtros.append("Todas as multas");

        } else if (tipo.contains("Estatísticas")) {
            filtros.append("Estatísticas gerais do sistema");
        }

        return filtros.toString();
    }

    private void mostrarSucesso(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}