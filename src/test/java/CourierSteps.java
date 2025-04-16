import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;


public class CourierSteps {
    @Step("Создание курьера")
    public static Response createCourier(CourierRequest courier) {
        return given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(courier)
                .when()
                .post(ApiEndpoints.COURIER_CREATE);
    }

    @Step("Логин курьера")
    public static Response loginCourier(CourierLoginRequest loginRequest) {
        return given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(loginRequest)
                .when()
                .post(ApiEndpoints.COURIER_LOGIN);
    }

    @Step("Удаление курьера")
    public static void deleteCourier(int courierId) {
        given()
                .filter(new AllureRestAssured())
                .when()
                .delete(ApiEndpoints.COURIER_DELETE + courierId)
                .then()
                .statusCode(200);
    }
}
