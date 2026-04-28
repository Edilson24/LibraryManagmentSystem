package org.example.librarymanagmentsystem.entidades;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public abstract class Pessoa {
    // Atributos privados (encapsulamento)
    private int id;
    private String nome;
    private int idade;
    private String departamento;
    private LocalDate dataCadastro;

    // Construtor completo
    public Pessoa(int id, String nome, int idade, String departamento) {
        this.id = id;
        this.nome = nome;
        this.idade = idade;
        this.departamento = departamento;
        this.dataCadastro = LocalDate.now();
    }

    // Construtor sem ID (para novos cadastros)
    public Pessoa(String nome, int idade, String departamento) {
        this.nome = nome;
        this.idade = idade;
        this.departamento = departamento;
        this.dataCadastro = LocalDate.now();
    }

    // Getters e Setters (encapsulamento)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id > 0) {
            this.id = id;
        } else {
            throw new IllegalArgumentException("ID deve ser positivo");
        }
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome != null && !nome.trim().isEmpty()) {
            this.nome = nome;
        } else {
            throw new IllegalArgumentException("Nome não pode ser vazio");
        }
    }

    public int getIdade() {
        return idade;
    }

    public void setIdade(int idade) {
        if (idade >= 0 && idade <= 150) {
            this.idade = idade;
        } else {
            throw new IllegalArgumentException("Idade inválida");
        }
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public LocalDate getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDate dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    // Método para calcular idade a partir da data de nascimento
    public void calcularIdade(LocalDate dataNascimento) {
        if (dataNascimento != null) {
            this.idade = Period.between(dataNascimento, LocalDate.now()).getYears();
        }
    }

    // Método abstrato - será implementado pelas classes filhas
    public abstract String getTipoPessoa();

    // Método sobrescrito para exibir informações
    @Override
    public String toString() {
        return String.format("Pessoa{id=%d, nome='%s', idade=%d, departamento='%s'}",
                id, nome, idade, departamento);
    }

    // equals e hashCode baseado no ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pessoa pessoa = (Pessoa) o;
        return id == pessoa.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}


