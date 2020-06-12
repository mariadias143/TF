package Servidor;

import Cliente.Triple;
import Communication.AggrementMiddleware.ServerGroupCom;
import Communication.GenericPair;
import Communication.Mensagem;
import Communication.StateUpdate;
import Communication.StubRequest;
import Servidor.Database.DAOs.EncomendaDAO;
import Servidor.Database.DAOs.ProdutoDAO;
import Servidor.Database.DAOs.StateUpdateDAO;
import Servidor.Database.Encomenda;
import Servidor.Database.Pair;
import Servidor.Database.Produto;
import io.atomix.utils.serializer.Serializer;
import spread.SpreadGroup;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;


public class ServerConcorrencyTestDAO implements StubRequest<StateUpdate>  {
    private ServerGroupCom com;
    private int timestamp = 0;
    private ProdutoDAO inventory;
    private StateUpdateDAO transactions;
    private EncomendaDAO orders;
    private int order_id = 0;
    private ExecutorService pool;
    private Lock l;
    private boolean transaction_ongoing;
    private Condition transactionNotify;
    private Set<Integer> concurrency_orders;
    private Set<Integer> concurrency_products;
    private Map<Integer,GenericPair<Integer,List<Integer>>> running_transactions;

    public ServerConcorrencyTestDAO(Serializer s, String name){
        this.inventory = new ProdutoDAO(name);
        this.transactions = new StateUpdateDAO(name);
        this.orders = new EncomendaDAO(name);
        int lastStamp = transactions.lastTimestamp();
        int lastOrder = orders.lastId();
        this.l = new ReentrantLock();
        this.transactionNotify = this.l.newCondition();
        this.transaction_ongoing = false;
        concurrency_orders = new HashSet<>();
        concurrency_products = new HashSet<>();
        running_transactions = new HashMap<>();

        this.timestamp = lastStamp == -1 ? 0 : lastStamp + 1;
        this.order_id = lastOrder == -1 ? 0 : lastOrder + 1;

        this.pool = Executors.newFixedThreadPool(5);
        this.com = new ServerGroupCom<StateUpdate>(s,this);
    }

