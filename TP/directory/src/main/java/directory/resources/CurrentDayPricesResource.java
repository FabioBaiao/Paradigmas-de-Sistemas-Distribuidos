package directory.resources;

import directory.api.Data;
import directory.core.Directory;

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
    public Data getCurrentData(@PathParam("company") String company){
        if(directory.containsKey(company)){
            Data d = directory.get(company).getCurrentDay();
            if(d != null) return d;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @DELETE
    public Response deleteCurrentData(@PathParam("company") String company) {
        if(directory.containsKey(company)){
            directory.get(company).setCurrentDay(new Data());
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/open")
    public double getCurrentDayOpenPrice(@PathParam("company") String company) {
        if(directory.containsKey(company)){
            Data d = directory.get(company).getCurrentDay();
            if(d != null) return d.getOpeningPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/min")
    public double getCurrentDayMinPrice(@PathParam("company") String company) {
        if(directory.containsKey(company)){
            Data d = directory.get(company).getCurrentDay();
            if(d != null) return d.getMinPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/max")
    public double getCurrentDayMaxPrice(@PathParam("company") String company) {
        if(directory.containsKey(company)){
            Data d = directory.get(company).getCurrentDay();
            if(d != null) return d.getMaxPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/close")
    public double getCurrentDayClosePrice(@PathParam("company") String company) {
        if(directory.containsKey(company)){
            Data d = directory.get(company).getCurrentDay();
            if(d != null) return d.getClosingPrice();
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }


    @PUT
    @Path("/open")
    public Response setCurrentDayOpenPrice(@PathParam("company") String company, double openingPrice) {
        if(directory.containsKey(company)){
            directory.get(company).getCurrentDay().setOpeningPrice(openingPrice);
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/min")
    public Response setCurrentDayMinPrice(@PathParam("company") String company, double minPrice) {
        if(directory.containsKey(company)){
            directory.get(company).getCurrentDay().setMinPrice(minPrice);
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/max")
    public Response setCurrentDayMaxPrice(@PathParam("company") String company, double maxPrice) {
        if(directory.containsKey(company)){
            directory.get(company).getCurrentDay().setMaxPrice(maxPrice);
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @PUT
    @Path("/close")
    public Response setCurrentDayClosePrice(@PathParam("company") String company, double closePrice) {
        if(directory.containsKey(company)){
            directory.get(company).getCurrentDay().setClosingPrice(closePrice);
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
