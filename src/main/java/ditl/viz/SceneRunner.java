/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011-2012 John Whitbeck <john@whitbeck.fr>                    *
 *                                                                             *
 * DITL is free software: you can redistribute it and/or modify                *
 * it under the terms of the GNU General Public License as published by        *
 * the Free Software Foundation, either version 3 of the License, or           *
 * (at your option) any later version.                                         *
 *                                                                             *
 * DITL is distributed in the hope that it will be useful,                     *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the               *
 * GNU General Public License for more details.                                *
 *                                                                             *
 * You should have received a copy of the GNU General Public License           *
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.       *
 *******************************************************************************/
package ditl.viz;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ditl.Runner;

@SuppressWarnings("serial")
public abstract class SceneRunner extends Component {

    protected Runner runner;
    private final Timer timer;
    private int fps = 20;
    private int speed = 1;
    private final List<TimeChangeListener> timeListeners = new LinkedList<TimeChangeListener>();
    private final List<PlayListener> playListeners = new LinkedList<PlayListener>();
    private boolean blockSeek = false;
    private final ChangeListener sliderListener;
    public long tics_per_second = 1L;

    public SceneRunner() {
        timer = new Timer(1000 / fps, timeOutListener());
        sliderListener = new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (!blockSeek) {
                    final JSlider slider = (JSlider) e.getSource();
                    final long time = slider.getValue() * tics_per_second;
                    pause();
                    seek(time);
                } else
                    blockSeek = false;
            }
        };
    }

    public void setTicsPerSecond(long ticsPerSecond) {
        tics_per_second = ticsPerSecond;
        updateIncrTime();
    }

    protected long incrTime() {
        return ((tics_per_second * speed) / fps);
    }

    protected void updateIncrTime() {
        runner.setIncrTime(incrTime());
    }

    public ActionListener timeOutListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                step();
            }
        };
    }

    public void addTimeChangeListener(TimeChangeListener listener) {
        timeListeners.add(listener);
    }

    public void addPlayListener(PlayListener listener) {
        playListeners.add(listener);
    }

    protected void notifyTime() {
        blockSeek = true;
        for (final TimeChangeListener listener : timeListeners)
            listener.changeTime(runner.time());
    }

    protected void pause() {
        timer.stop();
        for (final PlayListener listener : playListeners)
            listener.handlePause();
    }

    protected void play() {
        timer.start();
        for (final PlayListener listener : playListeners)
            listener.handlePlay();
    }

    public ActionListener playpauseListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (timer.isRunning())
                    pause();
                else
                    play();
            }
        };
    }

    public ActionListener incrListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (timer.isRunning())
                    pause();
                step();
            }
        };
    }

    public ActionListener stopListener() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pause();
                seek(runner.minTime());
            }
        };
    }

    public ChangeListener sliderListener() {
        return sliderListener;
    }

    public void seek(long time) {
        try {
            runner.seek(time);
        } catch (final IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe.getMessage());
        }
        notifyTime();
    }

    public ChangeListener speedListener() {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final JSpinner speedSpinner = (JSpinner) e.getSource();
                setSpeed((Integer) speedSpinner.getValue());
            }
        };
    }

    public ChangeListener fpsListener() {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                final JSpinner fpsSpinner = (JSpinner) e.getSource();
                setFps((Integer) fpsSpinner.getValue());
            }
        };
    }

    public void step() {
        if (runner.incrTime() + runner.time() > runner.maxTime())
            pause();
        else {
            try {
                runner.incr();
            } catch (final IOException ioe) {
                ioe.printStackTrace();
                throw new RuntimeException(ioe.getMessage());
            }
            notifyTime();
        }
    }

    public void setFps(int f) {
        fps = f;
        timer.setDelay(1000 / fps);
        updateIncrTime();
    }

    public void setSpeed(int s) {
        speed = s;
        updateIncrTime();
    }

    public int speed() {
        return speed;
    }

    public int fps() {
        return fps;
    }
}
