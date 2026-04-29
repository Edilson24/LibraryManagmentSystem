package org.example.librarymanagmentsystem.entidades;

public class Livro {
    private int idLivro;
    private String titulo;
    private String autor;
    private int anoPublicacao;
    private String isbn;
    private String status; // Disponível, Emprestado, Manutenção
    private String categoria;
    private int unidades;
    private Disciplina disciplina;

    public Livro() {}

    public Livro(String titulo, String autor, int anoPublicacao, String isbn,
                 String categoria, int unidades, Disciplina disciplina) {
        this.titulo = titulo;
        this.autor = autor;
        this.anoPublicacao = anoPublicacao;
        this.isbn = isbn;
        this.categoria = categoria;
        this.unidades = unidades;
        this.disciplina = disciplina;
        this.status = unidades > 0 ? "Disponível" : "Indisponível";
    }

    // Getters e Setters
    public int getIdLivro() { return idLivro; }
    public void setIdLivro(int idLivro) { this.idLivro = idLivro; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getAutor() { return autor; }
    public void setAutor(String autor) { this.autor = autor; }

    public int getAnoPublicacao() { return anoPublicacao; }
    public void setAnoPublicacao(int anoPublicacao) { this.anoPublicacao = anoPublicacao; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public int getUnidades() { return unidades; }
    public void setUnidades(int unidades) {
        this.unidades = unidades;
        // Atualizar status automaticamente
        this.status = unidades > 0 ? "Disponível" : "Indisponível";
    }

    public Disciplina getDisciplina() { return disciplina; }
    public void setDisciplina(Disciplina disciplina) { this.disciplina = disciplina; }

    public boolean isDisponivel() {
        return unidades > 0 && "Disponível".equals(status);
    }

    @Override
    public String toString() {
        return titulo + " - " + autor;
    }
}