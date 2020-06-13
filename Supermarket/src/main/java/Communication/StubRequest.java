package Communication;

import spread.SpreadGroup;

import java.util.List;

public interface StubRequest<K> {
    void handleRequest(Mensagem o);
    void handleResponse(Mensagem o);
    void setState(K o,Mensagem m, SpreadGroup dest);
    void setStates(List<K> oldEvents,List<GenericPair<K,Mensagem>> queuedEvents);
    void transferState(K o, SpreadGroup sender);
    void askState();
    void notifyLeader();
    void timeout(int idEnc);
}
