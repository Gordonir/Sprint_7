import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Epic("API Яндекс.Самоката")
@Feature("Создание курьера")
@DisplayName("Тесты на создание курьера")
public class CourierCreationTest extends BaseTest {
    private CourierRequest testCourier;

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Успешное создание курьера")
    @Description("Проверка, что курьер создается с валидными данными и возвращается код 201")
    @Story("Позитивный сценарий создания курьера")
    public void successfulCreationReturns201AndOkTrue() {
        testCourier = new CourierRequest("ninja_" + System.currentTimeMillis(), "1234", "saske");

        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(testCourier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Создание дубликата курьера")
    @Description("Попытка создать курьера с уже существующим логином должна возвращать ошибку 409")
    @Story("Негативный сценарий создания курьера")
    public void duplicateCourierReturns409Conflict() {
        testCourier = new CourierRequest("duplicate_ninja", "1234", "saske");


        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(testCourier)
                .when()
                .post("/api/v1/courier");


        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(testCourier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется"));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Создание курьера без обязательного поля")
    @Description("Попытка создать курьера без указания логина должна возвращать ошибку 400")
    @Story("Негативный сценарий создания курьера")
    public void createCourierWithoutRequiredFieldReturns400() {
        testCourier = new CourierRequest(null, "1234", "saske");

        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(testCourier)
                .when()
                .post("/api/v1/courier")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @After
    @Step("Очистка тестовых данных")
    public void cleanup() {
        if (testCourier != null && testCourier.getLogin() != null) {
            try {
                Allure.step("Получение ID курьера для удаления", () -> {
                    Response response = given()
                            .filter(new AllureRestAssured())
                            .header("Content-type", "application/json")
                            .body(new CourierLoginRequest(testCourier.getLogin(), testCourier.getPassword()))
                            .when()
                            .post("/api/v1/courier/login");

                    if (response.getStatusCode() == 200) {
                        int courierId = response.jsonPath().getInt("id");

                        Allure.step("Удаление курьера с ID: " + courierId, () -> {
                            given()
                                    .filter(new AllureRestAssured())
                                    .when()
                                    .delete("/api/v1/courier/" + courierId)
                                    .then()
                                    .statusCode(200);
                        });
                    }
                });
            } catch (Exception e) {
                Allure.addAttachment("Ошибка очистки",
                        "text/plain",
                        "Не удалось удалить курьера: " + e.getMessage());
            }
        }
    }
}