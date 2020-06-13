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

import javax.print.DocFlavor;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;


public class ServerTestPrototypeDAO implements StubRequest<StateUpdate>  {
    private ServerGroupCom com;
    private int timestamp = 0;
    private ProdutoDAO inventory;
    private StateUpdateDAO transactions;
    private EncomendaDAO orders;
    private int order_id = 0;
    private ExecutorService pool; //pool para os pedidos
    private ExecutorService leaderTask; //pool para as task do leader
    private Lock l;
    private boolean transaction_ongoing; //bool para verificar se há uma transação me curso
    private Condition transactionNotify;
    private boolean isLeader; //flag que diz se é leader
    private boolean ready; //flag que diz que se a reposição de estado já aconteceu
    private Map<Integer,OrderTimeout> timeouts;

    public ServerTestPrototypeDAO(Serializer s,String name){
        this.inventory = new ProdutoDAO(name);
        this.transactions = new StateUpdateDAO(name);
        this.orders = new EncomendaDAO(name);
        int lastStamp = transactions.lastTimestamp();
        int lastOrder = orders.lastId();
        this.l = new ReentrantLock();
        this.transactionNotify = this.l.newCondition();
        this.transaction_ongoing = false;
        this.isLeader = false;
        this.ready = false;

        this.timestamp = lastStamp == -1 ? 0 : lastStamp + 1;
        this.order_id = lastOrder == -1 ? 0 : lastOrder + 1;

        this.timeouts = new HashMap<>();
        this.leaderTask = Executors.newSingleThreadScheduledExecutor();
        this.pool = Executors.newFixedThreadPool(5);
        this.com = new ServerGroupCom<StateUpdate>(s,this);
    }

