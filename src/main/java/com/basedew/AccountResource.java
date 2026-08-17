package com.basedew;

import com.basedew.dto.CreateAccountRequest;
import com.basedew.dto.TransactionDto;
import com.basedew.service.AccountService;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

@Path("/api/v1/account/{userId}")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    private final AccountService accountService;

    public AccountResource(AccountService accountService) {
        this.accountService = accountService;
    }

    @POST
    @Transactional
    public Response create(CreateAccountRequest request, @Context UriInfo uriInfo) {
        try {
            Account account = accountService.create(request);
            URI createdUri = uriInfo.getAbsolutePathBuilder()
                    .path(String.valueOf(account.userId))
                    .path(String.valueOf(account.id))
                    .build();
            return Response.created(createdUri)
                    .entity(account)
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Could not create user account: " + e.getMessage())
                    .build();
        }
    }

    @GET
    public Response getUserAccounts(@PathParam("userId") Long userId) {
        List<Account> accounts = accountService.getUserAccounts(userId);
        if (accounts.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No accounts found for this user")
                    .build();
        }
        return Response.ok(accounts).build();
    }

    @GET
    @Path("/{accountId}")
    public Response getDetails(@PathParam("userId") Long userId, @PathParam("accountId") Long accountId) {
        Account account = accountService.getDetails(accountId);
        if (account != null && account.userId.equals(userId)) {
            return Response.ok(account).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Specified account not found for user")
                .build();
    }

    @DELETE
    @Path("/{accountId}")
    @Transactional
    public Response deleteAccount(@PathParam("userId") Long userId, @PathParam("accountId") Long accountId) {
        try {
            if(accountService.deleteAccount(userId, accountId)) {
                return Response.ok().build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
