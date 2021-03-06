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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.firebirdsql.fbjava.impl.FbClientLibrary.IAttachment;
import org.firebirdsql.fbjava.impl.FbClientLibrary.IExternalContext;
import org.firebirdsql.fbjava.impl.FbClientLibrary.IStatus;
import org.firebirdsql.fbjava.impl.FbClientLibrary.ITransaction;


// This (non-public) class is accessed by org.firebirdsql.fbjava.Context by reflection.
final class InternalContext implements AutoCloseable
{
	private static final ThreadLocal<InternalContext> tls = new ThreadLocal<InternalContext>();
	private IAttachment attachment;
	private ITransaction transaction;
	private Routine routine;
	private ValuesImpl inValues;
	private ValuesImpl outValues;
	private Connection connection;
	private ContextImpl contextImpl;

	public static InternalContext get()
	{
		return tls.get();
	}

	public static InternalContext set(final InternalContext newValue)
	{
		final InternalContext oldValue = tls.get();
		tls.set(newValue);
		return oldValue;
	}

	public static ContextImpl getContextImpl()
	{
		final InternalContext context = get();
		return context.contextImpl;
	}

	public static InternalContext create(final IStatus status, final IExternalContext context, final Routine routine,
		final ValuesImpl inValues, final ValuesImpl outValues) throws FbException
	{
		final InternalContext internalContext = new InternalContext();
		internalContext.setup(status, context, routine, 0, inValues, outValues);
		return internalContext;
	}

	public static InternalContext createTrigger(final IStatus status, final IExternalContext context,
		final Routine routine, final int action, final ValuesImpl oldValues, final ValuesImpl newValues)
			throws FbException
	{
		final InternalContext internalContext = new InternalContext();
		internalContext.setup(status, context, routine, action, oldValues, newValues);
		return internalContext;
	}

	private void setup(final IStatus status, final IExternalContext context, final Routine routine,
		final int triggerAction, final ValuesImpl inValues, final ValuesImpl outValues) throws FbException
	{
		attachment = context.getAttachment(status);
		transaction = context.getTransaction(status);

		this.routine = routine;
		this.inValues = inValues;
		this.outValues = outValues;

		if (routine == null)
			contextImpl = null;
		else
		{
			switch (routine.type)
			{
				case FUNCTION:
					contextImpl = new FunctionContextImpl(this);
					break;

				case PROCEDURE:
					contextImpl = new ProcedureContextImpl(this);
					break;

				case TRIGGER:
					contextImpl = new TriggerContextImpl(this, triggerAction);
					break;
			}
		}
	}

	public IAttachment getAttachment()
	{
		return attachment;
	}

	public ITransaction getTransaction()
	{
		return transaction;
	}

	public Routine getRoutine()
	{
		return routine;
	}

	public ValuesImpl getInValues()
	{
		return inValues;
	}

	public ValuesImpl getOutValues()
	{
		return outValues;
	}

	public Connection getConnection() throws SQLException
	{
		if (connection == null)
		{
			final Properties properties = new Properties();
			properties.setProperty("encoding", "utf8");

			connection = DriverManager.getConnection("jdbc:default:connection", properties);
		}

		return connection;
	}

	@Override
	public void close() throws Exception
	{
		if (connection != null)
			connection.close();
		connection = null;

		transaction.release();
		transaction = null;

		attachment.release();
		attachment = null;

		routine = null;
		contextImpl = null;
	}
}
