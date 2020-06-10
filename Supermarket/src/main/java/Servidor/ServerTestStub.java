package Servidor;

import Communication.AggrementMiddleware.ServerGroupCom;
import Communication.Mensagem;
import Communication.StateUpdate;
import Communication.StubRequest;
import io.atomix.utils.serializer.Serializer;
import spread.SpreadGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerTestStub implements StubRequest<Mensagem> {
    private ServerGroupCom com;
    private int i = 0;
    private Map<Integer,StateUpdate> oldres;

    public ServerTestStub(Serializer s){
        this.com = new ServerGroupCom(s,this);
        this.oldres = new HashMap<>();
    }

    @Override
    public void handleResponse(Mensagem o) {
    }

    @Override
    public synchronized void handleRequest(Mensagem o) {
        System.out.println("Recebi");
        inc();
    }

    @Override
    public synchronized void setState(StateUpdate o) {
        this.i = Math.max(this.i,o.getI()+1);
        oldres.put(o.getI(),o);
        System.out.println("SeS: " + o.getI() + " : " + o.getStr());
        System.out.println("i:" + this.i);
    }

    @Override
    public synchronized void setStates(List<StateUpdate> o) {
        int max_timestamp = o.stream().mapToInt(a -> a.getI()).max().orElse(0);
        if(max_timestamp != 0){
            this.i = max_timestamp + 1;
        }
        for(StateUpdate t : o){
            oldres.put(t.getI(),t);
            System.out.println("SeS: " + t.getI() + " : " + t.getStr());
        }
    }

    @Override
    public synchronized void transferState(int timestamp, SpreadGroup sender) {
        List<StateUpdate> lst = new ArrayList<>();
        while (timestamp < i){
            lst.add(oldres.get(timestamp));
            timestamp++;
        }
        Mensagem<List<StateUpdate>> m = new Mensagem<>("","STATE",lst);
        m.setClockStub(timestamp);
        this.com.sendMessage(m,sender);
    }

    @Override
    public synchronized void askState() {
        System.out.println("Preciso do estado atualizado");
        Mensagem<StateUpdate> rstate = new Mensagem<>("","ASKSTATE",null);
        this.com.state_request(rstate,i);
    }

    public synchronized void inc(){
        StateUpdate state = new StateUpdate(i);
        Mensagem<Integer> mensagem = new Mensagem<>("", "Test", 54);
        mensagem.setClockStub(i);
        Mensagem<StateUpdate> mstate = new Mensagem<>("","STATEU",state);
        mensagem.setClockStub(i);
        i++;
        this.com.multicast(mensagem,mstate);
    }
}
