import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrderSteps {

    @Step("Создание заказа")
    public static Response createOrder(Object order) {
        return given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ApiEndpoints.ORDERS_CREATE);
    }

    @Step("Создание заказа")
    public static Response createOrder(OrderRequest order) {
        return given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .body(order)
                .when()
                .post(ApiEndpoints.ORDERS_CREATE);
    }

    @Step("Получение заказа по треку")
    public static Response getOrderByTrack(String track) {
        return given()
                .filter(new AllureRestAssured())
                .queryParam("t", track)
                .when()
                .get(ApiEndpoints.ORDERS_TRACK);
    }

    @Step("Получение списка заказов")
    public static Response getOrdersList(String courierId, String nearestStation, int limit, int page) {
        return given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .queryParam("courierId", courierId)
                .queryParam("nearestStation", nearestStation)
                .queryParam("limit", limit)
                .queryParam("page", page)
                .when()
                .get(ApiEndpoints.ORDERS_LIST);
    }

    @Step("Отмена заказа")
    public static void cancelOrder(String track) {
        given()
                .filter(new AllureRestAssured())
                .queryParam("track", track)
                .when()
                .put(ApiEndpoints.ORDERS_CANCEL)
                .then()
                .statusCode(200);
    }
}
