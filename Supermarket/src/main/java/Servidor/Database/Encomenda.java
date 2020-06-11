package Servidor.Database;



import java.util.*;

public class Encomenda {
    private int idEnc;
    private Map<Integer, Pair> prods;
    private String idUser;
    private float timeout;

    public Encomenda(int idEnc,String idUser,float timeout){
        this.idEnc = idEnc;
        this.idUser = idUser;
        this.prods = new HashMap<>();
        this.timeout = timeout;
    }

    public Encomenda(){
        this.idEnc = 0;
        this.idUser = "";
        this.prods = new HashMap<>();
        this.timeout= 0;

    }


    public int getIdEnc() {
        return idEnc;
    }

    public void setIdEnc(int idEnc) {
        this.idEnc = idEnc;
    }

    public Map<Integer, Pair> getProds() {
        return prods;
    }

    public void setProds(Map<Integer, Pair> prods) {
        this.prods = prods;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public float getTimeout() {
        return timeout;
    }

    public void setTimeout(float timeout) {
        this.timeout = timeout;
    }
    public String toString(){
        return "Servidor.Database.Encomenda [id:"+this.idEnc+" idUser:"+this.idUser+" timeout:"+this.timeout + "produts:" +this.prods+ "]";
    }


    public void addProd(int prod, int qnt){
        Pair p = prods.get(prod);
        if (p == null){
            prods.put(prod,new Pair(prod,qnt));
        }
        else{
            p.updateqntProd(qnt);
        }
    }
}
