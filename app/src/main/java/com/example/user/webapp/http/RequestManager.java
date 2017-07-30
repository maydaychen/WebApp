package com.example.user.webapp.http;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.example.user.webapp.MainActivity;
import com.example.user.webapp.bean.BaseBean;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Weshine on 2017/5/8.
 */

public class RequestManager {
    private AppRequest request;

    private static RequestManager instance;
    private Context context;
    public static RequestManager getInstance(Context context){
        if(instance == null){
            instance = new RequestManager(context);
        }
        return instance;
    }
    protected RequestManager(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.duoyunjiav2.wshoto.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        request = retrofit.create(AppRequest.class);
    }

    public void getAva(Context context, File headPicFile,String acc,String session,String sign,int time) throws IOException {
        Log.d("wjj","getAva");
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        bodyBuilder.add("avatar", fileToBase64(headPicFile));
        bodyBuilder.add("access_token", acc);
        bodyBuilder.add("sessionkey", session);
        bodyBuilder.add("sign", sign);
        bodyBuilder.add("timestamp", time + "");
        Log.d("wjj",fileToBase64(headPicFile));
        RequestBody requestBody = bodyBuilder.build();
        Call<JSONObject> call = request.getAva(requestBody);
        final MainActivity activity = (MainActivity) context;
        call.enqueue(new Callback<JSONObject>() {
            @Override
            public void onResponse(Call<JSONObject> call, Response<JSONObject> response) {
                String msg = response.toString();
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                activity.deletePic();
                Log.d("wjj",msg);
                activity.getResult(msg);
            }

            @Override
            public void onFailure(Call<JSONObject> call, Throwable t) {
                Toast.makeText(activity, "上传失败，请稍后再试", Toast.LENGTH_SHORT).show();
                Log.d("wjj","err");
                activity.deletePic();
                t.printStackTrace();
            }
        });
    }
    public static String fileToBase64(File file) {
        String base64 = null;
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            byte[] bytes = new byte[in.available()];
            int length = in.read(bytes);
            base64 = Base64.encodeToString(bytes, 0, length, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return base64;
    }
}
