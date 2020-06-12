package Servidor.Database;

import static java.lang.Thread.sleep;

public class testDB {
    public static void main(String[] args) throws Exception{

        if (args.length == 0) {
            System.out.println("Error on args");
            return;
        }

        String name = args[0];
        Catalogo db = new Catalogo(name);
        db.connectDB();
        sleep(1000);
        System.out.println("--- PRODUTOS ---");
        db.printPRODUTOS();

        System.out.println("\n--- ENCOMENDAS ---");
        db.printENCOMENDAS();

        System.out.println("\n--- ENCOMENDA_PRODUTOS ---");
        db.printENCOMENDA_PRODUTOS();

        System.out.println("\n--- STATEUPDATE ---");
        db.printSTATEUPDATE();

        System.out.println("--- STATE_REMPROD ---");
        db.printSTATEREMPROD();

    }
}
