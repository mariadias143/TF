package Communication;

import spread.SpreadGroup;

import java.util.List;

public interface StubRequest<T> {
    void handleRequest(T o);
    void handleResponse(T o);
    void setState(StateUpdate o);
    void setStates(List<StateUpdate> o);
    void transferState(int timestamp, SpreadGroup sender);
    void askState();
}
