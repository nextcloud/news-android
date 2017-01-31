package de.luhmer.owncloudnewsreader.services.podcast;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;

import de.luhmer.owncloudnewsreader.model.MediaItem;
import de.luhmer.owncloudnewsreader.model.TTSItem;

/**
 * Created by david on 31.01.17.
 */

public class TTSPlaybackService extends PlaybackService implements TextToSpeech.OnInitListener {
    private TextToSpeech ttsController;

    public TTSPlaybackService(Context context, PodcastStatusListener podcastStatusListener, MediaItem mediaItem) {
        super(context, podcastStatusListener, mediaItem);

        try {
            ttsController = new TextToSpeech(context, this);
            setStatus(Status.PREPARING);

            if(ttsController == null) {

                ttsController.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onDone(String utteranceId) {
                        podcastCompleted();
                    }

                    @Override public void onStart(String utteranceId) {}
                    @Override public void onError(String utteranceId) {}
                });
            }
            else
                onInit(TextToSpeech.SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        pause();
        ttsController.shutdown();
        ttsController = null;
    }

    @Override
    public void play() {
        onInit(TextToSpeech.SUCCESS);//restart last tts
    }

    @Override
    public void pause() {
        if (ttsController.isSpeaking()) {
            ttsController.stop();
            setStatus(Status.PAUSED);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            /*
            int result = ttsController.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                ttsController.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }*/

            HashMap<String,String> ttsParams = new HashMap<>();
            ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"dummyId");
            ttsController.speak(((TTSItem)getMediaItem()).text, TextToSpeech.QUEUE_FLUSH, ttsParams);
            setStatus(Status.PLAYING);
        } else {
            Log.e("TTS", "Initilization Failed!");
            ttsController = null;
        }
    }

}
