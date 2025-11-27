package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    
    private final DrawNumber model;
    private final List<DrawNumberView> views;
    private Configuration config;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        } 
        setConfiguration();
        this.model = new DrawNumberImpl(config.getMin(), config.getMax(), config.getAttempts());
    }

    private void setConfiguration() {
        Configuration.Builder builder = new  Configuration.Builder();
        
        try {
            config = builder.build();
        }  catch (final IOException e) {
            for (final DrawNumberView view: views) {
                view.displayError("problem during the configuration\nset default config");
            }
            config = builder.defaultConfig();
        } catch (final IllegalStateException e) {
            for (final DrawNumberView view: views) {
                view.displayError("problem during the configuration\nset default config");
            }
            config = builder.defaultConfig();
        }
        catch (final NullPointerException e) {
            for (final DrawNumberView view: views) {
                view.displayError("file daous't exist\nstart with set default config");
            }
            config = builder.defaultConfig();
        }

    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(
            new DrawNumberViewImpl(),
            new DrawNumberViewImpl(), 
            new PrintStreamView(System.out),
            new PrintStreamView("out.txt")
        );
    }

}
