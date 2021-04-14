package betaversion.Remote;

import Model.MyResponse;
import Model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIServices {

    @Headers (
        {
            "Content-Type:application/json",
            "Authorization:key=AAAASC29szI:APA91bH9Ts5UTzEVnOVy3OslZfty8kch7mgK4c0v8aOPq_mO0xDFfBp9R_maDPdKmWSzbPidQTlYIlZhVqbpFbkhnnlMZyGANZHFr6oN2zQSkQgyv03diBSDmj4RW0QwJvRXOt6mdWZ0w0tVyDJJMnVaBCcsBKTfhA"
        }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
