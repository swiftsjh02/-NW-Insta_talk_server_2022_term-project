package chatting;

import java.net.Socket;

public class online_user {

    public Socket socket;
    public int user_id;

    //생성자
    public online_user(int user_id, Socket socket) {
        this.socket = socket;
        this.user_id=user_id;
    }
}
