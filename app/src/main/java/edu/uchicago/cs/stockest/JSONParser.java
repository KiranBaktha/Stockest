package edu.uchicago.cs.stockest;


import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

// Custom JSON Parser from Adam's book

public class JSONParser {

    static InputStream sInputStream = null;
    static JSONArray sReturnJsonObject = null;
    static String sRawJsonString = "";
    public JSONParser() {}
    public JSONArray getJSONFromUrl(String url) {
//attempt to get response from server
        try {
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity httpEntity = httpResponse.getEntity();
            sInputStream = httpEntity.getContent();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//read stream into string-builder
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    sInputStream, "iso-8859-1"), 8);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            sInputStream.close();
            sRawJsonString = stringBuilder.toString();
        } catch (Exception e) {
            Log.e("ErrorReadingBuffer: " + e.toString(), this.getClass().getSimpleName());
        }
        try {
            sReturnJsonObject = new JSONArray(sRawJsonString);
        } catch (JSONException e) {
            Log.e("Parser", "Error when parsing data " + e.toString());
        }
//return json object
        return sReturnJsonObject;
    }
}
