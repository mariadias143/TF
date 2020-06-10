package Cliente;
import Communication.ClientNetwork.ClientCom;
import Servidor.Produto;
import Communication.*;
import io.atomix.utils.serializer.Serializer;
import javafx.util.Pair;

import java.util.concurrent.CompletableFuture;

public class ClienteStub implements StubResponse<Mensagem> {
    //private int idCliente;
    private int n_pedido;
    private CompletableFuture result;
    private ClientCom com;

    public ClienteStub(Serializer s) throws Exception{
        this.n_pedido = 0;
        this.com = new ClientCom(s,this);
    }

    public boolean iniciar(){
        boolean iniciar = false;
        this.n_pedido++;
        Mensagem<Integer> m = new Mensagem<>("","Iniciar",0);
        try{
            this.result = new CompletableFuture<Boolean>();
            com.sendMessage(m);
            return (Boolean) result.get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return iniciar;
    }

    public Produto consultar(int id){
        Produto p = null;
        this.n_pedido++;
        Mensagem<Integer> m = new Mensagem<>("","Consultar",id);
        try{
            this.result = new CompletableFuture<Produto>();
            com.sendMessage(m);
            return (Produto) result.get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return p;
    }

    public boolean adicionarProduto(int id, int quantidade){
        boolean resposta = false;
        this.n_pedido++;
        Pair p = new Pair <Integer,Integer> (id,quantidade);
        Mensagem<Pair<Integer,Integer>> m = new Mensagem<>("","Adicionar",p);
        try{
            this.result = new CompletableFuture<Boolean>();
            com.sendMessage(m);
            return (Boolean) result.get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return resposta;
    }

    public boolean finalizarEncomenda(){
        boolean resposta = false;
        this.n_pedido++;
        Mensagem<Integer> m = new Mensagem<>("","Finalizar",0);
        try{
            this.result = new CompletableFuture<Boolean>();
            com.sendMessage(m);
            return (Boolean) result.get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return resposta;
    }

    public void handleResponse(Mensagem response){
        switch (response.type){
            case "IniciarResp":
                handleIniciar(response);
                break;
            case "ConsultarResp":
                handleConsultar(response);
                break;
            case "AdicionarResp":
                handleAdicionar(response);
                break;
            case "FinalizarResp":
                handleFinalizar(response);
                break;
        }
    }

    private void handleIniciar(Mensagem response){
        Mensagem<Boolean> r = (Mensagem<Boolean>) response;
        if(response.idClient == ""){
            result.complete(response.info);
        }
    }

    private void handleConsultar(Mensagem response){
        Mensagem<Produto> r = (Mensagem<Produto>) response;
        if(response.idClient == ""){
            result.complete(response.info);
        }
    }

    private void handleAdicionar(Mensagem response){
        Mensagem<Boolean> r = (Mensagem<Boolean>) response;
        if(response.idClient == ""){
            result.complete(response.info);
        }
    }

    private void handleFinalizar(Mensagem response){
        Mensagem<Boolean> r = (Mensagem<Boolean>) response;
        if(response.idClient == ""){
            result.complete(response.info);
        }
    }
}
