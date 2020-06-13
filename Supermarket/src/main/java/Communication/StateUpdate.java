package Communication;

import Servidor.Database.Pair;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private String begin;
    private String actual;
    private String end;
    private int idProdAdd;
    private int qntProdAdd;
    private List<Pair> remProd;
    private List<Integer> possibleTimestamps;

    /**
     * Construtor para state update da criação de uma encomenda
     * @param timestamp
     * @param userId
     * @param idEnc
     */

    public StateUpdate(int timestamp,String userId,int idEnc){
        Date dNow = new Date(System.currentTimeMillis());
        Date dEnd  = new Date(System.currentTimeMillis()+ 30*10000);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDNow = dateFormat.format(dNow);
        String strEnd = dateFormat.format(dEnd);
        this.timestamp = timestamp;
        this.userId = userId;
        this.type = 0;
        this.idEnc = idEnc;

        this.begin = strDNow; // TEMPO ATUAL;
        this.actual="";// ""
        this.end=strEnd;// ATUAL + TIMEOUT
        this.idProdAdd = 0;
        this.qntProdAdd = 0;
        this.remProd = new ArrayList<>();
        this.possibleTimestamps = new ArrayList<>();
    }

    public StateUpdate(int timestamp,int idEnc,int idProd,int qnt){
        this.timestamp = timestamp;
        this.userId = "";
        this.type = 1;
        this.idEnc = idEnc;
        this.begin = "";
        this.actual="";
        this.end="";
        this.idProdAdd = idProd;
        this.qntProdAdd = qnt;
        this.remProd = new ArrayList<>();
        this.possibleTimestamps = new ArrayList<>();
    }

    public StateUpdate(int timestamp,int idEnc,List<Pair> items){
        this.timestamp = timestamp;
        this.userId = "";
        this.type = 2;
        this.idEnc = idEnc;
        this.begin = "";
        this.actual="";
        this.end="";
        this.idProdAdd = 0;
        this.qntProdAdd = 0;
        this.remProd = new ArrayList<>();
        items.forEach(a -> remProd.add(a));
        this.possibleTimestamps = new ArrayList<>();
    }

    private StateUpdate(int timestamp,int idEnc){
        this.timestamp = timestamp;
        this.userId = "";
        this.type = 1;
        this.idEnc = idEnc;
        this.begin = "";
        this.actual="";
        this.end="";
        this.idProdAdd = 0;
        this.qntProdAdd = 0;
        this.remProd = new ArrayList<>();
        this.possibleTimestamps = new ArrayList<>();
    }

    public StateUpdate(int timestamp,List<Integer> gaptimestamps){
        this.timestamp = timestamp;
        this.userId = "";
        this.type = 1;
        this.idEnc = idEnc;
        this.begin = "";
        this.actual="";
        this.end="";
        this.idProdAdd = 0;
        this.qntProdAdd = 0;
        this.remProd = new ArrayList<>();
        gaptimestamps.forEach(a -> this.possibleTimestamps.add(a));
        this.possibleTimestamps = new ArrayList<>();
    }

    public StateUpdate(){
        this.timestamp = timestamp;
        this.userId = "";
        this.type = 0;
        this.idEnc = idEnc;
        this.idProdAdd = 0;
        this.qntProdAdd = 0;
        this.remProd = new ArrayList<>();
        this.possibleTimestamps = new ArrayList<>();
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

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
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

    public List<Integer> getPossibleTimestamps() { return this.possibleTimestamps;}


    public void newTime() throws ParseException {
        Date date2 = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateEnd = dateFormat.parse(this.end);

        long diff =Math.abs(dateEnd.toInstant().until(date2.toInstant(), ChronoUnit.MILLIS));

        this.end=dateFormat.format(date2.getTime() + diff);
    }

    public void newTime(long timeout) throws ParseException {
        Date date2 = new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateEnd = dateFormat.parse(this.end);

        long diff =Math.abs(dateEnd.toInstant().until(date2.toInstant(), ChronoUnit.MILLIS));

        this.end=dateFormat.format(date2.getTime() + timeout);
    }




}
