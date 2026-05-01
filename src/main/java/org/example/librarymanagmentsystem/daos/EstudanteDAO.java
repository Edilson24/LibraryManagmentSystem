package org.example.librarymanagmentsystem.daos;

import org.example.librarymanagmentsystem.config.Conexao;
import org.example.librarymanagmentsystem.entidades.Estudante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudanteDAO {

    // CREATE
    public void inserir(Estudante estudante) throws SQLException {
        String sql = "INSERT INTO estudantes (nome, curso, id_cartao_arduino, idade, departamento, codigo_estudante) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, estudante.getNome());
            stmt.setString(2, estudante.getCurso());
            stmt.setString(3, estudante.getIdCartaoArduino());
            stmt.setInt(4, estudante.getIdade());
            stmt.setString(5, estudante.getDepartamento());
            stmt.setString(6, estudante.getCodigoEstudante());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    estudante.setId(rs.getInt(1));
                }
            }
        }
    }

    // READ - Buscar por ID
    public Estudante buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM estudantes WHERE id_estudante = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEstudante(rs);
                }
            }
        }
        return null;
    }

    // READ - Buscar por cartão Arduino (RFID)
    public Estudante buscarPorCartaoArduino(String idCartao) throws SQLException {
        String sql = "SELECT * FROM estudantes WHERE id_cartao_arduino = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, idCartao);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEstudante(rs);
                }
            }
        }
        return null;
    }

    // READ - Buscar por código do estudante
    public Estudante buscarPorCodigo(String codigoEstudante) throws SQLException {
        String sql = "SELECT * FROM estudantes WHERE codigo_estudante = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codigoEstudante);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearEstudante(rs);
                }
            }
        }
        return null;
    }

    // READ - Buscar por nome (contém) - MÉTODO QUE FALTAVA!
    public List<Estudante> buscarPorNome(String nome) throws SQLException {
        List<Estudante> estudantes = new ArrayList<>();
        String sql = "SELECT * FROM estudantes WHERE nome LIKE ? ORDER BY nome";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    estudantes.add(mapearEstudante(rs));
                }
            }
        }
        return estudantes;
    }

    // READ - Buscar por curso
    public List<Estudante> buscarPorCurso(String curso) throws SQLException {
        List<Estudante> estudantes = new ArrayList<>();
        String sql = "SELECT * FROM estudantes WHERE curso = ? ORDER BY nome";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, curso);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    estudantes.add(mapearEstudante(rs));
                }
            }
        }
        return estudantes;
    }

    // READ - Buscar por departamento
    public List<Estudante> buscarPorDepartamento(String departamento) throws SQLException {
        List<Estudante> estudantes = new ArrayList<>();
        String sql = "SELECT * FROM estudantes WHERE departamento = ? ORDER BY nome";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departamento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    estudantes.add(mapearEstudante(rs));
                }
            }
        }
        return estudantes;
    }

    // READ - Listar todos
    public List<Estudante> listarTodos() throws SQLException {
        List<Estudante> estudantes = new ArrayList<>();
        String sql = "SELECT * FROM estudantes ORDER BY nome";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                estudantes.add(mapearEstudante(rs));
            }
        }
        return estudantes;
    }

    // READ - Listar ativos (se tiver campo ativo)
    public List<Estudante> listarAtivos() throws SQLException {
        List<Estudante> estudantes = new ArrayList<>();
        String sql = "SELECT * FROM estudantes ORDER BY nome";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                estudantes.add(mapearEstudante(rs));
            }
        }
        return estudantes;
    }

    // UPDATE
    public void atualizar(Estudante estudante) throws SQLException {
        String sql = "UPDATE estudantes SET nome = ?, curso = ?, id_cartao_arduino = ?, " +
                "idade = ?, departamento = ?, codigo_estudante = ? WHERE id_estudante = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estudante.getNome());
            stmt.setString(2, estudante.getCurso());
            stmt.setString(3, estudante.getIdCartaoArduino());
            stmt.setInt(4, estudante.getIdade());
            stmt.setString(5, estudante.getDepartamento());
            stmt.setString(6, estudante.getCodigoEstudante());
            stmt.setInt(7, estudante.getId());

            stmt.executeUpdate();
        }
    }

    // DELETE
    public void deletar(int id) throws SQLException {
        // Verificar se o estudante tem empréstimos ativos
        if (temEmprestimosAtivos(id)) {
            throw new SQLException("Não é possível deletar estudante com empréstimos ativos!");
        }

        String sql = "DELETE FROM estudantes WHERE id_estudante = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Verificar se estudante tem empréstimos ativos
    private boolean temEmprestimosAtivos(int idEstudante) throws SQLException {
        String sql = "SELECT COUNT(*) FROM emprestimos WHERE fk_estudante = ? AND data_devolucao_real IS NULL";

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

    // Contar total de estudantes
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM estudantes";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    // Buscar por múltiplos critérios (busca avançada)
    public List<Estudante> buscarAvancada(String nome, String curso, String departamento) throws SQLException {
        List<Estudante> estudantes = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM estudantes WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (nome != null && !nome.isEmpty()) {
            sql.append(" AND nome LIKE ?");
            params.add("%" + nome + "%");
        }
        if (curso != null && !curso.isEmpty()) {
            sql.append(" AND curso = ?");
            params.add(curso);
        }
        if (departamento != null && !departamento.isEmpty()) {
            sql.append(" AND departamento = ?");
            params.add(departamento);
        }

        sql.append(" ORDER BY nome");

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    estudantes.add(mapearEstudante(rs));
                }
            }
        }
        return estudantes;
    }

    // Mapear ResultSet para objeto Estudante
    private Estudante mapearEstudante(ResultSet rs) throws SQLException {
        Estudante estudante = new Estudante();
        estudante.setId(rs.getInt("id_estudante"));
        estudante.setNome(rs.getString("nome"));
        estudante.setCurso(rs.getString("curso"));
        estudante.setIdCartaoArduino(rs.getString("id_cartao_arduino"));
        estudante.setIdade(rs.getInt("idade"));
        estudante.setDepartamento(rs.getString("departamento"));
        estudante.setCodigoEstudante(rs.getString("codigo_estudante"));

        Timestamp timestamp = rs.getTimestamp("data_cadastro");
        if (timestamp != null) {
            estudante.setDataCadastro(timestamp.toLocalDateTime().toLocalDate());
        }

        return estudante;
    }
}