package directory.resources;

import directory.api.Company;
import directory.core.Directory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/companies/{company}")
@Produces(MediaType.APPLICATION_JSON)
public class CompanyResource {

    private Directory directory;

    public CompanyResource(Directory directory) {
        this.directory = directory;
    }

    @GET
    public Company getCompany(@PathParam("company") String company) {
        if(directory.containsKey(company)){
            Company c = directory.get(company);
            if(c != null) return c;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @GET
    @Path("/exchange")
    public String getExchange(@PathParam("company") String company) {
        if(directory.containsKey(company)){
            String s = directory.get(company).getExchange();
            if(s != null) return s;
        }
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
}
