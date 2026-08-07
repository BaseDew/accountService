package com.basedew;

import com.basedew.dto.CreateAccountRequest;
import com.basedew.dto.FundsDTO;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
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

@Path("/api/v1/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountResource {

    @POST
    @Transactional
    public Response create(@Valid CreateAccountRequest request, @Context UriInfo uriInfo) {
        try {
            Account account = new Account();
            account.userId = request.userId();
            account.currency = request.currency();
            account.persist();
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
    @Path("/{userId}")
    public Response getUserAccounts(@PathParam("userId") Long userId) {
        List<Account> accounts = Account.list("userId", userId);
        if (accounts.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("No accounts found for this user")
                    .build();
        }
        return Response.ok(accounts).build();
    }

    @GET
    @Path("/{userId}/{accountId}")
    public Response getDetails(@PathParam("userId") Long userId, @PathParam("accountId") Long accountId) {
        Account account = Account.findById(accountId);
        if (account != null && account.userId.equals(userId)) {
            return Response.ok(account).build();
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity("Specified account not found for user")
                .build();
    }

    @PATCH
    @Path("/{userId}/{accountId}/add")
    @Transactional
    public Response addFunds(@PathParam("userId") Long userId, @PathParam("accountId") Long accountId, @Valid FundsDTO fundsDto) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Specified account not found for user")
                    .build();
        }
        if (!account.currency.equals(fundsDto.currency())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Currency type not match account")
                    .build();
        }
        account.funds += fundsDto.funds();
        account.persist();
        return Response.ok().build();
    }

    @PATCH
    @Path("/{userId}/{accountId}/deduct")
    @Transactional
    public Response deductFunds(@PathParam("userId") Long userId, @PathParam("accountId") Long accountId, @Valid FundsDTO fundsDto) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Specified account not found for user")
                    .build();
        }
        if (!account.currency.equals(fundsDto.currency())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Currency type not match account")
                    .build();
        }
        if (account.funds - account.blockedFunds < fundsDto.funds()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Account balance too low")
                    .build();
        }
        account.funds -= fundsDto.funds(); //TODO deduct only if funds blocked?
        account.persist();
        return Response.ok().build();
    }

    @PATCH
    @Path("/{userId}/{accountId}/unblock")
    @Transactional
    public Response unblockFunds(@PathParam("userId") Long userId, @PathParam("accountId") Long accountId, @Valid FundsDTO fundsDto) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Specified account not found for user")
                    .build();
        }
        if (!account.currency.equals(fundsDto.currency())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Currency type not match account")
                    .build();
        }
        if (account.blockedFunds < fundsDto.funds()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("No funds to unblock")
                    .build();
        }
        account.blockedFunds -= fundsDto.funds();
        account.persist();
        return Response.ok().build();
    }

    @PATCH
    @Path("/{userId}/{accountId}/block")
    @Transactional
    public Response blockFunds(@PathParam("userId") Long userId, @PathParam("accountId") Long accountId, @Valid FundsDTO fundsDto) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Specified account not found for user")
                    .build();
        }
        if (!account.currency.equals(fundsDto.currency())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Currency type not match account")
                    .build();
        }
        if (account.funds - account.blockedFunds < fundsDto.funds()) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Account balance too low")
                    .build();
        }
        account.blockedFunds += fundsDto.funds();
        account.persist();
        return Response.ok().build();
    }

    @DELETE
    @Path("/{userId}/{accountId}")
    @Transactional
    public Response deleteAccount(@PathParam("userId") Long userId, @PathParam("accountId") Long accountId) {
        Account account = Account.findById(accountId);
        if (account == null || !account.userId.equals(userId)) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Specified account not found for user")
                    .build();
        }
        if (account.funds != 0) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Can not delete account with funds on it")
                    .build();
        }
        account.delete();
        return Response.ok().build();
    }
}
