package com.aws.snx;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class PahoMqttActivity extends AppCompatActivity {

    final String TAG = "PahoMqttActivity";
    // IoT Core Console的IoT Core endpoint组合，规则为 ssl://iot-core-endpoint:8883
    final String mBroker ="ssl://替换此处终端节点:8883";
    // 测试的Mqtt Topic
    String mTopic;
    // mqtt的client id
    String mClientId ="android_client_snx";
    MqttClient mSampleClient;
    MemoryPersistence mPersistence = new MemoryPersistence();

    // 证书信息
    InputStream mCaCrtFile;
    InputStream mCrtFile;
    InputStream mKeyFile;


    EditText mTopicEdit;
    TextView mMsgText;
    Button mSubcribeBtn, mUnSubcribeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paho_mqtt);
        // 初始化控件
        mTopicEdit = findViewById(R.id.mqtt_topic);
        mMsgText = findViewById(R.id.topic_info);
        mSubcribeBtn = findViewById(R.id.subcribe_btn);
        mUnSubcribeBtn = findViewById(R.id.unsubcribe_btn);
        mTopic = mTopicEdit.getText().toString();
        // 按钮添加监听
        mSubcribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTopic = mTopicEdit.getText().toString();
                try {
                    subcribeTopic(mTopic, 1);
                    mMsgText.setText(mMsgText.getText().toString() + "订阅" + mTopic + "\n");
                } catch (MqttException e) {
                    e.printStackTrace();
                    Log.e(TAG, "订阅" + mTopic + "出错");
                }
            }
        });

        mUnSubcribeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTopic = mTopicEdit.getText().toString();
                try {
                    unsubcribeTopic(mTopic);
                    mMsgText.setText(mMsgText.getText().toString() + "取消订阅" + mTopic  + "\n");
                } catch (MqttException e) {
                    e.printStackTrace();
                    Log.e(TAG, "取消订阅" + mTopic + "出错");
                }
            }
        });
        Log.i(TAG, "订阅主题为：" + mTopic);
        // 初始化证书
        initCert();
        try {
            mSampleClient = new MqttClient(mBroker, mClientId, mPersistence);
            final MqttConnectOptions connOpts = new MqttConnectOptions();
            System.out.println("Connecting to broker: " + mBroker);

            final String[] topicFilters=new String[]{mTopic};
            final int[]qos={1};
            connOpts.setServerURIs(new String[] { mBroker });
            connOpts.setSocketFactory(getSocketFactory(mCaCrtFile, mCrtFile, mKeyFile, ""));
            // MQTT clearSession 参数，设置确定是否继续接受离线消息
            connOpts.setCleanSession(false);
            // MQTT keepalive 参数，与离线时间有关，支持多久的掉线时间
            connOpts.setKeepAliveInterval(600);
            connOpts.setAutomaticReconnect(true);
            final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>());
            mSampleClient.setCallback(new MqttCallbackExtended() {
                public void connectComplete(boolean reconnect, String serverURI) {
                    System.out.println("connect success");
                    //连接成功，需要上传客户端所有的订阅关系
                    try
                    {
                        mSampleClient.subscribe(topicFilters, qos);
                    } catch(Exception me)
                    {
                        me.printStackTrace();
                    }
                }

                public void connectionLost(Throwable throwable) {
                    Log.d(TAG, "mqtt connection lost");
                }

                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

                    String msg = "接收到订阅主题消息\n时间：" + getCurrentDataFormat() + "\nTopic:" + topic + "\n消息内容: " +
                            new String(mqttMessage.getPayload());
                    Log.i(TAG, msg);
                    PahoMqttActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMsgText.setText(mMsgText.getText().toString() + msg + "\n-------分割线-------\n");
                        }
                    });

                }

                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    Log.i(TAG, "deliveryComplete:" + iMqttDeliveryToken.getMessageId());
                }

            });
            mSampleClient.connect(connOpts);
            mSampleClient.subscribe(topicFilters,qos);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getCurrentDataFormat() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format.format(date);
    }


    // 初始化证书
    public void initCert() {
        try {
            mCaCrtFile = this.getResources().openRawResource(R.raw.amazonca1);
            mCrtFile = this.getResources().openRawResource(R.raw.certpem);
            mKeyFile = this.getResources().openRawResource(R.raw.privatepem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void subcribeTopic(String topic, int qos) throws MqttException {
        mSampleClient.subscribe(topic, qos, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = "接收到订阅主题消息\n时间：" + getCurrentDataFormat() + "\nTopic:" + topic + "\n消息内容: " +
                        new String(message.getPayload());
                Log.i(TAG, msg);
                PahoMqttActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMsgText.setText(mMsgText.getText().toString() + msg + "\n-------分割线-------\n");
                    }
                });

            }
        });
    }

    public void unsubcribeTopic(String topic) throws MqttException {
        mSampleClient.unsubscribe(topic);
    }

    // SSLSocketFactory 实现双向TLS认证，因为IoT Core需要双向TLS认证
    public static SSLSocketFactory getSocketFactory(InputStream caCrtFile, InputStream crtFile, InputStream keyFile,
                                                    String password) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        // load CA certificate
        X509Certificate caCert = null;

        BufferedInputStream bis = new BufferedInputStream(caCrtFile);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        while (bis.available() > 0) {
            caCert = (X509Certificate) cf.generateCertificate(bis);
        }
        // load client certificate
        bis = new BufferedInputStream(crtFile);
        X509Certificate cert = null;
        while (bis.available() > 0) {
            cert = (X509Certificate) cf.generateCertificate(bis);
        }
        // load client private cert
        PEMParser pemParser = new PEMParser(new InputStreamReader(keyFile));
        Object object = pemParser.readObject();
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        KeyPair key = converter.getKeyPair((PEMKeyPair) object);

        KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
        caKs.load(null, null);
        caKs.setCertificateEntry("cert-certificate", caCert);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(caKs);

        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("certificate", cert);
        ks.setKeyEntry("private-cert", key.getPrivate(), password.toCharArray(),
                new java.security.cert.Certificate[]{cert});
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, password.toCharArray());

        SSLContext context = SSLContext.getInstance("TLSv1.2");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }
}