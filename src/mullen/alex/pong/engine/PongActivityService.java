package mullen.alex.pong.engine;

import java.util.Objects;

import mullen.alex.jge.TickSchedulerService.Task;
import mullen.alex.pong.engine.PongActivity.Context;
import mullen.alex.pong.engine.PongActivity.StoppedReason;
import mullen.alex.pong.gui.activity.NullActivity;

/**
 * An activity service implementation that renders activities using Java 2D.
 *
 * @author  Alex Mullen
 *
 */
public class PongActivityService {
    /** The engine instance we are using. */
    private final PongEngine engine;
    /** Holds the current activity instance. */
    private PongActivity currentActivity;
    /** Holds a reference to the task calling the activity's update method. */
    private Task activityUpdateTask;
    /**
     * Creates a new instance that uses the specified engine.
     *
     * @param eng  the engine instance
     */
    public PongActivityService(final PongEngine eng) {
        engine = Objects.requireNonNull(eng);
        currentActivity = new NullActivity();
    }
    public final void onServiceStarted() {
        engine.getRenderingService().setRenderer(currentActivity);
        activityUpdateTask =
                engine.getSchedulerService().scheduleRepeatedly(currentActivity::update, 0, 1);
        currentActivity.onActivityStarted(
                new Java2DActivityContext(engine, currentActivity));
    }
    public final void onServiceStopped() {
        activityUpdateTask.cancel();
        engine.getRenderingService().setRenderer(null);
        currentActivity.onActivityStopped(StoppedReason.SERVICE_SHUTDOWN);
    }
    /**
     * Starts the specified activity.
     * <p>
     * Only one activity is active at any time so the previous activity will
     * have its close down method invoked.
     * 
     * @param activity
     */
    public void startActivity(final PongActivity activity) {
        Objects.requireNonNull(activity);
        activityUpdateTask.cancel();
        final PongActivity previousActivity = currentActivity;
        previousActivity.onActivityStopped(StoppedReason.ACTIVITY_STARTED);
        currentActivity = activity;
        engine.getRenderingService().setRenderer(currentActivity);
        activityUpdateTask =
                engine.getSchedulerService().scheduleRepeatedly(currentActivity::update, 0, 1);
        currentActivity.onActivityStarted(new Java2DActivityContext(engine, previousActivity));
    }
    /**
     * Gets the currently active activity.
     *
     * @return  the activity
     */
    public final PongActivity getActiveActivity() {
        return currentActivity;
    }
    private static class Java2DActivityContext implements Context {
        private final PongEngine engine;
        private final PongActivity previousActivity;
        public Java2DActivityContext(final PongEngine eng,
                final PongActivity prevActivity) {
            engine = eng;
            previousActivity = prevActivity;
        }
        @Override
        public PongEngine getEngine() {
            return engine;
        }
        @Override
        public PongActivity getPreviousActivity() {
            return previousActivity;
        }
    }
}
