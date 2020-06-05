package Communication.AggrementMiddleware;

import Communication.Mensagem;
import spread.SpreadGroup;

import java.util.HashSet;
import java.util.Set;

public class PendingMsg {
    private Mensagem m;
    private Set<String> acks;
    private boolean selfDeliver;
    private Set<String> expected_acks;
    private String idserver;

    public PendingMsg(Mensagem m,String idserver,Set<String> view){
        this.m = m;
        this.acks = new HashSet<>();
        this.selfDeliver = false;
        this.expected_acks = new HashSet<>(view);
        this.idserver = idserver;
    }

    public synchronized boolean mayDeliver(){
        return selfDeliver && acks.containsAll(expected_acks);
    }

    public synchronized void acknowledge(String serv){
        if (serv.equals(this.idserver)){
            this.selfDeliver = true;
        }
        acks.add(serv);
    }

    public synchronized Mensagem getMessage(){
        return m;
    }

    public synchronized void updateView(Set<String> servers){
        if(!this.selfDeliver){
            this.expected_acks = new HashSet<>();
            for(String s : servers){
                expected_acks.add(s);
            }
        }
    }
}
