package de.luhmer.owncloudnewsreader.events.podcast;

public class RegisterYoutubeOutput {

    public RegisterYoutubeOutput(Object youTubePlayer, boolean wasRestored) {
        this.youTubePlayer = youTubePlayer;
        this.wasRestored = wasRestored;
    }

    public Object youTubePlayer; // (Type: com.google.android.youtube.player.YouTubePlayer;)
    public boolean wasRestored;

}
