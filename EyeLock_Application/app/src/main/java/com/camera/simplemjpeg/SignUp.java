package com.camera.simplemjpeg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class SignUp extends Activity implements View.OnClickListener{
    Button create;
    EditText SignUpUsername;
    EditText SignUpPassword;
    EditText SignUpSerialNumber;
    boolean connection;
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        create = (Button)findViewById(R.id.createbtn);
        SignUpUsername = (EditText)findViewById(R.id.signupusernameed);
        SignUpPassword = (EditText)findViewById(R.id.signuppassworded);
        SignUpSerialNumber = (EditText)findViewById(R.id.serialnumber);
        create.setEnabled(true);
        create.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.createbtn:
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        try{
                            socket = new Socket(Constants.IP_ADDRESS,Constants.PORT_NUM);
                            out =  new PrintWriter(socket.getOutputStream());
                            out.write("N");
                            out.flush();
                            connection = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            connection = false;
                        }
                        if(connection){
                            String ack = "";
                            String result = SignUpUsername.getText().toString();
                            result += "#";
                            result += SignUpPassword.getText().toString();
                            result += "#";
                            result += SignUpSerialNumber.getText().toString();
                            result += "";
                            System.out.println(result);
                            out.write(result);
                            out.flush();
                            try {
                                socket.setSoTimeout(2000);
                                InputStream is = socket.getInputStream();
                                in = new BufferedReader(
                                        new InputStreamReader(is));
                                ack = in.readLine();
                                System.out.println(ack);
                                if(ack.equals("ack")) {
                                    DisplayInThread("New user Created");
                                    socket.close();
                                }else{
                                    DisplayInThread("Serial Number not valid");
                                }
                                socket.close();
                            }catch (SocketTimeoutException s){
                                DisplayInThread("Timeout, problem with your TCP connection");
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }else{
                            DisplayInThread("No TCP connection");
                        }
                    }
                }).start();
        }
    }
    private void DisplayInThread(final String output){
        SignUp.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SignUp.this, output, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
