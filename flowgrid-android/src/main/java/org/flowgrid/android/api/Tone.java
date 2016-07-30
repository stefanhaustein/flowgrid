package org.flowgrid.android.api;

import org.flowgrid.model.Sound;

import android.util.Log;

public class Tone {
  private static final int SAMPLE_RATE = 16000;
  
  public static Sound newTone(double freqOfTone, double duration) {
    if (duration == 0 || freqOfTone > SAMPLE_RATE / 3 || freqOfTone < 8) {
      return new SoundImpl(SAMPLE_RATE);
    }

    int waveLenght = (int) (SAMPLE_RATE / freqOfTone);

    Log.d("FlowGrid", "freq: " + freqOfTone + " waveLength: " + waveLenght);

    freqOfTone = ((double) SAMPLE_RATE)  / waveLenght;

    int samplesPerUnit = 2000 / waveLenght;
    int unitLength = samplesPerUnit * waveLenght;
    
    Log.d("FlowGrid", "freq: " + freqOfTone + " samplesPerUnit: " + samplesPerUnit + " unitLength: "+ unitLength);
    
    float[] startData = new float[unitLength];
    float[] middleData = new float[unitLength];
    float[] endData = new float[unitLength];
    
    for (int i = 0; i < unitLength; ++i) {
      double sample = Math.sin(freqOfTone * 2 * Math.PI * i / SAMPLE_RATE);
      
      // Nullstellen sind bei sampleRate / freqOfTone
      
      startData[i] = (float) ((sample * i) / unitLength);
      middleData[i] = (float) sample; 
      endData[i] = (float) ((sample * (unitLength - i)) / unitLength);
    }

    SoundImpl result = new SoundImpl(SAMPLE_RATE);
    result.addSamples(startData);
    int loop = (int) (duration * SAMPLE_RATE / unitLength);
    for (int i = 0; i < loop; i++) {
      result.addSamples(middleData);
    }
    result.addSamples(endData);
    return result;
  }
}
