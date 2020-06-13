package Cliente;
import Communication.ClientNetwork.ClientCom;
import Servidor.Database.Produto;
import Communication.*;
import io.atomix.utils.serializer.Serializer;


import java.util.concurrent.CompletableFuture;

public class ClienteStub implements StubResponse<Mensagem> {
    //private int idCliente;
    private int n_pedido;
    private CompletableFuture result;
    private ClientCom com;
    private int error;

    public ClienteStub(Serializer s) throws Exception{
        this.n_pedido = 0;
        this.com = new ClientCom(s,this);
    }

    public int iniciar(){
        int iniciar = -1;
        this.n_pedido++;
        Mensagem<Integer> m = new Mensagem<>("","Iniciar",0);
        try{
            this.result = new CompletableFuture<Integer>();
            com.sendMessage(m);
            return (Integer) result.get();
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

    public boolean adicionarProduto(int idEnc,int idProd, int quantidade){
        boolean resposta = false;
        this.n_pedido++;
        Triple p = new Triple(idEnc,idProd,quantidade);
        Mensagem<Triple> m = new Mensagem<>("","Adicionar",p);
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

    public boolean finalizarEncomenda(int idEnc){
        boolean resposta = false;
        this.n_pedido++;
        Mensagem<Integer> m = new Mensagem<>("","Finalizar",idEnc);
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
        System.out.println("Recebi");
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
        Mensagem<Integer> r = (Mensagem<Integer>) response;
        if(response.idClient == ""){
            result.complete(r.info);
            this.error = r.result;
        }
    }

    private void handleConsultar(Mensagem response){
        Mensagem<Produto> r = (Mensagem<Produto>) response;
        if(response.idClient == ""){
            result.complete(r.info);
            this.error = r.result;
        }
    }

    private void handleAdicionar(Mensagem response){
        Mensagem<Boolean> r = (Mensagem<Boolean>) response;
        if(response.idClient == ""){
            result.complete(r.info);
            this.error = r.result;
        }
    }

    private void handleFinalizar(Mensagem response){
        Mensagem<Boolean> r = (Mensagem<Boolean>) response;
        if(response.idClient == ""){
            result.complete(r.info);
            this.error = r.result;
        }
    }

    public void printError(){
        switch (this.error){
            case 1:
                System.out.println("Erro na consulta do produto. Produto não existe.");
                break;
            case 2:
                System.out.println("Erro. Encomenda com o ID fornecido não existe ou a encomenda expirou.");
                break;
            case 3:
                System.out.println("Erro. ID do produto não existe.");
                break;
            case 4:
                System.out.println("Impossível finalizar encomenda. Stock indisponível.");
                break;
        }

    }
}
