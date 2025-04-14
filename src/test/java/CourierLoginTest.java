import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Epic("API Яндекс.Самоката")
@Feature("Авторизация курьера")
@DisplayName("Тесты на авторизацию курьера")
public class CourierLoginTest extends BaseTest {
    private String existingLogin;
    private String existingPassword;
    private Integer createdCourierId;

    @Before
    @Step("Подготовка тестовых данных")
    public void prepareTestData() {
        existingLogin = "courier_" + System.currentTimeMillis();
        existingPassword = "1234";

        Allure.step("Создание тестового курьера", () -> {
            CourierRequest courier = new CourierRequest(existingLogin, existingPassword, "test");
            given()
                    .filter(new AllureRestAssured())
                    .header("Content-type", "application/json")
                    .body(courier)
                    .post("/api/v1/courier");
        });
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Успешная авторизация курьера")
    @Description("Проверка, что курьер может авторизоваться с валидными данными")
    @Story("Позитивный сценарий авторизации")
    public void successfulLoginReturns200AndId() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(new CourierLoginRequest(existingLogin, existingPassword))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Авторизация без логина")
    @Description("Попытка авторизации без указания логина должна возвращать ошибку 400")
    @Story("Негативный сценарий авторизации")
    public void loginWithoutLoginFieldReturns400() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(new CourierLoginRequest(null, existingPassword))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Авторизация без пароля")
    @Description("Попытка авторизации без указания пароля должна возвращать ошибку 400")
    @Story("Негативный сценарий авторизации")
    public void loginWithoutPasswordFieldReturns400() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(new CourierLoginRequest(existingLogin, null))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для входа"));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Авторизация с неверным паролем")
    @Description("Попытка авторизации с неверным паролем должна возвращать ошибку 404")
    @Story("Негативный сценарий авторизации")
    public void loginWithWrongPasswordReturns404() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(new CourierLoginRequest(existingLogin, "wrong_password"))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Авторизация несуществующего курьера")
    @Description("Попытка авторизации несуществующего курьера должна возвращать ошибку 404")
    @Story("Негативный сценарий авторизации")
    public void loginNonExistingCourierReturns404() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(new CourierLoginRequest("nonexisting_login", "1234"))
                .when()
                .post("/api/v1/courier/login")
                .then()
                .statusCode(404)
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @After
    @Step("Очистка тестовых данных")
    public void cleanup() {
        if (existingLogin != null) {
            Allure.step("Удаление тестового курьера", () -> {
                try {

                    Response response = given()
                            .filter(new AllureRestAssured())
                            .header("Content-type", "application/json")
                            .body(new CourierLoginRequest(existingLogin, existingPassword))
                            .post("/api/v1/courier/login");

                    if (response.statusCode() == 200) {
                        int courierId = response.jsonPath().getInt("id");
                        given()
                                .filter(new AllureRestAssured())
                                .when()
                                .delete("/api/v1/courier/" + courierId)
                                .then()
                                .statusCode(200);
                    }
                } catch (Exception e) {
                    Allure.addAttachment("Ошибка очистки",
                            "text/plain",
                            "Не удалось удалить тестового курьера: " + e.getMessage());
                }
            });
        }
    }
}