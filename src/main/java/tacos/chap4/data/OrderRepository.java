package tacos.chap4.data;

import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import tacos.chap4.TacoOrder;

public interface OrderRepository extends CrudRepository<TacoOrder, UUID>{

}
