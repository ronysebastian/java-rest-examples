/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.arven.rs.services.example;

import static io.github.arven.rs.services.example.MicroBlogRestResource.MAX_LIST_SPAN;

import io.github.arven.rs.types.DataList;
import io.github.arven.rs.types.Hyper;
import java.io.Serializable;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

/**
 * This is the Group representation for the RESTful web service. You can
 * get group info, get a list of members, leave a group, or join a group.
 *
 * @author Brian Becker
 */
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/html" })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class GroupRestResource implements Serializable {
        
    private final MicroBlogService blogService;
    
    public GroupRestResource(MicroBlogService blogService) {
        this.blogService = blogService;
    }    
    
    /**
     * For a given group, this method gets the information and returns it back
     * to the user. Any group can be viewed by any user.
     * 
     * @param name
     * @return 
     */
    @GET
    public Hyper<Group> getGroupInfo(@PathParam("group") String name) {
        return new Hyper.Builder(blogService.getGroup(name))
                .link(Link.fromPath("/example/v1/group/{group}").rel("self").build(name))
                .link(Link.fromPath("/example/v1/group/{group}/members").rel("list").build(name))
                .build();
    }
    
    /**
     * For the given group name, get the list of members. This can be called
     * by any user regardless of group membership.
     * 
     * @param name
     * @param offset
     * @return 
     */
    @Path("/members") @GET
    public Hyper<Person> getGroupMembers(@PathParam("group") String name, @MatrixParam("offset") Integer offset) {
        return new Hyper.Builder(blogService.getGroupMembers(name))
                .offset(offset).limit(MAX_LIST_SPAN)
                .link(Link.fromPath("/example/v1/group/{group}/members").rel("self list").build(name)).each("delete")
                .build();
    }
    
    /**
     * For the given group name, join the group. This can be called by any user
     * which is not currently a member of the group, and they will then be
     * shown in the members list.
     * 
     * @param name
     * @param user
     * @param ctx 
     * @return  
     */
    @Path("/members/{user}") @PUT @RolesAllowed({"User"})
    public Hyper<StatusMessage> joinGroup(@PathParam("group") String name, @PathParam("user") String user, final @Context SecurityContext ctx) {
        if(user.equals(ctx.getUserPrincipal().getName())) {
            blogService.addGroupMember(name, ctx.getUserPrincipal().getName());
            return new Hyper.Builder(new StatusMessage(Status.CREATED)).build();
        } else {
            return new Hyper.Builder(new StatusMessage(Status.FORBIDDEN)).build();
        }
    }
    
    /**
     * For the authenticated user, this method leaves the chosen group. If the
     * group is empty upon leaving, the group will be disbanded as there are
     * no members, and it will be available for any other user to create.
     * 
     * @param name
     * @param user
     * @param ctx 
     * @return  
     */
    @Path("/members/{user}") @DELETE @RolesAllowed({"User"})
    public StatusMessage leaveGroup(@PathParam("group") String name, @PathParam("user") String user, final @Context SecurityContext ctx) {
        if(user.equals(ctx.getUserPrincipal().getName())) {
            blogService.leaveGroup(name, ctx.getUserPrincipal().getName());
            return new StatusMessage(Status.OK);
        } else {
            return new StatusMessage(Status.FORBIDDEN);
        }
    }
    
}
