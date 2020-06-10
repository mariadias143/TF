package Servidor;

import Communication.Mensagem;
import Communication.StateUpdate;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class TestServer {
    public static void main(String[] args) throws Exception{
        Serializer s = new SerializerBuilder()
                .addType(Mensagem.class)
                .addType(List.class)
                .addType(ArrayList.class)
                .addType(Pair.class)
                .addType(StateUpdate.class)
                .build();
        ServerTestStub stub = new ServerTestStub(s);

        CompletableFuture<Boolean> res = new CompletableFuture<Boolean>();
        res.get();
    }
}
