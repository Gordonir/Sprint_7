import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Epic("API Яндекс.Самоката")
@Feature("Создание заказа")
@DisplayName("Тесты на создание заказа")
@RunWith(Parameterized.class)
public class OrderCreationTest extends BaseTest {

    private final OrderRequest order;
    private final String testCaseName;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> testData() {
        return Arrays.asList(new Object[][]{
                {"Заказ только с BLACK цветом", Arrays.asList("BLACK")},
                {"Заказ только с GREY цветом", Arrays.asList("GREY")},
                {"Заказ с обоими цветами", Arrays.asList("BLACK", "GREY")},
                {"Заказ без указания цвета", null}
        });
    }

    public OrderCreationTest(String testCaseName, List<String> colors) {
        this.testCaseName = testCaseName;
        this.order = new OrderRequest(
                "Naruto",
                "Uchiha",
                "Konoha, 142 apt.",
                "4",
                "+7 800 355 35 35",
                5,
                "2024-06-06",
                "Saske, come back to Konoha",
                colors
        );
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Создание заказа с разными вариантами цветов")
    @Description("Проверка что заказ создается с разными комбинациями цветов")
    @Story("Позитивные сценарии создания заказа")
    public void shouldCreateOrderWithDifferentColorOptions() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(201)
                .body("track", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Проверка формата track-номера")
    @Description("Убедиться что track-номер является положительным целым числом")
    @Story("Валидация ответа сервера")
    public void trackNumberShouldBePositiveInteger() {
        OrderRequest testOrder = new OrderRequest(
                "Naruto",
                "Uchiha",
                "Konoha, 142 apt.",
                "4",
                "+7 800 355 35 35",
                5,
                "2024-06-06",
                "Saske, come back to Konoha",
                Arrays.asList("BLACK")
        );

        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(testOrder)
                .when()
                .post("/api/v1/orders")
                .then()
                .body("track", notNullValue())
                .body("track", instanceOf(Integer.class))
                .body("track", greaterThan(0));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Создание заказа без обязательных полей")
    @Description("Попытка создать заказ без обязательных полей должна возвращать ошибку")
    @Story("Негативные сценарии создания заказа")
    public void shouldNotCreateOrderWithoutRequiredFields() {

        String invalidOrderJson = "{"
                + "\"firstName\": null, "
                + "\"lastName\": null, "
                + "\"address\": null, "
                + "\"metroStation\": null, "
                + "\"phone\": null, "
                + "\"rentTime\": 0, "  // для примитивного int указываем 0
                + "\"deliveryDate\": null, "
                + "\"comment\": null, "
                + "\"color\": null"
                + "}";

        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(invalidOrderJson)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400);
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Создание заказа с неверным цветом")
    @Description("Попытка создать заказ с недопустимым цветом должна возвращать ошибку")
    @Story("Негативные сценарии создания заказа")
    public void shouldNotCreateOrderWithInvalidColor() {
        OrderRequest invalidOrder = new OrderRequest(
                "Naruto",
                "Uchiha",
                "Konoha, 142 apt.",
                "4",
                "+7 800 355 35 35",
                5,
                "2024-06-06",
                "Saske, come back to Konoha",
                Arrays.asList("RED")  // Недопустимый цвет
        );

        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(invalidOrder)
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400);
    }
}