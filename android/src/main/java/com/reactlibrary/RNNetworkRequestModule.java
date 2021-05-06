package com.reactlibrary;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.entertainmentoxygen.NativeFetch.RequestProgress;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RNNativeRequestModule extends ReactContextBaseJavaModule {
    private final String TAG = "RNNativeRequestModule";
    private final OkHttpClient client = new OkHttpClient();
    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    //    RequestQueue requestQueue = null;
    public static ReactApplicationContext reactContext;
    public RNNativeRequestModule(ReactApplicationContext reactContext) {

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
    public Headers getHeaders(JSONObject headers){
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

        return head.build();

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
    public void handleMultipartRequest(String urlString, JSONObject headers, ReadableMap mapBody, Promise promise) throws JSONException {

        JSONObject jsonBody = convertMapToJson(mapBody);
        RequestBody body = RequestBody.create(JSON, String.valueOf(jsonBody));
        MultipartBody.Builder buildernew = new MultipartBody.Builder().setType(MultipartBody.FORM);



        JSONObject newbody = new JSONObject(String.valueOf(jsonBody));
            JSONArray formdata = new JSONArray();
            formdata = newbody.getJSONArray("_parts");
            int iterator = 0;

            for (int i = 0; i < formdata.length() ; i++) {
                JSONArray eachData = (JSONArray) formdata.get(iterator);

                String key = (String) eachData.get(0);
                Object val = eachData.get(1) ;


                // condition to handle JSON objects in formdata
                if(eachData.get(1).getClass() == JSONObject.class){

                    //getting the uri from string value
                    JSONObject dataObj = new JSONObject(val.toString());
                    Uri myUri = Uri.parse(dataObj.get("uri").toString());


                    //then obtaining the file from the uri
                    File file = new File(myUri.getPath());
                    //set media type of file e.g: image/png or video
                    body = RequestBody.create(MediaType.parse(dataObj.get("type").toString()), file);
                    buildernew.addFormDataPart(key, dataObj.get("name").toString(),body);
                }

                // condition to handle string values in formdata
                if(eachData.get(1).getClass() == String.class){

                    buildernew.addFormDataPart(key,val.toString());

                }

                iterator++;

            }

            buildernew.addPart(body);

            // calculating upload stream
            RequestProgress newReq = null;
            newReq = new RequestProgress(buildernew.build(), new RequestProgress.Listener() {
                @Override
                public void onRequestProgress(long bytesWritten, long contentLength) {
                    float percentage = 100f * bytesWritten / contentLength;

                    percentage = Math.round(percentage);

                    // TODO: Do something useful with the values
                    Log.d("CountingBodyProgress", "onRequestProgress: " + percentage + "  uploaded " + bytesWritten + " out of  " + contentLength);
                }
            });

            Request request = new Request.Builder().headers(getHeaders(headers)).url(urlString).post(newReq).build();

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

//            try (Response response = client.newCall(request).execute()) {
//                String code = new Integer(response.code()).toString();
//                String responseString =  response.body().string();
//
//
//                return getPatchRequestResponse(responseString, code);
//
//
//
//            } catch (IOException ex) {
//                Log.d("Error ex", "Multipart request error: " + ex.getMessage());
//
//            }


    }
    private JSONObject convertMapToJson(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            if(key == "uri") {
                Log.d(TAG, "convertMapToJson: Here");
                object.put(key, "file:///storage/emulated/0/eo.webp");
            }else {
                switch (readableMap.getType(key)) {
                    case Null:
                        object.put(key, JSONObject.NULL);
                        break;
                    case Boolean:
                        object.put(key, readableMap.getBoolean(key));
                        break;
                    case Number:
                        object.put(key, readableMap.getDouble(key));
                        break;
                    case String:
                        object.put(key, readableMap.getString(key));
                        break;
                    case Map:
                        object.put(key, convertMapToJson(readableMap.getMap(key)));
                        break;
                    case Array:
                        object.put(key, convertArrayToJson(readableMap.getArray(key)));
                        break;
                }
            }
        }
        Log.d(TAG, "convertMapToJson: " + object);
        return object;
    }


    private JSONArray convertArrayToJson(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(convertMapToJson(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(convertArrayToJson(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
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
                if( configs.getType("body") == ReadableType.Map){
                    ReadableMap formBody = configs.getMap("body");
                    if(formBody.hasKey("_parts")){

                        this.handleMultipartRequest(urlString,jsonHeaders,formBody,promise);
                    }else{
                        promise.reject(new Throwable("invalid form data"));
                    }
                }else{
                    if(configs.hasKey("body")){
                        body = configs.getString("body");
                    }
                    this.handlePost(urlString,jsonHeaders,body,promise);
                }
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
