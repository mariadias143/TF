package Communication.ClientNetwork;

import Cliente.ClienteStub;
import Communication.ClientNetwork.ClientCom;
import Communication.Mensagem;
import Communication.StubResponse;
import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientResponseListener implements AdvancedMessageListener {
    private StubResponse st;
    private Serializer s;
    private ClientCom com;
    private boolean hasStarted;

    public  ClientResponseListener(StubResponse st, Serializer s, ClientCom com){
        this.st = st;
        this.s = s;
        this.com = com;
        this.hasStarted = false;
    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        MembershipInfo info = spreadMessage.getMembershipInfo();

        if (info.isCausedByJoin()){
            Pattern p = Pattern.compile("#SerV*");
            if (!hasStarted){
                for(SpreadGroup gp : info.getMembers()){
                    String sv_code = gp.toString();
                    Matcher mat = p.matcher(sv_code);
                    if (mat.find()){
                        hasStarted = true;
                        com.nextLeader(gp);
                        break;
                    }
                }
            }
            else{
                String join_code = info.getJoined().toString();
                Matcher mat = p.matcher(join_code);
                if (mat.find()){
                    com.nextLeader(info.getJoined());
                }
            }
        }
    }

    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        if (spreadMessage.isRegular() && spreadMessage.isReliable()){
            Mensagem m = this.s.decode(spreadMessage.getData());
            st.handleResponse(m);
        }
    }
}
