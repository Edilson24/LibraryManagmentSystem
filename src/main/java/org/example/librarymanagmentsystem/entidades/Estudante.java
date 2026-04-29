package org.example.librarymanagmentsystem.entidades;

import java.util.Objects;

public class Estudante extends Pessoa{
    // Atributos específicos do Estudante
    private String curso;
    private String idCartaoArduino;  // RFID do cartão
    private String codigoEstudante;   // Matrícula/RA
    private boolean ativo;



    // Construtor completo
    public Estudante(int id, String nome, int idade, String departamento, String curso, String idCartaoArduino, String codigoEstudante) {
        super(id, nome, idade, departamento);
        this.curso = curso;
        this.idCartaoArduino = idCartaoArduino;
        this.codigoEstudante = codigoEstudante;
        this.ativo = true;
    }

    // Construtor para novo cadastro (sem ID)
    public Estudante(String nome, int idade, String departamento, String curso, String idCartaoArduino, String codigoEstudante) {
        super(nome, idade, departamento);
        this.curso = curso;
        this.idCartaoArduino = idCartaoArduino;
        this.codigoEstudante = codigoEstudante;
        this.ativo = true;
    }

    // Getters e Setters específicos
    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        if (curso != null && !curso.trim().isEmpty()) {
            this.curso = curso;
        } else {
            throw new IllegalArgumentException("Curso não pode ser vazio");
        }
    }

    public String getIdCartaoArduino() {
        return idCartaoArduino;
    }

    public void setIdCartaoArduino(String idCartaoArduino) {
        if (idCartaoArduino != null && !idCartaoArduino.trim().isEmpty()) {
            this.idCartaoArduino = idCartaoArduino;
        } else {
            throw new IllegalArgumentException("ID do cartão Arduino não pode ser vazio");
        }
    }

    public String getCodigoEstudante() {
        return codigoEstudante;
    }

    public void setCodigoEstudante(String codigoEstudante) {
        if (codigoEstudante != null && !codigoEstudante.trim().isEmpty()) {
            this.codigoEstudante = codigoEstudante;
        } else {
            throw new IllegalArgumentException("Código do estudante não pode ser vazio");
        }
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    // Implementação do método abstrato
    @Override
    public String getTipoPessoa() {
        return "Estudante";
    }

    // Método específico para verificar se o estudante pode fazer empréstimo
    public boolean podePegarLivro() {
        return ativo;  // Pode adicionar mais regras depois
    }

    // Método para formatar os dados do cartão RFID (será usado pelo Arduino)
    public String gerarDadosCartao() {
        return String.format("%s|%s|%s", idCartaoArduino, codigoEstudante, getNome());
    }

    @Override
    public String toString() {
        return String.format("Estudante{id=%d, nome='%s', curso='%s', cartao='%s', codigo='%s'}",
                getId(), getNome(), curso, idCartaoArduino, codigoEstudante);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        Estudante estudante = (Estudante) o;
        return Objects.equals(codigoEstudante, estudante.codigoEstudante) ||
                Objects.equals(idCartaoArduino, estudante.idCartaoArduino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), codigoEstudante, idCartaoArduino);
    }
}