    @Override
    public void askState() {
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            try{
                l.lock();
                System.out.println("Preciso do estado atualizado: ");
                Mensagem<StateUpdate> rstate = new Mensagem<>("","ASKSTATE",null);
                rstate.setClockStub(timestamp);
                this.com.state_request(rstate);
            }
            finally {
                l.unlock();
            }
        },this.pool);
    }

    @Override
    public void setStates(List<StateUpdate> oldEvents,List<GenericPair<StateUpdate,Mensagem>> queuedEvents) {
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            int max_timestamp = oldEvents.stream().mapToInt(a -> a.getTimestamp()).max().orElse(0);
            if(max_timestamp != 0){
                this.timestamp = max_timestamp + 1;
            }

            for(StateUpdate update : oldEvents){
                switch (update.getType()){
                    case 0://criar enc
                        this.order_id = Math.max(this.order_id,update.getIdEnc() + 1);
                        orders.put(update.getIdEnc(),new Encomenda(update.getIdEnc(),update.getUserId(),0));
                        break;
                    case 1://add prod
                        orders.addProduct(update.getIdEnc(),new Pair(update.getIdProdAdd(),update.getQntProdAdd()));
                        break;
                    case 2://sucesso da enc
                        for(Pair p : update.getRemProd()){
                            this.inventory.updateStock(p.idProd,-p.qntProd);
                        }
                        this.orders.remove(update.getIdEnc());
                        break;
                    case 3: //failure do client
                        this.orders.remove(update.getIdEnc());
                        break;
                    case 4://failure do server
                        this.orders.remove(update.getIdEnc());
                        break;
                    default: {
                        System.out.println("Error");
                    }
                }
                this.transactions.put(update.getTimestamp(),update);
            }

            for(GenericPair<StateUpdate,Mensagem> aux: queuedEvents){
                StateUpdate update = aux.fst;
                this.timestamp = Math.max(this.timestamp,update.getTimestamp()+1);
                switch (update.getType()){
                    case 0://criar enc
                        this.order_id = Math.max(this.order_id,update.getIdEnc() + 1);
                        orders.put(update.getIdEnc(),new Encomenda(update.getIdEnc(),update.getUserId(),0));
                        break;
                    case 1://add prod
                        orders.addProduct(update.getIdEnc(),new Pair(update.getIdProdAdd(),update.getQntProdAdd()));
                        break;
                    case 2://sucesso da enc
                        for(Pair p : update.getRemProd()){
                            this.inventory.updateStock(p.idProd,-p.qntProd);
                        }
                        this.orders.remove(update.getIdEnc());
                        break;
                    case 3: //failure do client
                        this.orders.remove(update.getIdEnc());
                        break;
                    case 4://failure do server
                        this.orders.remove(update.getIdEnc());
                        break;
                    default: {
                        System.out.println("Error");
                    }
                }
                this.transactions.put(update.getTimestamp(),update);
                this.com.sendMessage(aux.snd,aux.destination);
            }

            System.out.println("My timestamp after recovery: " + this.timestamp);
        },this.pool);
    }

    @Override
    public void setState(StateUpdate update,Mensagem m,SpreadGroup dest) {
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            System.out.println("Update State");
            try{
                l.lock();
                this.timestamp = Math.max(this.timestamp,update.getTimestamp()+1);
                switch (update.getType()){
                    case 0://criar enc
                        order_id = Math.max(update.getIdEnc()+1,order_id);
                        orders.put(update.getIdEnc(),new Encomenda(update.getIdEnc(),update.getUserId(),0));
                        break;
                    case 1://add prod
                        orders.addProduct(update.getIdEnc(),new Pair(update.getIdProdAdd(),update.getQntProdAdd()));
                        break;
                    case 2://sucesso da enc
                        for(Pair p : update.getRemProd()){
                            this.inventory.updateStock(p.idProd,-p.qntProd);
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
                this.com.sendMessage(m,dest);
                System.out.println("Timestamp: " + this.timestamp);
            }
            finally {
                l.unlock();
            }

        },this.pool);
    }

    @Override
    public void handleResponse(Mensagem o) {
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            try{
                l.lock();
                System.out.println("Ping");
                this.com.send(o);

                GenericPair<Integer,List<Integer>> locks = this.running_transactions.get(o.StubClock);

                this.concurrency_orders.remove(locks.fst);
                if (locks.snd != null)
                    this.concurrency_products.removeAll(locks.snd);
                this.transactionNotify.signalAll();
            }
            finally {
                l.unlock();
            }
        },this.pool);
    }

    @Override
    public void handleRequest(Mensagem o) {
        CompletableFuture f = CompletableFuture.runAsync(() ->  {
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
                    finalizarEnc(o);
                    System.out.println("Recebi Finalizar");
                    break;
            }
        },this.pool);
    }

    @Override
    public void transferState(final int timestamp, SpreadGroup sender) {
        CompletableFuture f = CompletableFuture.runAsync(() ->  {
            try {
                l.lock();
                internalTransfer(timestamp,sender);
            }
            finally {
                l.unlock();
            }

        },this.pool);
    }

    private void internalTransfer(int timestamp, SpreadGroup sender){
        List<StateUpdate> lst = new ArrayList<>();
        int i = timestamp;
        System.out.println("Timestamp copy: " + i);
        while (i < this.timestamp){
            StateUpdate u = transactions.get(i);
            if (u != null){
                lst.add(u);
            }
            i++;
        }
        Mensagem<List<StateUpdate>> m = new Mensagem<>("","STATE",lst);
        m.setClockStub(i);
        this.com.sendMessage(m,sender);
    }

    private void iniciarEnc(Mensagem m){
        try{
            l.lock();

            String idClient = m.idClient;
            int encId = this.order_id++;
            Mensagem<Integer> msg = new Mensagem<>(idClient,"IniciarResp",encId);
            msg.setClientIP(m.clientIP);
            msg.setResult(0);
            StateUpdate n_status = new StateUpdate(this.timestamp,idClient,encId);
            msg.setClockStub(timestamp);
            Mensagem<StateUpdate> mstate = new Mensagem<>("","STATEU",n_status);
            mstate.setClockStub(timestamp);
            timestamp++;
            this.com.multicast(msg,mstate);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            l.unlock();
        }
    }

    private void consultaProd(Mensagem m){
        Mensagem<Integer> n_message = m;
        Produto p = this.inventory.get(m.info);

        Mensagem<Produto> resp = new Mensagem<>(m.idClient,"ConsultarResp",p);

        // erro, produto nao existe
        if(p==null)
            resp.setResult(1);
        else
            resp.setResult(0);

        resp.setClientIP(m.clientIP);
        this.com.send(resp);
    }

    private void addProduto(Mensagem m){
        try{
            l.lock();

            Mensagem<Triple> msg = m;
            int idEnc = (Integer) msg.info.fst;
            int idProd = (Integer) msg.info.snd;
            int quantidade = (Integer) msg.info.rd;

            //lock da encomenda
            while(this.concurrency_orders.contains(idEnc))
                this.transactionNotify.await();
            this.concurrency_orders.add(idEnc);

            Produto p = null;
            p = this.inventory.get(idProd);

            if (orders.containsKey(idEnc)){
                if(p != null) {

                    Mensagem<Boolean> resp = new Mensagem<>(m.idClient, "AdicionarResp", true);
                    resp.setClientIP(m.clientIP);
                    resp.setClockStub(timestamp);
                    resp.setResult(0);
                    StateUpdate n_status = new StateUpdate(this.timestamp, idEnc, idProd, quantidade);
                    Mensagem<StateUpdate> mstate = new Mensagem<>("", "STATEU", n_status);
                    mstate.setClockStub(timestamp);
                    this.running_transactions.put(timestamp,new GenericPair<>(idEnc,null));
                    timestamp++;
                    this.com.multicast(resp, mstate);
                } else {
                    Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"AdicionarResp",false);
                    resp.setResult(3);
                    resp.setClientIP(m.clientIP);
                    this.com.send(resp);
                    this.concurrency_orders.remove(idEnc); //unlock na encomenda
                    this.transactionNotify.signalAll();
                }
            }
            else{
                //error, não encontrou a encomenda
                Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"AdicionarResp",false);
                resp.setResult(2);
                resp.setClientIP(m.clientIP);
                this.com.send(resp);
                this.concurrency_orders.remove(idEnc); //unlock na encomenda
                this.transactionNotify.signalAll();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            l.unlock();
        }
    }

    private void finalizarEnc(Mensagem m){
        try{
            l.lock();

            Mensagem<Integer> msg = m;
            int idEnc = msg.info;

            //lock na encomenda
            while(this.concurrency_orders.contains(idEnc))
                this.transactionNotify.await();
            this.concurrency_orders.add(idEnc);

            if (orders.containsKey(idEnc)){
                Encomenda e = this.orders.get(idEnc);

                //lock nos produtos
                List<Integer> prodIds = e.getProds().values().stream().map(a -> a.idProd).collect(Collectors.toList());
                while (prodIds.stream().map(a -> this.concurrency_products.contains(a)).anyMatch(btrue -> btrue))
                    this.transactionNotify.await();

                this.concurrency_products.addAll(prodIds);

                boolean flag = true;
                for(Pair val : e.getProds().values()){
                    Produto p = this.inventory.get(val.idProd);
                    if (p.getStock() < val.qntProd){
                        flag = false;
                        break;
                    }
                }

                int localtimestamp = this.timestamp;
                //não tem stock, encomenda tem que ser cancelada
                if (flag == false){
                    Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"FinalizarResp",false);
                    resp.setClientIP(m.clientIP);
                    resp.setClockStub(timestamp);
                    resp.setResult(4);
                    StateUpdate st = StateUpdate.finishEncUserFail(this.timestamp,idEnc);
                    Mensagem<StateUpdate> mstate = new Mensagem<>("","STATEU",st);
                    mstate.setClockStub(timestamp);
                    timestamp++;
                    this.com.multicast(resp,mstate);
                }
                else{
                    Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"FinalizarResp",true);
                    resp.setClientIP(m.clientIP);
                    resp.setClockStub(timestamp);
                    resp.setResult(0);
                    StateUpdate st = new StateUpdate(timestamp,idEnc,e.getProds().values().stream().collect(Collectors.toList()));
                    Mensagem<StateUpdate> mstate = new Mensagem<>("","STATEU",st);
                    mstate.setClockStub(timestamp);
                    timestamp++;
                    this.com.multicast(resp,mstate);
                }

                this.running_transactions.put(localtimestamp,new GenericPair<>(idEnc,prodIds));
            }
            else{
                //não existe a encomenda
                Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"FinalizarResp",false);
                resp.setClientIP(m.clientIP);
                resp.setResult(2);
                this.com.send(resp);
                this.concurrency_orders.remove(idEnc);
                this.transactionNotify.signalAll();
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            l.unlock();
        }
    }
}
