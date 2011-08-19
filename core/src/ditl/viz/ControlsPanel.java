/*******************************************************************************
 * This file is part of DITL.                                                  *
 *                                                                             *
 * Copyright (C) 2011 John Whitbeck <john@whitbeck.fr>                         *
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

import java.awt.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class ControlsPanel extends JPanel { 
	
	protected JButton playButton;
	protected JButton stopButton;
	protected JButton incrButton;
	protected JSlider timeSlider;
	protected JLabel timeLabel;
	protected JTextField timeField;
	protected long tics_per_second = 1L;
	
	public ControlsPanel(final SceneRunner runner){
		/* init components */
		final ImageIcon playIcon = getIcon("icons/Play24.gif");
		final ImageIcon pauseIcon = getIcon("icons/Pause24.gif");
		playButton = new JButton(playIcon);
		playButton.addActionListener(runner.playpauseListener());
		runner.addPlayListener(new PlayListener(){
			@Override
			public void handlePause() {
				playButton.setIcon(playIcon);
			}

			@Override
			public void handlePlay() {
				playButton.setIcon(pauseIcon);
			}
		});
		
		incrButton = new JButton(getIcon("icons/StepForward24.gif"));
		incrButton.addActionListener(runner.incrListener());
		
		stopButton = new JButton(getIcon("icons/Stop24.gif"));
		stopButton.addActionListener(runner.stopListener());
		
			
		timeSlider = new JSlider();
		timeSlider.addChangeListener(runner.sliderListener());
		timeSlider.setPaintTicks(true);
		timeSlider.setPaintLabels(true);
		runner.addTimeChangeListener(new TimeChangeListener() {
			@Override
			public void changeTime(long time) {
				timeSlider.setValue((int)(time/tics_per_second));
			}
		});
		
		timeLabel = new JLabel("Time: ");
		
		timeField = new JTextField();
		timeField.setHorizontalAlignment(JTextField.RIGHT);
		timeField.setPreferredSize(new Dimension(75,0));
		runner.addTimeChangeListener(new TimeChangeListener(){
			@Override
			public void changeTime(long t) {
				timeField.setText(String.valueOf(t/tics_per_second));
			}
		});
		timeField.addActionListener(runner.timeEntryListener());
		
		/* layout */
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		buttonsPanel.add(playButton);
		buttonsPanel.add(incrButton);
		buttonsPanel.add(stopButton);
		
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new BoxLayout(timePanel, BoxLayout.LINE_AXIS));
		timePanel.add(timeLabel);
		timePanel.add(timeField);
		
		setLayout(new BorderLayout());
		add(buttonsPanel, BorderLayout.WEST);
		add(timeSlider, BorderLayout.CENTER);
		add(timePanel, BorderLayout.EAST);
		
		setReady(false);		
	}
	
	private ImageIcon getIcon(String url){
		return new ImageIcon(getClass().getResource(url));
	}
	
	public void setReady(boolean b){
		playButton.setEnabled(b);
		incrButton.setEnabled(b);
		stopButton.setEnabled(b);
		timeSlider.setEnabled(b);
		timeLabel.setEnabled(b);
		timeField.setEnabled(b);
	}
	
	public void updateTimes(long ticsPerSecond, long minTime, long maxTime){
		tics_per_second = ticsPerSecond;
		ChangeListener[] listeners = timeSlider.getChangeListeners();
		for ( ChangeListener listener : listeners )
			timeSlider.removeChangeListener(listener);
		int minT = (int)(minTime/ticsPerSecond);
		int maxT = (int)(maxTime/ticsPerSecond)+1;
		timeSlider.setMinimum(minT);
		timeSlider.setMaximum(maxT);
		int tickW = (maxT-minT)/20;
		timeSlider.setMajorTickSpacing(tickW*5);
		timeSlider.setMinorTickSpacing(tickW);
		timeSlider.setMinimum(minT);
		timeSlider.setMaximum(maxT);
		timeSlider.setLabelTable(timeSlider.createStandardLabels(tickW*5));
		for ( ChangeListener listener : listeners)
			timeSlider.addChangeListener(listener);
		timeField.setText(String.valueOf(minTime));
	}
	
}
