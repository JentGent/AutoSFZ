package io.github.jentgent.autosfz;

import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class AudioAnalysis {
    public static double hzToKey(double pitch) {
        return 12 * Math.log(pitch / 440) / Math.log(2) + 69;
    }
    public static double keyToHz(double key) { return Math.pow(2, (key - 69) / 12) * 440; }
    public static PitchEstimationAlgorithm getPitchAlgo(String algo) {
        return switch (algo) {
            case "YIN" -> PitchEstimationAlgorithm.YIN;
            case "MPM" -> PitchEstimationAlgorithm.MPM;
            case "FFT_YIN" -> PitchEstimationAlgorithm.FFT_YIN;
            case "DYNAMIC_WAVELET" -> PitchEstimationAlgorithm.DYNAMIC_WAVELET;
            case "FFT_PITCH" -> PitchEstimationAlgorithm.FFT_PITCH;
            case "AMDF" -> PitchEstimationAlgorithm.AMDF;
            case "FOURIER" -> null;
            default -> null;
        };
    }
    public static double hzPresence(float[] signal, double sampleRate, double hz) {
        double real = 0, imag = 0;
        double phaseInc = 2 * Math.PI * hz / sampleRate;
        double cosInc = Math.cos(phaseInc), sinInc = Math.sin(phaseInc);
        double cos = 1, sin = 0;
        for (int i = 0; i < signal.length; i++) {
            real += signal[i] * cos;
            imag -= signal[i] * sin;
            double newCos = cos * cosInc - sin * sinInc;
            sin = cos * sinInc + sin * cosInc;
            cos = newCos;
        }
        return real * real + imag * imag;
    }
    public static double autocorrelation(float[] signal, double sampleRate, double hz) {
        double corr = 0;
        double offset = sampleRate / hz;
        for (int i = 0; i < signal.length - offset; i++) {
            double a = (i + offset) % 1;
            double offsetSignal = signal[i + (int)offset] * (1 - a) + signal[i + (int)offset] * a;
//            corr += signal[i] * offsetSignal;
            corr -= (signal[i] - offsetSignal) * (signal[i] - offsetSignal);
        }
        return corr / (signal.length - offset);
    }
}
