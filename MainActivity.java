package com.example.nutritionapp;

import android.content.Intent;
import android.icu.number.Precision;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    Button button;
    Button logout;
    EditText search, serving;
    String enterUrl;
    double servSize;
    double numCal;
    Button dataCal;
    double total;
    Button goTotal;
    Button addTotal;

    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference root = db.getReference().child("Users");
    String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.id_textViewTest);

        button = findViewById(R.id.button);
        logout = findViewById(R.id.logout);
        dataCal = findViewById(R.id.dataCal);
        goTotal = findViewById(R.id.goTotal);
        addTotal = findViewById(R.id.addTotal);

        search = findViewById(R.id.search);
        serving = findViewById(R.id.serving);


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Login.class));
                finish();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                enterUrl = search.getText().toString();
                servSize = Double.parseDouble(serving.getText().toString());
                DownloadFilesTask downloadFilesTask = new DownloadFilesTask();
                downloadFilesTask.execute();
                textView.setText("Loading...");

            }
        });

        dataCal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getApplicationContext(), DataList.class));
                finish();
            }
        });
        goTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), CheckTotal.class));
                finish();
            }
        });
    }


    private class DownloadFilesTask extends AsyncTask<String, String, String>
    {
        String stringObj = "";
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder content = new StringBuilder();
            try {
                URL url = new URL("https://api.edamam.com/api/food-database/v2/parser?ingr="+enterUrl+"&app_id=37443fee&app_key=a7ba464cc9603615f499def5883a2096");
                Log.d("TAG7", url.toString());
                URLConnection urlConnection = url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                int charObj = bufferedReader.read();

                while (charObj != -1)
                {
                    stringObj += (char) charObj;
                    charObj = bufferedReader.read();
                }


            } catch (Exception e) {
                Log.d("error", e.toString());
                e.printStackTrace();

            }
            Log.d("TAG_INFO5", stringObj );
            Log.d("TAG_INFO5", String.valueOf(stringObj.length()));
            //String s = content.toString();

            return null;


        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            JSONObject filler = null;
            JSONObject foodOne = new JSONObject();
            try {
                filler = new JSONObject(stringObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                foodOne = filler.getJSONArray("hints").getJSONObject(0).getJSONObject("food").getJSONObject("nutrients");
                Log.d("FOOD!", foodOne.toString());
                Log.d("FOOD!", foodOne.getString("ENERC_KCAL"));


            } catch (JSONException e) {
                Log.d("error2", e.toString());
                e.printStackTrace();
            }

            try {
                numCal = Math.round(Double.parseDouble(foodOne.getString("ENERC_KCAL"))*servSize*100d)/100d;
                textView.setText(numCal+" calories");
                String newcal = serving.getText().toString()+" serving(s) of "+search.getText().toString()+": "+textView.getText().toString();
                ListData favcal = new ListData(newcal);
                addTotal.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        root.push().setValue(favcal).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(MainActivity.this, "Calorie data has been saved", Toast.LENGTH_SHORT).show();
                            }
                        });
                        //total+=numCal;
                        //Log.d("Total", String.valueOf(total));
                    }
                });
            } catch (JSONException e) {
                Log.d("error3", e.toString());
                e.printStackTrace();
            }





        }
    }
}