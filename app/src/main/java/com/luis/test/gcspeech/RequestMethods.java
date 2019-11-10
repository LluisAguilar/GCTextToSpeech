package com.luis.test.gcspeech;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.luis.test.gcspeech.gcp.AudioConfig;
import com.luis.test.gcspeech.gcp.GCPVoice;
import com.luis.test.gcspeech.gcp.Input;
import com.luis.test.gcspeech.gcp.VoiceMessage;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.RequestBody;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RequestMethods {

    Context context;
    private static LanguageListener languageListener;
    private VoiceMessage mVoiceMessage;
    private MediaPlayer mMediaPlayer;
    private int mVoiceLength = -1;

    public RequestMethods(){}

    public RequestMethods(Context context){
        this.context=context;
    }


    public void getLanguages() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient okHttpClient = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(Config.VOICES_ENDPOINT)
                        .addHeader(Config.API_KEY_HEADER, Config.API_KEY)
                        .build();
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        languageListener.onLanguageResponse(response.body().string());
                    } else {
                        throw new IOException(String.valueOf(response.code()));
                    }
                } catch (IOException IoEx) {
                    IoEx.printStackTrace();
                    Log.e("response","Error");
                }
            }
        }).start();
    }

    public void responseToJson(String text) {
        JsonElement jsonElement = new JsonParser().parse(text);
        if (jsonElement == null || jsonElement.getAsJsonObject() == null ||
                jsonElement.getAsJsonObject().get("voices").getAsJsonArray() == null) {
            Log.e("response", "get error json");
            return;
        }

        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonArray = jsonObject.get("voices").getAsJsonArray();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonArray jsonArrayLanguage = jsonArray.get(i)
                    .getAsJsonObject().get("languageCodes")
                    .getAsJsonArray();

            if (jsonArrayLanguage.get(0) != null) {
                String language = jsonArrayLanguage.get(0).toString().replace("\"", "");
                String name = jsonArray.get(i).getAsJsonObject().get("name").toString().replace("\"", "");
                String ssmlGender = jsonArray.get(i).getAsJsonObject().get("ssmlGender").toString().replace("\"", "");
                //ESSMLlVoiceGender essmLlVoiceGender = ESSMLlVoiceGender.convert(ssmlGender);
                int naturalSampleRateHertz = jsonArray.get(i).getAsJsonObject().get("naturalSampleRateHertz").getAsInt();

                Log.e("response l", language);
                Log.e("response n", name);
                Log.e("response s", ssmlGender);
                Log.e("response nat", String.valueOf(naturalSampleRateHertz));

            }
        }
    }

    public void sendTextForAudio(AudioConfig audioConfig, GCPVoice gcpVoice, String text) {
        if (audioConfig != null && gcpVoice != null){

            mVoiceMessage = new VoiceMessage.Builder()
                    .addParameter(new Input(text))
                    .addParameter(gcpVoice)
                    .addParameter(audioConfig)
                    .build();
            new Thread(runnableSend).start();

        }else {

            Log.e("error","ERROR ocurred");

        }
    }

    private Runnable runnableSend = new Runnable() {
        @Override
        public void run() {
            Log.d("response", "Message: " + mVoiceMessage.toString());

            com.squareup.okhttp.OkHttpClient okHttpClient = new com.squareup.okhttp.OkHttpClient();
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    mVoiceMessage.toString());
            Log.e("responsee",mVoiceMessage.toString());
            com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                    .url(Config.SYNTHESIZE_ENDPOINT)
                    .addHeader(Config.API_KEY_HEADER, Config.API_KEY)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .post(body)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(com.squareup.okhttp.Request request, IOException e) {
                    Log.e("Error",e.getMessage());
                }

                @Override
                public void onResponse(com.squareup.okhttp.Response response) throws IOException {
                    if (response != null) {
                        Log.i("response", "onResponse code = " + response.code());
                        if (response.code() == 200) {
                            String text = response.body().string();
                            JsonElement jsonElement = new JsonParser().parse(text);
                            JsonObject jsonObject = jsonElement.getAsJsonObject();

                            if (jsonObject != null) {
                                String json = jsonObject.get("audioContent").toString();
                                json = json.replace("\"", "");
                                playAudio(json);
                                return;
                            }
                        }
                    }

                    Log.e("Error","get response fail");
                }
            });
        }
    };

    private void playAudio(String base64EncodedString) {
        try {
            stopAudio();
            String url = "data:audio/mp3;base64," + base64EncodedString;
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException IoEx) {
            Log.e("response",IoEx.toString());
        }
    }
    public void stopAudio() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mVoiceLength = -1;
        }
    }

    public void resumeAudio() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying() && mVoiceLength != -1) {
            mMediaPlayer.seekTo(mVoiceLength);
            mMediaPlayer.start();
        }
    }

    public void pauseAudio() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mVoiceLength = mMediaPlayer.getCurrentPosition();
        }
    }



    public interface LanguageListener{
        void onLanguageResponse(String text);
    }

    public void getLanguagesResponse(LanguageListener languageListener){
        this.languageListener=languageListener;
    }


}
