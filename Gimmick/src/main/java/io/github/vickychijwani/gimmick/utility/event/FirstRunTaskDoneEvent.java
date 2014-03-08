package io.github.vickychijwani.gimmick.utility.event;

public class FirstRunTaskDoneEvent {

    private boolean mbSucceeded;

    public FirstRunTaskDoneEvent(boolean succeeded) {
        mbSucceeded = succeeded;
    }

    public boolean wasSuccessful() {
        return mbSucceeded;
    }

}
