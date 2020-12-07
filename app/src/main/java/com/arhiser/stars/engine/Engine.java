package com.arhiser.stars.engine;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

public class Engine {

    private Model model;
    private Render render;

    private SurfaceHolder.Callback callback;
    private SurfaceHolder surfaceHolder;

    private volatile boolean stopped;

    long time = System.nanoTime();

    Runnable threadRunnable = new Runnable() {
        @Override
        public void run() {
            while (!stopped)  {
                Canvas canvas;
                if (surfaceHolder == null || (canvas = surfaceHolder.lockCanvas()) == null) {
                    synchronized (Engine.this) {
                        try {
                            Engine.this.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    long timeElapsed = System.nanoTime() - time;
                    time = System.nanoTime();
                    model.update(timeElapsed);
                    render.draw(canvas, model);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    };

    public void stop() {
        this.stopped = true;
    }

    public Engine(SurfaceView surfaceView) {
        model = new Model();
        render = new Render();

        Thread engineThread = new Thread(threadRunnable, "EngineThread");
        engineThread.start();

        callback = new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                Engine.this.surfaceHolder = surfaceHolder;
                synchronized (Engine.this) {
                    Engine.this.notifyAll();
                }
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                Engine.this.surfaceHolder = surfaceHolder;
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
                Engine.this.surfaceHolder = null;
            }
        };

        surfaceView.getHolder().addCallback(callback);
    }
}
