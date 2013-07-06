/*
 * eclox : Doxygen plugin for Eclipse.
 * Copyright (C) 2003, 2004, 2007, 2008, Guillaume Brocker
 * 
 * This file is part of eclox.
 * 
 * eclox is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 * 
 * eclox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with eclox; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA	
 */

package eclox.ui.action;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PlatformUI;

import eclox.core.doxyfiles.Doxyfile;
import eclox.core.doxygen.BuildJob;
import eclox.ui.DoxyfileSelector;
import eclox.ui.Plugin;
import eclox.ui.wizard.NewDoxyfileWizard;

/**
 * Implement the action handling for the build action.
 * 
 * @author gbrocker
 */
public class BuildActionDelegate implements IWorkbenchWindowPulldownDelegate {
	/**
	 * Listens for the popup menu items pointing to doxyfiles to build.
	 * 
	 * @author Guillaume Brocker
	 */
	private class MenuSelectionListener implements SelectionListener {
		public void widgetSelected(SelectionEvent e) {
			processData(e.widget.getData());
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			processData(e.widget.getData());
		}
		
		private void processData(Object data) {
			boolean forceChoose = false;
			
			if(data != null && data instanceof IFile) {
				nextDoxyfile = (IFile) data;
				forceChoose = false;
			}
			else {
				forceChoose = true;
			}
			doRun(forceChoose);
		}
	}
	
	private Menu				menu;			///< The managed contextual menu.
	private IFile				nextDoxyfile;	///< Rembers the next doxyfile to build.
	private IWorkbenchWindow	window;			///< Holds the reference to the workbench window where the action takes place.
		
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		disposeMenu();
		this.menu = new Menu(parent);
	
		// Fill it up with the build history items.
		BuildJob[]	buildJobs = Plugin.getDefault().getBuildManager().getRecentBuildJobs();
		for( int i = buildJobs.length - 1; i >= 0; i-- ) {
			MenuItem	menuItem = new MenuItem(this.menu, SWT.PUSH);
			IFile		currentDoxyfile = buildJobs[i].getDoxyfile();
		
			menuItem.addSelectionListener( new MenuSelectionListener() );
			menuItem.setData( currentDoxyfile );
			menuItem.setText( currentDoxyfile.getName() + " [" + currentDoxyfile.getFullPath().toString() + "]" );
		}
		// Add some sugar in the ui
		if( buildJobs.length > 0 ) {
			new MenuItem(this.menu, SWT.SEPARATOR);
		}
		
		// Add the fall-back menu item to let the user choose another doxyfile.
		MenuItem chooseMenuItem = new MenuItem(this.menu, SWT.PUSH);
		
		chooseMenuItem.addSelectionListener(new MenuSelectionListener());
		chooseMenuItem.setText("Choose Doxyfile...");			 
	
