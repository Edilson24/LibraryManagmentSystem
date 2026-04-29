package org.example.librarymanagmentsystem.daos;

import org.example.librarymanagmentsystem.config.Conexao;
import org.example.librarymanagmentsystem.entidades.Disciplina;
import org.example.librarymanagmentsystem.entidades.Livro;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LivroDAO {

    private DisciplinaDAO disciplinaDAO = new DisciplinaDAO();

    // CREATE
    public void inserir(Livro livro) throws SQLException {
        String sql = "INSERT INTO livros (titulo, autor, ano_publicacao, isbn, status, categoria, unidades, fk_disciplina) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getAutor());
            stmt.setInt(3, livro.getAnoPublicacao());
            stmt.setString(4, livro.getIsbn());
            stmt.setString(5, livro.getStatus());
            stmt.setString(6, livro.getCategoria());
            stmt.setInt(7, livro.getUnidades());

            if (livro.getDisciplina() != null) {
                stmt.setInt(8, livro.getDisciplina().getIdDisciplina());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    livro.setIdLivro(rs.getInt(1));
                }
            }
        }
    }

    // READ - Buscar por ID
    public Livro buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM livros WHERE id_livro = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearLivro(rs);
                }
            }
        }
        return null;
    }

    // READ - Buscar por ISBN
    public Livro buscarPorIsbn(String isbn) throws SQLException {
        String sql = "SELECT * FROM livros WHERE isbn = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearLivro(rs);
                }
            }
        }
        return null;
    }

    // READ - Buscar por título (busca parcial)
    public List<Livro> buscarPorTitulo(String titulo) throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livros WHERE titulo LIKE ? ORDER BY titulo";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + titulo + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    livros.add(mapearLivro(rs));
                }
            }
        }
        return livros;
    }

    // READ - Listar todos
    public List<Livro> listarTodos() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT l.*, d.nome_disciplina FROM livros l " +
                "LEFT JOIN disciplinas d ON l.fk_disciplina = d.id_disciplina " +
                "ORDER BY l.titulo";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                livros.add(mapearLivroCompleto(rs));
            }
        }
        return livros;
    }

    // READ - Listar por disciplina
    public List<Livro> listarPorDisciplina(int idDisciplina) throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT l.*, d.nome_disciplina FROM livros l " +
                "LEFT JOIN disciplinas d ON l.fk_disciplina = d.id_disciplina " +
                "WHERE l.fk_disciplina = ? ORDER BY l.titulo";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idDisciplina);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    livros.add(mapearLivroCompleto(rs));
                }
            }
        }
        return livros;
    }

    // READ - Listar por status
    public List<Livro> listarPorStatus(String status) throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT l.*, d.nome_disciplina FROM livros l " +
                "LEFT JOIN disciplinas d ON l.fk_disciplina = d.id_disciplina " +
                "WHERE l.status = ? ORDER BY l.titulo";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    livros.add(mapearLivroCompleto(rs));
                }
            }
        }
        return livros;
    }

    // READ - Livros disponíveis (unidades > 0)
    public List<Livro> listarDisponiveis() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT l.*, d.nome_disciplina FROM livros l " +
                "LEFT JOIN disciplinas d ON l.fk_disciplina = d.id_disciplina " +
                "WHERE l.unidades > 0 ORDER BY l.titulo";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                livros.add(mapearLivroCompleto(rs));
            }
        }
        return livros;
    }

    // UPDATE
    public void atualizar(Livro livro) throws SQLException {
        String sql = "UPDATE livros SET titulo = ?, autor = ?, ano_publicacao = ?, " +
                "isbn = ?, status = ?, categoria = ?, unidades = ?, fk_disciplina = ? " +
                "WHERE id_livro = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, livro.getTitulo());
            stmt.setString(2, livro.getAutor());
            stmt.setInt(3, livro.getAnoPublicacao());
            stmt.setString(4, livro.getIsbn());
            stmt.setString(5, livro.getStatus());
            stmt.setString(6, livro.getCategoria());
            stmt.setInt(7, livro.getUnidades());

            if (livro.getDisciplina() != null) {
                stmt.setInt(8, livro.getDisciplina().getIdDisciplina());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.setInt(9, livro.getIdLivro());
            stmt.executeUpdate();
        }
    }

    // UPDATE - Atualizar unidades (para empréstimos/devoluções)
    public void atualizarUnidades(int idLivro, int novaQuantidade) throws SQLException {
        String sql = "UPDATE livros SET unidades = ?, status = ? WHERE id_livro = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, novaQuantidade);

            // Atualizar status automaticamente
            String status = novaQuantidade > 0 ? "Disponível" : "Indisponível";
            stmt.setString(2, status);
            stmt.setInt(3, idLivro);

            stmt.executeUpdate();
        }
    }

    // DELETE
    public void deletar(int id) throws SQLException {
        // Verificar se o livro tem empréstimos ativos
        if (temEmprestimosAtivos(id)) {
            throw new SQLException("Não é possível deletar livro com empréstimos ativos!");
        }

        String sql = "DELETE FROM livros WHERE id_livro = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Verificar se livro tem empréstimos ativos
    private boolean temEmprestimosAtivos(int idLivro) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emprestimos WHERE fk_livro = ? AND data_devolucao_real IS NULL";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idLivro);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Mapeamento básico
    private Livro mapearLivro(ResultSet rs) throws SQLException {
        Livro livro = new Livro();
        livro.setIdLivro(rs.getInt("id_livro"));
        livro.setTitulo(rs.getString("titulo"));
        livro.setAutor(rs.getString("autor"));
        livro.setAnoPublicacao(rs.getInt("ano_publicacao"));
        livro.setIsbn(rs.getString("isbn"));
        livro.setStatus(rs.getString("status"));
        livro.setCategoria(rs.getString("categoria"));
        livro.setUnidades(rs.getInt("unidades"));

        // Buscar disciplina se existir
        int fkDisciplina = rs.getInt("fk_disciplina");
        if (!rs.wasNull() && fkDisciplina > 0) {
            Disciplina disciplina = disciplinaDAO.buscarPorId(fkDisciplina);
            livro.setDisciplina(disciplina);
        }

        return livro;
    }

    // Mapeamento completo com nome da disciplina
    private Livro mapearLivroCompleto(ResultSet rs) throws SQLException {
        Livro livro = mapearLivro(rs);

        // Adicionar nome da disciplina se disponível
        String nomeDisciplina = rs.getString("nome_disciplina");
        if (nomeDisciplina != null && livro.getDisciplina() != null) {
            livro.getDisciplina().setNomeDisciplina(nomeDisciplina);
        }

        return livro;
    }

    // Contar total de livros
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM livros";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    // Somar unidades disponíveis
    public int somarUnidadesDisponiveis() throws SQLException {
        String sql = "SELECT SUM(unidades) as total FROM livros WHERE status = 'Disponível'";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt("total") : 0;
        }
    }
}