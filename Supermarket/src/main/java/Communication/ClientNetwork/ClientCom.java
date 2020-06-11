package Communication.ClientNetwork;

import Communication.Mensagem;
import Communication.StubResponse;
import io.atomix.utils.serializer.Serializer;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.net.InetAddress;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientCom {
    private SpreadConnection connection;
    private String privateName;
    private Serializer s;
    private Lock l;
    private StubResponse stub;
    private SpreadGroup primary_server;

    public ClientCom(Serializer s, StubResponse stub){
        Random randomGenerator = new Random();
        int randomInt = randomGenerator.nextInt(100000);
        this.privateName =  "CliC" + randomInt;
        this.l = new ReentrantLock();
        this.connection = new SpreadConnection();
        this.s = s;
        this.stub = stub;
        this.primary_server = null;

        ClientResponseListener listener = new ClientResponseListener(stub,s,this);

        try{
            connection.connect(InetAddress.getByName("localhost"), 0,
                    privateName, false, true);
            SpreadGroup group = new SpreadGroup();
            group.join(connection,"Clientes");
            //connection.add()
            connection.add(listener);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void nextLeader(SpreadGroup server){
        try{
            l.lock();
            this.primary_server = server;
        }
        finally {
            l.unlock();
        }
        System.out.println("New leader");
    }

    public void sendMessage(Mensagem m){
        SpreadMessage message = new SpreadMessage();
        message.setReliable();
        message.addGroup(this.primary_server);
        message.setData(this.s.encode(m));

        try{
            connection.multicast(message);
            System.out.println("Enviei");
        }
        catch (SpreadException e){
            e.printStackTrace();
        }
    }

}
