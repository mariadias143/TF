package Servidor;

import java.util.*;

public class Encomenda {
    private int idEnc;
    private Map<Integer,Pair> prods;
    private String idUser;

    public Encomenda(int idEnc,String idUser){
        this.idEnc = idEnc;
        this.idUser = idUser;
        this.prods = new HashMap<>();
    }

    public void addProd(int prod,int qnt){
        Pair p = prods.get(prod);
        if (p == null){
            prods.put(prod,new Pair(prod,qnt));
        }
        else{
            p.updateqntProd(qnt);
        }
    }
}
