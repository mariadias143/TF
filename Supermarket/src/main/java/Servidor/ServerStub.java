package Servidor;

import Communication.AggrementMiddleware.ServerGroupCom;
import io.atomix.utils.serializer.Serializer;
import Communication.*;

public class ServerStub implements StubRequest<Mensagem> {
    private int id;
    private Catalogo catalogo;
    private ServerGroupCom group_com;
    private int i = 0;

    public ServerStub(int id, Catalogo c, Serializer s) throws Exception {
        this.id = id;
        this.catalogo = c;
        this.group_com = new ServerGroupCom(s,this);
    }

    @Override
    public void handleResponse(Mensagem o) {
    }

    @Override
    public void handleRequest(Mensagem o) {
        System.out.println("Recebi");
        inc();
    }

    @Override
    public void setState(StateUpdate o) {
        this.i = o.getI();
        System.out.println("SeS: " + i);
    }

    public void inc(){
        i++;
        StateUpdate state = new StateUpdate(i);
        Mensagem<Integer> mensagem = new Mensagem<>("", "Test", 54);
        Mensagem<StateUpdate> mstate = new Mensagem<>("","State",state);
        this.group_com.multicast(mensagem,mstate);
    }
}
