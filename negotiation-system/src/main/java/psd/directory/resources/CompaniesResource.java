package psd.directory.resources;

import psd.directory.api.CompanyList;
import psd.directory.core.Directory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Path("/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompaniesResource {

    Directory directory;

    public CompaniesResource(){
    }

    public CompaniesResource(Directory directory){
        this.directory = directory;
    }

    @GET
    public List<String> listCompanies() {
        return directory.values().stream()
                        .map(c -> c.getName())
                        .collect(Collectors.toList());
    }

    /*
    NOT NECESSARY!

    @PUT
    @Path("/{name}")
    public Response putCompany(@PathParam("name") String name){
        if(companies.containsKey(name)){
            return Response.notModified().build();
        }
        else{
            Company newC = new Company(name);
            synchronized (companies){
                companies.put(name, newC);
            }
            return Response.ok().build();
        }
    }*/

    /*
    MOVED TO COMPANYRESOURCE

    @GET
    @Path("/{name}/exchange")
    public String getExchange(@PathParam("name") String name){
        if(companies.containsKey(name)){
            String s = companies.get(name).getExchange();
            if(s != null) return s;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    */

    /*
    NOT NECESSARY!

    @PUT
    @Path("/{comp}/exchange/{exc}")
    public Response putExchange(@PathParam("comp") String comp, @PathParam("exc") String exc){
        if(companies.containsKey(comp)){
            companies.get(comp).setExchange(exc);
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }*/

    /*
    NOT NECESSARY

    @PUT
    @Path("/{name}/current")
    public Response setCurrentData(@PathParam("name") String name, Data currentData){
        if(companies.containsKey(name)){
            companies.get(name).setCurrentDay(currentData);
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
    */

    /*
    MOVED TO CURRENTDAYPRICESRESOURCE

    @GET
    @Path("/{name}/current")
    public Data getCurrentData(@PathParam("name") String name){
        if(companies.containsKey(name)){
            Data d = companies.get(name).getCurrentDay();
            if(d != null) return d;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    */
}
