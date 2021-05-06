package com.reactlibrary;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RNNetworkRequestModule extends ReactContextBaseJavaModule {
  private final String TAG = "RNNativeRequestModule";
  private final OkHttpClient client = new OkHttpClient();
  public static final MediaType MEDIA_TYPE_MARKDOWN
          = MediaType.parse("text/x-markdown; charset=utf-8");
  //    RequestQueue requestQueue = null;
  public static ReactApplicationContext reactContext;
  public RNNetworkRequestModule(ReactApplicationContext reactContext) {

    super(reactContext);
  }
  public Map getHeaders(ReadableMap configs){
    HashMap<String, String> map = new HashMap<String, String>();
    if(configs.hasKey("headers")){
      ReadableMap headers = configs.getMap("headers");
      ReadableMapKeySetIterator iterator = headers.keySetIterator();
      while (iterator.hasNextKey()) {
        String key = iterator.nextKey();
        String value = headers.getString(key);
        map.put(key,value);
      }
    }else{
      map.put("Content-Type", "application/json");
      map.put("Accept", "application/json");
      map.put("X-Requested-With","XMLHttpRequest");
    }

    return map;
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void handlePost(String urlString, JSONObject headers, String body, Promise promise){
    this.handleJSONRequest(com.android.volley.Request.Method.POST,urlString,headers,body,promise);
  }
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void handlePut(String urlString, JSONObject headers, String body, Promise promise){
    this.handleJSONRequest(com.android.volley.Request.Method.PUT,urlString,headers,body,promise);
  }
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void handleDelete(String urlString, JSONObject headers, String body, Promise promise){
    this.handleJSONRequest(com.android.volley.Request.Method.DELETE,urlString,headers,body,promise);
  }
  @RequiresApi(api = Build.VERSION_CODES.N)
  public void handleJSONRequest(int method, String urlString, JSONObject headers, String body, Promise promise){

    Headers.Builder head = new Headers.Builder();


    JSONObject finalJsonHeaders = headers;
    headers.keys().forEachRemaining((s) -> {

      try {
        Log.d("Array", "Converted header key  : " + s + " value :  " +  finalJsonHeaders.get(s));
        head.add(s, finalJsonHeaders.get(s).toString());
      } catch (JSONException e) {
        e.printStackTrace();
      }


    });

    Headers allHeaders = head.build();
    Request request = new Request.Builder()
            .headers(allHeaders)
            .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, body))
            .url(urlString)
            .build();

    client.newCall(request).enqueue(new Callback() {
      @Override public void onFailure(Call call, IOException e) {
        e.printStackTrace();
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        try (ResponseBody responseBody = response.body()) {
          if (!response.isSuccessful()){
            JsonObject obj = new JsonObject();
            obj.addProperty("code", response.code());
            obj.addProperty("message",response.message());
            promise.resolve(obj.toString());
            throw new IOException("Unexpected code " + response);
          }

          Headers responseHeaders = response.headers();
          for (int i = 0, size = responseHeaders.size(); i < size; i++) {
            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
          }

//                    System.out.println(responseBody.string());
          JsonObject obj = new JsonObject();
          obj.addProperty("code", response.code());
          obj.addProperty("data",responseBody.string());
          promise.resolve(obj.toString());
        }
      }
    });
  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  public void handleGet(String urlString, JSONObject headers, Promise promise){
    Headers.Builder head = new Headers.Builder();


    JSONObject finalJsonHeaders = headers;
    headers.keys().forEachRemaining((s) -> {

      try {
        Log.d("Array", "Converted header key  : " + s + " value :  " +  finalJsonHeaders.get(s));
        head.add(s, finalJsonHeaders.get(s).toString());
      } catch (JSONException e) {
        e.printStackTrace();
      }


    });

    Headers allHeaders = head.build();
    Request request = new Request.Builder()
            .headers(allHeaders)
            .url(urlString)
            .build();

    client.newCall(request).enqueue(new Callback() {
      @Override public void onFailure(Call call, IOException e) {

        e.printStackTrace();
      }

      @Override public void onResponse(Call call, Response response) throws IOException {
        try (ResponseBody responseBody = response.body()) {
          if (!response.isSuccessful()){
            JsonObject obj = new JsonObject();
            obj.addProperty("code", response.code());
            obj.addProperty("message",response.message());
            promise.resolve(obj.toString());
            throw new IOException("Unexpected code " + response);
          }

          Headers responseHeaders = response.headers();
          for (int i = 0, size = responseHeaders.size(); i < size; i++) {
            System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
          }
          JsonObject obj = new JsonObject();
          obj.addProperty("code", response.code());
          obj.addProperty("data",responseBody.string());
          promise.resolve(obj.toString());
        }
      }
    });

  }

  @RequiresApi(api = Build.VERSION_CODES.N)
  @ReactMethod
  public void fetch(String urlAsString, ReadableMap configs, Promise promise) throws UnsupportedEncodingException, JSONException {
    String urlString = urlAsString;
    Log.d(TAG, "urlString: "+urlString);

    if(!isNetworkAvailable()){
      promise.reject(new Throwable("Network not reachable"));
      return;
    }

    JSONObject jsonHeaders = new JSONObject();
    String method = "GET";
    String body = null;
    Map headers = getHeaders(configs);
    jsonHeaders = new JSONObject(headers);


    if(configs.hasKey("method")){
      method = configs.getString("method");
    }

    switch (method){
      case "GET":{
        this.handleGet(urlString, jsonHeaders,promise);
        break;
      }
      case "POST":{
//                if( configs.getType("body") == ReadableType.Map){
//                    ReadableMap formBody = configs.getMap("body");
//                    if(formBody.hasKey("_parts")){
//
//                        this.handleMultipartRequest(urlString,headers,formBody,promise);
//                    }else{
//                        promise.reject(new Throwable("invalid form data"));
//                    }
//                }else{
        if(configs.hasKey("body")){
          body = configs.getString("body");
        }
        this.handlePost(urlString,jsonHeaders,body,promise);
//                }
        break;
      }
      case "PUT":{
        if(configs.hasKey("body")){
          body = configs.getString("body");
        }
        this.handlePut(urlString,jsonHeaders,body,promise);
        break;
      }
      case "DELETE":{
        if(configs.hasKey("body")){
          body = configs.getString("body");
        }
        this.handleDelete(urlString,jsonHeaders,body,promise);
        break;
      }
      default:{
        promise.reject(new Throwable("Invalid method provided"));
        break;
      }
    }
  }
  private boolean isNetworkAvailable() {
    if(reactContext == null){
      return true;
    }
    ConnectivityManager connectivityManager
            = (ConnectivityManager) reactContext.getSystemService(reactContext.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
  }
  @NonNull
  @Override
  public String getName() {
    return "RNNetworkRequest";
  }
}
