package de.mjpegsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

public class Cover extends Activity implements View.OnClickListener{
    public static final String EXTRA_CURRENT_IP = "";
    Button login,signup;
    TextView info,ip;
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cover);

        login = (Button)findViewById(R.id.loginbtn);
        signup = (Button)findViewById(R.id.signupbtn);
        info = (TextView)findViewById(R.id.infotv);
        ip  = (TextView)findViewById(R.id.ipraspberry);
        login.setVisibility(View.INVISIBLE);
        signup.setVisibility(View.INVISIBLE);
        login.setOnClickListener(this);
        signup.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginbtn:
                Intent intent = new Intent(this,MjpegSample.class);
                startActivity(intent);
                break;
            case R.id.signupbtn:
                Intent intent2 = new Intent(this,SignUp.class);
                startActivity(intent2);
                break;
        }
    }
    public void UminhoOnClick(View v){

        info.setText("Developed by:\n" +
                "\tEduardo Mendes & André Oliveira \n\n" +
                "Android Application developed\n" +
                "\tunder course Embedded Systems\n" +
                "\t\t\tERSG");

    }
    public void IpOnClick(View v){
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramSocket s = null;
                DatagramPacket sendPacket = null;

                String s_ip = null;
                String subs_ip = null;
                StringBuilder sb = null;
                String ip_broadcast = null;

                //Listening raspberry answer in order to know it's address
                Socket clientSocket = null; // raspberry socket
                ServerSocket serverSocket = null;
                StringBuilder aux = new StringBuilder(); //aux string builder
                String raspberry_ip = null; //final raspberry ip trated
                String receive_line = null; //line receiving from raspberry, just for test connections

                //String to handle the Android device IP , to begin broadcasting
                s_ip = getIpAddress();
                subs_ip = s_ip.substring(0,s_ip.lastIndexOf(".")+1);
                sb = new StringBuilder();
                sb.append(subs_ip);

                for(int i = 0 ; i < 255 ; i++ ){
                    sb.append(i);
                    ip_broadcast = sb.toString();
                    System.out.println(ip_broadcast);
                    byte[] sendData = "Is" .getBytes();
                    try {
                        s = new DatagramSocket();
                        sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(ip_broadcast), Constants.PORT);
                        s.send(sendPacket);
                    }catch (IOException e) {
                        System.out.println(ip_broadcast + "erro");
                    }
                    sb.delete(subs_ip.lastIndexOf(".")+1,ip_broadcast.length());
                }
                DisplayInThread("Aqui");
                //Receiving answer from Raspberry pi

                try {
                    serverSocket = new ServerSocket(Constants.PORT_TCP);
                    clientSocket = serverSocket.accept();
                    DisplayInThread("Connected");
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    receive_line = in.readLine();
                    DisplayInThread("Recieved from Rpi:"+receive_line);

                    raspberry_ip = clientSocket.getInetAddress().toString();
                    aux.append(raspberry_ip);
                    aux.deleteCharAt(0);
                    raspberry_ip = aux.toString();

                    //make raspberry pi Global for all
                    Constants.IP_ADDRESS = raspberry_ip;
                    DisplayInThread2(raspberry_ip);

                } catch (IOException e) {
                    try {
                        clientSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    DisplayInThread("Error accepting TCP communication");
                    e.printStackTrace();
                }

            }
        }).start();
    }
    private String getIpAddress() {
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        int ipInt = wifiManager.getConnectionInfo().getIpAddress();

        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipInt = Integer.reverseBytes(ipInt);
        }
        byte[] ipByteArray = BigInteger.valueOf(ipInt).toByteArray();
        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException e) {

            ipAddressString = "Error!";
        }
        System.out.println(ipAddressString);
        return ipAddressString;
    }
    private void DisplayInThread2(final String output){
        Cover.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                login.setVisibility(View.VISIBLE);
                signup.setVisibility(View.VISIBLE);
                ip.setText(output);
                Toast.makeText(Cover.this, output, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void DisplayInThread(final String output){
        Cover.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Cover.this, output, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
