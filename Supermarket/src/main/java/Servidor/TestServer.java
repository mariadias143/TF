package Servidor;

import Cliente.Triple;
import Communication.Mensagem;
import Communication.StateUpdate;
import Servidor.Database.Catalogo;
import Servidor.Database.Pair;
import Servidor.Database.Produto;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;

public class TestServer {
    public static void main(String[] args) throws Exception{

        if (args.length == 0) {
            System.out.println("Error on args");
            return;
        }

        String name = args[0];
        Catalogo db = new Catalogo(name);
        db.connectDB();
        sleep(1000);

        Serializer s = new SerializerBuilder()
                .addType(Mensagem.class)
                .addType(List.class)
                .addType(ArrayList.class)
                .addType(Pair.class)
                .addType(Triple.class)
                .addType(Produto.class)
                .addType(StateUpdate.class)
                .build();

        //ServerTestPrototypeDAO stub = new ServerTestPrototypeDAO(s,name);
        ServerConcorrencyDAOStub stub = new ServerConcorrencyDAOStub(s,name);

        CompletableFuture<Boolean> res = new CompletableFuture<Boolean>();
        res.get();
    }
}
