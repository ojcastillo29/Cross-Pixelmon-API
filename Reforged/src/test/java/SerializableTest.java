import com.google.gson.GsonBuilder;
import net.impactdev.pixelmonbridge.data.factory.JObject;
import net.impactdev.pixelmonbridge.details.SpecKeys;
import net.impactdev.pixelmonbridge.details.components.Moves;
import net.impactdev.pixelmonbridge.reforged.ReforgedDataManager;
import net.impactdev.pixelmonbridge.reforged.ReforgedPokemon;
import org.junit.Test;

public class SerializableTest {

    @Test
    public void run() {
        ReforgedPokemon pokemon = new ReforgedPokemon();
        pokemon.offer(SpecKeys.IV_HP, 15);
        pokemon.offer(SpecKeys.IV_ATK, 31);
        pokemon.offer(SpecKeys.MOVESET, new Moves(new Moves.Move("Tackle", 20, 3), new Moves.Move("Pound", 30, 2)));
        pokemon.offer(SpecKeys.ABILITY, "Wonderguard");
        pokemon.offer(SpecKeys.ABILITY_SLOT, 0);
        pokemon.offer(SpecKeys.GENDER, 1);
        pokemon.offer(SpecKeys.EV_SPATK, 255);
        pokemon.offer(SpecKeys.FORM, 0);
        pokemon.offer(SpecKeys.LEVEL, 5);
        pokemon.offer(SpecKeys.LEVEL_EXPERIENCE, 60);

        ReforgedDataManager data = new ReforgedDataManager();
        JObject json = data.serialize(pokemon);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(json.toJson()));

        ReforgedPokemon test = new ReforgedPokemon();
        test.offer(SpecKeys.IV_ATK, 16);

        JObject test2 = data.serialize(test);
        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(test2.toJson()));

    }

}
