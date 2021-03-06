// ========================================================================
// $Id: LifeCycleMBean.java,v 1.4 2004/05/09 20:33:23 gregwilkins Exp $
// Copyright 1999-2004 Mort Bay Consulting Pty. Ltd.
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

package org.browsermob.proxy.jetty.util.jmx;

import org.browsermob.proxy.jetty.util.LifeCycle;

import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;

// TODO: Auto-generated Javadoc
/* ------------------------------------------------------------ */
/**
 * The Class LifeCycleMBean.
 * 
 * @version $Revision: 1.4 $
 * @author Greg Wilkins (gregw)
 */
public class LifeCycleMBean extends ModelMBeanImpl {
	/* ------------------------------------------------------------ */
	/**
	 * Instantiates a new life cycle m bean.
	 * 
	 * @throws MBeanException
	 *             the m bean exception
	 */
	public LifeCycleMBean() throws MBeanException {
	}

	/* ------------------------------------------------------------ */
	/**
	 * Instantiates a new life cycle m bean.
	 * 
	 * @param object
	 *            the object
	 * @throws MBeanException
	 *             the m bean exception
	 */
	public LifeCycleMBean(LifeCycle object) throws MBeanException {
		super(object);
	}

	/* ------------------------------------------------------------ */
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.browsermob.proxy.jetty.util.jmx.ModelMBeanImpl#defineManagedResource
	 * ()
	 */
	protected void defineManagedResource() {
		super.defineManagedResource();
		defineAttribute("started");
		defineOperation("start", MBeanOperationInfo.ACTION);
		defineOperation("stop", MBeanOperationInfo.ACTION);
	}
}
