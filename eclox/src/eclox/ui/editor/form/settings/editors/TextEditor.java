// eclox : Doxygen plugin for Eclipse.
// Copyright (C) 2003-2005 Guillaume Brocker
//
// This file is part of eclox.
//
// eclox is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// any later version.
//
// eclox is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with eclox; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA	

package eclox.ui.editor.form.settings.editors;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import eclox.doxyfiles.Setting;

/**
 * Implements a simple setting editor.
 * 
 * @author gbrocker
 */
public class TextEditor implements IEditor {
    
	/**
	 * Defines a modify listener class.
	 */
	private class Listener implements ModifyListener {

		public void modifyText(ModifyEvent e) {
			hasChanged = true;		
		}
		
	};
	
    /**
     * The current editor input
     */
	private Setting input;
	
	/**
     * The text widget.
     */
    private Text text;
    
    /**
     * Remerbers if the text has changed.
     */
    private boolean hasChanged = false;
    
    
    public void commit() {
		input.setValue(text.getText());
	}
    
    public void createContent(Composite parent, FormToolkit formToolkit) {
        parent.setLayout(new GridLayout());
        formToolkit.paintBordersFor(parent);
        
        // Creates the text widget.
        text = formToolkit.createText(parent, new String());
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.addModifyListener(new Listener());
    }
    
    public void dispose() {
		text.dispose();		
	}

	public boolean isDirty() {
		return hasChanged;
	}

	public void setInput(Setting input) {
		this.input = input;
        text.setText(input.getValue());
        hasChanged = false;
    }
    
}