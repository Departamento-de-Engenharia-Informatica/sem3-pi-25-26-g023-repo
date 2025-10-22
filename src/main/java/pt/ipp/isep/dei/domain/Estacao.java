package pt.ipp.isep.dei.domain;

public class Estacao {
    private final int idEstacao;
    private final String nome;

    public Estacao(int idEstacao, String nome) {
        this.idEstacao = idEstacao;
        this.nome = nome;
    }

    public int getIdEstacao() {
        return idEstacao;
    }

    public String getNome() {
        return nome;
    }

    @Override
    public String toString() {
        return String.format("ID: %d - %s", idEstacao, nome);
    }
}