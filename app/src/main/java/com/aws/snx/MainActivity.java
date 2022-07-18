package com.aws.snx;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.services.iotdata.AWSIotDataClient;
import com.amazonaws.services.iotdata.model.PublishRequest;

import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String sessionToken = "";
        String accessKey = "";
        String secretKey = "";
        
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                testPublish();
//            }
//        }).start();
        Intent i = new Intent(this, PahoMqttActivity.class);
        startActivity(i);

    }

    //生产建议调用后端接口， 通过AWS STS获取临时访问访问凭证
    public Map<String, String> requestSTSToken() {
        return new HashMap<String, String>();
    }

    public void testPublish() {
        AWSIotDataClient client = new AWSIotDataClient(new snxSessionCredentials());
        client.setRegion(Region.getRegion("us-east-2"));
        PublishRequest request = new PublishRequest();
        try {
            String TestTopic = "$aws/things/test/shadow/name/shadow/update";
            String payload = "{\n" +
                    "  \"state\": {\n" +
                    "    \"desired\": {\n" +
                    "      \"welcome\": \"aws-iot\", \n" +
                    "      \"rua\": \"" + System.currentTimeMillis() + "\", \n" +
                    "      \"android\": \"android_message\"\n" +
                    "    },\n" +
                    "    \"reported\": {\n" +
                    "      \"welcome\": \"aws-iot\"\n" +
                    "    }\n" +
                    "  }\n" +
                    "}";
            request.withTopic(TestTopic).withPayload(ByteBuffer.wrap(payload.getBytes("utf-8")));
            client.publish(request);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //生产不建议
    class snxSessionCredentials implements AWSCredentials {

        String accessKey = "REPLACE ME";
        String secretKey = "REPLACE ME";

        @Override
        public String getAWSAccessKeyId() {
            if(accessKey == null) {
                try {
                    throw new Exception("accessKey 为空");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return accessKey;
        }

        @Override
        public String getAWSSecretKey() {
            if(secretKey == null) {
                try {
                    throw new Exception("secretKey 为空");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return secretKey;
        }
    }


    //生产建议
    class cecocecoSessionCredentials implements AWSSessionCredentials {

        String sessionToken;
        String accessKey;
        String secretKey;

        protected cecocecoSessionCredentials(String accessKey, String secretKey, String sessionToken) {
            this.accessKey = accessKey;
            this.secretKey = secretKey;
            this.sessionToken = sessionToken;
        }

        @Override
        public String getSessionToken() {
            if(sessionToken == null) {
                try {
                    throw new Exception("session 为空");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return sessionToken;
        }

        @Override
        public String getAWSAccessKeyId() {
            if(accessKey == null) {
                try {
                    throw new Exception("accessKey 为空");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return accessKey;
        }

        @Override
        public String getAWSSecretKey() {
            if(secretKey == null) {
                try {
                    throw new Exception("secretKey 为空");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return secretKey;
        }
    }
}