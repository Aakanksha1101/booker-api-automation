package testscripts;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import constants.Status_Code;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import pojo.request.createBooking.BookingDates;
import pojo.request.createBooking.CreateBookingRequest;

//given - all input details[URI, headers, path/query parameters, payload, headertype]
//when - submit api
//then - validate the response

public class CreateBookingTest {

	String token; // as token is needed in all the other methods
	CreateBookingRequest payload;
	int bookingId;

	@BeforeMethod
	// as this token is getting used for rest methods
	public void generateToken() {
		RestAssured.baseURI = "https://restful-booker.herokuapp.com";
		// parsed json in string format
		Response res = RestAssured.given().headers("Content-Type", "application/json")
				.body("{\r\n" + "    \"username\" : \"admin\",\r\n" + "    \"password\" : \"password123\"\r\n" + "}")
				.when().post("/Auth");
		// System.out.println(res.asPrettyString());
		Assert.assertEquals(res.statusCode(), 200, "Status code should be 200");

		token = res.jsonPath().getString("token");
		// System.out.println(token);
	}

	@Test(enabled = false)
	// using cucumber given when then
	public void createBookingTest() {

		Response res = RestAssured.given().headers("Content-Type", "application/json")
				.headers("Accept", "application/json")
				.body("{\r\n" + "    \"firstname\" : \"Jim\",\r\n" + "    \"lastname\" : \"Brown\",\r\n"
						+ "    \"totalprice\" : 111,\r\n" + "    \"depositpaid\" : true,\r\n"
						+ "    \"bookingdates\" : {\r\n" + "        \"checkin\" : \"2018-01-01\",\r\n"
						+ "        \"checkout\" : \"2019-01-01\"\r\n" + "    },\r\n"
						+ "    \"additionalneeds\" : \"Breakfast\"\r\n" + "}")
				.when().post("/booking");

		// System.out.println(res.statusCode());
		Assert.assertEquals(res.getStatusCode(), Status_Code.OK);
	}

	@Test(priority = 0, enabled = true)
	// using cucumber given when then
	public void createBookingTestWithPOJO() {

		BookingDates bookingDates = new BookingDates();
		bookingDates.setCheckin("2018-01-01");
		bookingDates.setCheckout("2018-01-03");

		payload = new CreateBookingRequest();
		payload.setFirstname("Aakanksha");
		payload.setLastname("G");
		payload.setTotalprice(1200);
		payload.setDepositpaid(true);
		payload.setAdditionalneeds("breakfast");
		payload.setBookingdates(bookingDates);

		Response res = RestAssured.given().headers("Content-Type", "application/json")
				.headers("Accept", "application/json").body(payload).log().all().when().post("/booking");

		Assert.assertEquals(res.getStatusCode(), Status_Code.OK);
		// Assert.assertTrue(Integer.valueOf(res.jsonPath().getInt("bookingid"))
		// instanceof Integer);
		bookingId = res.jsonPath().getInt("bookingid"); //
		Assert.assertTrue(bookingId > 0);
		validateResponse(res, payload, "booking.");

	}

	@Test(priority = 1)
	// using cucumber given when then
	public void getBookingIdTest() {

		Response res = RestAssured.given().headers("Content-Type", "application/json").log().all().when()
				.get("/booking/" + bookingId);
		Assert.assertEquals(res.getStatusCode(), Status_Code.OK);
		validateResponse(res, payload, "");

	}

	@Test(enabled=false)
	// using cucumber given when then
	public void getBookingIdDeserializedTest() {

		// bookingId= 7471;
		Response res = RestAssured.given().headers("Content-Type", "application/json").log().all().when()
				.get("/booking/" + bookingId);
		Assert.assertEquals(res.getStatusCode(), Status_Code.OK);

		CreateBookingRequest responseBody = res.as(CreateBookingRequest.class);
		Assert.assertTrue(responseBody.equals(payload));
		 
	}

	@Test(priority = 2)
	// using cucumber given when then
	public void getAllBookings() {

		Response res = RestAssured.given().headers("Content-Type", "application/json").log().all().when()
				.get("/booking");

		// System.out.println(res.asPrettyString());

		Assert.assertEquals(res.getStatusCode(), Status_Code.OK);
		List<String> listOfBookingIds = res.jsonPath().getList("bookingid");
		Assert.assertTrue(listOfBookingIds.contains(bookingId));

	}
	
	@Test(priority = 3)
	// using cucumber given when then
	public void updateBookingIdTest() {

		payload = new CreateBookingRequest();
		payload.setFirstname("Aakanksha_Updated");

		Response res = RestAssured.given()
				.headers("Content-Type", "application/json")
				.headers("Accept", "application/json")
				.headers("Cookies", "token="+token)
				.body(payload).log().all()
				.when()
				.put("/booking/"+bookingId);

		Assert.assertEquals(res.getStatusCode(), Status_Code.OK);
		//validateResponse(res, payload, "booking.");

	}

	
	

	@Test(enabled = false)
	// plain method
	public void createBookingTestPlain() {
		String payload = "{\r\n" + "    \"username\" : \"admin\",\r\n" + "    \"password\" : \"password123\"\r\n" + "}";

		RequestSpecification reqSpec = RestAssured.given();
		reqSpec.baseUri("https://restful-booker.herokuapp.com");
		reqSpec.headers("Content-Type", "application/json");
		reqSpec.body(payload);
		reqSpec.post("/auth");

	}

	private void validateResponse(Response res, CreateBookingRequest payload, String object) {
		Assert.assertEquals(res.jsonPath().getString(object + "firstname"), payload.getFirstname());
		Assert.assertEquals(res.jsonPath().getString(object + "lastname"), payload.getLastname());
		Assert.assertEquals(res.jsonPath().getInt(object + "totalprice"), payload.getTotalprice());
		Assert.assertEquals(res.jsonPath().getBoolean(object + "depositpaid"), payload.isDepositpaid());
		Assert.assertEquals(res.jsonPath().getString(object + "bookingdates.checkin"),
				payload.getBookingdates().getCheckin());
		Assert.assertEquals(res.jsonPath().getString(object + "bookingdates.checkout"),
				payload.getBookingdates().getCheckout());
	}

}
