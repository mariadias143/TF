package Communication;

import Servidor.Database.Pair;

import java.util.ArrayList;
import java.util.List;

/**
public class StateUpdate {
    private int i;
    private String str;
    public StateUpdate(int i){
        this.i = i;
        this.str = "";
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
        this.str = generatedString;

    }

    public int getI() {
        return i;
    }

    public String getStr(){
        return this.str;
    }
}*/

public class StateUpdate{
    private int timestamp;
    private int type; //0 create enc, 1 add, 2 fechar pelo user sucesso, 3 fechar user imp, 4 fechar server
    private String userId;
    private int idEnc;
    private int idProdAdd;
    private int qntProdAdd;
    private List<Pair> remProd;

    /**
     * Construtor para state update da criação de uma encomenda
     * @param timestamp
     * @param userId
     * @param idEnc
     */

    public StateUpdate(int timestamp,String userId,int idEnc){
        this.timestamp = timestamp;
        this.userId = userId;
        this.type = 0;
        this.idEnc = idEnc;
        this.idProdAdd = 0;
        this.qntProdAdd = 0;
        this.remProd = new ArrayList<>();
    }

    public StateUpdate(int timestamp,int idEnc,int idProd,int qnt){
        this.timestamp = timestamp;
        this.userId = "";
        this.type = 1;
        this.idEnc = idEnc;
        this.idProdAdd = idProd;
        this.qntProdAdd = qnt;
        this.remProd = new ArrayList<>();
    }

    public StateUpdate(int timestamp,int idEnc,List<Pair> items){
        this.timestamp = timestamp;
        this.userId = "";
        this.type = 2;
        this.idEnc = idEnc;
        this.idProdAdd = 0;
        this.qntProdAdd = 0;
        this.remProd = new ArrayList<>();
        items.forEach(a -> remProd.add(a));
    }

    private StateUpdate(int timestamp,int idEnc){
        this.timestamp = timestamp;
        this.userId = "";
        this.type = 1;
        this.idEnc = idEnc;
        this.idProdAdd = 0;
        this.qntProdAdd = 0;
        this.remProd = new ArrayList<>();
    }

    public StateUpdate(){
        this.timestamp = timestamp;
        this.userId = "";
        this.type = 0;
        this.idEnc = idEnc;
        this.idProdAdd = 0;
        this.qntProdAdd = 0;
        this.remProd = new ArrayList<>();
    }

    public static StateUpdate finishEncServer(int timestamp,int idEnc){
        StateUpdate st = new StateUpdate(timestamp,idEnc);
        st.type = 4;

        return st;
    }

    public static StateUpdate finishEncUserFail(int timestamp,int idEnc){
        StateUpdate st = new StateUpdate(timestamp,idEnc);
        st.type = 3;

        return st;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getType() {
        return type;
    }

    public int getIdEnc() {
        return idEnc;
    }

    public int getIdProdAdd() {
        return idProdAdd;
    }

    public int getQntProdAdd() {
        return qntProdAdd;
    }

    public List<Pair> getRemProd() {
        return remProd;
    }

    public String getUserId() {
        return userId;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setIdEnc(int idEnc) {
        this.idEnc = idEnc;
    }

    public void setIdProdAdd(int idProdAdd) {
        this.idProdAdd = idProdAdd;
    }

    public void setQntProdAdd(int qntProdAdd) {
        this.qntProdAdd = qntProdAdd;
    }

    public void setRemProd(List<Pair> remProd) {
        this.remProd = remProd;
    }
}
