package Servidor;

import Communication.AggrementMiddleware.ServerGroupCom;
import Communication.Mensagem;
import Communication.StateUpdate;
import Communication.StubRequest;
import io.atomix.utils.serializer.Serializer;

public class ServerTestStub implements StubRequest<Mensagem> {
    private ServerGroupCom com;
    private int i = 0;

    public ServerTestStub(Serializer s){
        this.com = new ServerGroupCom(s,this);
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
        this.com.multicast(mensagem,mstate);
    }
}
