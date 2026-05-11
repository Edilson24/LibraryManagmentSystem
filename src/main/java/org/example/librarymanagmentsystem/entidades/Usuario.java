package org.example.librarymanagmentsystem.entidades;

import java.time.LocalDateTime;

public class Usuario {
    private int idUsuario;
    private String nome;
    private String usuario;
    private String senha;
    private String tipo; // Administrador ou Funcionario
    private String foto; // Caminho da foto no sistema
    private boolean ativo;
    private LocalDateTime dataCadastro;

    // Construtores
    public Usuario() {}

    public Usuario(String nome, String usuario, String senha, String tipo) {
        this.nome = nome;
        this.usuario = usuario;
        this.senha = senha;
        this.tipo = tipo;
        this.ativo = true;
    }

    public Usuario(String nome, String usuario, String senha, String tipo, String foto) {
        this.nome = nome;
        this.usuario = usuario;
        this.senha = senha;
        this.tipo = tipo;
        this.foto = foto;
        this.ativo = true;
    }

    // Getters e Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    // Métodos utilitários
    public boolean isAdministrador() {
        return "Administrador".equals(tipo);
    }

    public boolean isFuncionario() {
        return "Funcionario".equals(tipo);
    }

    public String getIniciais() {
        if (nome == null || nome.isEmpty()) return "?";
        String[] partes = nome.trim().split(" ");
        if (partes.length == 1) {
            return String.valueOf(partes[0].charAt(0)).toUpperCase();
        }
        return String.valueOf(partes[0].charAt(0)).toUpperCase() +
                String.valueOf(partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    @Override
    public String toString() {
        return nome + " (" + usuario + ") - " + tipo;
    }
}