    @Override
    public void askState() {
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            try{
                l.lock();
                System.out.println("Preciso do estado atualizado: ");
                StateUpdate st = new StateUpdate(timestamp,new ArrayList<>());
                Mensagem<StateUpdate> rstate = new Mensagem<>("","ASKSTATE",st);
                rstate.setClockStub(timestamp);
                this.com.state_request(rstate);
            }
            finally {
                l.unlock();
            }
        },this.pool);
    }

    @Override
    public void setStates(List<StateUpdate> oldEvents,List<GenericPair<StateUpdate,Mensagem>> queuedEvents){
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            try{
                l.lock();
                int max_timestamp = oldEvents.stream().mapToInt(a -> a.getTimestamp()).max().orElse(0);
                if(max_timestamp != 0){
                    this.timestamp = max_timestamp + 1;
                }

                for(StateUpdate update : oldEvents){
                    switch (update.getType()) {
                        case 0://criar enc
                            this.order_id = Math.max(this.order_id, update.getIdEnc() + 1);
                            String end = (update.getEnd());
                            try {
                                update.newTime();

                                // FIM-ATUAL = TEMPO QUE FALTA
                                // Momento + Tempo que falta = FIM
                                // ALTERAR NO STATE UPDATE AS VARIAVEIS DE TEMPO NO sTATE UPDATDE
                                System.out.println("SetStates Old: " + update.getEnd());
                                orders.put(update.getIdEnc(), new Encomenda(update.getIdEnc(), update.getUserId(), update.getEnd()));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                        case 1://add prod
                            orders.addProduct(update.getIdEnc(), new Pair(update.getIdProdAdd(), update.getQntProdAdd()));
                            break;
                        case 2://sucesso da enc
                            for (Pair p : update.getRemProd()) {
                                this.inventory.updateStock(p.idProd, -p.qntProd);
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

                            String endDate = null;
                            try {
                                update.newTime(30*60*1000);
                                orders.put(update.getIdEnc(),new Encomenda(update.getIdEnc(),update.getUserId(),update.getEnd()));

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

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
                this.ready = true;
                this.transactionNotify.signalAll();
            }
            finally {
                l.unlock();
            }
        },this.pool);
    }

    @Override
    public void setState(StateUpdate update,Mensagem m,SpreadGroup dest) {
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            System.out.println("Update State");

            //garantir que já restaurou o estado.
            try{
                l.lock();
                while (!this.ready)
                    this.transactionNotify.signalAll();
            }
            finally {
                l.unlock();
            }
            int localTimestamp = update.getTimestamp()+1;
            int localOrder_id = -1;
            try{
                switch (update.getType()){
                    case 0://criar enc
                        localOrder_id = update.getIdEnc()+1;
                        update.newTime(60*1000);
                        orders.put(update.getIdEnc(),new Encomenda(update.getIdEnc(),update.getUserId(),update.getEnd()));
                        System.out.println("SetState: " + update.getEnd());

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
            }
            catch (Exception e){
                e.printStackTrace();
            }

            try{
                l.lock();
                this.timestamp = Math.max(this.timestamp,localTimestamp);
                if (localOrder_id != -1){
                    order_id = Math.max(localOrder_id,order_id);
                }
                if (update.getType() == 0 && this.isLeader){
                    this.timeouts.put(update.getIdEnc(),new OrderTimeout(update.getIdEnc(),this,update.getEnd()));
                }
                System.out.println("Timestamp: " + this.timestamp);
            }
            catch (Exception e){
                e.printStackTrace();
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
                if (o.clientIP != null)
                    this.com.send(o);

                this.transaction_ongoing = false;
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
    public void transferState(StateUpdate st, SpreadGroup sender) {
        CompletableFuture f = CompletableFuture.runAsync(() ->  {
            try {
                l.lock();
                internalTransfer(st.getTimestamp(),sender);
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
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                u.setActual(dateFormat.format(new Date()));
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

            while(this.transaction_ongoing == true)
                transactionNotify.await();
            this.transaction_ongoing = true;

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
            while(this.transaction_ongoing == true)
                transactionNotify.await();
            this.transaction_ongoing = true;

            Mensagem<Triple> msg = m;
            int idEnc = (Integer) msg.info.fst;
            int idProd = (Integer) msg.info.snd;
            int quantidade = (Integer) msg.info.rd;

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
                    timestamp++;
                    this.com.multicast(resp, mstate);
                } else {
                    Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"AdicionarResp",false);
                    resp.setResult(3);
                    resp.setClientIP(m.clientIP);
                    this.com.send(resp);
                    this.transaction_ongoing = false;
                    this.transactionNotify.signalAll();
                }
            }
            else{
                //error, não encontrou o produto
                Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"AdicionarResp",false);
                resp.setResult(2);
                resp.setClientIP(m.clientIP);
                this.com.send(resp);
                this.transaction_ongoing = false;
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
            while(this.transaction_ongoing == true)
                transactionNotify.await();
            this.transaction_ongoing = true;

            Mensagem<Integer> msg = m;
            int idEnc = msg.info;

            if (orders.containsKey(idEnc)){
                Encomenda e = this.orders.get(idEnc);
                boolean flag = true;
                for(Pair val : e.getProds().values()){
                    Produto p = this.inventory.get(val.idProd);
                    if (p.getStock() < val.qntProd){
                        flag = false;
                        break;
                    }
                }

                //cancelar o timeout
                OrderTimeout time = this.timeouts.get(idEnc);
                if(time != null){
                    time.delete();
                    this.timeouts.remove(idEnc);
                }

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
            }
            else{
                //não existe a encomenda
                Mensagem<Boolean> resp = new Mensagem<>(m.idClient,"FinalizarResp",false);
                resp.setClientIP(m.clientIP);
                resp.setResult(2);
                this.com.send(resp);
                this.transaction_ongoing = false;
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

    @Override
    public void notifyLeader() {
        this.leaderTask.submit(() -> {
            try{
                l.lock();
                this.isLeader = true;
                this.ready = true;
                this.transactionNotify.signalAll();

                while(transaction_ongoing)
                    this.transactionNotify.await();

                for(Encomenda e : this.orders.values()){
                    timeouts.put(e.getIdEnc(),new OrderTimeout(e.getIdEnc(),this,e.getEnd()));
                }

                transaction_ongoing = false;
                this.transactionNotify.signalAll();
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                l.unlock();
            }
        });
    }

    public void timeout(int idEnc){
        CompletableFuture f = CompletableFuture.runAsync(() -> {
            try{
                l.lock();
                while(transaction_ongoing)
                    this.transactionNotify.await();

                transaction_ongoing = true;

                if (orders.containsKey(idEnc)){
                    this.timeouts.remove(idEnc);

                    Mensagem<Boolean> resp = new Mensagem<>("","ServFinEnc",false);
                    resp.setClientIP(null);
                    resp.setClockStub(timestamp);
                    StateUpdate st1 = StateUpdate.finishEncServer(this.timestamp,idEnc);
                    Mensagem<StateUpdate> mstate = new Mensagem<>("","STATEU",st1);
                    mstate.setClockStub(timestamp);
                    timestamp++;
                    this.com.multicast(resp,mstate);
                }
                else{
                    System.out.println("Order not found");
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                l.unlock();
            }

        },this.leaderTask);
    }
}
