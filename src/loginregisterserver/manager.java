package loginregisterserver;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import database.*;

public class manager implements Runnable {


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
        public void run ()
        {
            try
            {
                while (true) //계속 새로운 클라이언트의 연결을 수락하고 새 소켓을 cLIENTtHREAD에 넘겨줌
                {
                    Socket socket = serverSocket.accept();  //클라이언트의 연결을 수락
                    System.out.println("    Thread " + count + " is started.");
                    login_server_multithread serverThread = new login_server_multithread(socket, count);
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
    public void run() {
        ServerSocket serverSocket = null;
        try
        {   // 서버소켓을 생성, 9898 포트와 binding
            serverSocket = new ServerSocket(9898); // 생성자 내부에 bind()가 있고, bind() 내부에 listen() 있음
            ConnectThread connectThread = new ConnectThread(serverSocket); // 서버소켓을 connectThread에 넘겨줌
            connectThread.start(); // connectThread 시작


        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }




    private static class login_server_multithread extends Thread
    {
        Socket socket;
        int id;

        InputStream is =null;
        DataInputStream dis=null;
        DataOutputStream dos=null;

        BufferedReader br=null;

        OutputStream os =null;
        byte[] buf = null;


        //생성자를 통해 입력받은 소켓과 클라이언트(쓰레드)의 id를 저장
        login_server_multithread (Socket socket, int id)
        {
            this.socket = socket;
            this.id = id;
        }

        public String getServerDateTime(){
            String DateTime=null;
            LocalTime now = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH_mm_ss");
            DateTime = now.format(formatter);
            return DateTime;

        }



        @Override
        public void run ()
        {
            try {
                database db= new database();
                is = socket.getInputStream();
                os = socket.getOutputStream();
                dis= new DataInputStream(is);
                dos= new DataOutputStream(os);
                br = new BufferedReader(new InputStreamReader(is));

                String id=null;
                String password=null;
                String nickname=null;
                String phone=null;
                int statuscode= dis.readInt();
                if(statuscode==100) {//if register
                    System.out.println("NEW Register Requests");
                    id=br.readLine();
                    password=br.readLine();
                    nickname=br.readLine();
                    phone=br.readLine();
                    System.out.println("id:" +id);
                    System.out.println("password: "+password);
                    System.out.println("nickname: "+nickname);
                    System.out.println("phone: "+phone);
                    if(db.duplicateemailcheck(id)==true){
                        if(db.register(id,password,nickname,phone)==true){
                            int user_id=db.get_user_id(id);
                            dos.writeInt(user_id);
                        }
                    }
                    else{
                        System.out.println("duplicated email");
                        dos.writeInt(2);
                    }


                } else if (statuscode==200) { // if login
                    System.out.println("New Login Requests");
                    id=br.readLine();
                    password=br.readLine();
                    if(db.logincheck(id,password)==true) {
                        dos.writeInt(1);
                    }else{
                        dos.writeInt(-1);//비밀번호가 틀리면 -1을 클라이언트로 보내기
                    }

                } else if (statuscode==300) {//if logout
                    System.out.println("New Logout Requests");
                    int user_id=dis.readInt();
                    if(db.logout(user_id)==true){
                        System.out.println("logout complete");
                    }else{
                        System.out.println("error occured when logout");
                    }
                }
            }catch (Exception e) {
                System.out.println(e);
            }

        }
    }
}

