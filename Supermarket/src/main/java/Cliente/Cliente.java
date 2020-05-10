package Cliente;

import java.util.Scanner;
import Servidor.Produto;

public class Cliente {
    public String [] menus = {"1 - Iniciar compra","2 - Consultar produto","3 - Acrescentar produto","4 - Confirmar encomenda"};

    public void printMenu(){
        for (String s : menus) {
            System.out.println(s);
        }
    }

    public Cliente(){
    }

    public static void main(String[] args) throws Exception {
        ClienteStub stub = new ClienteStub();
        Cliente c = new Cliente();
        Scanner sca = new Scanner(System.in);
        while (true){
            c.printMenu();
            System.out.println("Opção:");
            String line = sca.nextLine();
            int option = Integer.parseInt(line);

            switch (option){
                case 1:
                    stub.iniciar();
                    System.out.println("Compra iniciada");
                    break;
                case 2:
                    System.out.println("Nome do produto:");
                    line = sca.nextLine();
                    Produto result = stub.consultar(line);
                    System.out.println(result);
                    break;
                case 3:
                    System.out.println("Id do produto a acrescentar:");
                    line = sca.nextLine();
                    boolean result1 = stub.adicionarProduto(Integer.parseInt(line));
                    System.out.println("Produto adicionado"); // Em vez disto imprimir encomenda atual
                    break;
                case 4:
                    int res = stub.finalizarEncomenda();
                    if(res != 0)
                        System.out.println("Encomenda confirmada");
                    else System.out.println("Erro. Um ou mais produtos não estão disponíveis.");
                    break;
            }
        }
    }
}
