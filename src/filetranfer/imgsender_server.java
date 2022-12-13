package filetranfer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
public class imgsender_server implements Runnable {


    private static class ConnectThread extends Thread
    {
        ServerSocket serverSocket;
        int count = 1;

        ConnectThread (ServerSocket serverSocket) //생성자를 통해 서버소켓을 받음
        {
            System.out.println("이미지 받기 요청 Server opened"); //서버가 열렸다는 메세지 출력
            this.serverSocket = serverSocket; //서버소켓을 저장
        }

        @Override
        public void run () {
            try {
                while (true){ //계속 새로운 클라이언트의 연결을 수락하고 새 소켓을 cLIENTtHREAD에 넘겨줌
                    Socket socket = serverSocket.accept();  //클라이언트의 연결을 수락
                    System.out.println("이미지 받기 요청 Thread " + count + " is started.");
                    imgsender serverThread = new imgsender(socket, count);
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
        try {   // 서버소켓을 생성, 9797 포트와 binding
            serverSocket = new ServerSocket(9797); // 생성자 내부에 bind()가 있고, bind() 내부에 listen() 있음
            ConnectThread connectThread = new ConnectThread(serverSocket); // 서버소켓을 connectThread에 넘겨줌
            connectThread.start(); // connectThread 시작
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static class imgsender extends Thread {
        Socket socket;
        int id;
        String filesavepath="post/";
        DataInputStream dataInput = null;
        BufferedReader br=null;

        InputStream is =null;

        //생성자를 통해 입력받은 소켓과 클라이언트(쓰레드)의 id를 저장
        imgsender (Socket socket, int id) {
            this.socket = socket;
            this.id = id;
        }
        @Override
        public void run () {
            try {
                is=socket.getInputStream();
                br = new BufferedReader(new InputStreamReader(is));

                String writer= br.readLine();
                System.out.println("작성자:"+writer);
                String new_file_name=br.readLine(); //파일 이름 읽기
                System.out.println("새 파일 이름:"+new_file_name);

                // 파일 전송
                File file =  new File(filesavepath+writer+"/"+new_file_name);
                int file_len=(int)file.length();

                String file_type=new_file_name.substring(new_file_name.lastIndexOf("."));

                OutputStream os= socket.getOutputStream();
                PrintWriter pw=new PrintWriter(os);

                pw.println(String.valueOf(file_len)); //파일크기
                pw.flush();

                DataOutputStream dataOutput = new DataOutputStream(os);

                System.out.println((int) file.length()); // 송신 파일 사이즈 콘솔출력
                System.out.println("filetype:"+file_type);

                int totalSize1 = (int) file.length();
                int totalSize2= (int) file.length(); // 아래에서 파일 전송시 totalsize 변수 크기가 변해서 원래 파일 크기 기억용 변수 만듬
                byte[] bytes = new byte[104857600];  //100MB 저장할 바이트 배열
                dataInput = new DataInputStream(new FileInputStream(file));
                BufferedOutputStream bufferedOutput = new BufferedOutputStream(dataOutput);
                int i = 0;     //buf 배열 인덱스용 변수
                int progress= 0; //몇 바이트가 전송됬는지 표시하는 변수

                //전송받은 파일 사이즈가 100MB 보다 크다면 100MB 단위로 배열에 저장 후 소켓 버퍼에 write 하고
                //소켓 버퍼에 write한 100MB만큼을  파일 사이즈에서 제외하는 while문!!!
                while (totalSize1 > 104857600) {
                    while (i < 104857600) {
                        bytes[i] = (byte) dataInput.read();
                        i++;
                        if(progress/(float)totalSize2*100 % 0.5 == 0){
                            System.out.println("전송 진행률: "+String.format("%.1f",progress/(float)totalSize2*100)+"%");
                        }
                        progress++;
                    }//while(i < 104857600)문
                    totalSize1 -= 104857600;   //파일사이즈 - 100MB
                    i = 0;                    //배열 인덱스 초기화
                    bufferedOutput.write(bytes);   //소켓 버퍼에 write
                }//while(totalSize1 > 104857600)문

                //100MB보다 같거나 작은 남은 사이즈 혹은 원래의 사이즈가 100MB 보다 작을 시 if문 내용이 실행 되어
                //소켓 버퍼에 write 함
                if (totalSize1 <= 104857600) {
                    i = 0;        //배열 인덱스 초기화
                    bytes = new byte[totalSize1];
                    while (i < totalSize1) {
                        bytes[i] = (byte) dataInput.read();
                        i++;           //배열인덱스 이동
                        if(progress/(float)totalSize2*100 % 0.5 == 0){
                            System.out.println("전송 진행률: "+String.format("%.1f",progress/(float)totalSize2*100)+"%");
                        }
                        progress++;
                    }//while문
                    bufferedOutput.write(bytes);  //소켓 버퍼에 write
                }//if문
                bufferedOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}