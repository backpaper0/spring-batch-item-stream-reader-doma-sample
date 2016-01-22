package sample;

import java.util.Iterator;
import java.util.stream.Stream;

import org.seasar.doma.jdbc.Config;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookReader extends AbstractItemCountingItemStreamItemReader<Book> {

    @Autowired
    private Config config;
    private LazyClosingConfig lazyClosingConfig;
    private Iterator<Book> iterator;

    public BookReader() {
        setName(BookReader.class.getSimpleName());
    }

    @Override
    protected Book doRead() throws Exception {
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    @Override
    protected void doOpen() throws Exception {
        //インジェクションされたConfigをLazyClosingConfigでラップして
        //DAO実装クラスに渡しています。
        lazyClosingConfig = new LazyClosingConfig(config);
        BookDao dao = new BookDaoImpl(lazyClosingConfig);

        //DAOのSELECT系メソッドではstrategyをSTREAMにしておいて
        //Stream.iteratorで結果をIteratorで取得しています。
        iterator = dao.select(Stream::iterator);
    }

    @Override
    protected void doClose() throws Exception {
        //ここでConnection・PreparedStatement・ResultSetを全てcloseしています。
        lazyClosingConfig.close();
    }
}
