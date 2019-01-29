package com.example.ch225253.baidumap;


import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class server {
    public static void main(String args[]) throws Exception {
        // 要连接的服务端IP地址和端口
        String host = "127.0.0.1";
        int port = 8989;
        // 与服务端建立连接
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 2000);
        // 建立连接后获得输出流
        OutputStream outputStream = socket.getOutputStream();
        String message = "你好！  server";
        outputStream.write(message.getBytes("UTF-8"));
        //通过shutdownOutput高速服务器已经发送完数据，后续只能接受数据
//        socket.shutdownOutput();

        InputStream inputStream = socket.getInputStream();
        byte[] bytes = new byte[1024];
        int len;
//        StringBuilder sb = new StringBuilder();
        String sb = "";
        while ((len = inputStream.read(bytes)) != -1) {
            //注意指定编码格式，发送方和接收方一定要统一，建议使用UTF-8
//            sb.append(new String(bytes, 0, len, "GB2312"));
            sb = new String(bytes, 0, len,"GB2312");
            System.out.println("get message from server: " +sb);
        }

//        inputStream.close();
//        outputStream.close();
//        socket.close();
    }
}





//原socket

// try {
//         Socket socket = new Socket(item_ip,port);
//         Log.d("isConnected", String.valueOf(socket.isConnected()));
//
//         DataOutputStream out = new DataOutputStream(socket.getOutputStream());     //可用PrintWriter
//         DataInputStream in = new DataInputStream(socket.getInputStream());
//         if(socket.isConnected()) {
//         out.writeUTF(a);
//         }
////            socket.shutdownOutput();
//         while (true) {
//         Log.d("in.readUTF()", in.readUTF());
//         if(in.readUTF()!=null){
//         break;
//         }
//         }
//         response =in.readUTF();
//         out.close();
//         socket.close();
//         } catch (IOException e) {
//         Log.e("服务器异常:" ,e.toString());
//         }






//public class server {
//
//    public static final String IP = "10.151.61.30";//服务器地址
//    public static final int PORT = 8989;//服务器端口号
//
//    public static void main(String[] args) {
//        handler();
//    }
//
//    private static void handler(){
//        try {
//            //实例化一个Socket，并指定服务器地址和端口
//            Socket client = new Socket(IP, PORT);
//            //开启两个线程，一个负责读，一个负责写
//            new Thread(new ReadHandlerThread(client)).stop();
//            new Thread(new WriteHandlerThread(client)).stop();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
//
///*
// *处理读操作的线程
// */
//class ReadHandlerThread implements Runnable{
//    private Socket client;
//
//    public ReadHandlerThread(Socket client) {
//
//        this.client = client;
//    }
//
//    @Override
//    public void run() {
//        DataInputStream dis = null;
//        try {
//            while(true){
//                //读取服务器端数据
//                dis = new DataInputStream(client.getInputStream());
//                String receive = dis.readUTF();
//                System.out.println("服务器端返回过来的是: " + receive);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally{
//            try {
//                if(dis != null){
//                    dis.close();
//                }
//                if(client != null){
//                    client = null;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//}
//
///*
// * 处理写操作的线程
// */
//class WriteHandlerThread implements Runnable{
//    private Socket client;
//
//    public WriteHandlerThread(Socket client) {
//        this.client = client;
//    }
//
//    @Override
//    public void run() {
//        DataOutputStream dos = null;
//        BufferedReader br = null;
//        try {
//            while(true){
//                //取得输出流
//                dos = new DataOutputStream(client.getOutputStream());
//                System.out.print("请输入: \t");
//                //键盘录入
//                br = new BufferedReader(new InputStreamReader(System.in));
//                String send = br.readLine();
//                //发送数据
//                dos.writeUTF(send);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }  finally{
//            try{
//                if(dos != null){
//                    dos.close();
//                }
//                if(br != null){
//                    br.close();
//                }
//                if(client != null){
//                    client = null;
//                }
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//    }
//}