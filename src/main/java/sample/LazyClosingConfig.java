package sample;

import java.util.LinkedList;
import java.util.Queue;

import javax.sql.DataSource;

import org.seasar.doma.jdbc.ClassHelper;
import org.seasar.doma.jdbc.CommandImplementors;
import org.seasar.doma.jdbc.Commenter;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.EntityListenerProvider;
import org.seasar.doma.jdbc.JdbcLogger;
import org.seasar.doma.jdbc.MapKeyNaming;
import org.seasar.doma.jdbc.Naming;
import org.seasar.doma.jdbc.QueryImplementors;
import org.seasar.doma.jdbc.RequiresNewController;
import org.seasar.doma.jdbc.SqlFileRepository;
import org.seasar.doma.jdbc.SqlLogType;
import org.seasar.doma.jdbc.UnknownColumnHandler;
import org.seasar.doma.jdbc.dialect.Dialect;
import org.seasar.doma.jdbc.tx.TransactionManager;

public class LazyClosingConfig implements Config, AutoCloseable {

    private final Config config;
    private final Queue<AutoCloseable> autoCloseables = new LinkedList<>();
    private final CommandImplementors commandImplementors = new LazyClosingCommandImplementors(
            this);

    public LazyClosingConfig(Config config) {
        this.config = config;
    }

    /*
     * SELECT文を実行した場合、Connection・PreparedStatement・ResultSetを閉じずに
     * 結果を返すSelectCommandを使用するCommandImplementorsの実装を返します。
     */
    @Override
    public CommandImplementors getCommandImplementors() {
        return commandImplementors;
    }

    /*
     * AutoCloseableを集めます。
     * Connection・PreparedStatement・ResultSetはAutoCloseableをimplementsしています。
     * このメソッドはLazyClosingCommandImplementorsが生成するLazyClosingSelectCommandから
     * 呼ばれることを想定しています。
     */
    public void addAutoCloseable(AutoCloseable autoCloseable) {
        autoCloseables.add(autoCloseable);
    }

    /*
     * 収集したAutoCloseableを全てcloseします。
     */
    @Override
    public void close() throws Exception {
        AutoCloseable autoCloseable;
        while ((autoCloseable = autoCloseables.poll()) != null) {
            autoCloseable.close();
        }
    }

    /*
     * これより下に定義したメソッドはすべて保持しているconfigの同メソッドを呼び出します。
     */

    @Override
    public DataSource getDataSource() {
        return config.getDataSource();
    }

    @Override
    public Dialect getDialect() {
        return config.getDialect();
    }

    @Override
    public String getDataSourceName() {
        return config.getDataSourceName();
    }

    @Override
    public SqlFileRepository getSqlFileRepository() {
        return config.getSqlFileRepository();
    }

    @Override
    public JdbcLogger getJdbcLogger() {
        return config.getJdbcLogger();
    }

    @Override
    public RequiresNewController getRequiresNewController() {
        return config.getRequiresNewController();
    }

    @Override
    public ClassHelper getClassHelper() {
        return config.getClassHelper();
    }

    @Override
    public QueryImplementors getQueryImplementors() {
        return config.getQueryImplementors();
    }

    @Override
    public SqlLogType getExceptionSqlLogType() {
        return config.getExceptionSqlLogType();
    }

    @Override
    public UnknownColumnHandler getUnknownColumnHandler() {
        return config.getUnknownColumnHandler();
    }

    @Override
    public Naming getNaming() {
        return config.getNaming();
    }

    @Override
    public MapKeyNaming getMapKeyNaming() {
        return config.getMapKeyNaming();
    }

    @Override
    public TransactionManager getTransactionManager() {
        return config.getTransactionManager();
    }

    @Override
    public Commenter getCommenter() {
        return config.getCommenter();
    }

    @Override
    public int getMaxRows() {
        return config.getMaxRows();
    }

    @Override
    public int getFetchSize() {
        return config.getFetchSize();
    }

    @Override
    public int getQueryTimeout() {
        return config.getQueryTimeout();
    }

    @Override
    public int getBatchSize() {
        return config.getBatchSize();
    }

    @Override
    public EntityListenerProvider getEntityListenerProvider() {
        return config.getEntityListenerProvider();
    }
}
