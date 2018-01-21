package com.example.tradingsystem.resources;

import com.example.tradingsystem.api.Company;
import com.example.tradingsystem.api.CompanyList;
import com.example.tradingsystem.api.Data;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/companies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CompaniesResource {
    private Map<String, Company> companies;

    public CompaniesResource(){
    }

    public CompaniesResource(List<Company> companies){
        this.companies = new HashMap<>();
        companies.stream().forEach(c -> this.companies.put(c.getName(), c));
    }

    @GET
    public CompanyList listCompanies(){
        return new CompanyList(new ArrayList(companies.values()));
    }

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
    }

    @GET
    @Path("/{name}/exchange")
    public String getExchange(@PathParam("name") String name){
        if(companies.containsKey(name)){
            String s = companies.get(name).getExchange();
            if(s != null) return s;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @PUT
    @Path("/{comp}/exchange/{exc}")
    public Response putExchange(@PathParam("comp") String comp, @PathParam("exc") String exc){
        if(companies.containsKey(comp)){
            companies.get(comp).setExchange(exc);
            return Response.ok().build();
        }else{
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

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

    @GET
    @Path("/{name}/current")
    public Data getCurrentData(@PathParam("name") String name){
        if(companies.containsKey(name)){
            Data d = companies.get(name).getCurrentDay();
            if(d != null) return d;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
