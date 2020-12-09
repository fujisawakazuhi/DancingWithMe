# 手錶偵測程式開發

## 前置作業

* 智慧手錶需要先開啟**開發者模式**，Android Studio才能讀取。
* 在Android Studio中確認手錶有被讀取到，可在右上`Device`確認。

## 程式碼

### 開啟手錶偵測權限

* 在Manifest中加入`BODY_SENSORS`權限要求。[範例](https://github.com/fujisawakazuhi/WatchDevelopmentConcept/blob/master/DancingWithMe/watchdancingsensor/src/main/AndroidManifest.xml#L5)
* 可在偵測前再次確認權限是否被開啟。[範例](mainactivity114)
* 可參考[各權限說明文件](https://developer.android.com/reference/android/Manifest.permission)、[權限要求權限要求方法](https://developer.android.com/training/articles/wear-permissions)。

### 偵測

* 獲取裝置的管理器，手錶Sensor也是裝置之一。用getSystemService獲取相對應的裝置的Object。[範例](mainactivity127)

```java
SensorManager mSensorManager;
mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
```

> * 指定`mSensorManager`為Sensor的裝置管理器，getSystemService()的參數放要監聽的裝置。
> * （題外話）除了Sensor以外，若要監聽其他裝置相關訊息也一樣。例如 : wifi,battery...
* 註冊要偵測的Sensor。[範例](mainactivity130)


```java
Sensor mHeartRateSensor;
mHeartRateSensor = Objects.requireNonNull(mSensorManager).getDefaultSensor(Sensor.TYPE_HEART_RATE);
```

> * 指定`mHeartRateSensor`為心律的Sensor。
> * `Objects.requireNonNull`確保該物件有值，非必要。
> * 可用getSensorList()語法來列出此硬體的所有Sensor。[說明文件](https://developer.android.com/reference/android/hardware/SensorManager#getSensorList(int))
> * 如果有別的sensor要註冊就再設一個Sensor。[範例](mainactivity137)

* 設定在哪監聽Sensor。[範例](mainactivity162)

```java
 //                              1.          2.           3.
mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_NORMAL);
```

> 1. 用來監聽此Sensor的Activity 或 Class （在哪監聽）。
> 2. 目標Sensor （監聽哪個Sensor）。
> 3. 監聽頻率 （多久監聽一次）。


> * 若需要監聽多個Sensor，可將`1.`參數指定為相應的Class [範例1](mainactivity162)[範例2](mainactivity192)
> * [registerListener的說明文件](https://developer.android.com/reference/android/hardware/SensorManager#registerListener(android.hardware.SensorListener,%20int,%20int))

### 取得偵測數值

* 若有數值進來，會呼叫onSensorChanged()，並帶入含有偵測數值的串列。
* 心律的數值為串列中的第一項，若偵測的Sensor有多項數值則為依序存放在串列中。

```java
    public void onSensorChanged(SensorEvent event) {
        
        // 取得目前時間
        time_now = System.currentTimeMillis();
        double record_time = (double) (time_now - time_start) / 1000.0;
        // 將心律數值指定給變數
        double heartrate = event.values[0];
        // 處理心律數值格式
        DecimalFormat df = new DecimalFormat("##.00");
        heartrate = Double.parseDouble(df.format(heartrate));
        String mHeartRate = String.format(Locale.getDefault(), "%3.2f", heartrate);
        // 將目前心律顯示在前端介面上
        mTextView.setText(mHeartRate);
        // 將數值存入串列中
        record_time = Double.parseDouble(df.format(record_time));
        String mrecord_time = String.format(Locale.getDefault(), "%3.2f", record_time);
        data_heart_rate.add(mHeartRate);
        data_time_that.add(mrecord_time);
        Log.e(TAG, "HR: " + mHeartRate + "TIME:" + record_time);
        
    }
```
> * event.values[0]為心律數值。
> * 數值與時間處理的格式僅供參考。

## 相關連結
* 若要計算卡路里可參考此[程式碼](https://github.com/fujisawakazuhi/WatchDevelopmentConcept/blob/4be8f6c39728fdb823a922326b52146ec7733203/Sporden/watch/src/main/java/com/fjuim/watch/MainActivity.java#L1068)。
* 手錶程式：[Sporden](https://github.com/fujisawakazuhi/WatchDevelopmentConcept/blob/master/Sporden/watch/src/main/java/com/fjuim/watch/MainActivity.java) 、[DanceWithMe](https://github.com/fujisawakazuhi/WatchDevelopmentConcept/blob/master/Sporden/watch/src/main/java/com/fjuim/watch/MainActivity.java)