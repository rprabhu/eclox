/*
 * eclox : Doxygen plugin for Eclipse.
 * Copyright (C) 2003-2006 Guillaume Brocker
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

package eclox.core.doxygen;

import java.net.URL;
import java.util.Collection;
import java.util.Vector;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import eclox.core.Plugin;

public final class BundledDoxygen extends Doxygen {
	
	private URL	location;
	
	/**
	 * Retrieves all available bundled doxygen binaries.
	 * 
	 * @return	a collection with all collected bundled doxygen wrappers
	 */
	public static Collection getAll() {
		Collection			doxygens	= new Vector();
		IExtensionRegistry	registry	= Platform.getExtensionRegistry();
		IExtensionPoint		point		= registry.getExtensionPoint("org.gna.eclox.core.doxygen");
		IExtension[]		extensions	= point.getExtensions();
		
		for (int i = 0; i < extensions.length; i++) {
			IExtension				extension	= extensions[i];
			IConfigurationElement[] elements	= extension.getConfigurationElements();
			
			for (int j = 0; j < elements.length; j++) {
				final Path		path	= new Path( elements[j].getAttribute("path") );
				final String	arch	= elements[j].getAttribute("arch");
				final String	os		= elements[j].getAttribute("os");
				URL				url		= Plugin.getDefault().find( path );
				
				if( url != null && Platform.getOS().equals(os) && Platform.getOSArch().equals(arch) ) {
					doxygens.add( new BundledDoxygen(url) );
				}
				else {
					Plugin.getDefault().logError( path.toString() + ": not a valid doxygen path." );
				}
			}
		}
		return doxygens;		
	}
	
	/**
	 * Creates a bundled doxygen instance from the given identifier
	 * 
	 * @param	identifier	a string containing an identifier
	 * 
	 * @return	a new bundled doxygen wrapper
	 */
	public static BundledDoxygen createFromIdentifier( final String identifier ) {
		BundledDoxygen	doxygen = null;
		
		if( identifier.startsWith( BundledDoxygen.class.getName() ) ) {
			final String	location =  identifier.substring( identifier.indexOf(' ') + 1 );
			
			try {
				doxygen = new BundledDoxygen( new URL(location) );
			}
			catch( Throwable t ) {
				Plugin.log( t );
				doxygen = null;
			}
		}
		return doxygen;
	}

	/**
	 * @see eclox.core.doxygen.Doxygen#getCommand()
	 */
	public String getCommand() {
		try {
			return Platform.resolve( location ).getPath();
		}
		catch( Throwable t ) {
			Plugin.log(t);
			return null;
		}
	}

	/**
	 * @see eclox.core.doxygen.Doxygen#getDescription()
	 */
	public String getDescription() {
		return new String();
	}

	/**
	 * @see eclox.core.doxygen.Doxygen#getIdentifier()
	 */
	public String getIdentifier() {
		return this.getClass().getName() + " " + location;
	}
	
	/**
	 * Constructor
	 * 
	 * @param	location	an url giving the location a doxygen binary
	 */
	private BundledDoxygen( URL location ) {
		this.location = location;

		assert( location != null );
	}

}
