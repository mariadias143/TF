package Communication;

import io.atomix.utils.serializer.Serializer;
import spread.AdvancedMessageListener;
import spread.MembershipInfo;
import spread.SpreadGroup;
import spread.SpreadMessage;

public class ServerBroadcastListener implements AdvancedMessageListener {
    //private ServerStub st;
    private Serializer s;
    private Boolean hasStarted;

    public void regularMessageReceived(SpreadMessage spreadMessage) {

    }

    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        MembershipInfo info = spreadMessage.getMembershipInfo();

        if (info.isCausedByJoin()){
            if (!hasStarted){
                int n_members = info.getMembers().length;

                //é o primeiro servidor a ligar?
                if (n_members == 1){
                    this.hasStarted = true;
                }
            }
            else{
                //já começou, envia estado para quem se ligou.
                SpreadGroup dest = spreadMessage.getSender();
                //st.stateTransfer(dest);
            }
        }
    }
}
