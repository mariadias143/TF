package Cliente;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Communication.Mensagem;
import Communication.StateUpdate;
import Servidor.Database.Pair;
import Servidor.Database.Produto;
import io.atomix.utils.serializer.Serializer;
import io.atomix.utils.serializer.SerializerBuilder;

public class Cliente {
    public String [] menus = {"1 - Iniciar compra","2 - Consultar produto","3 - Acrescentar produto","4 - Confirmar encomenda"};

    public void printMenu(){
        for (String s : menus) {
            System.out.println(s);
        }
    }

    public static void main(String[] args) throws Exception {
        Serializer s = new SerializerBuilder()
                .addType(Mensagem.class)
                .addType(List.class)
                .addType(ArrayList.class)
                .addType(Pair.class)
                .addType(Triple.class)
                .addType(Produto.class)
                .addType(StateUpdate.class)
                .build();

        ClienteStub stub = new ClienteStub(s);
        Cliente c = new Cliente();
        Scanner sca = new Scanner(System.in);

        while (true){
            c.printMenu();
            System.out.println("Opção:");
            String line = sca.nextLine();
            int option = Integer.parseInt(line);

            switch (option){
                case 1:
                    int id = stub.iniciar();
                    System.out.println("Compra iniciada id: " + id);
                    break;
                case 2:
                    System.out.println("Id do produto:");
                    line = sca.nextLine();
                    Produto result = stub.consultar(Integer.parseInt(line));
                    if (result != null){
                        System.out.println(result);
                    }
                    else {
                        stub.printError();
                    }
                    break;
                case 3:
                    System.out.println("Id da encomenda:");
                    String line3 = sca.nextLine();
                    System.out.println("Id do produto a acrescentar:");
                    line = sca.nextLine();
                    System.out.println("Quantidade a acrescentar em unidades:");
                    String line2 = sca.nextLine();
                    boolean result1 = stub.adicionarProduto(Integer.parseInt(line3),Integer.parseInt(line),Integer.parseInt(line2));
                    if (result1){
                        System.out.println("Produto adicionado");
                    }
                    else {
                        stub.printError();
                    }
                    // Em vez disto imprimir encomenda atual
                    break;
                case 4:
                    System.out.println("Id da encomenda a finalizar:");
                    line = sca.nextLine();
                    boolean res = stub.finalizarEncomenda(Integer.parseInt(line));
                    if(res)
                        System.out.println("Encomenda confirmada");
                    else stub.printError();
                    break;
            }
        }
    }
}
