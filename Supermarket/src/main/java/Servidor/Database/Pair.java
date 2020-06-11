package Servidor.Database;

public class Pair {
    public int idProd;
    public int qntProd;

    public Pair(int idProd,int qntProd){
        this.idProd = idProd;
        this.qntProd = qntProd;
    }

    public void updateqntProd(int qnt){
        this.qntProd += qnt;
    }
}
