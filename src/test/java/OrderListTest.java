import io.qameta.allure.*;
import io.qameta.allure.junit4.DisplayName;
import io.qameta.allure.restassured.AllureRestAssured;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.time.LocalDate;
import java.util.Arrays;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Epic("API Яндекс.Самоката")
@Feature("Получение списка заказов")
@DisplayName("Тесты для получения списка заказов")
public class OrderListTest extends BaseTest {
    private String testCourierId;
    private String testOrderId;
    private String testOrderTrack;

    @Before
    @Step("Подготовка тестовых данных")
    public void prepareTestData() {
        try {
            // 1. Создаем тестового курьера
            CourierRequest courier = new CourierRequest(
                    "courier_" + System.currentTimeMillis(),
                    "pass123",
                    "Test Courier"
            );

            testCourierId = given()
                    .filter(new AllureRestAssured())
                    .header("Content-type", "application/json")
                    .body(courier)
                    .when()
                    .post("/api/v1/courier")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("id")
                    .toString();


            OrderRequest order = new OrderRequest(
                    "Иван",
                    "Иванов",
                    "Москва, ул. Тестовая, д. 1",
                    "1", // станция метро "Черкизовская"
                    "+79991112233",
                    1,
                    LocalDate.now().plusDays(1).toString(),
                    "Тестовый комментарий",
                    Arrays.asList("BLACK")
            );

            testOrderTrack = given()
                    .filter(new AllureRestAssured())
                    .header("Content-type", "application/json")
                    .body(order)
                    .when()
                    .post("/api/v1/orders")
                    .then()
                    .statusCode(201)
                    .extract()
                    .path("track")
                    .toString();


            testOrderId = given()
                    .filter(new AllureRestAssured())
                    .queryParam("t", testOrderTrack)
                    .when()
                    .get("/api/v1/orders/track")
                    .then()
                    .statusCode(200)
                    .extract()
                    .path("order.id")
                    .toString();

        } catch (Exception e) {
            throw new RuntimeException("Не удалось подготовить тестовые данные: " + e.getMessage(), e);
        }
    }

    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Получение списка всех заказов")
    @Description("Должен возвращаться список заказов с корректной структурой")
    @Story("Позитивный сценарий получения заказов")
    public void shouldReturnListOfOrders() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200)
                .body("orders", not(empty()))
                .body("orders[0].id", notNullValue())
                .body("orders[0].firstName", notNullValue())
                .body("orders[0].lastName", notNullValue())
                .body("pageInfo", notNullValue())
                .body("pageInfo.page", notNullValue())
                .body("pageInfo.total", notNullValue())
                .body("pageInfo.limit", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Получение заказов конкретного курьера")
    @Description("Должны возвращаться только заказы указанного курьера")
    @Story("Фильтрация заказов по курьеру")
    public void shouldReturnOrdersForSpecificCourier() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .queryParam("courierId", testCourierId)
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200)
                .body("orders", not(empty()))
                .body("orders.courierId", everyItem(equalTo(testCourierId)));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Фильтрация заказов по станции метро")
    @Description("Должны возвращаться только заказы для указанных станций метро")
    @Story("Фильтрация заказов по метро")
    public void shouldFilterOrdersByMetroStation() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .queryParam("nearestStation", "[\"1\"]") // Черкизовская
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200)
                .body("orders", not(empty()))
                .body("orders.metroStation", everyItem(equalTo("1")));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Пагинация списка заказов")
    @Description("Должна работать пагинация при получении списка заказов")
    @Story("Пагинация заказов")
    public void shouldPaginateOrderList() {
        int limit = 2;
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .queryParam("limit", limit)
                .queryParam("page", 0)
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(200)
                .body("orders.size()", lessThanOrEqualTo(limit))
                .body("pageInfo.limit", equalTo(limit))
                .body("pageInfo.page", equalTo(0));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Запрос заказов несуществующего курьера")
    @Description("Должна возвращаться ошибка 404 для несуществующего курьера")
    @Story("Негативный сценарий получения заказов")
    public void shouldReturn404ForNonExistentCourier() {
        given()
                .filter(new AllureRestAssured())
                .header("Content-type", "application/json")
                .queryParam("courierId", "999999")
                .when()
                .get("/api/v1/orders")
                .then()
                .statusCode(404)
                .body("message", equalTo("Курьер с идентификатором 999999 не найден"));
    }

    @After
    @Step("Очистка тестовых данных")
    public void cleanTestData() {
        try {

            if (testOrderTrack != null) {
                given()
                        .filter(new AllureRestAssured())
                        .queryParam("track", testOrderTrack)
                        .when()
                        .put("/api/v1/orders/cancel")
                        .then()
                        .statusCode(200);
            }


            if (testCourierId != null) {
                given()
                        .filter(new AllureRestAssured())
                        .when()
                        .delete("/api/v1/courier/" + testCourierId)
                        .then()
                        .statusCode(200);
            }
        } catch (Exception e) {
            Allure.addAttachment("Ошибка очистки", "text/plain", e.getMessage());
        }
    }
}