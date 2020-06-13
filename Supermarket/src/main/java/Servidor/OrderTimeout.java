package Servidor;

import Communication.StubRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class OrderTimeout extends TimerTask {
    private StubRequest stub;
    private int idEnc;
    private Timer timer;

    public void run(){
        this.stub.timeout(idEnc);
    }

    public OrderTimeout(int idEnc,StubRequest stub,String DateString) throws Exception{
        this.stub = stub;
        this.idEnc = idEnc;

        this.timer = new Timer();

        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = dateFormatter.parse(DateString);

        timer.schedule(this,date);
    }

    public void delete(){
        timer.cancel();
    }
}
