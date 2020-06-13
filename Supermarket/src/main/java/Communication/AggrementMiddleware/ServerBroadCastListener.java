package Communication.AggrementMiddleware;

import Communication.GenericPair;
import Communication.Mensagem;
import Communication.StateUpdate;
import Communication.StubRequest;
import io.atomix.utils.serializer.Serializer;
import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ServerBroadCastListener<T> implements AdvancedMessageListener {
    private StubRequest stub;
    private ServerGroupCom com;
    private Serializer s;
    private LeaderQueue queue;
    private String id;
    private boolean hasStarted;
    private Map<Integer,PendingMsg> pendingMsgMap;
    private Set<String> view;
    private boolean iamleader;
    private List<T> pendingUpdates;
    private List<GenericPair<T,Mensagem>> pendingACK;
    private Lock l;


    public ServerBroadCastListener(StubRequest stub,Serializer s,String id,ServerGroupCom com){
        this.stub = stub;
        this.s = s;
        this.id = id;
        this.com = com;
        this.hasStarted = false;
        this.queue = new LeaderQueue();
        this.pendingMsgMap = new HashMap<>();
        this.view = new HashSet<>();
        this.iamleader = false;
        this.pendingUpdates = new ArrayList<>();
        this.pendingACK = new ArrayList<>();
        this.l = new ReentrantLock();
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage){
        MembershipInfo info = spreadMessage.getMembershipInfo();

        //discard msg do grupo dos clientes
        if (!info.getGroup().toString().equals("Servidores"))
            return;

        if (info.isCausedByJoin() && this.queue.isEmpty()){
            String join_code = info.getJoined().toString().split("#")[1];
            for (SpreadGroup group : info.getMembers()) {
                String code = group.toString().split("#")[1];
                if (!join_code.equals(code))
                    this.queue.add(code);
            }
            this.queue.add(join_code);
            //sou o unico server
            if (info.getMembers().length == 1){
                try{
                    l.lock();
                    this.hasStarted = true;
                }
                finally {
                    l.unlock();
                }
            }
            else{
                stub.askState();
            }
        }
        else if (info.isCausedByDisconnect()){
            String name = info.getDisconnected().toString().split("#")[1];
            this.queue.remove(name);
        }
        else if (info.isCausedByLeave()){
            String name = info.getLeft().toString().split("#")[1];
            this.queue.remove(name);
        }

        //update view
        if (info.isCausedByJoin() | info.isCausedByLeave() | info.isCausedByDisconnect()){
            //atualizar as views das mensagens
            try{
                l.lock();
                if (this.iamleader == false && queue.isLeader(this.id)){
                    this.iamleader = true;
                    this.com.updateLeader();
                }
            }
            finally {
                l.unlock();
            }

            synchronized (this.pendingMsgMap){
                synchronized (this.view){
                    this.view = new HashSet<>();
                    for(SpreadGroup s : info.getMembers()){
                        String server_id = s.toString().split("#")[1];
                        this.view.add(server_id);
                    }
                    this.pendingMsgMap.values().forEach(a -> a.updateView(this.view));
                }
            }
        }

        if (info.isCausedByLeave() || info.isCausedByDisconnect()){
            String name;
            if (info.isCausedByLeave()){
                name = info.getLeft().toString().split("#")[1];
            }
            else{
                name = info.getDisconnected().toString().split("#")[1];
            }
            try{
                l.lock();
                List<Integer> clocks_to_rem = new ArrayList<>();
                if(this.iamleader){
                    synchronized (this.pendingMsgMap){
                        for(Map.Entry<Integer,PendingMsg> pair : pendingMsgMap.entrySet()){
                            PendingMsg msg = pair.getValue();
                            msg.remove(name);

                            if (msg.mayDeliver()){
                                //deliver ao stub
                                this.stub.handleResponse(msg.getMessage());
                                clocks_to_rem.add(pair.getKey());
                            }
                        }
                        clocks_to_rem.forEach(a -> this.pendingMsgMap.remove(a));
                    }
                }
            }
            finally {
                l.unlock();
            }
        }
    }

    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        Pattern p = Pattern.compile("#SerV*");
        Matcher mat = p.matcher(spreadMessage.getSender().toString());
        if (!mat.find()) {
            return;
        }


        if (spreadMessage.isRegular() && spreadMessage.isSafe()){
            //atualizar o estado
            String sender =  spreadMessage.getSender().toString().split("#")[1];
            Mensagem<StateUpdate> m = this.s.decode(spreadMessage.getData());
            //sou o leader e recebi o pedido
            if(queue.isLeader(this.id) && this.id.equals(sender) && m.type.equals("STATEU")){
                stub.setState(m.info,new Mensagem("ACK",m.clock,m.privateSender),spreadMessage.getSender());
                //this.deliverMessage(this.id,m.clock);
                //com.sendMessage(new Mensagem("ACK",m.clock,m.privateSender),spreadMessage.getSender());
            }
            //transferência de estado, guarda as mensagens
            else if (!queue.isLeader(this.id) && !this.id.equals(sender) && m.type.equals("STATEU")){
                boolean flag = false;
                try{
                    l.lock();
                    flag = this.hasStarted;
                    if(!flag){
                        //this.pendingUpdates.add((T)m.info);
                        this.pendingACK.add(new GenericPair<>((T)m.info,new Mensagem("ACK",m.clock,m.privateSender),spreadMessage.getSender()));
                        System.out.println("Estado pendente");
                    }
                }
                finally {
                    l.unlock();
                }

                if (flag){
                    stub.setState((T)m.info,new Mensagem("ACK",m.clock,m.privateSender),spreadMessage.getSender());
                }
                //com.sendMessage(new Mensagem("ACK",m.clock,m.privateSender),spreadMessage.getSender());
            }
            //pedido para enviar o estado
            else if (queue.isLeader(this.id) && !this.id.equals(sender) && m.type.equals("ASKSTATE")){
                System.out.println("Recebi pedido de copia do stado " + m.StubClock);
                stub.transferState((T)m.info,spreadMessage.getSender());
            }
        }
        else if (spreadMessage.isRegular() && spreadMessage.isReliable()){
            String sender =  spreadMessage.getSender().toString().split("#")[1];
            Mensagem m = this.s.decode(spreadMessage.getData());
            if(queue.isLeader(this.id) && m.privateSender.equals(this.id)){
                this.deliverMessage(sender,m.clock);
            }
            //transferência de estado replica a receber o pedido
            else if(m.type.equals("STATE")){
                System.out.println("Recebi estado");
                List<T> updates = (List<T>) m.info;
                try{
                    l.lock();
                    stub.setStates(updates,this.pendingACK.stream().collect(Collectors.toList()));
                    this.pendingACK = new ArrayList<>();
                    this.hasStarted = true;
                }
                finally {
                    l.unlock();
                }
            }
        }
    }

    public synchronized void addMessage(Mensagem m){
        pendingMsgMap.put(m.clock,new PendingMsg(m,this.id,this.view));
    }

    public void deliverMessage(String ack,int clock){
        synchronized (pendingMsgMap){
            PendingMsg msg = this.pendingMsgMap.get(clock);

            assert msg != null;
            msg.acknowledge(ack);
            if (msg.mayDeliver()){
                //deliver ao stub
                this.stub.handleResponse(msg.getMessage());
                pendingMsgMap.remove(clock);
            }
        }
    }
}
