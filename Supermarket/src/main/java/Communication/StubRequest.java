package Communication;

public interface StubRequest<T> {
    void handleRequest(T o);
    void handleResponse(T o);
    void setState(StateUpdate o);
}
