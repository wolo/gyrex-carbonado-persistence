package net.ageto.gyrex.persistence.carbonado.storage.internal.jdbc;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.Locale;

import org.eclipse.gyrex.monitoring.metrics.StopWatch;
import org.eclipse.gyrex.monitoring.metrics.TimerMetric;
import org.eclipse.gyrex.monitoring.metrics.TimerMetric.TimerMetricFactory;
import org.eclipse.gyrex.monitoring.profiling.Profiler;
import org.eclipse.gyrex.monitoring.profiling.Transaction;

import net.ageto.gyrex.persistence.carbonado.storage.jdbc.ITracingConstants;

public class TracingStatement<T extends Statement> implements Statement {

	private final Connection connection;
	protected final T statement;

	TracingStatement(final Connection connection, final T statement) {
		this.connection = connection;
		this.statement = statement;
	}

	@Override
	public void addBatch(final String sql) throws SQLException {
		statement.addBatch(sql);
	}

	@Override
	public void cancel() throws SQLException {
		statement.cancel();
	}

	@Override
	public void clearBatch() throws SQLException {
		statement.clearBatch();
	}

	@Override
	public void clearWarnings() throws SQLException {
		statement.clearWarnings();
	}

	@Override
	public void close() throws SQLException {
		statement.close();
	}

	@Override
	public boolean execute(final String sql) throws SQLException {
		final TimerMetric metric = getMetricForStatement(sql);
		if (metric == null)
			return statement.execute(sql);

		final StopWatch watch = metric.processStarted();
		try {
			return statement.execute(sql);
		} finally {
			watch.stop();
		}
	}

	@Override
	public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
		final TimerMetric metric = getMetricForStatement(sql);
		if (metric == null)
			return statement.execute(sql, autoGeneratedKeys);

