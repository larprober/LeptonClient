package dev.lepton.event;

public class Cancellable {
    private boolean cancelled;

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    protected void reset() {
        cancelled = false;
    }
}
