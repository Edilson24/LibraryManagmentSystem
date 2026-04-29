package org.example.librarymanagmentsystem.daos;

import org.example.librarymanagmentsystem.config.Conexao;
import org.example.librarymanagmentsystem.entidades.Emprestimo;
import org.example.librarymanagmentsystem.entidades.Estudante;
import org.example.librarymanagmentsystem.entidades.Livro;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoDAO {

    private EstudanteDAO estudanteDAO = new EstudanteDAO();
    private LivroDAO livroDAO = new LivroDAO();

    // CREATE - Realizar empréstimo
    public void inserir(Emprestimo emprestimo) throws SQLException {
        String sql = "INSERT INTO emprestimos (fk_estudante, fk_livro, data_saida, data_prevista_devolucao, valor_multa, pago) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = Conexao.obterConexao();
            conn.setAutoCommit(false); // Iniciar transação

            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, emprestimo.getEstudante().getId());
            stmt.setInt(2, emprestimo.getLivro().getIdLivro());
            stmt.setTimestamp(3, Timestamp.valueOf(emprestimo.getDataSaida()));
            stmt.setDate(4, Date.valueOf(emprestimo.getDataPrevistaDevolucao()));
            stmt.setDouble(5, emprestimo.getValorMulta());
            stmt.setBoolean(6, emprestimo.isPago());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    emprestimo.setIdEmprestimo(rs.getInt(1));
                }
            }

            // Atualizar unidades do livro
            Livro livro = emprestimo.getLivro();
            livro.setUnidades(livro.getUnidades() - 1);
            livroDAO.atualizarUnidades(livro.getIdLivro(), livro.getUnidades());

            conn.commit(); // Confirmar transação

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback(); // Desfazer em caso de erro
            }
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // CREATE - Registrar devolução
    public void registrarDevolucao(int idEmprestimo, LocalDate dataDevolucao, double multaCalculada) throws SQLException {
        String sql = "UPDATE emprestimos SET data_devolucao_real = ?, valor_multa = ?, pago = ? WHERE id_emprestimo = ?";

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = Conexao.obterConexao();
            conn.setAutoCommit(false);

            // Buscar o empréstimo primeiro
            Emprestimo emprestimo = buscarPorId(idEmprestimo);
            if (emprestimo == null) {
                throw new SQLException("Empréstimo não encontrado!");
            }

            stmt = conn.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(dataDevolucao));
            stmt.setDouble(2, multaCalculada);
            stmt.setBoolean(3, multaCalculada == 0); // Pago automaticamente se não houver multa
            stmt.setInt(4, idEmprestimo);
            stmt.executeUpdate();

            // Devolver unidade do livro
            Livro livro = emprestimo.getLivro();
            livro.setUnidades(livro.getUnidades() + 1);
            livroDAO.atualizarUnidades(livro.getIdLivro(), livro.getUnidades());

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (stmt != null) stmt.close();
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // READ - Buscar por ID
    public Emprestimo buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM emprestimos WHERE id_emprestimo = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEmprestimo(rs);
                }
            }
        }
        return null;
    }

    // READ - Empréstimos ativos de um estudante
    public List<Emprestimo> listarAtivosPorEstudante(int idEstudante) throws SQLException {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM emprestimos WHERE fk_estudante = ? AND data_devolucao_real IS NULL ORDER BY data_saida DESC";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEstudante);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    emprestimos.add(mapearEmprestimo(rs));
                }
            }
        }
        return emprestimos;
    }

    // READ - Histórico de empréstimos de um estudante
    public List<Emprestimo> listarHistoricoPorEstudante(int idEstudante) throws SQLException {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM emprestimos WHERE fk_estudante = ? ORDER BY data_saida DESC";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEstudante);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    emprestimos.add(mapearEmprestimo(rs));
                }
            }
        }
        return emprestimos;
    }

    // READ - Todos os empréstimos ativos
    public List<Emprestimo> listarTodosAtivos() throws SQLException {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM emprestimos WHERE data_devolucao_real IS NULL ORDER BY data_prevista_devolucao ASC";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                emprestimos.add(mapearEmprestimo(rs));
            }
        }
        return emprestimos;
    }

    // READ - Empréstimos com atraso
    public List<Emprestimo> listarEmprestimosAtrasados() throws SQLException {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM emprestimos WHERE data_devolucao_real IS NULL AND data_prevista_devolucao < CURDATE()";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                emprestimos.add(mapearEmprestimo(rs));
            }
        }
        return emprestimos;
    }

    // READ - Empréstimos com multa pendente
    public List<Emprestimo> listarMultasPendentes() throws SQLException {
        List<Emprestimo> emprestimos = new ArrayList<>();
        String sql = "SELECT * FROM emprestimos WHERE pago = 0 AND valor_multa > 0";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                emprestimos.add(mapearEmprestimo(rs));
            }
        }
        return emprestimos;
    }

    // UPDATE - Pagar multa
    public void pagarMulta(int idEmprestimo) throws SQLException {
        String sql = "UPDATE emprestimos SET pago = 1 WHERE id_emprestimo = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEmprestimo);
            stmt.executeUpdate();
        }
    }

    // Calcular multa automaticamente
    public double calcularMulta(Emprestimo emprestimo, LocalDate dataDevolucao) {
        if (emprestimo.getDataDevolucaoReal() != null) {
            return emprestimo.getValorMulta(); // Já calculada
        }

        LocalDate dataPrevista = emprestimo.getDataPrevistaDevolucao();

        if (dataDevolucao.isAfter(dataPrevista)) {
            long diasAtraso = ChronoUnit.DAYS.between(dataPrevista, dataDevolucao);
            // R$ 2,00 por dia de atraso (ajuste conforme necessário)
            return diasAtraso * 2.0;
        }

        return 0.0;
    }

    // Verificar se estudante pode pegar mais livros (limite de 3)
    public boolean podePegarLivro(int idEstudante) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emprestimos WHERE fk_estudante = ? AND data_devolucao_real IS NULL";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEstudante);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) < 3; // Máximo 3 livros ao mesmo tempo
                }
            }
        }
        return true;
    }

    // Verificar se estudante tem multa pendente
    public boolean temMultaPendente(int idEstudante) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emprestimos WHERE fk_estudante = ? AND pago = 0 AND valor_multa > 0";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idEstudante);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Mapeamento
    private Emprestimo mapearEmprestimo(ResultSet rs) throws SQLException {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setIdEmprestimo(rs.getInt("id_emprestimo"));

        // Buscar estudante
        int idEstudante = rs.getInt("fk_estudante");
        Estudante estudante = estudanteDAO.buscarPorId(idEstudante);
        emprestimo.setEstudante(estudante);

        // Buscar livro
        int idLivro = rs.getInt("fk_livro");
        Livro livro = livroDAO.buscarPorId(idLivro);
        emprestimo.setLivro(livro);

        emprestimo.setDataSaida(rs.getTimestamp("data_saida").toLocalDateTime());

        Date dataPrevista = rs.getDate("data_prevista_devolucao");
        if (dataPrevista != null) {
            emprestimo.setDataPrevistaDevolucao(dataPrevista.toLocalDate());
        }

        Date dataDevolucao = rs.getDate("data_devolucao_real");
        if (dataDevolucao != null) {
            emprestimo.setDataDevolucaoReal(dataDevolucao.toLocalDate());
        }

        emprestimo.setValorMulta(rs.getDouble("valor_multa"));
        emprestimo.setPago(rs.getBoolean("pago"));

        return emprestimo;
    }

    // Contar empréstimos ativos
    public int contarAtivos() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM emprestimos WHERE data_devolucao_real IS NULL";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    // Somar multas pendentes
    public double somarMultasPendentes() throws SQLException {
        String sql = "SELECT SUM(valor_multa) as total FROM emprestimos WHERE pago = 0 AND valor_multa > 0";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getDouble("total") : 0;
        }
    }
}