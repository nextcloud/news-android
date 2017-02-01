package de.luhmer.owncloudnewsreader.events.podcast;

public class TogglePlayerStateEvent {

    public enum State { Toggle, Play, Pause }
    private State mState = State.Toggle;

    public TogglePlayerStateEvent() { }

    public TogglePlayerStateEvent(State state) {
        this.mState = state;
    }

    public State getState() {
        return mState;
    }

}
