package mullen.alex.pong.engine;

import java.awt.Canvas;

import mullen.alex.jge.AbstractFixedUpdateEngine;
import mullen.alex.jge.DefaultTickSchedulerService;
import mullen.alex.jge.TickSchedulerService;
import mullen.alex.jge.input.AWTKeyboardService;
import mullen.alex.jge.input.AWTMouseService;
import mullen.alex.jge.input.KeyboardService;
import mullen.alex.jge.input.MouseService;
import mullen.alex.jge.j2d.BufferedCanvas;
import mullen.alex.jge.j2d.Java2DRenderingService;

/**
 * An engine tailored for pong that updates at a specified fixed interval.
 * <p>
 * Interfacing with the engine outside the engine thread should only be done
 * through scheduling a task via {@link #execute(Runnable)}.
 *
 * @author  Alex Mullen
 *
 */
public final class PongEngine extends AbstractFixedUpdateEngine {
    /** The number of updates per second to update the game at. */
    private static final int UPDATES_PER_SECOND = 60;
    /** The Java 2D rendering instance to use. */
    private Java2DRenderingService renderingService;
    /** The mouse service to use. */
    private AWTMouseService mouseService;
    /** The keyboard service to use. */
    private AWTKeyboardService keyboardService;
    /** The tick scheduler service to use. */
    private DefaultTickSchedulerService schedulerService;
    /** The Java 2D activity service instance to use. */
    private PongActivityService activityService;
    /**
     * A class for holding the required constructor arguments required for
     * instantiating the outer class.
     * <p>
     * Using an object like this helps improve readability and maintainability
     * when a constructor requires many arguments.
     *
     * @author  Alex Mullen
     *
     */
    public static class ConstructorArgs {
        /** The canvas to render to. */
        public Canvas canvas;
        /** The buffered canvas to render to. */
        public BufferedCanvas bufferedCanvas;
    }
    /**
     * Creates a new instance.
     *
     * @see AbstractFixedUpdateEngine#AbstractFixedUpdateEngine(int)
     */
    private PongEngine() {
        super(UPDATES_PER_SECOND);
    }
    /**
     * Initialises the object.
     * <p>
     * <b>This should only be called by the static instance creation method.</b>
     *
     * @param args  the supplied arguments
     *
     * @throws NullPointerException  if <code>args</code> is <code>null</code>
     *                               or any of its supplied fields are
     *                               <code>null</code>
     */
    private void init(final ConstructorArgs args) {
        renderingService =
                new Java2DRenderingService(this, args.bufferedCanvas);
        mouseService = new AWTMouseService(args.canvas);
        keyboardService = new AWTKeyboardService(this, args.canvas);
        schedulerService = new DefaultTickSchedulerService(this);
        activityService = new PongActivityService(this);
    }
    @Override
    protected void onEngineStarted() {
        schedulerService.onServiceStarted();
        renderingService.onServiceStarted();
        mouseService.onServiceStarted();
        keyboardService.onServiceStarted();
        activityService.onServiceStarted();
    }
    @Override
    protected void onEngineStopped() {
        renderingService.onServiceStopped();
        mouseService.onServiceStopped();
        keyboardService.onServiceStopped();
        activityService.onServiceStopped();
        schedulerService.onServiceStopped();
    }
    /**
     * Gets the rendering service.
     *
     * @return  the renderingService
     */
    public Java2DRenderingService getRenderingService() {
        return renderingService;
    }
    /**
     * Gets the mouse service.
     *
     * @return  the mouseService
     */
    public MouseService getMouseService() {
        return mouseService;
    }
    /**
     * Gets the keyboard service.
     *
     * @return  the keyboardService
     */
    public KeyboardService getKeyboardService() {
        return keyboardService;
    }
    /**
     * Gets the scheduling service.
     *
     * @return  the schedulerService
     */
    public TickSchedulerService getSchedulerService() {
        return schedulerService;
    }
    /**
     * Gets the activity service.
     *
     * @return  the activityService
     */
    public PongActivityService getActivityService() {
        return activityService;
    }
    /**
     * Creates and starts a new {@link PongEngine} instance.
     *
     * @param args  the supplied arguments required to initialise
     * @return      the new instance
     */
    public static PongEngine createInstance(final ConstructorArgs args) {
        final PongEngine engine = new PongEngine();
        engine.init(args);
        engine.start();
        return engine;
    }
}
