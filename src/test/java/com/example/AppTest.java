package com.example;

import static io.restassured.RestAssured.given;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class AppTest {

    @DataProvider(name = "apiData")
    public Object[][] readExcelData() throws IOException {
        FileInputStream file = new FileInputStream(new File("testdata.xlsx"));
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);
        int rowCount = sheet.getPhysicalNumberOfRows();
        Object[][] data = new Object[rowCount - 1][3];

        for (int i = 1; i < rowCount; i++) {
            Row row = sheet.getRow(i);
            data[i - 1][0] = row.getCell(0).getStringCellValue(); // Method
            data[i - 1][1] = row.getCell(1).getStringCellValue(); // Endpoint
            data[i - 1][2] = row.getCell(2) != null ? row.getCell(2).getStringCellValue() : ""; // Request Body
        }
        workbook.close();
        file.close();
        return data;
    }

    @Test(dataProvider = "apiData")
    public void testApiRequests(String method, String endpoint, String requestBody) {
        RestAssured.baseURI = "https://jsonplaceholder.typicode.com";
        Response response = null;

        switch (method.toUpperCase()) {
            case "GET":
                response = RestAssured.get(endpoint);
                break;
            case "POST":
                response = given().contentType("application/json").body(requestBody).post(endpoint).then().statusCode(201).extract().response();
                break;
            case "PUT":
                response = given().contentType("application/json").body(requestBody).put(endpoint).then().statusCode(200).extract().response();
                break;
            case "DELETE":
                response = RestAssured.delete(endpoint);
                break;
            default:
                throw new IllegalArgumentException("Invalid HTTP method: " + method);
        }
        System.out.println("Response for " + method + " " + endpoint + " : " + response.asPrettyString());
        System.out.println("Status Code: "+response.getStatusCode());
    }
}
