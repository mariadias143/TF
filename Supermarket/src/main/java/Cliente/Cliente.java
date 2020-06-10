package Cliente;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Communication.Mensagem;
import Communication.StateUpdate;
import Servidor.Pair;
import Servidor.Produto;
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
                    System.out.println(result);
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
                        System.out.println("Error");
                    }
                    // Em vez disto imprimir encomenda atual
                    break;
                case 4:
                    boolean res = stub.finalizarEncomenda();
                    if(res)
                        System.out.println("Encomenda confirmada");
                    else System.out.println("Erro. Um ou mais produtos não estão disponíveis.");
                    break;
            }
        }
    }
}
