package Communication;

import io.atomix.utils.net.Address;

import java.io.Serializable;

public class Mensagem<T>{
    public String idClient; // 0 id
    public String type;
    public String privateSender = "";
    public String clientIP = "";
    public int clock = 0;
    public T info;

    public Mensagem(String idClient, String type, T object){
        this.idClient = idClient;
        this.type = type;
        this.clock = 0;
        this.info = object;
    }

    public Mensagem(String type,int clock,String privateSender){
        this.idClient = "";
        this.type = type;
        this.clock = clock;
        this.privateSender = privateSender;
    }

    public void setPrivateSender(String id){
        this.privateSender = id;
    }

    public void setClientIP(String ip) { this.clientIP = ip; }

    public void setClock(int clock) {this.clock = clock;}
}
