package de.luhmer.owncloudnewsreader.events.podcast;

import android.view.SurfaceView;
import android.view.View;

public class RegisterVideoOutput {

    public RegisterVideoOutput(SurfaceView surfaceView, View parentResizableView) {
        this.surfaceView = surfaceView;
        this.parentResizableView = parentResizableView;
    }

    public SurfaceView surfaceView;
    public View parentResizableView;
}
