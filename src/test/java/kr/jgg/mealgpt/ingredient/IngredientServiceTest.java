package kr.jgg.mealgpt.ingredient;

import kr.jgg.mealgpt.ollama.OllamaClient;
import kr.jgg.mealgpt.upload.UploadService;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class IngredientServiceTest {
    private final IngredientService service = new IngredientService(
            mock(OllamaClient.class),
            new ObjectMapper(),
            mock(UploadService.class)
    );

    @Test
    void extractsStringArrayAndNormalizesEnglish() throws Exception {
        List<String> ingredients = extract("{\"ingredients\":[\"egg\",\"lettuce\",\"bell pepper\",\"egg\"]}");

        assertThat(ingredients).containsExactly("계란", "상추", "파프리카");
    }

    @Test
    void extractsObjectArrayWithKoreanNameKeys() throws Exception {
        List<String> ingredients = extract("```json\n{\"ingredients\":[{\"이름\":\"오랜지\"},{\"name\":\"tomato\"}]}\n```");

        assertThat(ingredients).containsExactly("오렌지", "토마토");
    }

    @Test
    void recoversNamesFromBrokenJson() throws Exception {
        List<String> ingredients = extract("{\"ingredients\":[{\"name\":\"cucumber\"},{\"이름\":\"시간치\"}");

        assertThat(ingredients).containsExactly("오이", "시금치");
    }

    @SuppressWarnings("unchecked")
    private List<String> extract(String raw) throws Exception {
        Method method = IngredientService.class.getDeclaredMethod("extractIngredients", String.class);
        method.setAccessible(true);
        return (List<String>) method.invoke(service, raw);
    }
}
