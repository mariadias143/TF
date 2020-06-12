package Servidor;

import Cliente.Triple;
import Communication.AggrementMiddleware.ServerGroupCom;
import Communication.Mensagem;
import Communication.StateUpdate;
import Communication.StubRequest;
import Servidor.Database.Encomenda;
import Servidor.Database.Pair;
import Servidor.Database.Produto;
import io.atomix.utils.serializer.Serializer;
import spread.SpreadGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
public class ServerTestPrototype implements StubRequest<Mensagem>  {
    private ServerGroupCom com;
    private int timestamp = 0;
    private Map<Integer,Produto> inventory;
    private Map<Integer, StateUpdate> transactions;
    private Map<Integer, Encomenda> orders;
    private int order_id = 0;

    public ServerTestPrototype(Serializer s){
        this.com = new ServerGroupCom(s,this);
        this.inventory = new HashMap<>();
        this.transactions = new HashMap<>();
        this.orders = new HashMap<>();

        this.inventory.put(1,new Produto(1,"Leite",(float) 4.0,5));
        this.inventory.put(2,new Produto(2,"Massa",(float) 2.0,10));
    }

    @Override
    public synchronized void askState() {
        System.out.println("Preciso do estado atualizado");
        Mensagem<StateUpdate> rstate = new Mensagem<>("","ASKSTATE",null);
        rstate.setClockStub(timestamp);
        this.com.state_request(rstate);
    }

    @Override
    public synchronized void setStates(List<StateUpdate> o) {
        int max_timestamp = o.stream().mapToInt(a -> a.getTimestamp()).max().orElse(0);
        if(max_timestamp != 0){
            this.timestamp = max_timestamp + 1;
        }

        for(StateUpdate update : o){
            switch (update.getType()){
                case 0://criar enc
                    orders.put(update.getIdEnc(),new Encomenda(update.getIdEnc(),update.getUserId(),0));
                    break;
                case 1://add prod
                    Encomenda e = orders.get(update.getIdEnc());
                    e.addProd(update.getIdProdAdd(),update.getQntProdAdd());
                    break;
                case 2://sucesso da enc
                    for(Pair p : update.getRemProd()){
                        Produto aux = this.inventory.get(p.idProd);
                        aux.updateStock(-p.qntProd);
                    }
                    this.orders.remove(update.getIdEnc());
                    break;
                case 3: //failure do client
                    this.orders.remove(update.getIdEnc());
                    break;
                case 4:
                    this.orders.remove(update.getIdEnc());
                    break;
                default: {
                    System.out.println("Error");
                }
            }
            this.transactions.put(update.getTimestamp(),update);
        }
    }

    @Override
    public synchronized void setState(StateUpdate update) {
        System.out.println("Update State");
        this.timestamp = Math.max(this.timestamp,update.getTimestamp());
        switch (update.getType()){
            case 0://criar enc
                order_id = Math.max(update.getIdEnc()+1,order_id);
                orders.put(update.getIdEnc(),new Encomenda(update.getIdEnc(),update.getUserId(),0));
                break;
            case 1://add prod
                Encomenda e = orders.get(update.getIdEnc());
                e.addProd(update.getIdProdAdd(),update.getQntProdAdd());
                break;
            case 2://sucesso da enc
                for(Pair p : update.getRemProd()){
                    Produto aux = this.inventory.get(p.idProd);
                    aux.updateStock(-p.qntProd);
                }
                this.orders.remove(update.getIdEnc());
                break;
            case 3: //failure do client
                this.orders.remove(update.getIdEnc());
                break;
            case 4:
                this.orders.remove(update.getIdEnc());
                break;
            default: {
                System.out.println("Error");
            }
        }
        this.transactions.put(update.getTimestamp(),update);
    }

    @Override
    public void handleResponse(Mensagem o) {
        System.out.println("Ping");
        this.com.send(o);
    }

    @Override
    public void handleRequest(Mensagem o) {
        switch (o.type){
            case "Iniciar":
                iniciarEnc(o);
                System.out.println("Recebi Iniciar");
                break;
            case "Consultar":
                consultaProd(o);
                System.out.println("Recebi Consultar");
                break;
            case "Adicionar":
                addProduto(o);
                System.out.println("Recebi Adicionar");
                break;
            case "Finalizar":
                System.out.println("Recebi Finalizar");
                break;
        }
    }

    @Override
    public void transferState(int timestamp, SpreadGroup sender) {
        List<StateUpdate> lst = new ArrayList<>();
        while (timestamp < this.timestamp){
            StateUpdate u = transactions.get(timestamp);
            if (u != null){
                lst.add(u);
            }
            timestamp++;
        }
        Mensagem<List<StateUpdate>> m = new Mensagem<>("","STATE",lst);
        m.setClockStub(timestamp);
        this.com.sendMessage(m,sender);
    }

    private synchronized void iniciarEnc(Mensagem m){
        String idClient = m.idClient;
        int encId = this.order_id++;
        Mensagem<Integer> msg = new Mensagem<>(idClient,"IniciarResp",encId);
        msg.setClientIP(m.clientIP);
        StateUpdate n_status = new StateUpdate(this.timestamp,idClient,encId);
        msg.setClockStub(timestamp);
        Mensagem<StateUpdate> mstate = new Mensagem<>("","STATEU",n_status);
        mstate.setClockStub(timestamp);
        timestamp++;
        this.com.multicast(msg,mstate);
    }

    private synchronized void consultaProd(Mensagem m){
        Mensagem<Integer> n_message = m;
        Produto p = this.inventory.get(m.info);

        Mensagem<Produto> resp = new Mensagem<>(m.idClient,"ConsultarResp",p);
        resp.setClientIP(m.clientIP);
        this.com.send(resp);
    }

    private synchronized void addProduto(Mensagem m){
        Mensagem<Triple> msg = m;
        int idEnc = msg.info.fst;
        int idProd = msg.info.snd;
        int quantidade = msg.info.rd;

        if (orders.containsKey(idEnc)){
            Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"AdicionarResp",true);
            resp.setClientIP(m.clientIP);
            resp.setClockStub(timestamp);
            StateUpdate n_status = new StateUpdate(this.timestamp,idEnc,idProd,quantidade);
            Mensagem<StateUpdate> mstate = new Mensagem<>("","STATEU",n_status);
            mstate.setClockStub(timestamp);
            timestamp++;
            this.com.multicast(resp,mstate);
        }
        else{
            Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"AdicionarResp",false);
            resp.setClientIP(m.clientIP);
            this.com.send(resp);
        }
    }
}
*/