		final StopWatch watch = metric.processStarted();
		try {
			return statement.execute(sql, autoGeneratedKeys);
		} finally {
			watch.stop();
		}
	}

	@Override
	public boolean execute(final String sql, final int columnIndexes[]) throws SQLException {
		final TimerMetric metric = getMetricForStatement(sql);
		if (metric == null)
			return statement.execute(sql, columnIndexes);

		final StopWatch watch = metric.processStarted();
		try {
			return statement.execute(sql, columnIndexes);
		} finally {
			watch.stop();
		}
	}

	@Override
	public boolean execute(final String sql, final String columnNames[]) throws SQLException {
		final TimerMetric metric = getMetricForStatement(sql);
		if (metric == null)
			return statement.execute(sql, columnNames);

		final StopWatch watch = metric.processStarted();
		try {
			return statement.execute(sql, columnNames);
		} finally {
			watch.stop();
		}
	}

	@Override
	public int[] executeBatch() throws SQLException {
		return statement.executeBatch();
	}

	@Override
	public ResultSet executeQuery(final String sql) throws SQLException {
		final TimerMetric metric = getMetric(ITracingConstants.METRIC_ID_SELECTS);
		if (metric == null)
			return statement.executeQuery(sql);

		final ResultSet rs;
		final StopWatch watch = metric.processStarted();
		try {
			rs = statement.executeQuery(sql);
		} finally {
			watch.stop();
		}

		final TimerMetric rsMetric = getMetric(ITracingConstants.METRIC_ID_FETCH);
		if (rsMetric == null)
			return rs;

		return tracingResultSet(rs, rsMetric);
	}

	@Override
	public int executeUpdate(final String sql) throws SQLException {
		final TimerMetric metric = getMetric(ITracingConstants.METRIC_ID_OTHER);
		if (metric == null)
			return statement.executeUpdate(sql);

		final StopWatch watch = metric.processStarted();
		try {
			return statement.executeUpdate(sql);
		} finally {
			watch.stop();
		}
	}

	@Override
	public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
		final TimerMetric metric = getMetric(ITracingConstants.METRIC_ID_OTHER);
		if (metric == null)
			return statement.executeUpdate(sql, autoGeneratedKeys);

		final StopWatch watch = metric.processStarted();
		try {
			return statement.executeUpdate(sql, autoGeneratedKeys);
		} finally {
			watch.stop();
		}
	}

	@Override
	public int executeUpdate(final String sql, final int columnIndexes[]) throws SQLException {
		final TimerMetric metric = getMetric(ITracingConstants.METRIC_ID_OTHER);
		if (metric == null)
			return statement.executeUpdate(sql, columnIndexes);

		final StopWatch watch = metric.processStarted();
		try {
			return statement.executeUpdate(sql, columnIndexes);
		} finally {
			watch.stop();
		}
	}

	@Override
	public int executeUpdate(final String sql, final String columnNames[]) throws SQLException {
		final TimerMetric metric = getMetric(ITracingConstants.METRIC_ID_OTHER);
		if (metric == null)
			return statement.executeUpdate(sql, columnNames);

		final StopWatch watch = metric.processStarted();
		try {
			return statement.executeUpdate(sql, columnNames);
		} finally {
			watch.stop();
		}
	}

	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public int getFetchDirection() throws SQLException {
		return statement.getFetchDirection();
	}

	@Override
	public int getFetchSize() throws SQLException {
		return statement.getFetchSize();
	}

	@Override
	public ResultSet getGeneratedKeys() throws SQLException {
		return statement.getGeneratedKeys();
	}

	@Override
	public int getMaxFieldSize() throws SQLException {
		return statement.getMaxFieldSize();
	}

	@Override
	public int getMaxRows() throws SQLException {
		return statement.getMaxRows();
	}

	protected TimerMetric getMetric(final String id) {
		final Transaction tx = Profiler.getTransaction();
		if (tx == null)
			return null;
		return tx.getOrCreateMetric(id, TimerMetricFactory.NANOSECONDS);
	}

	protected TimerMetric getMetricForStatement(final String sql) {
		if ((sql == null) || (Profiler.getTransaction() == null))
			return null;

		if (sql.trim().toLowerCase(Locale.US).startsWith("select"))
			return getMetric(ITracingConstants.METRIC_ID_SELECTS);

		return getMetric(ITracingConstants.METRIC_ID_OTHER);
	}

	@Override
	public boolean getMoreResults() throws SQLException {
		return statement.getMoreResults();
	}

	@Override
	public boolean getMoreResults(final int current) throws SQLException {
		return statement.getMoreResults(current);
	}

	@Override
	public int getQueryTimeout() throws SQLException {
		return statement.getQueryTimeout();
	}

	@Override
	public ResultSet getResultSet() throws SQLException {
		final TimerMetric metric = getMetric(ITracingConstants.METRIC_ID_FETCH);
		if (metric == null)
			return statement.getResultSet();

		final ResultSet rs = statement.getResultSet();
		if (rs == null)
			return rs;

		return tracingResultSet(rs, metric);
	}

	@Override
	public int getResultSetConcurrency() throws SQLException {
		return statement.getResultSetConcurrency();
	}

	@Override
	public int getResultSetHoldability() throws SQLException {
		return statement.getResultSetHoldability();
	}

	@Override
	public int getResultSetType() throws SQLException {
		return statement.getResultSetType();
	}

	@Override
	public int getUpdateCount() throws SQLException {
		return statement.getUpdateCount();
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		return statement.getWarnings();
	}

	@Override
	public boolean isClosed() throws SQLException {
		return statement.isClosed();
	}

	@Override
	public boolean isPoolable() throws SQLException {
		return statement.isPoolable();
	}

	@Override
	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setCursorName(final String name) throws SQLException {
		statement.setCursorName(name);
	}

	@Override
	public void setEscapeProcessing(final boolean enable) throws SQLException {
		statement.setEscapeProcessing(enable);
	}

	@Override
	public void setFetchDirection(final int direction) throws SQLException {
		statement.setFetchDirection(direction);
	}

	@Override
	public void setFetchSize(final int rows) throws SQLException {
		statement.setFetchSize(rows);
	}

	@Override
	public void setMaxFieldSize(final int max) throws SQLException {
		statement.setMaxFieldSize(max);
	}

	@Override
	public void setMaxRows(final int max) throws SQLException {
		statement.setMaxRows(max);
	}

	@Override
	public void setPoolable(final boolean poolable) throws SQLException {
		statement.setPoolable(poolable);
	}

	@Override
	public void setQueryTimeout(final int seconds) throws SQLException {
		statement.setQueryTimeout(seconds);
	}

	protected ResultSet tracingResultSet(final ResultSet rs, final TimerMetric metric) {
		return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] { ResultSet.class }, new TracingResultSetHandler(rs, metric));
	}

	@Override
	public <E> E unwrap(final Class<E> iface) throws SQLException {
		throw new UnsupportedOperationException();
	}
}
