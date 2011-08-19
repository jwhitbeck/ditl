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

import javax.swing.*;

@SuppressWarnings("serial")
public class FPSPanel extends JPanel {

	public FPSPanel(SceneRunner runner){
		SpinnerModel fpsModel = new SpinnerNumberModel(runner.fps(),1,30,1);
		JSpinner fpsSpinner = new JSpinner(fpsModel);
		fpsSpinner.addChangeListener(runner.fpsListener());
		
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		add(Box.createHorizontalGlue());
		add(new JLabel("Frames/s: "));
		add(fpsSpinner);
	}
}
