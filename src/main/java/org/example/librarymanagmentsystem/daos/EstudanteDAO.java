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

            // Recupera o ID gerado
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    estudante.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // READ - Buscar por ID do cartão Arduino (para leitor RFID)
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

    // DELETE (soft delete ou hard delete?)
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM estudantes WHERE id_estudante = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Método auxiliar para mapear ResultSet para objeto Estudante
    private Estudante mapearEstudante(ResultSet rs) throws SQLException {
        Estudante estudante = new Estudante(
                rs.getInt("id_estudante"),
                rs.getString("nome"),
                rs.getInt("idade"),
                rs.getString("departamento"),
                rs.getString("curso"),
                rs.getString("id_cartao_arduino"),
                rs.getString("codigo_estudante")
        );
        estudante.setDataCadastro(rs.getTimestamp("data_cadastro").toLocalDateTime().toLocalDate());
        return estudante;
    }
}