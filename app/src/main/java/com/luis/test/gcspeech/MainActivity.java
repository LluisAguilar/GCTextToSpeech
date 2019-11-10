package com.luis.test.gcspeech;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.luis.test.gcspeech.gcp.AudioConfig;
import com.luis.test.gcspeech.gcp.EAudioEncoding;
import com.luis.test.gcspeech.gcp.GCPVoice;

//this is just a test by Luis AG

public class MainActivity extends AppCompatActivity implements RequestMethods.LanguageListener {

    RequestMethods requestMethods;
    EditText editText;
    Button speakBtn;
    GCPVoice gcpVoice;
    AudioConfig audioConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestMethods = new RequestMethods(this);
        RequestMethods req = new RequestMethods(this);
        req.getLanguagesResponse(this);

        speakBtn = findViewById(R.id.speakBtn);
        editText = findViewById(R.id.text);

        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String languageCode = "en-US";
                String name = "en-US-Wavenet-A";
                float pit = 2000;
                float speak = 75;


                float pitch = ( ( pit - 2000) / 100);
                float speakRate = ( (speak + 25) / 100);

                gcpVoice = new GCPVoice(languageCode, name);
                audioConfig = new AudioConfig.Builder()
                        .addAudioEncoding(EAudioEncoding.MP3)
                        .addSpeakingRate(speakRate)
                        .addPitch(pitch)
                        .build();

                String text = editText.getText().toString();

                requestMethods.sendTextForAudio(audioConfig,gcpVoice,text);
            }
        });

        //requestMethods.getLanguages();
    }

    @Override
    public void onLanguageResponse(String text) {
        requestMethods.responseToJson(text);
    }
}
