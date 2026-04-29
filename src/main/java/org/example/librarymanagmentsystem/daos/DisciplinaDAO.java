package org.example.librarymanagmentsystem.daos;

import org.example.librarymanagmentsystem.config.Conexao;
import org.example.librarymanagmentsystem.entidades.Disciplina;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DisciplinaDAO {

    // CREATE
    public void inserir(Disciplina disciplina) throws SQLException {
        String sql = "INSERT INTO disciplinas (nome_disciplina) VALUES (?)";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, disciplina.getNomeDisciplina());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    disciplina.setIdDisciplina(rs.getInt(1));
                }
            }
        }
    }

    // READ - Buscar por ID
    public Disciplina buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM disciplinas WHERE id_disciplina = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearDisciplina(rs);
                }
            }
        }
        return null;
    }

    // READ - Buscar por nome
    public Disciplina buscarPorNome(String nome) throws SQLException {
        String sql = "SELECT * FROM disciplinas WHERE nome_disciplina = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearDisciplina(rs);
                }
            }
        }
        return null;
    }

    // READ - Listar todas
    public List<Disciplina> listarTodas() throws SQLException {
        List<Disciplina> disciplinas = new ArrayList<>();
        String sql = "SELECT * FROM disciplinas ORDER BY nome_disciplina";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                disciplinas.add(mapearDisciplina(rs));
            }
        }
        return disciplinas;
    }

    // UPDATE
    public void atualizar(Disciplina disciplina) throws SQLException {
        String sql = "UPDATE disciplinas SET nome_disciplina = ? WHERE id_disciplina = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, disciplina.getNomeDisciplina());
            stmt.setInt(2, disciplina.getIdDisciplina());
            stmt.executeUpdate();
        }
    }

    // DELETE
    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM disciplinas WHERE id_disciplina = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Mapeamento
    private Disciplina mapearDisciplina(ResultSet rs) throws SQLException {
        Disciplina disciplina = new Disciplina();
        disciplina.setIdDisciplina(rs.getInt("id_disciplina"));
        disciplina.setNomeDisciplina(rs.getString("nome_disciplina"));
        return disciplina;
    }
}