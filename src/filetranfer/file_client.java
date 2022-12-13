package filetranfer;

import java.io.*;
import java.net.Socket;
public class file_client {
    DataOutputStream dataOutput = null;
    OutputStream os=null;
    Socket socket = null;
    File file = null;        //파일에 정보를 얻기위한 File 클래스
    DataInputStream dataInput = null;
    BufferedOutputStream bufferedOutput = null; //output 속도 향상을 위한 BufferedOutputStream


    public static void main(String[] args){
        Socket socket=null;
        filechoose choice= new filechoose();
        String filename= choice.jFileChooserUtil();
        try{
            socket = new Socket("175.112.14.58", 8080); //서버에 접속

        }catch(IOException e){
            System.out.println("It seems like There is no response from Server");
        }
        if(socket!=null){
            new file_client(socket,filename);
        }



    }


    public file_client(Socket socket, String fileName) {



        try {

            String filetype = fileName.substring(fileName.lastIndexOf("."));
            file = new File(fileName);
            os=socket.getOutputStream();
            dataOutput = new DataOutputStream(os); //output 스크림 생성
            dataOutput.writeInt((int) file.length());  //수신측에 전송파일 사이즈 전달
            dataOutput.flush();
            System.out.println((int) file.length()); // 송신 파일 사이즈 콘솔출력
            os.write(filetype.getBytes());
            os.flush();
            System.out.println("filetype:"+filetype);
            int totalSize = (int) file.length();
            int totalSize2= (int) file.length(); // 아래에서 파일 전송시 totalsize 변수 크기가 변해서 원래 파일 크기 기억용 변수 만듬
            byte[] bytes = new byte[104857600];  //100MB 저장할 바이트 배열
            dataInput = new DataInputStream(new FileInputStream(fileName));
            bufferedOutput = new BufferedOutputStream(dataOutput);
            int i = 0;     //buf 배열 인덱스용 변수
            int progress= 0; //몇 바이트가 전송됬는지 표시하는 변수

            //전송받은 파일 사이즈가 100MB 보다 크다면 100MB 단위로 배열에 저장 후 소켓 버퍼에 write 하고
            //소켓 버퍼에 write한 100MB만큼을  파일 사이즈에서 제외하는 while문!!!
            while (totalSize > 104857600) {
                while (i < 104857600) {
                    bytes[i] = (byte) dataInput.read();
                    i++;
                    System.out.println("전송 진행률: "+String.format("%.2f",progress/(float)totalSize2*100)+"%");
                    progress++;
                }//while(i < 104857600)문
                totalSize -= 104857600;   //파일사이즈 - 100MB
                i = 0;                    //배열 인덱스 초기화
                bufferedOutput.write(bytes);   //소켓 버퍼에 write
            }//while(totalSize > 104857600)문


            //100MB보다 같거나 작은 남은 사이즈 혹은 원래의 사이즈가 100MB 보다 작을 시 if문 내용이 실행 되어
            //소켓 버퍼에 write 함
            if (totalSize <= 104857600) {
                i = 0;        //배열 인덱스 초기화
                bytes = new byte[totalSize];
                while (i < totalSize) {
                    bytes[i] = (byte) dataInput.read();
                    i++;           //배열인덱스 이동
                    System.out.println("전송 진행률: "+String.format("%.2f",progress/(float)totalSize2*100)+"%");
                    progress++;

                }//while문
                bufferedOutput.write(bytes);  //소켓 버퍼에 write
            }//if문
            bufferedOutput.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedOutput != null)
                    bufferedOutput.close();
                if (dataInput != null)
                    dataInput.close();
                if (dataOutput != null)
                    dataOutput.close();
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("file transfer complete");
        }//finally
    }//main
}//ServerSend 클래스
