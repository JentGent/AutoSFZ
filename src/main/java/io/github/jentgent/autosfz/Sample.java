package io.github.jentgent.autosfz;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.MultichannelToMono;
import be.tarsos.dsp.filters.LowPassFS;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.util.fft.FFT;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicReference;

public class Sample {
    private final File file;
    private final String filePath;
    private double pitchHz;
    private double pitchKey;
    private double prob;

    public Sample(File file, PitchProcessor.PitchEstimationAlgorithm algorithm) throws Exception {
        this.file = file;
        filePath = file.getName();
        detectPitch(algorithm);
    }

    private double setPitchHz(double hz) {
        pitchHz = hz;
        pitchKey = AudioAnalysis.hzToKey(pitchHz);
        return pitchHz;
    }
    private void detectPitch(PitchProcessor.PitchEstimationAlgorithm algorithm) throws Exception {
        int bufferSize = 2048, overlap = 1024;
        float sampleRate;
        int channels;
        try (var stream = AudioSystem.getAudioInputStream(file)) {
            var format = stream.getFormat();
            sampleRate = format.getSampleRate();
            channels = format.getChannels();
        }
        var pitches = new ArrayList<PitchProb>();
        var dispatcher = AudioDispatcherFactory.fromFile(file, bufferSize, overlap);
        dispatcher.addAudioProcessor(new MultichannelToMono(channels, false));

        PitchDetectionHandler handler = (result, event) -> {
            float pitchHz = result.getPitch();
            float prob = result.getProbability();
            double timeStampSec = event.getTimeStamp();
            if (pitchHz > 20 && pitchHz < 4200) {
                pitches.add(new PitchProb(pitchHz, prob, event.getTimeStamp()));
                System.out.printf("%.2f Hz at %.2fs with probability %.2f\n", pitchHz, timeStampSec, prob);
            } else {
                System.out.printf("No pitch at %.2fs\n", timeStampSec);
            }
        };
        dispatcher.addAudioProcessor(new PitchProcessor(algorithm, sampleRate, bufferSize, handler));

        dispatcher.run();
        if (pitches.isEmpty()) throw new Exception("Pitch not detected");

        double finalPitch = 0, finalProb = 0;
        for (var pitchProb : pitches) {
            finalPitch += pitchProb.pitch * pitchProb.prob;
            finalProb += pitchProb.prob;
        }
        setPitchHz(finalPitch / finalProb);
        prob = finalProb;

//        pitches.sort(Comparator.comparing(PitchProb::pitch));
//        for (var pitchProb : pitches) {
//            double dist = (pitchProb.pitch - pitchHz) * (pitchProb.pitch - pitchHz);
//            prob += pitchProb.prob / (1 + dist);
//        }

        System.out.printf("Pitch: %.2f Hz\n", pitchHz);
        System.out.printf("Prob: %.2f\n", prob);
    }
    private record PitchProb(double pitch, double prob, double time) {}

    public double getProb() { return prob; }
    public String getPath() { return filePath; }
    public double getPitch() { return pitchHz; }
    public double getKey() { return pitchKey; }
}
