package org.example.librarymanagmentsystem.daos;

import org.example.librarymanagmentsystem.config.Conexao;
import org.example.librarymanagmentsystem.entidades.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // CREATE - Inserir novo usuário
    public void inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, usuario, senha, tipo, foto, ativo) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getUsuario());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getTipo());
            stmt.setString(5, usuario.getFoto());
            stmt.setBoolean(6, usuario.isAtivo());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1));
                }
            }
        }
    }

    // READ - Autenticar usuário
    public Usuario autenticar(String usuario, String senha) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE usuario = ? AND senha = ? AND ativo = 1";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    // READ - Buscar usuário por ID
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    // READ - Buscar usuário por nome de usuário
    public Usuario buscarPorUsuario(String usuario) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE usuario = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearUsuario(rs);
                }
            }
        }
        return null;
    }

    // READ - Listar todos os usuários
    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY nome";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    // READ - Listar apenas administradores
    public List<Usuario> listarAdministradores() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE tipo = 'Administrador' ORDER BY nome";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    // READ - Listar apenas funcionários
    public List<Usuario> listarFuncionarios() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE tipo = 'Funcionario' ORDER BY nome";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    // READ - Listar usuários ativos
    public List<Usuario> listarAtivos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE ativo = 1 ORDER BY nome";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        return usuarios;
    }

    // UPDATE - Atualizar usuário
    public void atualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuarios SET nome = ?, usuario = ?, senha = ?, tipo = ?, foto = ?, ativo = ? WHERE id_usuario = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getUsuario());
            stmt.setString(3, usuario.getSenha());
            stmt.setString(4, usuario.getTipo());
            stmt.setString(5, usuario.getFoto());
            stmt.setBoolean(6, usuario.isAtivo());
            stmt.setInt(7, usuario.getIdUsuario());

            stmt.executeUpdate();
        }
    }

    // UPDATE - Atualizar apenas a foto
    public void atualizarFoto(int idUsuario, String caminhoFoto) throws SQLException {
        String sql = "UPDATE usuarios SET foto = ? WHERE id_usuario = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, caminhoFoto);
            stmt.setInt(2, idUsuario);

            stmt.executeUpdate();
        }
    }

    // UPDATE - Alterar senha
    public void alterarSenha(int idUsuario, String novaSenha) throws SQLException {
        String sql = "UPDATE usuarios SET senha = ? WHERE id_usuario = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaSenha);
            stmt.setInt(2, idUsuario);

            stmt.executeUpdate();
        }
    }

    // UPDATE - Ativar/Desativar usuário
    public void setAtivo(int idUsuario, boolean ativo) throws SQLException {
        String sql = "UPDATE usuarios SET ativo = ? WHERE id_usuario = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, ativo);
            stmt.setInt(2, idUsuario);

            stmt.executeUpdate();
        }
    }

    // DELETE - Deletar usuário (físico)
    public void deletar(int idUsuario) throws SQLException {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            stmt.executeUpdate();
        }
    }

    // DELETE - Soft delete (apenas desativar)
    public void desativar(int idUsuario) throws SQLException {
        setAtivo(idUsuario, false);
    }

    // Contar total de usuários
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) as total FROM usuarios";

        try (Connection conn = Conexao.obterConexao();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getInt("total") : 0;
        }
    }

    // Verificar se usuário existe
    public boolean usuarioExiste(String usuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE usuario = ?";

        try (Connection conn = Conexao.obterConexao();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Mapear ResultSet para objeto Usuario
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getInt("id_usuario"));
        usuario.setNome(rs.getString("nome"));
        usuario.setUsuario(rs.getString("usuario"));
        usuario.setSenha(rs.getString("senha"));
        usuario.setTipo(rs.getString("tipo"));
        usuario.setFoto(rs.getString("foto"));
        usuario.setAtivo(rs.getBoolean("ativo"));

        Timestamp timestamp = rs.getTimestamp("data_cadastro");
        if (timestamp != null) {
            usuario.setDataCadastro(timestamp.toLocalDateTime());
        }

        return usuario;
    }
}