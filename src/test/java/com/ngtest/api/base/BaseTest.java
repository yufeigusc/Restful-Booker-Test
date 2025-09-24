package com.ngtest.api.base;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    // 将RequestSpecification定义为静态变量，方便所有测试类使用
    protected static RequestSpecification requestSpec;

    @BeforeSuite(alwaysRun = true)
    public void setup() {
        // 创建一个RequestSpecBuilder实例
        RequestSpecBuilder builder = new RequestSpecBuilder();

        // 设置所有请求的基础URI
        builder.setBaseUri("https://restful-booker.herokuapp.com");

        // 关键：添加所有请求都需要的通用Header
        builder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36");
        builder.addHeader("Accept", "application/json");

        // 从builder构建出RequestSpecification
        requestSpec = builder.build();

        System.out.println("Request Specification has been built.");
    }
}