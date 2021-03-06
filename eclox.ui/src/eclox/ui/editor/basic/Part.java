/*******************************************************************************
 * Copyright (C) 2003-2007, 2013, Guillaume Brocker
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Guillaume Brocker - Initial API and implementation
 *
 ******************************************************************************/

package eclox.ui.editor.basic;

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import eclox.core.doxyfiles.Doxyfile;
import eclox.ui.editor.editors.IEditor;

/**
 * Implements a specialized part that provide convenient features
 * to build user interfaces and manage editor life-cycles.
 *
 * @author gbrocker
 */
public class Part extends SectionPart {

    /**
     * The managed collection of editors
     */
    private Collection<IEditor> editors = new Vector<IEditor>();

    /**
     * The content of the section control
     */
    private Composite content;

    /**
     * The toolkit used for control creation
     */
    private FormToolkit toolkit;

    /**
     * The accumulation of separation spaces
     */
    private int spacer = 0;

    /**
     * the doxyfile being edited
     */
    protected Doxyfile doxyfile;

    /**
     * Contructor
     *
     * @param	parent		the parent control
     * @param	tk			the toolit used to create controls
     * @param	title		the title of the part
     * @param	doxyfile	the doxyfile being edited
     */
    public Part(Composite parent, FormToolkit tk, String title, Doxyfile doxyfile) {
        super(parent, tk, Section.TITLE_BAR/*|Section.EXPANDED|Section.TWISTIE*/ );

        this.doxyfile = doxyfile;

        // Initializes the section and its client component.
        Section section = getSection();
        GridLayout layout = new GridLayout();

        toolkit = tk;
        content = toolkit.createComposite(section);
        layout.numColumns = 2;
        layout.marginTop = 0;
        layout.marginRight = 0;
        layout.marginBottom = 0;
        layout.marginLeft = 0;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 2;
        content.setLayout(layout);
        section.setText(title);
        section.setClient(content);
    }

    /**
     * Adds a new label to the part
     *
     * @param	text	the text of the label
     */
    protected void addLabel(String text) {
        Label label = toolkit.createLabel(content, text, SWT.WRAP);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);

        data.horizontalSpan = 2;
        data.verticalIndent = spacer;
        label.setLayoutData(data);
        spacer = 0;
    }

    /**
     * Adds the given editor instance to the part, with the given label.
     *
     * The editor life-cycle will get managed by the part.
     *
     * @param	text	a string containing a label text
     * @param	editor	an editor instance
     */
    protected void addEditor(String text, IEditor editor) {
        addEditor(text, editor, 0);
    }

    /**
     * Adds the given editor instance to the part, with the given label.
     *
     * The editor life-cycle will get managed by the part.
     *
     * @param	text	a string containing a label text
     * @param	editor	an editor instance
     * @param	indent	an extra margin that will be added to the left side of the editor's cell
     */
    protected void addEditor(String text, IEditor editor, int indent) {
        // Creates the controls
        Label label = toolkit.createLabel(content, text);
        Composite container = toolkit.createComposite(content);
        GridData labelData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        GridData containerData = new GridData(SWT.FILL, SWT.FILL, true, false);

        labelData.verticalIndent = spacer;
        labelData.horizontalIndent = indent;
        labelData.grabExcessVerticalSpace = editor.grabVerticalSpace();
        containerData.verticalIndent = spacer;
        containerData.grabExcessVerticalSpace = editor.grabVerticalSpace();
        label.setLayoutData(labelData);
        label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
        container.setLayoutData(containerData);
        editor.createContent(container, toolkit);
        spacer = 0;

        // Registers the editor
        editors.add(editor);
    }

    /**
     * Adds the given editor instance to the part
     *
     * The editor life-cycle will get managed by the part.
     *
     * @param	editor	an editor instance
     */
    protected void addEditor(IEditor editor) {
        addEditor(editor, 0);
    }

    /**
     * Adds the given editor instance to the part
     *
     * The editor life-cycle will get managed by the part.
     *
     * @param	editor	an editor instance
     * @param	indent	an extra margin that will be added to the left side of the editor's cell
     */
    protected void addEditor(IEditor editor, int indent) {
        // Create the controls
        Composite container = toolkit.createComposite(content);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);

        data.verticalIndent = spacer;
        data.horizontalSpan = 2;
        data.horizontalIndent = indent;
        data.grabExcessVerticalSpace = editor.grabVerticalSpace();
        editor.createContent(container, toolkit);
        container.setLayoutData(data);
        spacer = 0;

        // Registers the editor
        editors.add(editor);
    }

    /**
     * Adds a new separator to the part
     */
    protected void addSperator() {
        spacer += 8;
    }

    /**
     * @see org.eclipse.ui.forms.AbstractFormPart#isDirty()
     */
    public boolean isDirty() {
        boolean dirty = super.isDirty();
        Iterator<IEditor> i = editors.iterator();

        while (i.hasNext() && !dirty) {
            IEditor editor = (IEditor) i.next();

            dirty = editor.isDirty();
        }
        return dirty;
    }

    /**
     * @see org.eclipse.ui.forms.AbstractFormPart#isStale()
     */
    public boolean isStale() {
        boolean stale = super.isStale();
        Iterator<IEditor> i = editors.iterator();

        while (i.hasNext() && !stale) {
            IEditor editor = (IEditor) i.next();

            stale = editor.isStale();
        }
        return stale;
    }

    /**
     * @see org.eclipse.ui.forms.AbstractFormPart#refresh()
     */
    public void refresh() {
        super.refresh();

        Iterator<IEditor> i = editors.iterator();
        while (i.hasNext()) {
            IEditor editor = (IEditor) i.next();

            editor.refresh();
        }
    }

    /**
     * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
     */
    public void commit(boolean onSave) {
        super.commit(onSave);

        Iterator<IEditor> i = editors.iterator();
        while (i.hasNext()) {
            IEditor editor = (IEditor) i.next();

            if (editor.isDirty()) {
                editor.commit();
            }
        }
    }

}
