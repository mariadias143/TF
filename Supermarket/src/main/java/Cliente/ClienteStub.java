package Cliente;

import Communication.Mensagem;
import Servidor.Produto;

public class ClienteStub {
    private int idCliente;


    public void iniciar(){

    }

    public Produto consultar(String nome){
        Produto p = null;
        return p;
    }

    public boolean adicionarProduto(int id){
        return true;
    }

    public int finalizarEncomenda(){
        return 0;
    }

    public void handleResponse(Mensagem response){
        switch (response.type){
            case "Resp1":

                break;
            case "Resp2":
                
                break;
        }
    }
}
