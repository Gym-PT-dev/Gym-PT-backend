@Service
@RequiredArgsConstructor
public class FatSecretConversionService {

    private final ObjectMapper objectMapper;

    public DietRequestDto convertToDietDto(String fatSecretJson, MealType mealType) {
        try {
            JsonNode root = objectMapper.readTree(fatSecretJson);

            // 🔥 여기만 수정
            JsonNode foodsNode = root.path("items");

            List<DietRequestDto.FoodItemDto> foodList = new ArrayList<>();

            if (foodsNode.isArray()) {
                for (JsonNode item : foodsNode) {
                    String name = item.path("food_entry_name").asText("Unknown");
                    JsonNode nutri = item.path("eaten").path("total_nutritional_content");

                    int kcal = 0, carbs = 0, protein = 0, fat = 0;
                    if (!nutri.isMissingNode() && !nutri.isNull()) {
                        kcal = parseInt(nutri.path("calories").asText());
                        carbs = parseInt(nutri.path("carbohydrate").asText());
                        protein = parseInt(nutri.path("protein").asText());
                        fat = parseInt(nutri.path("fat").asText());
                    }

                    DietRequestDto.FoodItemDto dto = new DietRequestDto.FoodItemDto();
                    dto.setName(name);
                    dto.setAmount("1인분"); // 기본값
                    dto.setKcal(kcal);
                    dto.setCarbs(carbs);
                    dto.setProtein(protein);
                    dto.setFat(fat);

                    foodList.add(dto);
                }
            } else {
                // 디버그용: keys 찍어보기
                System.out.println("[FatSecretConversionService] items 배열을 찾지 못했습니다. root = " + root.toString());
            }

            DietRequestDto requestDto = new DietRequestDto();
            DietRequestDto.DietMealDto mealDto = new DietRequestDto.DietMealDto();
            mealDto.setMealType(mealType);
            mealDto.setFoods(foodList);
            requestDto.setMeals(List.of(mealDto));

            return requestDto;
        } catch (Exception e) {
            throw new RuntimeException("FatSecret Parsing Error", e);
        }
    }

    private int parseInt(String v) {
        try { return (int) Math.round(Double.parseDouble(v)); } catch (Exception e) { return 0; }
    }
}
