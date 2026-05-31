package de.luhmer.owncloudnewsreader.services.podcast;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import java.util.List;
import java.util.Locale;

import de.luhmer.owncloudnewsreader.model.MediaItem;
import de.luhmer.owncloudnewsreader.model.TTSItem;

/**
 * Created by david on 31.01.17.
 */

public class TTSPlaybackService extends PlaybackService implements TextToSpeech.OnInitListener {

    private static final String TAG = "TTSPlaybackService";

    // A single speak() call drops text longer than getMaxSpeechInputLength(), so longer
    // articles have to be split and queued. The size is kept small on purpose: it stays clear
    // of multi byte scripts (e.g. Greek) and it is also the granularity we can resume at after
    // a pause, since TextToSpeech itself cannot pause and continue. The splitter cuts at
    // sentence ends, so the pieces still sound natural.
    private static final int CHUNK_SIZE = Math.min(TextToSpeech.getMaxSpeechInputLength(), 200);

    private static final String UTTERANCE_PREFIX = "tts_";

    private TextToSpeech ttsController;
    private List<String> chunks;
    private int currentChunk;
    private String lastUtteranceId;

    public TTSPlaybackService(Context context, PodcastStatusListener podcastStatusListener, MediaItem mediaItem) {
        super(podcastStatusListener, mediaItem);

        try {
            ttsController = new TextToSpeech(context, this);
            setStatus(PlaybackStateCompat.STATE_CONNECTING);

            if(ttsController != null) {
                ttsController.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        // Remember the piece we are on so play() can resume here after a pause
                        currentChunk = indexOf(utteranceId);
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        // Only finish once the last chunk was spoken
                        if (utteranceId != null && utteranceId.equals(lastUtteranceId)) {
                            podcastCompleted();
                        }
                    }

                    @Override
                    public void onError(String utteranceId) {
                        Log.e(TAG, "TTS error while speaking " + utteranceId);
                        setStatus(PlaybackStateCompat.STATE_ERROR);
                    }
                });
            } else {
                onInit(TextToSpeech.SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        pause();
        if (ttsController != null) {
            ttsController.shutdown();
            ttsController = null;
        }
    }

    @Override
    public void play() {
        // Resume at the piece that was interrupted. Start fresh if nothing was prepared yet.
        if (ttsController != null && chunks != null && !chunks.isEmpty()) {
            speakFrom(currentChunk);
        } else {
            onInit(TextToSpeech.SUCCESS);
        }
    }

    @Override
    public void pause() {
        if (ttsController != null && ttsController.isSpeaking()) {
            ttsController.stop();
            setStatus(PlaybackStateCompat.STATE_PAUSED);
        }
    }

    @Override
    public void playbackSpeedChanged(float currentPlaybackSpeed) {
        ttsController.setSpeechRate(currentPlaybackSpeed);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // Use the device language so the engine picks a fitting voice. The user can change
            // engine and voice through the system TTS settings shortcut in the app settings.
            ttsController.setLanguage(Locale.getDefault());

            String text = ((TTSItem) getMediaItem()).text;
            chunks = TtsTextSplitter.split(text, CHUNK_SIZE);

            if (chunks.isEmpty()) {
                setStatus(PlaybackStateCompat.STATE_ERROR);
                return;
            }

            speakFrom(0);
        } else {
            Log.e("TTS", "Initialization Failed!");
            ttsController = null;
        }
    }

    private void speakFrom(int startIndex) {
        lastUtteranceId = UTTERANCE_PREFIX + (chunks.size() - 1);

        for (int i = startIndex; i < chunks.size(); i++) {
            int queueMode = (i == startIndex) ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
            int result = ttsController.speak(chunks.get(i), queueMode, null, UTTERANCE_PREFIX + i);
            if (result == TextToSpeech.ERROR) {
                Log.e(TAG, "Failed to queue chunk " + i);
                setStatus(PlaybackStateCompat.STATE_ERROR);
                return;
            }
        }
        setStatus(PlaybackStateCompat.STATE_PLAYING);
    }

    private int indexOf(String utteranceId) {
        if (utteranceId != null && utteranceId.startsWith(UTTERANCE_PREFIX)) {
            try {
                return Integer.parseInt(utteranceId.substring(UTTERANCE_PREFIX.length()));
            } catch (NumberFormatException e) {
                Log.e(TAG, "Unexpected utterance id " + utteranceId);
            }
        }
        return 0;
    }

}
