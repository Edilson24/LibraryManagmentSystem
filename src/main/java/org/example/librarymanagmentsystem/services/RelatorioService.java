package org.example.librarymanagmentsystem.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.example.librarymanagmentsystem.config.Conexao;
import java.io.FileOutputStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RelatorioService {

    private Connection connection;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public RelatorioService() throws SQLException {
        this.connection = Conexao.obterConexao();
    }

    // SELECT para Livros
    public ResultSet getRelatorioLivros() throws SQLException {
        String sql = "SELECT l.id_livro, l.titulo, l.autor, l.ano_publicacao, " +
                "l.isbn, l.status, l.unidades, d.nome_disciplina " +
                "FROM livros l " +
                "LEFT JOIN disciplinas d ON l.fk_disciplina = d.id_disciplina " +
                "ORDER BY l.titulo";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    // SELECT para Estudantes
    public ResultSet getRelatorioEstudantes(String curso) throws SQLException {
        String sql = "SELECT id_estudante, nome, curso, departamento, " +
                "codigo_estudante, idade, data_cadastro " +
                "FROM estudantes ";

        if (curso != null && !curso.equals("Todos")) {
            sql += "WHERE curso = ? ";
        }
        sql += "ORDER BY nome";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        if (curso != null && !curso.equals("Todos")) {
            pstmt.setString(1, curso);
        }
        return pstmt.executeQuery();
    }

    // SELECT para Empréstimos
    public ResultSet getRelatorioEmprestimos(LocalDate dataInicio, LocalDate dataFim, String status) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.id_emprestimo, est.nome as estudante_nome, est.codigo_estudante, ");
        sql.append("l.titulo as livro_titulo, e.data_saida, e.data_prevista_devolucao, ");
        sql.append("e.data_devolucao_real, e.valor_multa, e.pago ");
        sql.append("FROM emprestimos e ");
        sql.append("JOIN estudantes est ON e.fk_estudante = est.id_estudante ");
        sql.append("JOIN livros l ON e.fk_livro = l.id_livro ");
        sql.append("WHERE 1=1 ");

        if (dataInicio != null && dataFim != null) {
            sql.append("AND DATE(e.data_saida) BETWEEN ? AND ? ");
        }

        if (status != null && !status.equals("Todos")) {
            if (status.equals("Ativos")) {
                sql.append("AND e.data_devolucao_real IS NULL ");
            } else if (status.equals("Finalizados")) {
                sql.append("AND e.data_devolucao_real IS NOT NULL ");
            }
        }

        sql.append("ORDER BY e.data_saida DESC");

        PreparedStatement pstmt = connection.prepareStatement(sql.toString());
        int paramIndex = 1;

        if (dataInicio != null && dataFim != null) {
            pstmt.setDate(paramIndex++, Date.valueOf(dataInicio));
            pstmt.setDate(paramIndex++, Date.valueOf(dataFim));
        }

        return pstmt.executeQuery();
    }

    // SELECT para Multas
    public ResultSet getRelatorioMultas() throws SQLException {
        String sql = "SELECT e.id_emprestimo, est.nome as estudante_nome, " +
                "l.titulo as livro_titulo, e.data_prevista_devolucao, " +
                "e.data_devolucao_real, e.valor_multa, e.pago " +
                "FROM emprestimos e " +
                "JOIN estudantes est ON e.fk_estudante = est.id_estudante " +
                "JOIN livros l ON e.fk_livro = l.id_livro " +
                "WHERE e.valor_multa > 0 " +
                "ORDER BY e.valor_multa DESC";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    // SELECT para Estatísticas
    public ResultSet getEstatisticas() throws SQLException {
        String sql = "SELECT 'Total Estudantes' as indicador, COUNT(*) as valor FROM estudantes " +
                "UNION SELECT 'Total Livros', COUNT(*) FROM livros " +
                "UNION SELECT 'Empréstimos Ativos', COUNT(*) FROM emprestimos WHERE data_devolucao_real IS NULL " +
                "UNION SELECT 'Multas Pendentes', SUM(valor_multa) FROM emprestimos WHERE pago = 0 AND valor_multa > 0 " +
                "UNION SELECT 'Livros Disponíveis', SUM(unidades) FROM livros WHERE status = 'Disponível'";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    // Obter cursos distintos para filtro
    public ResultSet getCursos() throws SQLException {
        String sql = "SELECT DISTINCT curso FROM estudantes ORDER BY curso";
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    // Gerar PDF
    public void gerarPDF(String tipo, ResultSet dados, String filtros) throws Exception {
        String outputPath = "relatorios/relatorio_" + tipo.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis() + ".pdf";

        // Criar diretório se não existir
        new java.io.File("relatorios").mkdirs();

        Document document = new Document(PageSize.A4.rotate()); // Orientação paisagem para mais colunas
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();

        // Adicionar cabeçalho
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        Font normalFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);

        // Título
        Paragraph title = new Paragraph("SISMON - Sistema de Biblioteca", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Relatório de " + tipo, subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);

        Paragraph date = new Paragraph("Gerado em: " + LocalDate.now().format(dateFormatter), subtitleFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        document.add(date);

        document.add(new Paragraph(" "));
        document.add(new Paragraph("Filtros aplicados: " + filtros, normalFont));
        document.add(new Paragraph(" "));

        // Criar tabela
        ResultSetMetaData metaData = dados.getMetaData();
        int columnCount = metaData.getColumnCount();

        // Calcular larguras das colunas (distribuição proporcional)
        float[] columnWidths = new float[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnWidths[i] = 1f;
        }

        PdfPTable table = new PdfPTable(columnCount);
        table.setWidthPercentage(100);
        table.setWidths(columnWidths);

        // Adicionar cabeçalhos
        for (int i = 1; i <= columnCount; i++) {
            String columnName = metaData.getColumnName(i);
            // Traduzir nomes das colunas para português
            String displayName = traduzirNomeColuna(columnName);

            PdfPCell header = new PdfPCell(new Phrase(displayName, headerFont));
            header.setBackgroundColor(BaseColor.LIGHT_GRAY);
            header.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.setPadding(8);
            table.addCell(header);
        }

        // Adicionar dados
        int rowCount = 0;
        while (dados.next()) {
            rowCount++;
            for (int i = 1; i <= columnCount; i++) {
                String value = dados.getString(i) != null ? dados.getString(i) : "";
                PdfPCell cell = new PdfPCell(new Phrase(value, normalFont));
                cell.setPadding(5);
                table.addCell(cell);
            }
        }

        // Adicionar rodapé com total de registros
        document.add(table);
        document.add(new Paragraph(" "));
        Paragraph footer = new Paragraph("Total de registros: " + rowCount, normalFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();

        // Abrir o PDF gerado
        java.awt.Desktop.getDesktop().open(new java.io.File(outputPath));
    }

    private String traduzirNomeColuna(String columnName) {
        switch (columnName) {
            case "id_livro": return "ID Livro";
            case "titulo": return "Título";
            case "autor": return "Autor";
            case "ano_publicacao": return "Ano";
            case "isbn": return "ISBN";
            case "status": return "Status";
            case "categoria": return "Categoria";
            case "unidades": return "Unidades";
            case "nome_disciplina": return "Disciplina";
            case "id_estudante": return "ID Estudante";
            case "nome": return "Nome";
            case "curso": return "Curso";
            case "departamento": return "Departamento";
            case "codigo_estudante": return "Código";
            case "id_cartao_arduino": return "Cartão RFID";
            case "id_emprestimo": return "ID Empréstimo";
            case "estudante_nome": return "Estudante";
            case "livro_titulo": return "Livro";
            case "data_saida": return "Data Saída";
            case "data_prevista_devolucao": return "Prev. Devolução";
            case "data_devolucao_real": return "Data Devolução";
            case "valor_multa": return "Multa";
            case "pago": return "Pago";
            case "indicador": return "Indicador";
            default: return columnName;
        }
    }
}