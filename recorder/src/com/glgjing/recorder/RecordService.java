package com.glgjing.recorder;

import android.app.Service;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Binder;
import android.os.Environment;
import android.os.HandlerThread;
import android.os.IBinder;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class RecordService extends Service {
  private MediaProjection mediaProjection;
  private MediaRecorder mediaRecorder;
  private VirtualDisplay virtualDisplay;

  private boolean running;
  private int width = 720;
  private int height = 1080;
  private int dpi;


  @Override
  public IBinder onBind(Intent intent) {
    return new RecordBinder();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    running = false;
    mediaRecorder = new MediaRecorder();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public void setMediaProject(MediaProjection project) {
    mediaProjection = project;
  }

  public boolean isRunning() {
    return running;
  }

  public void setConfig(int width, int height, int dpi) {
    this.width = width;
    this.height = height;
    this.dpi = dpi;
  }

  public boolean startRecord() {
    if (mediaProjection == null || running) {
      return false;
    }

    initRecorder();
    createVirtualDisplay();
    mediaRecorder.start();
    running = true;
    return true;
  }

  public boolean stopRecord() {
    if (!running) {
      return false;
    }
    running = false;
    mediaRecorder.stop();
    mediaRecorder.reset();
    virtualDisplay.release();
    mediaProjection.stop();

    return true;
  }

  private void createVirtualDisplay() {
    virtualDisplay = mediaProjection.createVirtualDisplay("MainScreen", width, height, dpi,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mediaRecorder.getSurface(), null, null);
  }

  private void initRecorder() {
    //音频源
    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    //视频源
    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
    //音视频封装格式，这个需要兼容下面设置的编码格式,参考官方文档：https://developer.android.com/guide/topics/media/media-formats
    mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    //文件名称
    mediaRecorder.setOutputFile(getsaveDirectory() + System.currentTimeMillis() + ".mp4");
    //文件大小
    mediaRecorder.setVideoSize(width, height);
    //视频编码格式
    mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
    //音频编码格式
    mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
    //码流
    mediaRecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
    //帧率
    mediaRecorder.setVideoFrameRate(30);
    try {
      mediaRecorder.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getsaveDirectory() {
    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
      String rootDir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + "ScreenRecord" + "/";

      File file = new File(rootDir);
      if (!file.exists()) {
        if (!file.mkdirs()) {
          return null;
        }
      }

      Toast.makeText(getApplicationContext(), rootDir, Toast.LENGTH_SHORT).show();

      return rootDir;
    } else {
      return null;
    }
  }

  public class RecordBinder extends Binder {
    public RecordService getRecordService() {
      return RecordService.this;
    }
  }
}