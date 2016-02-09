package de.mjpegsample;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.util.Random;

import de.mjpegsample.MjpegView.MjpegInputStream;
import de.mjpegsample.MjpegView.MjpegView;


/**
 * Created by Eduardo on 26-01-2015.
 */
public class MjpegStream extends Activity implements View.OnTouchListener   {

    private MjpegView mv = null;
    private static final boolean DEBUG = false;
    private static final String TAG = "MJPEG";
    Socket socket;
    Button buttonUP, buttonDOWN, buttonRIGHT, buttonLEFT, screenshot;
    TextView window;
    PrintWriter out;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mv = new MjpegView(this);
        setContentView(R.layout.stream);
        mv = (MjpegView) findViewById(R.id.mv);
        String URL = "http://"+Constants.IP_ADDRESS+":8080/?action=stream";
        new DoRead().execute(URL);
        //mv.setSource(MjpegInputStream.read(URL));
        //mv.setDisplayMode(MjpegView.SIZE_FULLSCREEN);
        //mv.showFps(true);

        buttonUP = (Button)findViewById(R.id.buttonUP);
        buttonDOWN = (Button)findViewById(R.id.buttonDOWN);
        buttonRIGHT = (Button)findViewById(R.id.buttonRIGHT);
        buttonLEFT = (Button)findViewById(R.id.buttonLEFT);
        screenshot = (Button)findViewById(R.id.screenshot);
        window = (TextView)findViewById(R.id.textView);

        buttonUP.setOnTouchListener(this);
        buttonDOWN.setOnTouchListener(this);
        buttonRIGHT.setOnTouchListener(this);
        buttonLEFT.setOnTouchListener(this);
        screenshot.setOnTouchListener(this);

        OpenSocket();
}
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            switch(v.getId()){
                case R.id.buttonUP:
                    SendCommand("W\n");
                    disableButtons(buttonDOWN,buttonLEFT,buttonRIGHT);
                    window.append("W");
                    break;
                case R.id.buttonDOWN:
                    SendCommand("S\n");

                    disableButtons(buttonUP,buttonLEFT,buttonRIGHT);
                    window.append("S");
                    break;
                case R.id.buttonRIGHT:
                    SendCommand("D\n");
                    disableButtons(buttonDOWN,buttonLEFT,buttonUP);
                    window.append("D");
                    break;
                case R.id.buttonLEFT:
                    SendCommand("A\n");
                    disableButtons(buttonDOWN,buttonUP,buttonRIGHT);
                    window.append("A");
                    break;
                case R.id.screenshot:
                    String URL_SNAPSHOT = "http://"+Constants.IP_ADDRESS+":8080/?action=snapshot";
                    Picasso.with(getApplicationContext()).load(URL_SNAPSHOT).into(target);
                    window.append("Shot");
                    break;
            }
        } else if (event.getAction()== MotionEvent.ACTION_UP) {
            SendCommand("P\n");
            window.append("P");
            enableButtons();
        }
        return false;
    }
    public void disableButtons(Button b1, Button b2, Button b3){
        b1.setEnabled(false);
        b1.setVisibility(View.INVISIBLE);
        b2.setEnabled(false);
        b2.setVisibility(View.INVISIBLE);
        b3.setEnabled(false);
        b3.setVisibility(View.INVISIBLE);
    }
    public void enableButtons(){
        buttonUP.setEnabled(true);
        buttonUP.setVisibility(View.VISIBLE);
        buttonDOWN.setEnabled(true);
        buttonDOWN.setVisibility(View.VISIBLE);
        buttonRIGHT.setEnabled(true);
        buttonRIGHT.setVisibility(View.VISIBLE);
        buttonLEFT.setEnabled(true);
        buttonLEFT.setVisibility(View.VISIBLE);
    }
    public void disableButtons(){
        buttonUP.setEnabled(false);
        buttonUP.setVisibility(View.INVISIBLE);
        buttonDOWN.setEnabled(false);
        buttonDOWN.setVisibility(View.INVISIBLE);
        buttonRIGHT.setEnabled(false);
        buttonRIGHT.setVisibility(View.INVISIBLE);
        buttonLEFT.setEnabled(false);
        buttonLEFT.setVisibility(View.INVISIBLE);
    }
    @Override
    public void onPause() {
        super.onPause();
        mv.stopPlayback();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //Function to Take a Screenshot
    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Random r = new Random();
                    int iterator=r.nextInt();

                    File file = new File(Constants.STORAGE_FOLDER,+iterator+"screeshoot.jpg");
                    try
                    {
                        file.createNewFile();
                        FileOutputStream ostream = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, ostream);
                        ostream.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
        }
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            if (placeHolderDrawable != null) {
            }
        }
    };
    protected void SendCommand(final String command){
        new Thread(new Runnable(){
            @Override
            public void run(){
                out.write(command);
                out.flush();
            }
        }).start();
    }
    protected void OpenSocket(){
        new Thread(
                new Runnable(){
                    @Override
                    public void run(){
                        try{
                            socket = new Socket(Constants.IP_ADDRESS,Constants.PORT_NUM);
                            out =  new PrintWriter(socket.getOutputStream());
                            out.write("M\n");
                            out.flush();
                        } catch (IOException e) {
                            disableButtons();
                            window.append("No TCP connection\n");
                            e.printStackTrace();
                        }
                    }
                }).start();
    }

    public class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
        protected MjpegInputStream doInBackground(String... url) {
            //TODO: if camera has authentication deal with it and don't just not work
            HttpResponse res = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpParams httpParams = httpclient.getParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 5 * 1000);
            HttpConnectionParams.setSoTimeout(httpParams, 5 * 1000);
            if (DEBUG) Log.d(TAG, "1. Sending http request");
            try {
                res = httpclient.execute(new HttpGet(URI.create(url[0])));
                if (DEBUG)
                    Log.d(TAG, "2. Request finished, status = " + res.getStatusLine().getStatusCode());
                if (res.getStatusLine().getStatusCode() == 401) {
                    //You must turn off camera User Access Control before this will work
                    return null;
                }
                return new MjpegInputStream(res.getEntity().getContent());
            } catch (ClientProtocolException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-ClientProtocolException", e);
                }
                //Error connecting to camera
            } catch (IOException e) {
                if (DEBUG) {
                    e.printStackTrace();
                    Log.d(TAG, "Request failed-IOException", e);
                }
                //Error connecting to camera
            }
            return null;
        }

        protected void onPostExecute(MjpegInputStream result) {
            mv.setSource(result);
            if (result != null) {
                //result.setSkip(1);
                //setTitle(R.string.app_name);
            } else {
                //setTitle(R.string.title_disconnected);
            }
            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
            mv.showFps(false);
        }
    }
}
