package chatting;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import database.*;

public class chating_server implements Runnable {

    public static ArrayList<connection> connection_list = new ArrayList<connection>();
    public static ArrayList<online_user> online_user_list =new ArrayList<>();
    public static database db=  new database();
    private static class ConnectThread extends Thread
    {
        ServerSocket serverSocket;
        int count = 1;

        //조태완이 참가한 채팅방의 고유번호(md5) 다 검색을 해 db에서
        //채팅방의 수만큼 소켓을 저장하는 배열을 만들어야해
        //채팅방 고유번호 리스트



        ConnectThread (ServerSocket serverSocket) //생성자를 통해 서버소켓을 받음
        {
            System.out.println(" Server opened"); //서버가 열렸다는 메세지 출력
            this.serverSocket = serverSocket; //서버소켓을 저장
        }



        @Override
        public void run ()
        {
            try
            {
                while (true) //계속 새로운 클라이언트의 연결을 수락하고 새 소켓을 cLIENTtHREAD에 넘겨줌
                {
                    Socket socket = serverSocket.accept();  //클라이언트의 연결을 수락
                    System.out.println("    Thread " + count + " is started.");
                    chat_unit serverThread = new chat_unit(socket, count);
                    serverThread.start(); //새로운 클라이언트의 연결을 수락하고 새 소켓을 cLIENTtHREAD에 넘겨줌
                    count++;
                }
            } catch (IOException e)
            {
                System.out.println(e);
                System.out.println("    SERVER CLOSE    ");
            }
        }
    }
    @Override
    public void run(){
        ServerSocket serverSocket = null;
        try
        {   // 서버소켓을 생성, 25588 포트와 binding
            serverSocket = new ServerSocket(25588); // 생성자 내부에 bind()가 있고, bind() 내부에 listen() 있음
            ConnectThread connectThread = new ConnectThread(serverSocket); // 서버소켓을 connectThread에 넘겨줌
            connectThread.start(); // connectThread 시작


        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }




    private static class chat_unit extends Thread
    {
        public Socket socket;
        public int id;


        //생성자를 통해 입력받은 소켓과 클라이언트(쓰레드)의 id를 저장
        chat_unit(Socket socket, int id)
        {
            this.socket = socket;
            this.id = id;
        }

        public boolean caching(protocol content){
            String room_id=content.getRoomnumber();
            String msg=content.getMessege();
            try {
                File file =new File("chatting_data/" + room_id + "/" + room_id + ".txt");
                FileWriter fw =new FileWriter(file,true);
                BufferedWriter bw= new BufferedWriter(fw);
                bw.append(content.getTime()+":"+ content.getSender()+":"+msg+":"+content.isFile_exist() + ":" + content.getFile_name()+":" +"\n");
                bw.close();
                return  true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }

        public String getServerDateTime(){
            String DateTime=null;
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss");
            DateTime = now.format(formatter);
            return DateTime;

        }



        @Override
        public void run () {
            protocol content = null;
            String user_id=null;

            try {
                InputStream is = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                DataInputStream dis = new DataInputStream(is);
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                user_id = br.readLine();
                user_id=user_id.trim();
                String room_id = null;

                //user_id와 소켓정보를 저장한 online_user개체를 online_user_list에 등록
                online_user_list.add(new online_user(db.get_user_id(user_id),socket));

                //user_id 이용해서 속해 있는 room_id를 db에서 찾은후 커넥션 리스트에 등록하기
                ArrayList<String> room_id_list = db.get_users_room(db.get_user_id(user_id));
                if (room_id_list.size() == 0) {
                    System.out.println(user_id+" 유저가 속한 채팅방이 없습니다.");
                } else {
                    for (int i = 0; i < room_id_list.size(); i++) {
                        room_id = room_id_list.get(i);
                        System.out.println(user_id + " 유저가 속한 방 : " + room_id);
                        connection tmp = new connection(room_id, db.get_user_id(user_id), socket);
                        if (connection_list.contains(tmp)) {

                        } else {
                            connection_list.add(tmp);
                        }

                    }
                }


                while ((content = (protocol) ois.readObject()) != null) {
                    System.out.println("커넥션 리스트를 출력합니다.");
                    for(int i=0; i<connection_list.size(); i++){
                        System.out.println("유저: "+db.get_user_id_as_String(connection_list.get(i).user_id)+" 방: "+connection_list.get(i).room_id);

                    }
                    if (content.getTypeofrequest() == 1) { //새 방 만들기 요청
                        String new_room_id=db.newroom(content);
                        if (new_room_id!=null) {
                            System.out.println("새 방 만들기 성공");
                            connection tmp = new connection(new_room_id, db.get_user_id(content.getSender()), socket);
                            connection_list.add(tmp);
                            for(int i=0; i<online_user_list.size(); i++){
                                for(int j=0; j<content.getList().size(); j++){
                                    if(online_user_list.get(i).user_id==db.get_user_id(content.getList().get(j))){
                                        connection A = new connection(new_room_id,online_user_list.get(i).user_id,online_user_list.get(i).socket);
                                        boolean isduplicated = false;
                                        for (int k = 0; k < connection_list.size(); k++) {
                                            connection t = connection_list.get(k);
                                            if (t.room_id.equals(A.room_id) && t.user_id == A.user_id) {
                                                System.out.println("이미 연결정보에 등록됨");
                                                isduplicated = true;
                                                break;
                                            }
                                        }
                                        if (isduplicated == false) {
                                            System.out.println("연결정보에 추가됨");
                                            connection_list.add(A);
                                        }else {

                                        }
                                    }
                                }
                            }
                        } else {
                            System.out.println("새 방 만들기 실패");
                        }

                    } else if (content.getTypeofrequest() == 2) { //방에 유저 초대
                        if(db.invite_user_to_room(db.get_user_id(content.getSender()),content.getRoomnumber(),content.getList())==true){
                            System.out.println(content.getSender()+"가 요청한 초대 기능이 정상 작동함");
                            for(int i=0; i<online_user_list.size(); i++){
                                for(int j=0; j<content.getList().size(); j++){
                                    if(online_user_list.get(i).user_id==db.get_user_id(content.getList().get(j))){
                                        connection A = new connection(content.getRoomnumber(),online_user_list.get(i).user_id,online_user_list.get(i).socket);
                                        boolean isduplicated = false;
                                        for (int k = 0; k < connection_list.size(); k++) {
                                            connection t = connection_list.get(k);
                                            if (t.room_id.equals(A.room_id) && t.user_id == A.user_id) {
                                                System.out.println("이미 연결정보에 등록됨");
                                                isduplicated = true;
                                                break;
                                            }
                                        }
                                        if (isduplicated == false) {
                                            System.out.println("연결정보에 추가됨");
                                            connection_list.add(A);
                                        }else {

                                        }
                                    }
                                }
                            }
                        }
                        else {
                            System.out.println("방 초대 실패");
                        }
                    } else if (content.getTypeofrequest() == 3) { //방에서 나가긴데
                        for (int i = 0; i < connection_list.size(); i++) {
                            if (connection_list.get(i).user_id == db.get_user_id(content.getSender()) && connection_list.get(i).room_id.equals(content.getRoomnumber())) {
                                connection_list.remove(i);
                                System.out.println(user_id + "가 방번호: " + content.getRoomnumber() + "의 커넥션리스트에서 제거됨");
                            }
                        }
                        if (db.exitroom(content.getRoomnumber(), db.get_user_id(content.getSender())) == true) {
                            System.out.println(content.getSender()+"가 "+content.getRoomnumber() + "에서 " + content.getSender() + "가 나갔습니다.");
                        }

                    } else if (content.getTypeofrequest() == 4) { //메시지 보내기
                        System.out.println(content.getSender()+"로 부터 메세지 보내기 요청이 들어옴");
                        System.out.println("내용:"+content.getMessege());
                        room_id = content.getRoomnumber();
                        if(caching(content)==true){

                        }else{
                            System.out.println("캐싱 실패");
                        }
                        connection tmp = new connection(room_id, db.get_user_id(user_id), socket);

                        boolean isduplicated = false;
                        for (int i = 0; i < connection_list.size(); i++) {
                            connection t = connection_list.get(i);
                            if (t.room_id.equals(tmp.room_id) && t.user_id == tmp.user_id) {
                                System.out.println("이미 연결정보에 등록됨");
                                isduplicated = true;
                                break;
                            }
                        }
                        if (isduplicated == false) {
                            System.out.println("연결정보에 추가됨");
                            connection_list.add(tmp);
                        }
                        for (int i = 0; i < connection_list.size(); i++) {
                            try{
                                if (connection_list.get(i).room_id.equals(room_id)) {
                                    System.out.println("room_id: "+room_id);
                                    System.out.println("방에 있는 사람들에게 메세지 전송");
                                    Socket temp_socket = connection_list.get(i).socket;
                                    ObjectOutputStream temp_oos = new ObjectOutputStream(temp_socket.getOutputStream());
                                    temp_oos.writeObject(content);
                                    temp_oos.flush();
                                }
                            }catch (Exception e){
                                continue;
                            }

                        }
                    } else if (content.getTypeofrequest() == 5) { //채팅서버 로그아웃요청
                        System.out.println(content.getSender() + "로 부터 로그아웃 요청이 들어옴");
                        for (int i = 0; i < connection_list.size(); i++) {
                            if (connection_list.get(i).user_id == db.get_user_id(content.getSender())) {
                                connection_list.remove(i);

                            }
                        }

                        for(int i=0; i<online_user_list.size(); i++){
                            if(online_user_list.get(i).user_id==db.get_user_id(content.getSender())){
                                online_user_list.remove(i);
                            }
                        }
                        System.out.println(user_id + "가 로그아웃 하였습니다.");
                        break;
                    }else if(content.getTypeofrequest()==10){


                    }
                    else if(content.getTypeofrequest()==11){
                        System.out.println(content.getSender()+"로 부터 방 목록 요청이 들어옴");
                        String sender = content.getSender();
                        ArrayList<String> response = db.get_users_room(db.get_user_id(sender));
                        for(int i=0; i<response.size(); i++){
                            System.out.println("방 목록 : "+response.get(i));
                        }

                        protocol tmp_content = new protocol(11,"server",response);
                        ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                        temp_oos.writeObject(tmp_content);
                        temp_oos.flush();
                        System.out.println(content.getSender()+"에게 방 목록 전송");

                    }else if(content.getTypeofrequest()==13){
                        System.out.println(content.getSender()+"로 부터 "+content.getRoomnumber()+"방안의 유저 목록 요청이 들어옴");
                        String room_id_tmp=content.getRoomnumber();
                        ArrayList<String> response = db.get_user_list_in_room(room_id_tmp);
                        for(int i=0; i<response.size(); i++){
                            System.out.println("방 안 유저  : "+response.get(i));
                        }
                        protocol tmp_content= new protocol(14,"server",response);
                        ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                        temp_oos.writeObject(tmp_content);
                        temp_oos.flush();
                    }else if(content.getTypeofrequest()==15) {
                        System.out.println(content.getSender() + "로 부터 전체 유저 목록 요청이 들어옴");
                        ArrayList<String> response = db.get_all_user_id();
                        protocol tmp_content = new protocol(16, "server", response);
                        ObjectOutputStream temp_oos = new ObjectOutputStream(socket.getOutputStream());
                        temp_oos.writeObject(tmp_content);
                        temp_oos.flush();
                    }
                    else {
                        System.out.println("잘못된 요청입니다.");
                    }


                }


                ois.close();
                socket.close();
            } catch (EOFException e) {
                System.out.println("[EOFException]");
                System.out.println(user_id + "로 부터 비정상적인 종료에 의한 로그아웃 요청이 들어옴");
                for (int i = 0; i < connection_list.size(); i++) {
                    if (connection_list.get(i).user_id == db.get_user_id(user_id)) {
                        connection_list.remove(i);

                    }
                }

                for(int i=0; i<online_user_list.size(); i++){
                    if(online_user_list.get(i).user_id==db.get_user_id(user_id)){
                        online_user_list.remove(i);
                    }
                }
                db.logout(db.get_user_id(user_id));

            }catch (SocketException e){
                System.out.println("[SocketException]");
                System.out.println(user_id + "로 부터 비정상적인 종료에 의한 로그아웃 요청이 들어옴");
                for (int i = 0; i < connection_list.size(); i++) {
                    if (connection_list.get(i).user_id == db.get_user_id(user_id)) {
                        connection_list.remove(i);

                    }
                }

                for(int i=0; i<online_user_list.size(); i++){
                    if(online_user_list.get(i).user_id==db.get_user_id(user_id)){
                        online_user_list.remove(i);
                    }
                }
                db.logout(db.get_user_id(user_id));


            }catch (Exception e) {
                e.printStackTrace();
                System.out.println(user_id + "로 부터 비정상적인 종료에 의한 로그아웃 요청이 들어옴");
                for (int i = 0; i < connection_list.size(); i++) {
                    if (connection_list.get(i).user_id == db.get_user_id(user_id)) {
                        connection_list.remove(i);

                    }
                }

                for(int i=0; i<online_user_list.size(); i++){
                    if(online_user_list.get(i).user_id==db.get_user_id(user_id)){
                        online_user_list.remove(i);
                    }
                }
                db.logout(db.get_user_id(user_id));
            }
        }
    }
}



//ClientList