package Communication.AggrementMiddleware;

import Communication.Mensagem;
import Communication.StubRequest;
import io.atomix.utils.serializer.Serializer;
import spread.BasicMessageListener;
import spread.SpreadMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientServerListener implements BasicMessageListener {
    private StubRequest stub;
    private Serializer s;

    public ClientServerListener(StubRequest stub, Serializer s){
        this.stub = stub;
        this.s = s;
        System.out.println("Connectei");
    }

    @Override
    public void messageReceived(SpreadMessage spreadMessage) {
        Pattern p = Pattern.compile("#CliC*");
        Matcher mat = p.matcher(spreadMessage.getSender().toString());
        if (!mat.find()) {
            return;
        }

        if(spreadMessage.isRegular() && spreadMessage.isReliable()){
            String sender =  spreadMessage.getSender().toString();
            Mensagem m = this.s.decode(spreadMessage.getData());
            m.setClientIP(sender);
            stub.handleRequest(m);
        }
    }
}
