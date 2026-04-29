package org.example.librarymanagmentsystem.services;

import org.example.librarymanagmentsystem.config.Conexao;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DashboardService {
    // SELECT para o Card de Total de Estudantes
    public int getTotalEstudantes() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM estudantes";
        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    // SELECT para Livros Disponíveis (unidades_disponiveis > 0)
    public int getLivrosDisponiveis() throws SQLException {
        String sql = "SELECT SUM(unidades) as total FROM livros WHERE status = 'Disponível' AND unidades > 0";
        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    // SELECT para Empréstimos Ativos (sem data_devolucao_real)
    public int getEmprestimosAtivos() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM emprestimos WHERE data_devolucao_real IS NULL";
        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    // SELECT para Multas Pendentes (valor_multa > 0 AND pago = 0)
    public double getMultasPendentes() throws SQLException {
        String sql = "SELECT SUM(valor_multa) as total FROM emprestimos WHERE pago = 0 AND valor_multa > 0";
        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getDouble("total") : 0;
        }
    }

    // SELECT para Últimos Empréstimos (últimos 5)
    public ResultSet getUltimosEmprestimos() throws SQLException {
        String sql = "SELECT e.id_emprestimo, est.nome as estudante_nome, l.titulo as livro_titulo, " +
                "e.data_saida, e.data_prevista_devolucao, e.data_devolucao_real " +
                "FROM emprestimos e " +
                "JOIN estudantes est ON e.fk_estudante = est.id_estudante " +
                "JOIN livros l ON e.fk_livro = l.id_livro " +
                "ORDER BY e.data_saida DESC LIMIT 5";
        Connection conn = Conexao.obterConexao();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // SELECT para Top Livros Mais Emprestados
    public ResultSet getTopLivros() throws SQLException {
        String sql = "SELECT l.titulo, COUNT(e.id_emprestimo) as total_emprestimos " +
                "FROM livros l " +
                "JOIN emprestimos e ON l.id_livro = e.fk_livro " +
                "GROUP BY l.id_livro " +
                "ORDER BY total_emprestimos DESC LIMIT 5";
        Connection conn = Conexao.obterConexao();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // SELECT para Dados do Gráfico (Empréstimos por Mês)
    public ResultSet getEmprestimosPorMes() throws SQLException {
        String sql = "SELECT MONTH(data_saida) as mes, COUNT(*) as total " +
                "FROM emprestimos " +
                "WHERE YEAR(data_saida) = YEAR(CURDATE()) " +
                "GROUP BY MONTH(data_saida) " +
                "ORDER BY mes";
        Connection conn = Conexao.obterConexao();
        Statement stmt = conn.createStatement();
        return stmt.executeQuery(sql);
    }

    // SELECT para Alertas (livros com poucas unidades)
    public ResultSet getLivrosPoucasUnidades(int limite) throws SQLException {
        String sql = "SELECT titulo, unidades FROM livros WHERE unidades <= ? AND unidades > 0 ORDER BY unidades ASC";
        Connection conn = Conexao.obterConexao();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, limite);
        return pstmt.executeQuery();
    }

    public ResultSet listarEstudantes() throws SQLException {
        String sql = "SELECT * FROM estudantes";
        Connection conn = Conexao.obterConexao();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        return pstmt.executeQuery();
    }
}