import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
public class CourierLoginTest extends BaseTest {
    private String existingLogin;
    private String existingPassword;

    @Before
    @Step("Подготовка тестовых данных")
    public void prepareTestData() {
        existingLogin = "courier_" + System.currentTimeMillis();
        existingPassword = "1234";
        CourierSteps.createCourier(new CourierRequest(existingLogin, existingPassword, "test"));
    }

    @Test
    @DisplayName("Успешная авторизация курьера")
    public void successfulLoginReturns200AndId() {
        CourierSteps.loginCourier(new CourierLoginRequest(existingLogin, existingPassword))
                .then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @After
    @Step("Очистка тестовых данных")
    public void cleanup() {
        Response response = CourierSteps.loginCourier(
                new CourierLoginRequest(existingLogin, existingPassword));

        if (response.statusCode() == 200) {
            int courierId = response.jsonPath().getInt("id");
            CourierSteps.deleteCourier(courierId);
        }
    }
}