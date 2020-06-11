package Servidor;

import Cliente.Triple;
import Communication.Mensagem;
import Communication.StateUpdate;
import Servidor.Database.Pair;
import Servidor.Database.Produto;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TestServer {
    public static void main(String[] args) throws Exception{
        Serializer s = new SerializerBuilder()
                .addType(Mensagem.class)
                .addType(List.class)
                .addType(ArrayList.class)
                .addType(Pair.class)
                .addType(Triple.class)
                .addType(Produto.class)
                .addType(StateUpdate.class)
                .build();

        ServerTestPrototype stub = new ServerTestPrototype(s);

        CompletableFuture<Boolean> res = new CompletableFuture<Boolean>();
        res.get();
    }
}
