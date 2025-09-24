package com.ngtest.api.helpers;

import io.restassured.http.ContentType;
import java.util.HashMap;
import java.util.Map;
import static io.restassured.RestAssured.given;

public class AuthHelper {

    // 这个方法将负责获取token
    public static String getAuthToken() {
        // 创建一个Map来存放JSON请求体
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", "admin");
        requestBody.put("password", "password123");

        // 发送POST请求
        String token = given()
                // 设置请求头，告诉服务器我们发送的是JSON
                .contentType(ContentType.JSON)
                // 设置请求体
                .body(requestBody)
                .when()
                // 指定请求的URL
                .post("https://restful-booker.herokuapp.com/auth")
                .then()
                // 断言HTTP状态码为200 (OK)
                .statusCode(200)
                // 从响应体中提取token字段的值
                // 这就是提取token的核心步骤！
                .extract().path("token");

        // 打印并返回token
        System.out.println("Extracted Token: " + token);
        return token;
    }
}