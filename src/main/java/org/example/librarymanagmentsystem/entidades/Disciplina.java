package org.example.librarymanagmentsystem.entidades;

public class Disciplina {
    private int idDisciplina;
    private String nomeDisciplina;

    public Disciplina() {}

    public Disciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    // Getters e Setters
    public int getIdDisciplina() { return idDisciplina; }
    public void setIdDisciplina(int idDisciplina) { this.idDisciplina = idDisciplina; }

    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }

    @Override
    public String toString() {
        return nomeDisciplina;
    }
}