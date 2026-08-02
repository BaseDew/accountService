package com.basedew;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/v1/account")
public class AccountResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    public String create(Account account) {
        try {
            account.persist();
            return "Account successfully created";
        } catch (Exception e) {
            return "An error occurred, could not create user account. " + e.getMessage();
        }
    }

    @GET
    @Path("/{userId}")
    public List<Account> getUserAccounts(@PathParam("userId") long userId) {
        return Account.list("userId", userId);
    }

    @GET
    @Path("/{userId}/{accountId}")
    public Account getDetails(@PathParam("userId") long userId, @PathParam("accountId") long accountId) {
        Account account = Account.findById(accountId);
        if (account.userId.equals(userId)){
            return account;
        }
        return null;
    }
}
