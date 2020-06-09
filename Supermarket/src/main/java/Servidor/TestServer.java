package Servidor;

import Communication.Mensagem;
import Communication.StateUpdate;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class TestServer {
    public static void main(String[] args) throws Exception{
        Serializer s = new SerializerBuilder()
                .addType(Mensagem.class)
                .addType(StateUpdate.class)
                .build();
        ServerTestStub stub = new ServerTestStub(s);

        if (args.length > 1) {
            Scanner sca = new Scanner(System.in);
            while (true) {
                System.out.println("Opção:");
                String line = sca.nextLine();
                stub.inc();
            }
        }
        else {
            CompletableFuture<Boolean> res = new CompletableFuture<Boolean>();
            res.get();
        }
    }
}
