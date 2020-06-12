package Servidor;



import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

public class Testes {

    public static void main(String[] args) throws Exception{
        long Hour = 60*60*1000;
        long MINUTE = 60*1000;
        long Seconds = 1000;
        //String a ="2020-01-21 23:45:43";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //Date strDNow = dateFormat.parse(a);
        Date strDs = dateFormat.parse("2020-06-11 23:46:46");
        Date d1 = new Date();


        long diff =strDs.toInstant().until(d1.toInstant(),ChronoUnit.MILLIS);


        Date fin = new Date(d1.getTime()+diff);
        fin.toString();





        System.out.println(dateFormat.format(fin));
    }
}
