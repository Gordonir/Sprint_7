import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

@Epic("API Яндекс.Самоката")
@Feature("Создание курьера")
@DisplayName("Тесты на создание курьера")
public class CourierCreationTest extends BaseTest {
    private CourierRequest testCourier;

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Успешное создание курьера")
    public void successfulCreationReturns201AndOkTrue() {
        testCourier = new CourierRequest("ninja_" + System.currentTimeMillis(), "1234", "saske");
        CourierSteps.createCourier(testCourier)
                .then()
                .statusCode(201)
                .body("ok", equalTo(true));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Создание дубликата курьера")
    public void duplicateCourierReturns409Conflict() {
        testCourier = new CourierRequest("duplicate_ninja_" + System.currentTimeMillis(), "1234", "saske");
        CourierSteps.createCourier(testCourier);
        CourierSteps.createCourier(testCourier)
                .then()
                .statusCode(409)
                .body("message", equalTo("Этот логин уже используется"));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Создание курьера без обязательного поля")
    public void createCourierWithoutRequiredFieldReturns400() {
        testCourier = new CourierRequest(null, "1234", "saske");
        CourierSteps.createCourier(testCourier)
                .then()
                .statusCode(400)
                .body("message", equalTo("Недостаточно данных для создания учетной записи"));
    }

    @After
    @Step("Очистка тестовых данных")
    public void cleanup() {
        if (testCourier != null && testCourier.getLogin() != null) {
            try {
                Response response = CourierSteps.loginCourier(
                        new CourierLoginRequest(testCourier.getLogin(), testCourier.getPassword()));
                if (response.statusCode() == 200) {
                    int courierId = response.jsonPath().getInt("id");
                    CourierSteps.deleteCourier(courierId);
                }
            } catch (Exception e) {
                System.out.println("Ошибка при удалении курьера: " + e.getMessage());
            }
        }
    }
}