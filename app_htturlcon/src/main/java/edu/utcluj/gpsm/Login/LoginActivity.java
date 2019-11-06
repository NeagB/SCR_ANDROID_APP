package edu.utcluj.gpsm.Login;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.utcluj.gpsm.Maps.MapsActivity;
import edu.utcluj.gpsm.R;
import edu.utcluj.gpsm.Register.RegisterActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnLogin, btnRegister;
    EditText etEmail, etPassword;
    private Executor executor = Executors.newFixedThreadPool(1);
    private static final String STATIC_USER_LOGIN = "{" +
            "\"email\":\"%s\"," +
            "\"password\":\"%s\"" +
            "}" ;

    private volatile Handler msgHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);

        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        btnLogin.setOnClickListener(this);
        btnRegister.setOnClickListener(this);
        msgHandler = new MsgHandler(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnLogin:
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();
                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(this,"Please complete all fileds",Toast.LENGTH_LONG).show();
                }
                else {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                                Message msg = msgHandler.obtainMessage();
                                msg.arg1 = doLogin(email,password)?1:0;
                                msgHandler.sendMessage(msg);
                                if(msg.arg1 == 1){
                                    startActivity(new Intent(LoginActivity.this, MapsActivity.class));

                                }

                        }
                    });


                }
                break;
            case R.id.btnRegister:
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));

                break;
        }

    }

    private boolean doLogin(final String email, final String password) {
        HttpURLConnection connection = null;
        try {

            String Base_URL = "http://192.168.0.103:8082/user/login";
            URL url = new URL(Base_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(String.format(STATIC_USER_LOGIN,email, password).getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader((new InputStreamReader(connection.getInputStream())));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return false;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            // connection.disconnect();

//        RetrofitClient client = new RetrofitClient(Base_URL);
//        client.getUserService().login(userLogin).enqueue(new Callback<ResUser>() {
//            @Override
//            public void onResponse(Call<ResUser> call, Response<ResUser> response) {
//                if(response.code() == 200){
//                    if(response.isSuccessful()){
//                        ResUser resUser = (ResUser) response.body();
//                        startActivity(new Intent(LoginActivity.this, MapsActivity.class));
//                    }
//                }else{
//                    Toast.makeText(LoginActivity.this, "The email or password are incorrect", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResUser> call, Throwable t) {
//                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });

        }


    }
    private static class MsgHandler extends Handler {
        private final WeakReference<Activity> sendActivity;

        public MsgHandler(Activity activity) {
            sendActivity = new WeakReference<>(activity);
        }

        public void handleMessage(Message msg) {
            if (msg.arg1 == 1) {
                Toast.makeText(sendActivity.get().getApplicationContext(),
                        "Success!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(sendActivity.get().getApplicationContext(),
                        "Error!", Toast.LENGTH_LONG).show();
            }
        }
    }
    }
