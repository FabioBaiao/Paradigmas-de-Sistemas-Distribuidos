package psd.directory.resources;

import psd.directory.api.Company;
import psd.directory.api.Data;
import psd.directory.core.Directory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/companies/{company}/previous")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PreviousDayPricesResource {

    private Directory directory;

    public PreviousDayPricesResource(Directory directory) {
        this.directory = directory;
    }

    @GET
    public Data getPreviousData(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            Data d = c.getPreviousDay();
            if (d != null) return d;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @PUT
    public Response setPreviousData(@PathParam("company") String company, Data data) {
        Company c = directory.get(company);
        if (c != null) {
            c.setPreviousDay(data);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/open")
    public double getCurrentDayOpenPrice(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            Data d = c.getPreviousDay();
            if (d != null) return d.getOpeningPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/min")
    public double getCurrentDayMinPrice(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            Data d = c.getPreviousDay();
            if (d != null) return d.getMinPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/max")
    public double getCurrentDayMaxPrice(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            Data d = c.getPreviousDay();
            if (d != null) return d.getMaxPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/close")
    public double getCurrentDayClosePrice(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            Data d = c.getPreviousDay();
            if (d != null) return d.getClosingPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
