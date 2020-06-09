package Cliente;

import Communication.ClientNetwork.ClientCom;
import Communication.Mensagem;
import Communication.StubResponse;
import io.atomix.utils.serializer.Serializer;

public class ClientTestStub implements StubResponse<Mensagem> {
    private ClientCom com;

    public ClientTestStub(Serializer s){
        this.com = new ClientCom(s,this);
    }

    @Override
    public void handleResponse(Mensagem o) {
        System.out.println("Recebi");
    }

    public void inc(){
        Mensagem<Integer> m = new Mensagem<>("", "TEST", 1);
        com.sendMessage(m);
    }
}
