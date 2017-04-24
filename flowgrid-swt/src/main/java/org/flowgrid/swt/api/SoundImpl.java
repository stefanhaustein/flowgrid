package org.flowgrid.swt.api;


import org.flowgrid.model.Sound;
import org.flowgrid.model.annotation.Blocking;

import java.util.ArrayList;

public class SoundImpl implements Sound {
  private static final String TAG = "SoundImpl";
  private static int count;
  ArrayList<float[]> data = new ArrayList<float[]>();
  int sampleRate;
  final int id = ++count;


  public SoundImpl(int sampleRate) {
    this.sampleRate = sampleRate;
  }

  
  public void addSamples(float[] samples) {
    data.add(samples);
  }
  
  
  @Blocking
  public void play() {
    //new Thread(new Player()).start();
    new Player().run();
  }
  
  class Player implements Runnable {

    @Override
    public void run() {
      System.out.println("FIXME Sound.Player.run()");   // FIXME
      /*
      final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
          sampleRate, AudioFormat.CHANNEL_OUT_MONO,
          AudioFormat.ENCODING_PCM_16BIT, 2048 + AudioTrack.getMinBufferSize(
              sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT),
          AudioTrack.MODE_STREAM);

      try {
        audioTrack.play();
    
        short[] buf = null;
        for (float[] sample: data) {
          int len = sample.length;
          if (buf == null || len > buf.length) {
            buf = new short[len];
          }
          for (int i = 0; i < len; i++) {
            buf[i] = (short) (sample[i] * Short.MAX_VALUE);
          }
          audioTrack.write(buf, 0, len);
        }        

        audioTrack.release();
        Log.i(TAG, "audio track released");
      } catch(Exception e) {
        Log.e(TAG, "audio track issue", e);
      }
      */
    }
  }

  @Override
  public int samplingRate() {
    return sampleRate;
  }

  @Override
  public int sampleCount() {
    int sampleCount = 0;
    for (float[] segment : data) {
      sampleCount += segment.length;
    }
    return sampleCount;
  }

  @Override
  public double length() {
    return (double) sampleCount() / (double) samplingRate();
  }

  @Override
  public void getData(float[] target) {
    double advance = (double) sampleCount() / (double) target.length;
    float[] segment = new float[0];
    double sourcePosition = 0;
    int segmentIndex = 0;
    outer:
    for (int i = 0; i < target.length; i++) {
      int sourceIndex = (int) sourcePosition;
      sourcePosition += advance;
      while(sourceIndex >= segment.length && segmentIndex < data.size()) {
        if (segmentIndex >= data.size()) {
          break outer;
        }
        sourceIndex -= segment.length;
        sourcePosition -= segment.length;
        segment = data.get(segmentIndex++);
      }
      target[i] = segment[sourceIndex];
    }
  }

  public String toString() {
    return "Sound#" + id;
  }
}
