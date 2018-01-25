package psd.directory.resources;

import psd.directory.api.Company;
import psd.directory.api.Data;
import psd.directory.core.Directory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/companies/{company}/current")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CurrentDayPricesResource {

    private Directory directory;

    public CurrentDayPricesResource(Directory directory) {
        this.directory = directory;
    }

    @GET
    public Data getCurrentData(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            Data d = c.getCurrentDay();
            if (d != null) return d;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @DELETE
    public Response deleteCurrentData(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            c.setCurrentDay(new Data());
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
            Data d = c.getCurrentDay();
            if (d != null && d.getOpeningPrice() >= 0)
                return d.getOpeningPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/min")
    public double getCurrentDayMinPrice(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            Data d = c.getCurrentDay();
            if (d != null && d.getMinPrice() >= 0)
                return d.getMinPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/max")
    public double getCurrentDayMaxPrice(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            Data d = c.getCurrentDay();
            if (d != null && d.getMaxPrice() >= 0)
                return d.getMaxPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/close")
    public double getCurrentDayClosePrice(@PathParam("company") String company) {
        Company c = directory.get(company);
        if (c != null) {
            Data d = c.getCurrentDay();
            if (d != null && d.getClosingPrice() >= 0)
                return d.getClosingPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }


    @PUT
    @Path("/open")
    public Response setCurrentDayOpenPrice(@PathParam("company") String company, double openingPrice) {
        Company c = directory.get(company);
        if (c != null) {
            c.getCurrentDay().setOpeningPrice(openingPrice);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/min")
    public Response setCurrentDayMinPrice(@PathParam("company") String company, double minPrice) {
        Company c = directory.get(company);
        if (c != null) {
            c.getCurrentDay().setMinPrice(minPrice);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/max")
    public Response setCurrentDayMaxPrice(@PathParam("company") String company, double maxPrice) {
        Company c = directory.get(company);
        if (c != null) {
            c.getCurrentDay().setMaxPrice(maxPrice);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/close")
    public Response setCurrentDayClosePrice(@PathParam("company") String company, double closePrice) {
        Company c = directory.get(company);
        if (c != null) {
            c.getCurrentDay().setClosingPrice(closePrice);
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
