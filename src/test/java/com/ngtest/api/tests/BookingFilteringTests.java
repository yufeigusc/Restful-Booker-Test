package com.ngtest.api.tests;

import com.ngtest.api.base.BaseTest;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BookingFilteringTests extends BaseTest {

    // 在测试开始前，创建一些特定的数据用于筛选
    @BeforeClass
    public void setupTestData() {
        // 创建一个名为 Eric 的预定
        createBookingForTest("Eric", "Guo", "2025-09-24", "2025-09-30");
        // 创建另一个也叫 Eric 的预定
        createBookingForTest("Eric", "Chen", "2025-10-01", "2025-10-05");
        // 创建一个名为 Yufei 的预定
        createBookingForTest("Yufei", "Guo", "2025-09-24", "2025-09-25");
    }

    @Test(description = "Filter bookings by firstname")
    public void filterByFirstNameTest() {
        given()
                .spec(requestSpec)
                // 添加查询参数
                .queryParam("firstname", "Eric")
                .when()
                .get("/booking")
                .then()
                .log().body()
                .statusCode(200)
                // 断言返回的数组不为空，且数组大小至少为2
                .body("bookingid", not(empty()))
                .body("size()", greaterThanOrEqualTo(2));
    }

    @Test(description = "Filter bookings by checkin date")
    public void filterByCheckinDateTest() {
        given()
                .spec(requestSpec)
                .queryParam("checkin", "2025-09-24")
                .when()
                .get("/booking")
                .then()
                .log().body()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
    }

    // 一个用于创建测试数据的帮助方法
    private void createBookingForTest(String firstname, String lastname, String checkin, String checkout) {
        Map<String, Object> booking = new HashMap<>();
        booking.put("firstname", firstname);
        booking.put("lastname", lastname);
        booking.put("totalprice", 200);
        booking.put("depositpaid", true);
        Map<String, String> dates = new HashMap<>();
        dates.put("checkin", checkin);
        dates.put("checkout", checkout);
        booking.put("bookingdates", dates);
        booking.put("additionalneeds", "Test Data");

        given().spec(requestSpec).contentType(ContentType.JSON).body(booking).post("/booking");
    }
}