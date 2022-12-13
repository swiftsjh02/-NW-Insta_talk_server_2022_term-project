package chatting;
import database.*;

public class database_reset {
    public static void main(String[] args){
        database db=new database();
        System.out.println("data base reset");
        db.reset_db();
    }
}
