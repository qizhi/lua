Java client for the Cubeia Network User Service

The client is a Java wrapper for the HTTP REST API of the user service.

To use the client simply instantiate it with the URL (including context) of the user service.

EXAMPLE USAGE:

    import com.cubeia.backoffice.users.client;
    import com.cubeia.backoffice.users.api.dto.AuthenticationResponse;
    ...
    UserServiceClient client = new UserServiceClientHTTP("http://localhost:8080/user-service");
    ...
    AuthenticationResponse authResponse = client.authenticate(myOperatorId, "user", "password");
    ...
    

