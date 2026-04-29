package org.example.librarymanagmentsystem.entidades;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Emprestimo {
    private int idEmprestimo;
    private Estudante estudante;
    private Livro livro;
    private LocalDateTime dataSaida;
    private LocalDate dataPrevistaDevolucao;
    private LocalDate dataDevolucaoReal;
    private double valorMulta;
    private boolean pago;

    public Emprestimo() {}

    public Emprestimo(Estudante estudante, Livro livro, LocalDateTime dataSaida, LocalDate dataPrevistaDevolucao) {
        this.estudante = estudante;
        this.livro = livro;
        this.dataSaida = dataSaida;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
        this.valorMulta = 0;
        this.pago = false;
    }

    // Getters e Setters
    public int getIdEmprestimo() { return idEmprestimo; }
    public void setIdEmprestimo(int idEmprestimo) { this.idEmprestimo = idEmprestimo; }

    public Estudante getEstudante() { return estudante; }
    public void setEstudante(Estudante estudante) { this.estudante = estudante; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }

    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }

    public LocalDate getDataPrevistaDevolucao() { return dataPrevistaDevolucao; }
    public void setDataPrevistaDevolucao(LocalDate dataPrevistaDevolucao) { this.dataPrevistaDevolucao = dataPrevistaDevolucao; }

    public LocalDate getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDate dataDevolucaoReal) { this.dataDevolucaoReal = dataDevolucaoReal; }

    public double getValorMulta() { return valorMulta; }
    public void setValorMulta(double valorMulta) { this.valorMulta = valorMulta; }

    public boolean isPago() { return pago; }
    public void setPago(boolean pago) { this.pago = pago; }

    public boolean isAtivo() {
        return dataDevolucaoReal == null;
    }

    public boolean isAtrasado() {
        if (isAtivo()) {
            return LocalDate.now().isAfter(dataPrevistaDevolucao);
        }
        return false;
    }

    public long getDiasAtraso() {
        if (isAtrasado()) {
            return java.time.temporal.ChronoUnit.DAYS.between(dataPrevistaDevolucao, LocalDate.now());
        }
        return 0;
    }
}