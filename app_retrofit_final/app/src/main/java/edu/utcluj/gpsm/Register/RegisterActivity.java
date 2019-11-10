package edu.utcluj.gpsm.Register;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import edu.utcluj.gpsm.Login.LoginActivity;
import edu.utcluj.gpsm.R;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnSave;
    private EditText etfirstName, etlastName, etEmail, etPassword;
    private Executor executor = Executors.newFixedThreadPool(1);
    private volatile Handler msgHandler;
    private static final String STATIC_USER_REGISTER = "{"+

            "\"firstName\":\"%s\"," +
            "\"lastName\":\"%s\"," +
            "\"email\":\"%s\"," +
            "\"password\":\"%s\"" +
            "}" ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnSave = (Button) findViewById(R.id.btnSave);

        etfirstName = (EditText) findViewById(R.id.etfirstName);
        etlastName = (EditText) findViewById(R.id.etlastName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);

        btnSave.setOnClickListener(this);
        msgHandler = new RegisterActivity.MsgHandler(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btnSave:
                final String firstName = etfirstName.getText().toString();
                final String lastName = etlastName.getText().toString();
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();
                if(email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()){
                    Toast.makeText(this,"Please complete all fileds",Toast.LENGTH_LONG).show();
                }
                else {
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            Message msg = msgHandler.obtainMessage();
                            msg.arg1 = doRegister(firstName, lastName, email, password )?1:0;
                            msgHandler.sendMessage(msg);
                            if(msg.arg1 == 1) {
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            }
                        }
                    });

                }
                break;
        }

    }

    private boolean doRegister(final String firstName,final String lastName,final String email,final String password) {
        HttpURLConnection connection = null;
        try {
            String BASE_URL = "http://192.168.0.102:8082/user/register";
            URL url = new URL(BASE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            OutputStream os = connection.getOutputStream();
            os.write(String.format(STATIC_USER_REGISTER, firstName, lastName,email, password).getBytes());
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
            } else {
                return false;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return false;

        } finally {
            if (connection != null) {
                connection.disconnect();
            }
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
