package Communication;

import java.io.Serializable;

public class Mensagem<T> implements Serializable  {
    public int id;
    public String type;
    public T info;

    public Mensagem(int id, String type, T object){
        this.id = id;
        this.type = type;
        this.info = object;
    }
}
