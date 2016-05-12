/*
 * FB/Java plugin
 *
 * Distributable under LGPL license.
 * You may obtain a copy of the License at http://www.gnu.org/copyleft/lgpl.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * LGPL License for more details.
 *
 * This file was created by members of the Firebird development team.
 * All individual contributions remain the Copyright (C) of those
 * individuals.  Contributors to this file are either listed here or
 * can be obtained from a git log command.
 *
 * All rights reserved.
 */
package org.firebirdsql.fbjava.impl;

import org.firebirdsql.fbjava.CallableRoutineContext;
import org.firebirdsql.fbjava.Values;
import org.firebirdsql.fbjava.ValuesMetadata;


abstract class CallableRoutineContextImpl extends ContextImpl implements CallableRoutineContext
{
	//// TODO: input/output parameters

	public CallableRoutineContextImpl(InternalContext internalContext)
	{
		super(internalContext);
	}

	@Override
	public String getPackageName()
	{
		return internalContext.getRoutine().packageName;
	}

	@Override
	public ValuesMetadata getInputMetadata()
	{
		return internalContext.getRoutine().inputMetadata;
	}

	@Override
	public ValuesMetadata getOutputMetadata()
	{
		return internalContext.getRoutine().outputMetadata;
	}

	@Override
	public Values getInputValues()
	{
		return internalContext.getInValues();
	}
}