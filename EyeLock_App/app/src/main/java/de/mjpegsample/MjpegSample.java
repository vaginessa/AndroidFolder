package de.mjpegsample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class MjpegSample extends Activity implements View.OnClickListener{
    public static final String EXTRA_CURRENT_USER = "user";
    private static final String PREFS_NAME = "preferences";
    private static final String PREF_UNAME = "Username";
    private static final String PREF_PASSWORD = "Password";
    private final String DefaultUnameValue = "";
    private String UnameValue;
    private final String DefaultPasswordValue = "";
    private String PasswordValue;

    Button connect, stream, sign_out, changepassword;
    EditText username, password;
    boolean connection;

    Socket socket;
    BufferedReader in;
    PrintWriter out;

	public void onCreate(Bundle savedInstanceState) {
       requestWindowFeature(Window.FEATURE_NO_TITLE);
       super.onCreate(savedInstanceState);
       setContentView(R.layout.layout);

       connect = (Button)findViewById(R.id.connectbtn);
       stream = (Button)findViewById(R.id.streambtn);
       sign_out = (Button)findViewById(R.id.signoutbtn);
       changepassword = (Button)findViewById(R.id.changepassbtn);
       username = (EditText)findViewById(R.id.usernameed);
       password = (EditText)findViewById(R.id.passworded);


       sign_out.setEnabled(false);
       stream.setEnabled(false);
       connect.setEnabled(true);
       changepassword.setEnabled(false);

       connect.setOnClickListener(this);
       stream.setOnClickListener(this);
       sign_out.setOnClickListener(this);
       changepassword.setOnClickListener(this);

        connection = false;
   	}
	@Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.connectbtn:
                new Thread(new Runnable(){
                    String ack = "";
                    @Override
                    public void run(){
                        //podes por isto dentro de uma função
                        try{
                            socket = new Socket(Constants.IP_ADDRESS,Constants.PORT_NUM);
                            out =  new PrintWriter(socket.getOutputStream());
                            connection = true;
                        }catch (IOException e) {
                            connection = false;
                            e.printStackTrace();
                        }
                        if(connection){
                                out.write("L\n");
                                out.flush();
                                String result = username.getText().toString();
                                result += "#";
                                result += password.getText().toString();
                                out.write(result);
                                out.flush();

                            while(true){
                                try {
                                    socket.setSoTimeout(2000);
                                    InputStream is = socket.getInputStream();
                                    in = new BufferedReader(
                                            new InputStreamReader(is));
                                        ack = in.readLine();
                                    System.out.println(ack);
                                    if(ack.equals("ack")) {
                                       DisplayInThread("Valid User");
                                       EnableStreamAndSignOut();
                                       socket.close();
                                    }else{
                                       DisplayInThread("Invalid User");
                                    }
                                    break;
                                }catch (SocketTimeoutException s){
                                    DisplayInThread("Timeout, problem with your TCP connection");
                                    break;
                                }
                                catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    }else{
                            DisplayInThread("No TCP connection");
                        }
                   }
                }).start();
                break;
            case R.id.streambtn:
                Intent intent = new Intent(this,MjpegStream.class);
                startActivity(intent);
                break;
            case R.id.signoutbtn:
                stream.setEnabled(false);
                SignOut("C");
               //    ???? Toast.makeText(MjpegSample.this, "Sign out", Toast.LENGTH_SHORT).show();
                break;
            case R.id.changepassbtn:
                Intent intent2 = new Intent(this,ChangePassword.class);
                intent2.putExtra(EXTRA_CURRENT_USER,username.getText().toString());
                startActivity(intent2);
                break;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        savePreferences();
        System.out.println("onPause\n");
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadPreferences();
        System.out.println("onResume\n");
    }
    private void savePreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Edit and commit
        UnameValue = username.getText().toString();
        PasswordValue = password.getText().toString();
        System.out.println("onPause save name: " + UnameValue);
        System.out.println("onPause save password: " + PasswordValue);
        editor.putString(PREF_UNAME, UnameValue);
        editor.putString(PREF_PASSWORD, PasswordValue);
        editor.commit();
    }
    private void loadPreferences() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME,
                Context.MODE_PRIVATE);
        // Get value
        UnameValue = settings.getString(PREF_UNAME, DefaultUnameValue);
        PasswordValue = settings.getString(PREF_PASSWORD, DefaultPasswordValue);
        username.setText(UnameValue);
        password.setText(PasswordValue);
        System.out.println("onResume load name: " + UnameValue);
        System.out.println("onResume load password: " + PasswordValue);
    }
    private void DisplayInThread(final String output){
        MjpegSample.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MjpegSample.this, output, Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void EnableStreamAndSignOut(){
        MjpegSample.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stream.setEnabled(true);
                sign_out.setEnabled(true);
                changepassword.setEnabled(true);
            }
        });
    }
    private void SignOut(final String RaspberryInformer){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    socket = new Socket(Constants.IP_ADDRESS,Constants.PORT_NUM);
                    out =  new PrintWriter(socket.getOutputStream());
                    out.write(RaspberryInformer);
                    out.flush();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}

