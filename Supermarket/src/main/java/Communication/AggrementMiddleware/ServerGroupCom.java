package Communication.AggrementMiddleware;

import Communication.Mensagem;
import Communication.StubRequest;
import io.atomix.utils.serializer.Serializer;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerGroupCom<T> {
    private SpreadConnection connection;
    private String privateName;
    private Serializer s;
    private Lock l;
    private ServerBroadCastListener listener;
    private int clock;
    private StubRequest stub;

    public ServerGroupCom(Serializer s, StubRequest stub){
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(100000);
        this.privateName =  "SerV" + randomInt;
        System.out.println(this.privateName);
        this.clock = 0;
        this.l = new ReentrantLock();
        this.connection = new SpreadConnection();
        this.s = s;
        this.stub = stub;
        this.listener = new ServerBroadCastListener<T>(stub,s,this.privateName,this);
        try{
            connection.connect(InetAddress.getByName("localhost"), 0,
                    privateName, false, true);
            SpreadGroup group = new SpreadGroup();
            group.join(connection,"Servidores");
            //connection.add()
            connection.add(listener);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void state_request(Mensagem m){
        m.setPrivateSender(this.privateName);
        SpreadMessage message = new SpreadMessage();
        message.setSafe();
        message.addGroup("Servidores");
        message.setData(this.s.encode(m));

        try{
            //add ao listener para acks
            connection.multicast(message);
        }
        catch (SpreadException e){
            e.printStackTrace();
        }
    }

    public int multicast(Mensagem m,Mensagem state){
        m.setPrivateSender(this.privateName);
        state.setPrivateSender(this.privateName);
        int id;
        try{
            l.lock();
            id = ++this.clock;
            m.setClock(clock);
            state.setClock(clock);
        }
        finally {
            l.unlock();
        }
        SpreadMessage message = new SpreadMessage();
        message.setSafe();
        message.addGroup("Servidores");
        message.setData(this.s.encode(state));

        try{
            //add ao listener para acks
            this.listener.addMessage(m);
            connection.multicast(message);
        }
        catch (SpreadException e){
            e.printStackTrace();
        }
        return id;
    }

    public void send(Mensagem m){
        SpreadMessage message = new SpreadMessage();
        message.setReliable();
        message.addGroup(m.clientIP);
        message.setData(this.s.encode(m));
        try{
            connection.multicast(message);
        }
        catch (SpreadException e){
            e.printStackTrace();
        }
    }

    public void sendMessage(Mensagem m, SpreadGroup sender){
        SpreadMessage message = new SpreadMessage();
        message.setReliable();
        message.addGroup(sender);
        message.setData(this.s.encode(m));

        try{
            connection.multicast(message);
        }
        catch (SpreadException e){
            e.printStackTrace();
        }
    }

    public void updateClock(int clock){
        try{
            l.lock();
            this.clock = clock;
        }
        finally {
            l.unlock();
        }
    }

    public void updateLeader(){
        try{
            l.lock();
            ClientServerListener c_listener = new ClientServerListener(this.stub,this.s);
            SpreadGroup group = new SpreadGroup();
            group.join(connection,"Clientes");
            connection.add(c_listener);
            stub.notifyLeader();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            l.unlock();
        }
    }
}
