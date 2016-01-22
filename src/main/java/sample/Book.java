package sample;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;

@Entity
public class Book {
    @Id
    public Long id;
    public String title;
}
