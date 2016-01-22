package sample;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.seasar.doma.internal.jdbc.util.JdbcUtil;
import org.seasar.doma.jdbc.CommandImplementors;
import org.seasar.doma.jdbc.SqlExecutionException;
import org.seasar.doma.jdbc.command.ResultSetHandler;
import org.seasar.doma.jdbc.command.SelectCommand;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.query.SelectQuery;

public class LazyClosingCommandImplementors implements CommandImplementors {

    private final LazyClosingConfig config;

    public LazyClosingCommandImplementors(LazyClosingConfig configWrapper) {
        this.config = configWrapper;
    }

    @Override
    public <RESULT> SelectCommand<RESULT> createSelectCommand(Method method, SelectQuery query,
            ResultSetHandler<RESULT> resultSetHandler) {
        return new IterationSelectCommand<>(query, resultSetHandler);
    }

    /*
     * SelectCommandのexecuteメソッドとexecuteQueryメソッドを
     * コピペしてcloseしている箇所をコメントアウトしました。
     * 
     * それからConnection・PreparedStatement・ResultSetを
     * LazyClosingConfig.addAutoCloseableでLazyClosingConfigに集めています。
     */
    class IterationSelectCommand<RESULT> extends SelectCommand<RESULT> {

        public IterationSelectCommand(SelectQuery query,
                ResultSetHandler<RESULT> resultSetHandler) {
            super(query, resultSetHandler);
        }

        @Override
        public RESULT execute() {
            Supplier<RESULT> supplier = null;
            Connection connection = JdbcUtil.getConnection(query.getConfig().getDataSource());
            config.addAutoCloseable(connection);
            try {
                PreparedStatement preparedStatement = JdbcUtil.prepareStatement(connection, sql);
                config.addAutoCloseable(preparedStatement);
                try {
                    log();
                    setupOptions(preparedStatement);
                    bindParameters(preparedStatement);
                    supplier = executeQuery(preparedStatement);
                } catch (SQLException e) {
                    Dialect dialect = query.getConfig().getDialect();
                    throw new SqlExecutionException(query.getConfig().getExceptionSqlLogType(), sql,
                            e, dialect.getRootCause(e));
                } finally {
                    //JdbcUtil.close(preparedStatement, query.getConfig().getJdbcLogger());
                }
            } finally {
                //JdbcUtil.close(connection, query.getConfig().getJdbcLogger());
            }
            return supplier.get();
        }

        @Override
        protected Supplier<RESULT> executeQuery(PreparedStatement preparedStatement)
                throws SQLException {
            ResultSet resultSet = preparedStatement.executeQuery();
            config.addAutoCloseable(resultSet);
            try {
                return handleResultSet(resultSet);
            } finally {
                //JdbcUtil.close(resultSet, query.getConfig().getJdbcLogger());
            }
        }
    }
}
