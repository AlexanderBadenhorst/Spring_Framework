package tacos.chap4.data;

import org.springframework.data.repository.CrudRepository;
import tacos.chap4.Ingredient;

public interface IngredientRepository extends CrudRepository<Ingredient, String>{

}
