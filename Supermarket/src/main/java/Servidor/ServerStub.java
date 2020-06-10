package Servidor;

import Communication.AggrementMiddleware.ServerGroupCom;
import io.atomix.utils.serializer.Serializer;
import Communication.*;

public class ServerStub {
    private int id;
    private Catalogo catalogo;
    private ServerGroupCom group_com;
    private int i = 0;

    public ServerStub(int id, Catalogo c, Serializer s) throws Exception {
        this.id = id;
        this.catalogo = c;
    }
}
