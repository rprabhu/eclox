// eclox : Doxygen plugin for Eclipse.
// Copyright (C) 2003-2006 Guillaume Brocker
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

package eclox.ui.editor.editors;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;


public class FileEditor extends TextEditor {
	/**
	 * the push button for browsing the file system
	 */
	private Button	browseFileSystemButton;
	
	/**
	 * Implements the selection listener attached to the push buttons.
	 */
	class MySelectionListener implements SelectionListener {

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
			if( e.widget == browseFileSystemButton ) {
				browseFileSystem();
			}
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			if( e.widget == browseFileSystemButton ) {
				browseFileSystem();
			}
		}
		
	}
	
	public void createContent(Composite parent, FormToolkit formToolkit) {
		super.createContent(parent, formToolkit);
		
		// Create controls and their associated layout data.
		SelectionListener	selectionListener = new MySelectionListener();
		GridLayout			layout				= (GridLayout) parent.getLayout();
		
		layout.numColumns = 2;
		
		browseFileSystemButton = formToolkit.createButton( parent, "Browse...", 0 );
		browseFileSystemButton.addSelectionListener( selectionListener );
	}

	/**
	 * @see eclox.ui.editor.editors.TextEditor#dispose()
	 */
	public void dispose() {
		// Local cleaning.
		browseFileSystemButton.dispose();
		
		// Default cleaning.
		super.dispose();
	}
	
	/**
	 * Browses the file system for a path and updates the managed text field.
	 */
	private void browseFileSystem() {
		String	result;
		result = Convenience.browseFileSystemForFile( text.getShell(), getInput().getOwner(), getInput().getValue() );
		if( result!= null ) {
			super.text.setText( result );
		}
	}

	/**
	 * @see eclox.ui.editor.editors.TextEditor#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		assert browseFileSystemButton != null;
		
		browseFileSystemButton.setEnabled(enabled);
		super.setEnabled(enabled);
	}
	
	

}