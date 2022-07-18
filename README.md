# MyApplication

##PahoMqttActivity为测试的Activity

## 证书
res/raw中需要加三个证书文件，没有raw文件夹就在res下创建，三个文件分别为AmazonRootCA1，private.pem.key, cert.pem.crt文件，在AWS console 创建IoT Core证书时下载写进入后，文件名需要改为小写字母，不能有其他字符，

证书在PahoMqttActivity中178行initCert()方法中初始化，

代码需要创建SSLSocketFactory，来读取raw中的三个证书文件，基于'org.bouncycastle:bcpkix-jdk15on:1.59'写；

## mqtt 终端节点连接地址
PahoMqttActivity有一个终端节点参数，需要修改，代码49行mBroker，替换为IoT Core Console设置中的endpoint

mqtt client为paho-mqtt
