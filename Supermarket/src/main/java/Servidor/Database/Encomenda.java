package Servidor.Database;



import java.util.*;

public class Encomenda {
    private int idEnc;
    private Map<Integer, Pair> prods;
    private String idUser;
    private String end;

    public Encomenda(int idEnc,String idUser,String end){
        this.idEnc = idEnc;
        this.idUser = idUser;
        this.prods = new HashMap<>();
        this.end = end; // 1;
    }

    public Encomenda(){
        this.idEnc = 0;
        this.idUser = "";
        this.prods = new HashMap<>();
        this.end ="";

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


    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String toString(){
        return "Servidor.Database.Encomenda [id:"+this.idEnc+" idUser:"+this.idUser+"]";
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
