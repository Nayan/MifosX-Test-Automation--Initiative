// ========================================================================
// $Id: JarResource.java,v 1.19 2005/08/13 00:01:28 gregwilkins Exp $
// Copyright 1996-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.browsermob.proxy.jetty.util;

import org.apache.commons.logging.Log;
import org.browsermob.proxy.jetty.log.LogFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

// TODO: Auto-generated Javadoc
/* ------------------------------------------------------------ */
/**
 * The Class JarResource.
 */
public class JarResource extends URLResource {

	/** The log. */
	private static Log log = LogFactory.getLog(JarResource.class);

	/** The _jar connection. */
	protected transient JarURLConnection _jarConnection;

	/* -------------------------------------------------------- */
	/**
	 * Instantiates a new jar resource.
	 * 
	 * @param url
	 *            the url
	 */
	JarResource(URL url) {
		super(url, null);
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.browsermob.proxy.jetty.util.URLResource#release()
	 */
	public synchronized void release() {
		_jarConnection = null;
		super.release();
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.browsermob.proxy.jetty.util.URLResource#checkConnection()
	 */
	protected boolean checkConnection() {
		super.checkConnection();
		try {
			if (_jarConnection != _connection)
				newConnection();
		} catch (IOException e) {
			LogSupport.ignore(log, e);
			_jarConnection = null;
		}

		return _jarConnection != null;
	}

	/* ------------------------------------------------------------ */
	/**
	 * New connection.
	 * 
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	protected void newConnection() throws IOException {
		_jarConnection = (JarURLConnection) _connection;
	}

	/* ------------------------------------------------------------ */
	/**
	 * Returns true if the respresenetd resource exists.
	 * 
	 * @return true, if successful
	 */
	public boolean exists() {
		if (_urlString.endsWith("!/"))
			return checkConnection();
		else
			return super.exists();
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.browsermob.proxy.jetty.util.URLResource#getFile()
	 */
	public File getFile() throws IOException {
		return null;
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.browsermob.proxy.jetty.util.URLResource#getInputStream()
	 */
	public InputStream getInputStream() throws java.io.IOException {
		if (!_urlString.endsWith("!/"))
			return super.getInputStream();

		URL url = new URL(_urlString.substring(4, _urlString.length() - 2));
		return url.openStream();
	}

	/* ------------------------------------------------------------ */
	/**
	 * Extract.
	 * 
	 * @param resource
	 *            the resource
	 * @param directory
	 *            the directory
	 * @param deleteOnExit
	 *            the delete on exit
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void extract(Resource resource, File directory,
			boolean deleteOnExit) throws IOException {
		if (log.isDebugEnabled())
			log.debug("Extract " + resource + " to " + directory);
		JarInputStream jin = new JarInputStream(resource.getInputStream());
		JarEntry entry = null;
		while ((entry = jin.getNextJarEntry()) != null) {
			File file = new File(directory, entry.getName());
			if (entry.isDirectory()) {
				// Make directory
				if (!file.exists())
					file.mkdirs();
			} else {
				// make directory (some jars don't list dirs)
				File dir = new File(file.getParent());
				if (!dir.exists())
					dir.mkdirs();

				// Make file
				FileOutputStream fout = null;
				try {
					fout = new FileOutputStream(file);
					IO.copy(jin, fout);
				} finally {
					IO.close(fout);
				}

				// touch the file.
				if (entry.getTime() >= 0)
					file.setLastModified(entry.getTime());
			}
			if (deleteOnExit)
				file.deleteOnExit();
		}
	}

	/* ------------------------------------------------------------ */
	/**
	 * Extract.
	 * 
	 * @param directory
	 *            the directory
	 * @param deleteOnExit
	 *            the delete on exit
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void extract(File directory, boolean deleteOnExit)
			throws IOException {
		extract(this, directory, deleteOnExit);
	}
}
