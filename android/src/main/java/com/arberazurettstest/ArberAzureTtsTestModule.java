package com.arberazurettstest;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@ReactModule(name = ArberAzureTtsTestModule.NAME)

public class ArberAzureTtsTestModule extends ReactContextBaseJavaModule {
  public static final String NAME = "ArberAzureTtsTest";

  public ArberAzureTtsTestModule(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }



  @ReactMethod
  public void textToSpeech (String SubKey,String RegKey,String lang,String textMessage) throws IOException {
    String result;
    String token = getToken(SubKey,RegKey);
    if (token.length() > 0){
      result = turnTextToSpeech(token,RegKey,lang,textMessage);
    }   else {
      result = "Problem getting token";
    }
    getReactApplicationContext()
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit("Test", result);
  }

  public String turnTextToSpeech (String token,String RegKey,String lang,String textMessage) throws IOException {
    String region = RegKey;

    OkHttpClient client = new OkHttpClient().newBuilder()
      .build();
    MediaType mediaType = MediaType.parse("application/ssml+xml");
    RequestBody body = RequestBody.create(mediaType, "<speak version='1.0' xml:lang='en-US'><voice xml:lang='en-US' xml:gender='Male'\n    name='en-US-ChristopherNeural'>\n" + textMessage + "\n</voice></speak>");
    Request request = new Request.Builder()
      .url("https://westeurope.tts.speech.microsoft.com/cognitiveservices/v1")
      .method("POST", body)
      .addHeader("X-Microsoft-OutputFormat", "riff-24khz-16bit-mono-pcm")
      .addHeader("Content-Type", "application/ssml+xml")
      .addHeader("Host", "westeurope.tts.speech.microsoft.com")
      .addHeader("Content-Length", "199")
      .addHeader("Authorization", token)
      .addHeader("User-Agent", "Test")
      .build();
    Response response = client.newCall(request).execute();

    return response.body().byteString().base64();
  }


  public String getToken(String SubKey, String RegKey) throws IOException {
    String subscriptionKey = SubKey;
    String region = RegKey;


    OkHttpClient client = new OkHttpClient().newBuilder()
      .build();
    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
    RequestBody body = RequestBody.create(mediaType, "");
    Request request = new Request.Builder()
      .url("https://westeurope.api.cognitive.microsoft.com/sts/v1.0/issueToken?'Ocp-Apim-Subscription-Key'=" + SubKey)
      .method("POST", body)
      .addHeader("Ocp-Apim-Subscription-Key", SubKey)
      .addHeader("Host", "westeurope.api.cognitive.microsoft.com")
      .addHeader("Content-type", "application/x-www-form-urlencoded")
      .addHeader("Content-Length", "0")
      .build();
    Response response = client.newCall(request).execute();


    return response.body().string();

  }


  @ReactMethod
  public void speechToText (String SubKeyTest,String RegKey,String lang,String file) throws IOException, InterruptedException {
    String result = turnSpeechToText(SubKeyTest,RegKey,lang,file);

    // Send the result back to JavaScript
    getReactApplicationContext()
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit("NonVoidMethodResult", result);

  }

  public String turnSpeechToText(String SubKey,String RegKey,String lang,String file) throws IOException, InterruptedException {

    String subscriptionKey = SubKey; // replace this with your subscription key
    String region = RegKey; // replace this with the region corresponding to your subscription key, e.g. westus, eastasia

    // a common wave header, with zero audio length
    // since stream data doesn't contain header, but the API requires header to fetch format information, so you need post this header as first chunk for each query
    final byte[] WaveHeader16K16BitMono = new byte[] { 82, 73, 70, 70, 78, (byte)128, 0, 0, 87, 65, 86, 69, 102, 109, 116, 32, 18, 0, 0, 0, 1, 0, 1, 0, (byte)128, 62, 0, 0, 0, 125, 0, 0, 2, 0, 16, 0, 0, 0, 100, 97, 116, 97, 0, 0, 0, 0 };

    // build pronunciation assessment parameters
//        String referenceText = "Good morning.";
//        String pronAssessmentParamsJson = "{\"ReferenceText\":\"" + referenceText + "\",\"GradingSystem\":\"HundredMark\",\"Dimension\":\"Comprehensive\"}";
//        byte[] pronAssessmentParamsBase64 = new byte[0];
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            pronAssessmentParamsBase64 = Base64.getEncoder().encode(pronAssessmentParamsJson.getBytes("utf-8"));
//        }
//        String pronAssessmentParams = new String(pronAssessmentParamsBase64, "utf-8");

    // build request (when re-run below code in short time, the connect can be cached and reused behind, with lower connecting time cost)
    URL url = new URL("https://" + region + ".stt.speech.microsoft.com/speech/recognition/conversation/cognitiveservices/v1?language=" + lang);
    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
    connection.setRequestMethod("POST");
    connection.setDoOutput(true);
    connection.setChunkedStreamingMode(0);
    connection.setRequestProperty("Accept", "application/json;text/xml");
    connection.setRequestProperty("Content-Type", "audio/wav; codecs=audio/pcm; samplerate=16000");
    connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);
//        connection.setRequestProperty("Pronunciation-Assessment", pronAssessmentParams);

    // send request with chunked data
//        File file = new File("../../goodmorning.pcm");
    FileInputStream fileStream = new FileInputStream(file);
    byte[] audioChunk = new byte[1024];

    OutputStream outputStream = connection.getOutputStream();
    outputStream.write(WaveHeader16K16BitMono);
    int chunkSize = fileStream.read(audioChunk);
    while (chunkSize > 0)
    {
      Thread.sleep(chunkSize / 32); // to simulate human speaking rate
      outputStream.write(audioChunk, 0, chunkSize);
      chunkSize = fileStream.read(audioChunk);
    }

    fileStream.close();
    outputStream.flush();
    outputStream.close();
    byte[] responseBuffer = new byte[connection.getContentLength()];
    InputStream inputStream = connection.getInputStream();

    int offset = 0;
    int readBytes = inputStream.read(responseBuffer);
    while (readBytes != -1)
    {
      offset += readBytes;
      readBytes = inputStream.read(responseBuffer, offset, responseBuffer.length - offset);
    }

    String result = new String(responseBuffer, "utf-8"); // the result is a JSON, you can parse it with a JSON library

    return result;
  };
}
