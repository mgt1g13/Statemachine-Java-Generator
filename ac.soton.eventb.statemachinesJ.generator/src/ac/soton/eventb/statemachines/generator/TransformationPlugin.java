/**
 * Copyright (c) 2010
 * University of Southampton.
 * All rights reserved. This program and the accompanying materials  are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package ac.soton.eventb.statemachines.generator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TransformationPlugin extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ac.soton.eventb.statemachines.transformation";

	// The shared instance
	private static TransformationPlugin plugin;
	
	/**
	 * The constructor
	 */
	public TransformationPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static TransformationPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Log an error.
	 * Copied from diagram generated code
	 * @param error
	 */
	public void logError(String error) {
		logError(error, null);
	}
	
	/**
	 * Log an error with throwable.
	 * Copied from diagram generated code
	 * @param error
	 * @param throwable
	 */
	public void logError(String error, Throwable throwable) {
		if (error == null && throwable != null) {
			error = throwable.getMessage();
		}
		getLog().log(
				new Status(IStatus.ERROR, TransformationPlugin.PLUGIN_ID,
						IStatus.OK, error, throwable));
		debug(error, throwable);
	}
	
	/**
	 * Debug an error.
	 * Copied from diagram generated code
	 * @param message
	 * @param throwable
	 */
	private void debug(String message, Throwable throwable) {
		if (!isDebugging()) {
			return;
		}
		if (message != null) {
			System.err.println(message);
		}
		if (throwable != null) {
			throwable.printStackTrace();
		}
	}
}
