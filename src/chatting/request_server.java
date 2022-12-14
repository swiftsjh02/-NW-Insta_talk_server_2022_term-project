package chatting;

import database.database;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class request_server implements Runnable {


    private static class ConnectThread extends Thread
    {
        ServerSocket serverSocket;
        int count = 1;

        ConnectThread (ServerSocket serverSocket) //생성자를 통해 서버소켓을 받음
        {
            System.out.println(" Server opened"); //서버가 열렸다는 메세지 출력
            this.serverSocket = serverSocket; //서버소켓을 저장
        }

        @Override
        public void run () {
            try {
                while (true){ //계속 새로운 클라이언트의 연결을 수락하고 새 소켓을 cLIENTtHREAD에 넘겨줌
                    Socket socket = serverSocket.accept();  //클라이언트의 연결을 수락
                    System.out.println("    Thread " + count + " is started.");
                    request serverThread = new request(socket, count);
                    serverThread.start(); //새로운 클라이언트의 연결을 수락하고 새 소켓을 cLIENTtHREAD에 넘겨줌
                    count++;
                }
            } catch (IOException e) {
                System.out.println(e);
                System.out.println("    SERVER CLOSE    ");
            }
        }
    }
    @Override
    public void run(){
        ServerSocket serverSocket = null;
        try {   // 서버소켓을 생성, 8080 포트와 binding
            serverSocket = new ServerSocket(9998); // 생성자 내부에 bind()가 있고, bind() 내부에 listen() 있음
            ConnectThread connectThread = new ConnectThread(serverSocket); // 서버소켓을 connectThread에 넘겨줌
            connectThread.start(); // connectThread 시작
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class request extends Thread {
        Socket socket;
        int id;
        database db = new database();


        //생성자를 통해 입력받은 소켓과 클라이언트(쓰레드)의 id를 저장
        request(Socket socket, int id) {
            this.socket = socket;
            this.id = id;
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                protocol content = null;
                content = (protocol) ois.readObject();
                if (content.getTypeofrequest() == 11) {
                    System.out.println(content.getSender() + "로 부터 방 목록 요청이 들어옴");
                    String sender = content.getSender();
                    ArrayList<String> response = db.get_users_room(db.get_user_id(sender));
                    for (int i = 0; i < response.size(); i++) {
                        System.out.println("방 목록 : " + response.get(i));
                    }

                    protocol tmp_content = new protocol(11, "server", response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();
                    System.out.println(content.getSender() + "에게 방 목록 전송");

                } else if (content.getTypeofrequest() == 12) {
                    System.out.println(content.getSender() + "로 부터 " + content.getRoomnumber() + "방안의 유저 목록 요청이 들어옴");
                    String room_id_tmp = content.getRoomnumber();
                    ArrayList<String> response = db.get_user_list_in_room(room_id_tmp);
                    for (int i = 0; i < response.size(); i++) {
                        System.out.println("방 안 유저  : " + response.get(i));
                    }
                    protocol tmp_content = new protocol(12, "server", response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();
                } else if (content.getTypeofrequest() == 15) {
                    System.out.println(content.getSender() + "로 부터 전체 유저 목록 요청이 들어옴");
                    ArrayList<String> response = db.get_all_user_id();
                    protocol tmp_content = new protocol(15, "server", response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();
                }
                else if (content.getTypeofrequest() == 8) {
                    System.out.println(content.getSender()+"로 부터 아이디 찾기 요청");
                    String response=db.get_email(content.getName(),content.getPhoneNum());
                    protocol tmp_content = new protocol(8, "server", response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();

                } else if (content.getTypeofrequest() == 20) {

                }else if(content.getTypeofrequest()==21){

                }else if(content.getTypeofrequest()==22){

                }else if(content.getTypeofrequest()==23){

                }
                else if(content.getTypeofrequest()==49){//좋아요 확인요청

                    
                }else if(content.getTypeofrequest()==50) {
                    System.out.println(content.getSender()+"로 부터 온라인 유저 목록 요청 받음");
                    ArrayList<String> response=db.get_all_online();
                    protocol tmp_content = new protocol(50, "server", response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();
                    System.out.println("온라인 유저 목록 반환 성공");
                }else if(content.getTypeofrequest()==9){
                    System.out.println(content.getEmail()+"로 부터 비밀번호 찾기 유효 확인 요청");
                    boolean response=db.get_password_chng_vaild(content.getEmail(),content.getName(),content.getPhoneNum());
                    protocol tmp_content = new protocol(9, response);
                    System.out.println(response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();
                }else if(content.getTypeofrequest()==10){
                    System.out.println(content.getSender()+"로 부터 비밀번호 바꾸기 요청");
                    boolean response=db.change_password(content.getSender(),content.getPassword());
                    protocol tmp_content = new protocol(10, response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();
                }else if(content.getTypeofrequest()==16){
                    System.out.println(content.getSender()+"로 부터 친구 추가 리스트 받음");
                    boolean response=db.friend_add_list(content.getSender(),content.getList());
                    protocol tmp_content = new protocol(16, response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();
                }else if(content.getTypeofrequest()==54){
                    System.out.println(content.getSender()+"로부터 자신의 친구 목록 요청");
                    ArrayList<String> response=db.get_friend_list(content.getSender());

                    protocol tmp_content = new protocol(54, "server", response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();
                }else if(content.getTypeofrequest()==55){
                    System.out.println(content.getSender()+"에서 탈퇴요청");
                    boolean response=db.delete_user(content.getSender());
                    protocol tmp_content = new protocol(55, response);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();
                }else if(content.getTypeofrequest()==51){
                    System.out.println(content.getSender()+"로 부터 내 정보 요청");
                    String name=db.get_name(content.getSender());
                    String phoneNum=db.get_phoneNum(content.getSender());
                    protocol tmp_content = new protocol(51,"server", name,phoneNum);
                    ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                    temp_oos.writeObject(tmp_content);
                    temp_oos.flush();

                }
                else {
                    System.out.println("잘못된 요청입니다.");
                }


            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }
}
