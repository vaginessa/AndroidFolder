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

public class ChangePassword extends Activity implements View.OnClickListener{
    Socket socket;
    PrintWriter out;
    BufferedReader in;
    Button change;
    EditText OldPassword;
    EditText NewPassword;
    String CurrentUserName;
    boolean connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changepassword);

        Bundle bundle = getIntent().getExtras();
        CurrentUserName = bundle.getString(MjpegSample.EXTRA_CURRENT_USER);

        change = (Button)findViewById(R.id.changepwbtn);
        OldPassword = (EditText)findViewById(R.id.oldpassworded);
        NewPassword = (EditText)findViewById(R.id.newpassworded);

        change.setEnabled(true);
        change.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.changepwbtn:
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        try{
                            socket = new Socket(Constants.IP_ADDRESS,Constants.PORT_NUM);
                            out =  new PrintWriter(socket.getOutputStream());
                            out.write("P");
                            out.flush();
                            connection = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                            connection = false;
                        }
                        if(connection) {
                            System.out.println("aqui");
                            String ack = "";
                            String result = CurrentUserName.toString();
                            result += "#";
                            result += OldPassword.getText().toString();
                            result += "#";
                            result += NewPassword.getText().toString();
                            result += "";
                            out.write(result);
                            out.flush();
                            try {
                                socket.setSoTimeout(2000);
                                InputStream is = socket.getInputStream();
                                in = new BufferedReader(
                                        new InputStreamReader(is));
                                ack = in.readLine();
                                if(ack.equals("ack")) {
                                    DisplayInThread("Valid, you've alter your password");
                                    socket.close();
                                }else{
                                    DisplayInThread("Invalid Old Password");
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
        ChangePassword.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChangePassword.this, output, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
