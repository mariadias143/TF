package Servidor;

public class Produto {
    private int id;
    private String nome;
    private float preco;
    private int stock;

    public Produto(int id, String nome, float preco, int quant){
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.stock = quant;
    }

    public int getId(){
        return this.id;
    }

    public String getNome(){
        return this.nome;
    }

    public float getPreco(){
        return this.preco;
    }

    public int getStock(){
        return this.stock;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setNome(String n){
        this.nome = n;
    }

    public void setId(float p){
        this.preco = p;
    }

    public void setStock(int quant){
        this.stock = quant;
    }

    public String toString(){
        return "Servidor.Produto [id:"+this.id+" nome:"+this.nome+" preco:"+this.preco+" stock:"+this.stock+"]";
    }
}
