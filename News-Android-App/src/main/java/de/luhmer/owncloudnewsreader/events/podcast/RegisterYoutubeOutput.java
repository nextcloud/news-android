package de.luhmer.owncloudnewsreader.events.podcast;

import com.google.android.youtube.player.YouTubePlayer;

public class RegisterYoutubeOutput {

    public RegisterYoutubeOutput(YouTubePlayer youTubePlayer, boolean wasRestored) {
        this.youTubePlayer = youTubePlayer;
        this.wasRestored = wasRestored;
    }

    public YouTubePlayer youTubePlayer;
    public boolean wasRestored;

}
