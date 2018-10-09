package mullen.alex.pong.engine;

import mullen.alex.jge.j2d.Java2DRenderingService;

/**
 * Defines an interface for representing an activity.
 *
 * @author  Alex Mullen
 *
 */
public interface PongActivity extends Java2DRenderingService.Renderer {
    /**
     * Invoked every engine tick.
     */
    void update();
    /**
     * Invoked when the activity is started.
     *
     * @param context  the context object for the activity
     */
    void onActivityStarted(Context context);
    /**
     * Invoked when the activity is stopped.
     *
     * @param reason  the reason the activity is being stopped
     */
    void onActivityStopped(StoppedReason reason);
    /**
     * Defines an interface for representing context about when an activity is
     * started or stopped.
     *
     * @author  Alex Mullen
     *
     */
    public interface Context {
        /**
         * Gets the engine instance we are running on.
         *
         * @return  the engine instance
         */
        PongEngine getEngine();
        /**
         * Gets the activity that was previously active before the current one.
         *
         * @return  the previous activity
         */
        PongActivity getPreviousActivity();
    }
    public enum StoppedReason {
        ACTIVITY_STARTED,
        SERVICE_SHUTDOWN,
    }
}
