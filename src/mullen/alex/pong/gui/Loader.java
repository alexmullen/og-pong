package mullen.alex.pong.gui;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import mullen.alex.jge.j2d.BufferedCanvas;
import mullen.alex.pong.World2D;
import mullen.alex.pong.engine.PongEngine;
import mullen.alex.pong.gui.activity.MainMenuActivitiy;


/**
 * The main class for Pong.
 *
 * @author  Alex Mullen
 *
 */
public final class Loader {
    /** The logger instance for this class. */
    private static final Logger LOG =
            Logger.getLogger(Loader.class.getName());
    /** The title of the window. */
    private static final String WINDOW_TITLE = "Pong";
    /** The initial width the window should be in pixels. */
    private static final int INITIAL_WINDOW_WIDTH = 800;
    /** The initial height the window should be in pixels. */
    private static final int INITIAL_WINDOW_HEIGHT = 600;
    /** The minimum width the window is allowed to be in pixels. */
    private static final int MINIMUM_WINDOW_WIDTH = 350;
    /** The minimum height the window is allowed to be in pixels. */
    private static final int MINIMUM_WINDOW_HEIGHT = 350;
    /** The background colour of the canvas. */
    private static final Color CANVAS_BACKGROUND_COLOUR = Color.DARK_GRAY;
    /** Holds the engine. */
    private static PongEngine engine;
    /** Holds the window Frame. */
    private static Frame window;
    /** Holds the canvas to render to. */
    private static Canvas canvas;
    /**
     * Private constructor to prevent this being instantiated.
     */
    private Loader() {
        // Constructor is intentionally empty.
    }
    /**
     * Main program entry point.
     *
     * @param args  supplied program arguments
     *
     * @throws InterruptedException       if we are interrupted
     * @throws InvocationTargetException  if it is thrown
     */
    public static void main(final String... args) throws
            InvocationTargetException, InterruptedException {
        /*
         * This thread forces Windows to use a 1ms timer resolution so we can
         * have much smoother animation.
         *
         * https://blogs.oracle.com/dholmes/entry/inside_the_hotspot_vm_clocks
         */
        final Thread sleeperTimerFixThread = new Thread(() -> {
            do {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.log(Level.SEVERE, e.toString(), e);
                }
            } while (true);
        }, "TimerResolutionFixDaemonThread");
        sleeperTimerFixThread.setPriority(Thread.MIN_PRIORITY);
        sleeperTimerFixThread.setDaemon(true);
        sleeperTimerFixThread.start();
        // Make sure to initialise in the EDT thread.
        EventQueue.invokeAndWait(() -> {
            init();
            final PongEngine.ConstructorArgs constructorArgs =
                    new PongEngine.ConstructorArgs();
            constructorArgs.canvas = canvas;
            constructorArgs.bufferedCanvas = new BufferedCanvas(canvas, 2);
            engine = PongEngine.createInstance(constructorArgs);
            engine.execute(() ->
                engine.getActivityService().startActivity(new MainMenuActivitiy(engine, new World2D(1024, 768)))
            );
        });
    }
    /**
     * Initialises the window and other required resources.
     */
    private static void init() {
        canvas = new Canvas();
        canvas.setIgnoreRepaint(true);
        canvas.setSize(INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
        canvas.setBackground(CANVAS_BACKGROUND_COLOUR);
        canvas.setFocusTraversalKeysEnabled(false);

        window = new Frame(WINDOW_TITLE);
        window.addWindowListener(new WindowAdapter() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void windowClosing(final WindowEvent e) {
                handleWindowClosing();
//              System.exit(0);   // not needed??
            }
        });

        window.setIgnoreRepaint(true);
        window.setResizable(true);
        window.setMinimumSize(
                new Dimension(MINIMUM_WINDOW_WIDTH, MINIMUM_WINDOW_HEIGHT));
        window.add(canvas);
        window.pack();
        window.setVisible(true);
    }
    /**
     * Handles the 'windowClosing' event.
     */
    private static void handleWindowClosing() {
        try {
            /*
             * Shutdown and wait so that we do not prematurely dispose
             * of UI resources before it has shutdown.
             */
            engine.shutdownAndWait();
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.log(Level.WARNING, 
                    "Interruptedd whilst waiting on engine shutdown.",
                    e);
        }
        window.setVisible(false);
        window.dispose();
    }
}
