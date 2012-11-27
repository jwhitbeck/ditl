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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class ControlsPanel extends JPanel {

    final ImageIcon playIcon = getIcon("/icons/Play24.gif");
    final ImageIcon pauseIcon = getIcon("/icons/Pause24.gif");

    protected JButton playButton;
    protected JButton stopButton;
    protected JButton incrButton;
    protected JSlider timeSlider;
    protected JLabel timeLabel;
    protected JTextField timeField;
    protected long tics_per_second = 1L;
    protected long min_time;
    protected long max_time;
    protected int mod = 1;
    protected String mod_text = "s";
    protected DecimalFormat df = new DecimalFormat("0.00");

    public ControlsPanel() {
        /* init components */

        playButton = new JButton(playIcon);
        incrButton = new JButton(getIcon("/icons/StepForward24.gif"));
        stopButton = new JButton(getIcon("/icons/Stop24.gif"));

        timeSlider = new JSlider();
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);

        timeLabel = new JLabel("Time: ");
        timeField = new JTextField();
        timeField.setHorizontalAlignment(SwingConstants.RIGHT);
        timeField.setPreferredSize(new Dimension(75, 0));

        /* layout */
        final JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        buttonsPanel.add(playButton);
        buttonsPanel.add(incrButton);
        buttonsPanel.add(stopButton);

        final JPanel timePanel = new JPanel();
        timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.LINE_AXIS));
        timePanel.add(timeLabel);
        timePanel.add(timeField);

        setLayout(new BorderLayout());
        add(buttonsPanel, BorderLayout.WEST);
        add(timeSlider, BorderLayout.CENTER);
        add(timePanel, BorderLayout.EAST);

        setReady(false);
    }

    void setRunner(final SceneRunner runner) {
        playButton.addActionListener(runner.playpauseListener());
        runner.addPlayListener(new PlayListener() {
            @Override
            public void handlePause() {
                playButton.setIcon(playIcon);
            }

            @Override
            public void handlePlay() {
                playButton.setIcon(pauseIcon);
            }
        });
        incrButton.addActionListener(runner.incrListener());
        stopButton.addActionListener(runner.stopListener());
        timeSlider.addChangeListener(runner.sliderListener());
        runner.addTimeChangeListener(new TimeChangeListener() {
            @Override
            public void changeTime(long time) {
                timeSlider.setValue((int) (time / tics_per_second));
            }
        });
        runner.addTimeChangeListener(new TimeChangeListener() {
            @Override
            public void changeTime(long t) {
                setText(t);
            }
        });
        timeField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                final long t = parseText();
                runner.pause();
                runner.seek(t);
            }
        });
    }

    private void setText(long t) {
        final double v = (double) t / (double) (tics_per_second * mod);
        timeField.setText(df.format(v) + " " + mod_text);
    }

    public long parseText() {
        final String s = timeField.getText().split(" ")[0];
        float v;
        try {
            v = df.parse(s).floatValue();
        } catch (final ParseException e) {
            return min_time;
        }
        return (long) (v * tics_per_second * mod);
    }

    private ImageIcon getIcon(String url) {
        return new ImageIcon(getClass().getResource(url));
    }

    public void setReady(boolean b) {
        playButton.setEnabled(b);
        incrButton.setEnabled(b);
        stopButton.setEnabled(b);
        timeSlider.setEnabled(b);
        timeLabel.setEnabled(b);
        timeField.setEnabled(b);
    }

    private void updateSlider() {
        final ChangeListener[] listeners = timeSlider.getChangeListeners();
        for (final ChangeListener listener : listeners)
            timeSlider.removeChangeListener(listener);
        final int minT = (int) (min_time / (tics_per_second));
        int maxT = (int) (max_time / (tics_per_second));
        if (max_time % tics_per_second != 0)
            maxT += 1;
        timeSlider.setMinimum(minT);
        timeSlider.setMaximum(maxT);
        int tickW = Math.max(((maxT - minT) / 20), mod);
        tickW -= (tickW % mod);
        timeSlider.setMajorTickSpacing(tickW * 5);
        timeSlider.setMinorTickSpacing(tickW);
        timeSlider.setMinimum(minT);
        timeSlider.setMaximum(maxT);
        final Dictionary<Integer, JLabel> label_map = new Hashtable<Integer, JLabel>();
        label_map.put(minT, new JLabel(String.valueOf((minT) / mod)));
        for (int i = (minT + tickW * 5 - (minT % tickW * 5)); i <= maxT; i += tickW * 5)
            label_map.put(minT + i, new JLabel(String.valueOf((minT + i) / mod)));
        timeSlider.setLabelTable(label_map);
        for (final ChangeListener listener : listeners)
            timeSlider.addChangeListener(listener);
    }

    public void setModifier(int modifier, String modText) {
        final long t = parseText();
        mod = modifier;
        mod_text = modText;
        updateSlider();
        setText(t);
    }

    public void setBounds(long ticsPerSecond, long minTime, long maxTime) {
        tics_per_second = ticsPerSecond;
        min_time = minTime;
        max_time = maxTime;
        updateSlider();
        setText(min_time);
    }

}
