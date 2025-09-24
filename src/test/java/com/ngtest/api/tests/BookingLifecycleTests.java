package com.ngtest.api.tests;

import com.ngtest.api.base.BaseTest;
import com.ngtest.api.helpers.AuthHelper;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;


// 继承BaseTest
public class BookingLifecycleTests extends BaseTest {

    private String authToken;
    private int bookingId;

    // 在这个测试类运行前，获取一次token
    @BeforeClass
    public void getAuthToken() {
        authToken = AuthHelper.getAuthToken();
    }

    @Test(priority = 1, description = "Create a new booking")
    public void createBookingTest() {
        // 准备请求体
        Map<String, Object> bookingDetails = new HashMap<>();
        bookingDetails.put("firstname", "Jim");
        bookingDetails.put("lastname", "Brown");
        bookingDetails.put("totalprice", 111);
        bookingDetails.put("depositpaid", true);
        Map<String, String> bookingDates = new HashMap<>();
        bookingDates.put("checkin", "2023-01-01");
        bookingDates.put("checkout", "2024-01-01");
        bookingDetails.put("bookingdates", bookingDates);
        bookingDetails.put("additionalneeds", "Breakfast");

        // 发送请求
        bookingId = given()
                // 使用在BaseTest中创建的通用配置
                .spec(requestSpec)
                // 为本次请求添加JSON内容类型和请求体
                .contentType(ContentType.JSON)
                .body(bookingDetails)
                .when()
                .post("/booking")
                .then()
                .log().body()
                .statusCode(200)
                // 验证响应体
                .body("bookingid", is(notNullValue()))
                .body("booking.firstname", equalTo("Jim"))
                // 从响应中提取出bookingid
                .extract().path("bookingid");

        System.out.println("Created booking with ID: " + bookingId);
    }

    @Test(priority = 2, dependsOnMethods = "createBookingTest", description = "Delete the created booking")
    public void deleteBookingTest() {
        given()
                // 使用通用配置
                .spec(requestSpec)
                // 关键：根据最新API文档，将token作为Cookie传入
                .contentType(ContentType.JSON)
                .cookie("token", authToken)
                .when()
                // 使用上一步创建的bookingId
                .delete("/booking/{id}", bookingId)
                .then()
                // 删除成功后，预期状态码是201 Created
                .statusCode(201);

        System.out.println("Booking with ID: " + bookingId + " has been deleted.");
    }

    // ... 在 BookingLifecycleTests 中 ...

    @Test(priority = 3, description = "Partially update a booking and verify data integrity")
    public void partialUpdateBookingTest() {
        // 步骤1：准备我们想要创建的原始数据，并将其保存在一个Map中
        Map<String, Object> initialRequestData = new HashMap<>();
        initialRequestData.put("firstname", "OriginalFirstname");
        initialRequestData.put("lastname", "OriginalLastname");
        initialRequestData.put("totalprice", 500);
        initialRequestData.put("depositpaid", true);
        Map<String, String> dates = new HashMap<>();
        dates.put("checkin", "2025-11-11");
        dates.put("checkout", "2025-11-12");
        initialRequestData.put("bookingdates", dates);

        // 步骤2：发送创建请求，并只从响应中提取我们需要的 bookingId
        int testBookingId = given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .body(initialRequestData) // 使用我们准备好的原始数据
                .when()
                .post("/booking")
                .then()
                .statusCode(200)
                .extract().path("bookingid"); // 只提取ID

        System.out.println("Created booking for PATCH test with ID: " + testBookingId);

        // 步骤3：准备PATCH请求体，只更新firstname和totalprice
        Map<String, Object> patchData = new HashMap<>();
        patchData.put("firstname", "Yufei-Updated");
        patchData.put("totalprice", 999);

        String token = AuthHelper.getAuthToken();

        given()
                .spec(requestSpec)
                .contentType(ContentType.JSON)
                .cookie("token", token)
                .body(patchData)
                .when()
                .patch("/booking/{id}", testBookingId)
                .then()
                .statusCode(200)
                .body("firstname", equalTo("Yufei-Updated"))
                .body("totalprice", equalTo(999));

        // 步骤4：再次GET这条记录，进行最终的数据完整性验证
        given()
                .spec(requestSpec)
                .when()
                .get("/booking/{id}", testBookingId)
                .then()
                .log().body()
                .statusCode(200)
                // 断言被修改的字段
                .body("firstname", equalTo("Yufei-Updated"))
                .body("totalprice", equalTo(999))
                // 关键修正：断言未被修改的字段值，与我们最开始定义的 initialRequestData 中的值完全一样！
                .body("lastname", equalTo(initialRequestData.get("lastname")))
                .body("depositpaid", equalTo(initialRequestData.get("depositpaid")));

        System.out.println("PATCH test successful. Data integrity verified.");
    }

//    // 帮助方法：创建一个预定并返回其完整信息
//    private Map<String, Object> createBookingAndGetDetails() {
//        Map<String, Object> booking = new HashMap<>();
//        booking.put("firstname", "OriginalFirstname");
//        booking.put("lastname", "OriginalLastname");
//        booking.put("totalprice", 500);
//        booking.put("depositpaid", true);
//        Map<String, String> dates = new HashMap<>();
//        dates.put("checkin", "2025-11-11");
//        dates.put("checkout", "2025-11-12");
//        booking.put("bookingdates", dates);
//
//        // 使用 .extract().as(Map.class) 可以将整个响应体转为Map，方便后续比较
//        return given().spec(requestSpec).contentType(ContentType.JSON).body(booking)
//                .post("/booking").as(Map.class);
//    }

    @Test(description = "Verify the structure of the get booking response")
    public void getBooking_SchemaValidationTest() {
        given()
                .spec(requestSpec)
                .when()
                .get("/booking/1")
                .then()
                .statusCode(200)
                // 关键：断言响应体符合指定的JSON Schema文件
                .body(matchesJsonSchemaInClasspath("schemas/booking_schema.json"));

        System.out.println("Get Booking API response schema validated successfully.");
    }
}