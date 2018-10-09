package mullen.alex.pong.gui.activity;

import java.util.Objects;

import mullen.alex.jge.FixedUpdateEngine;
import mullen.alex.jge.FixedUpdateEngine.BetweenTickTask;
import mullen.alex.jge.FixedUpdateEngine.TickTask;

public abstract class TickAnimator implements TickTask, BetweenTickTask {
    final FixedUpdateEngine engine;
    TickAnimator(final FixedUpdateEngine e) {
        engine = Objects.requireNonNull(e); 
    }
    public void execute() {
        engine.registerBetweenTickTask(this);
        engine.registerTickTask(this);
    }
    public void cancel() {
        engine.unregisterBetweenTickTask(this);
        engine.unregisterTickTask(this);
        onAnimateFinished();
    }
    @Override
    public void onBetweenTick(int tick, double delta) {
        onAnimate(delta);
    }
    @Override
    public void onTick(int tick) {
        engine.unregisterBetweenTickTask(this);
        engine.unregisterTickTask(this);
        onAnimateFinished();
    }
    abstract void onAnimate(double delta);
    abstract void onAnimateFinished();
}
