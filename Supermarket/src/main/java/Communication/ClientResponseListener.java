package Communication;

import Cliente.ClienteStub;
import io.atomix.utils.serializer.Serializer;
import spread.BasicMessageListener;
import spread.SpreadMessage;

public class ClientResponseListener implements BasicMessageListener {
    private ClienteStub st;
    private Serializer s;

    public  ClientResponseListener(ClienteStub st,Serializer s){
        this.st = st;
        this.s = s;
    }

    public void messageReceived(SpreadMessage spreadMessage) {
        if (spreadMessage.isRegular() && spreadMessage.isReliable()){
            Mensagem m = this.s.decode(spreadMessage.getData());
            st.handleResponse(m);
        }
    }
}