		// Job's done.
		return this.menu;
	}

	/**
	 * Dispose the delegate.
	 */
	public void dispose() {
		// Frees all resources and references.
		disposeMenu();
		nextDoxyfile = null;
		window = null;
	}

	
	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	
	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		try {
			doRun(false);
		}
		catch( Throwable throwable ) {
			MessageDialog.openError(window.getShell(), "Unexpected Error", throwable.toString());
		}
	}


	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		try {
			// Retrieve the next doxyfile to build from the current selection.
			nextDoxyfile = getDoxyfileFromSelection(selection);
			
			// Retrieve the next doxyfile from the current editor.
			if( nextDoxyfile == null ) {
				nextDoxyfile = getDoxyfileFromActiveEditor(window);
			}
			
			
			// If there is no next doxyfile to build and the history is not empty
			// set the first history element as the next doxyfile.
			if( nextDoxyfile == null ) {
				BuildJob[]	buildJobs = Plugin.getDefault().getBuildManager().getRecentBuildJobs();
				int			buildJobsCount = buildJobs.length;
				
				if( buildJobsCount > 0 ) {
					nextDoxyfile = buildJobs[buildJobsCount - 1].getDoxyfile();	
				}				
			}
			
			
			// Check the existence of the doxyfile.
			if( nextDoxyfile != null && nextDoxyfile.exists() == false ) {
				nextDoxyfile = null;
			}
			
			// Update the tooltip.
			String	tooltipText = nextDoxyfile != null ?
				"Build " + nextDoxyfile.getFullPath().toString() :
				"Choose Next Doxyfile";
				
			action.setToolTipText(tooltipText);
		}
		catch(Throwable throwable) {
			MessageDialog.openError(window.getShell(), "Unexpected Error", throwable.toString());
		}
	}
	
	
	/**
	 * Uses the next doxyfile specified to determine what to do.
	 * 
	 * @param	forceChoose	@c true to ask the user for a doxyfile to build.
	 */
	protected void doRun(boolean forceChoose) {
		try {
			IFile				doxyfile = (forceChoose == true) ? null : this.nextDoxyfile;
			DoxyfileSelector	selector = new DoxyfileSelector(null);
			
			// If there is no next doxyfile to build, ask the user for one.
			if(doxyfile == null) {
				selector.open();
				doxyfile = selector.getDoxyfile();
			}
			
			// If there is no doxyfile,
			// we will prompt the user to create one and then edit it.
			if (doxyfile == null && selector.hadDoxyfiles() == false) {
				doxyfile = askUserToCreateDoxyfile();
			}
			else if (doxyfile != null) {
				Plugin.getDefault().getBuildManager().build( doxyfile );
			}
		}
		catch( Throwable throwable ) {
			MessageDialog.openError(window.getShell(), "Unexpected Error", throwable.toString());
		}
	}
	
	
	/**
	 * Dispose the owned menu.
	 */
	private void disposeMenu() {
		if(this.menu != null) {
			this.menu.dispose();
			this.menu = null;
		}
	}
	
	
	/**
	 * Retrieves a doxyfile from the active editor.
	 * 
	 * @param	window	a reference to a workbench window
	 * 
	 * @return	a doxfile retrieved from the active editor input.
	 */
	private static IFile getDoxyfileFromActiveEditor( IWorkbenchWindow window ) {
		IFile			doxyfile			= null;
		IWorkbenchPage	activePage			= window.getActivePage();
		IEditorPart		activeEditorPart	= activePage != null ? window.getActivePage().getActiveEditor() : null;
		
		if(activeEditorPart != null) {
			IEditorInput activeEditorInput  = activeEditorPart.getEditorInput();
			
			if(activeEditorInput instanceof IFileEditorInput) {
				IFileEditorInput fileEditorInput = (IFileEditorInput)activeEditorInput;
				IFile file = fileEditorInput.getFile();
				
				if(Doxyfile.isDoxyfile(file) == true) {
					doxyfile = file;
				}
			}	
		}
		
		return doxyfile;
	}
	
	
	/**
	 * Retrieves a doxyfile from the specified selection.
	 * 
	 * @return	a doxyfile retrieved from the specified selection.
	 */
	private static IFile getDoxyfileFromSelection(ISelection selection) {
		IFile doxyfile = null;

		// Test if the current selection is not empty.
		if(selection instanceof IStructuredSelection && selection.isEmpty() == false) {
			IStructuredSelection	structSel = (IStructuredSelection) selection;
			Object					element = structSel.getFirstElement();

			if(element != null && element instanceof IFile) {
				IFile	fileElement = (IFile) element;

				if(fileElement.exists() == true && Doxyfile.isDoxyfile(fileElement) == true) {
					doxyfile = fileElement;
				}
			}
		}

		
		// Job's done.
		return doxyfile;
	}
	
	/**
	 * Prompts the user to create a new doxyfile.
	 * 
	 * @return	a doxyfile, or null if none.
	 */
	private static IFile askUserToCreateDoxyfile() {
		IFile	doxyfile		= null;
		Shell	shell			= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		boolean	wantDoxyfile	= MessageDialog.openQuestion(shell, "No Doxyfile Found", "No doxyfile has been found in opened projects.\n\nDo you want to create a new doxyfile now ?" );

		if( wantDoxyfile ) {
			NewDoxyfileWizard		wizard = new NewDoxyfileWizard();
			ISelection				selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
			IStructuredSelection	strcSelection = (selection != null && selection instanceof IStructuredSelection) ? (IStructuredSelection) selection : new StructuredSelection();

			wizard.init(PlatformUI.getWorkbench(), strcSelection);
			
			
			WizardDialog	wizardDialog = new WizardDialog(shell, wizard);
			
			wizardDialog.open();
			doxyfile = wizard.getDoxyfile();
		}
		
		return doxyfile;
	}
	
}