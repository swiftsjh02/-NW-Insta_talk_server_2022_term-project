package chatting;

import java.net.Socket;

public class connection {
    public String room_id;
    public Socket socket;
    public int user_id;

    //생성자
    public connection(String room_id,int user_id, Socket socket) {
        this.room_id = room_id;
        this.socket = socket;
        this.user_id = user_id;
    }


}