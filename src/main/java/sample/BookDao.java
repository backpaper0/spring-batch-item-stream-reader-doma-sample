package sample;

import java.util.function.Function;
import java.util.stream.Stream;

import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.SelectType;

@Dao
public interface BookDao {

    @Select(strategy = SelectType.STREAM)
    <R> R select(Function<Stream<Book>, R> f);
}
