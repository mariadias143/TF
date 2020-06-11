package Servidor.Database;

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
    public Produto(){
        this.id = 0;
        this.nome = "";
        this.preco = 0;
        this.stock=0;
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

    public void setPreco(float p){
        this.preco = p;
    }

    public void setStock(int quant){
        this.stock = quant;
    }

    public String toString(){
        return "Servidor.Database.Produto [id:"+this.id+" nome:"+this.nome+" preco:"+this.preco+" stock:"+this.stock+"]";
    }

    public void updateStock(int qnt){
        this.stock += qnt;
    }


}
