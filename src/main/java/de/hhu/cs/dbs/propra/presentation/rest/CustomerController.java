package de.hhu.cs.dbs.propra.presentation.rest;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.internal.inject.Custom;
import org.glassfish.jersey.media.multipart.FormDataParam;
import javax.annotation.security.RolesAllowed;
import javax.crypto.ExemptionMechanism;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.validation.constraints.Email;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Date;
import java.time.*;


@Path("/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;



    @Path("/ausleihen")
    @RolesAllowed("USER")
    @POST
    public Response addOwnAusleihe(@FormDataParam("exemplarid") Integer ExemplarID,
                                   @FormDataParam("zurueckgegeben") Boolean Returned,
                                   @FormDataParam("beginn") String Start,
                                   @FormDataParam("ende") String Deadline) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        Connection connection = dataSource.getConnection();
        String returnID;
        String CollectIDs = "SELECT max(BorrowID) FROM BORROW";
        PreparedStatement preparedStatement = connection.prepareStatement(CollectIDs);
        ResultSet resultSet = preparedStatement.executeQuery();
        Integer MaxID = resultSet.getInt(1) + 1;
        String TimeStamp = DateTimeFormatter.ofPattern(" HH:mm:ss").format(LocalDateTime.now());
        String FetchedEmail = securityContext.getUserPrincipal().getName();
        Start += TimeStamp;
        Deadline += TimeStamp;
        String StringVersionOfReturned = Boolean.toString(Returned);
        try {
            connection.setAutoCommit(false);
            String userSql ="INSERT INTO BORROW VALUES (?,?,?,?,?,?)";
            PreparedStatement preparedStatementUser = connection.prepareStatement(userSql);
            preparedStatementUser.setInt(1,MaxID);
            preparedStatementUser.setString(2,Start);
            preparedStatementUser.setString(3,Deadline);
            preparedStatementUser.setString(4,StringVersionOfReturned);
            preparedStatementUser.setString(5,FetchedEmail);
            preparedStatementUser.setInt(6,ExemplarID);
            preparedStatementUser.executeUpdate();
            ResultSet generatedKeys = preparedStatementUser.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementUser.close();
            connection.commit();

        }catch (SQLException e){
            try {
                connection.rollback();
            }catch (SQLException e1){
                e1.printStackTrace();
            }
            Map<String,Object> entity = new HashMap<>();
            return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
        }finally {
            try {
                connection.close();
            }catch (SQLException e2){
                Map<String,Object> entity = new HashMap<>();
                entity.put("message", e2.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
            }
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(returnID).build()).build();
    }

    @Path("/ausleihen")
    @RolesAllowed("USER")
    @GET
    public List<Map<String, Object>> GetBorrow(@QueryParam("zurueckgegeben") Boolean Returned,
                                               @QueryParam("beginn") String StartDate)throws SQLException {

        Connection connection = null;
        List<Map<String,Object> > entities = new ArrayList<>();
        String FetchedEmail = securityContext.getUserPrincipal().getName();
        String StringVersionOfReturned = Boolean.toString(Returned);



        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String query = "SELECT BorrowID, Start, Deadline, Returned FROM BORROW WHERE Email = ";
            query+= "'"+ FetchedEmail +"'";
            if(Returned != null)
                query += " AND Returned = '"+StringVersionOfReturned+"'";
            if(StartDate != null){
                StartDate += " 00:00:00";
                query += " AND Start >= '"+StartDate+"'";
            }
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new HashMap<>();
                entity.put("BorrowID", resultSet.getObject(1));
                entity.put("Returned", resultSet.getObject(2));
                entity.put("StartDate", resultSet.getObject(3));
                entities.add(entity);
            }
            preparedStatement.close();
            connection.commit();
        }catch (SQLException e){
            try {
                connection.rollback();
            }catch (SQLException e1){
                e1.printStackTrace();
            }
            Map<String,Object> entity = new HashMap<>();
        }finally {
            try {
                connection.close();
            }catch (SQLException e2){
                Map<String,Object> entity = new HashMap<>();
                entity.put("message", e2.getMessage());
            }
        }
        return entities;
    }

    @Path("/adressen")
    @RolesAllowed("USER")
    @PATCH
    public Response UpdateAddress(@QueryParam("hausnummer") String HouseNr,
                                  @QueryParam("strasse") String Street,
                                  @QueryParam("stadt")String City,
                                  @QueryParam("plz")String PostalCode) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        Connection connection = dataSource.getConnection();
        String returnID;
        String FetchedEmail = securityContext.getUserPrincipal().getName();
        String CustomerAddressID = "SELECT ID FROM CUSTOMER WHERE Email = '"+FetchedEmail+"'";
        PreparedStatement preparedStatement = connection.prepareStatement(CustomerAddressID);
        ResultSet resultSet = preparedStatement.executeQuery();
        String FetchedAddressId = resultSet.getString(1);

        String FetchAddressValues = "SELECT HouseNr, Street, PostCode, City FROM ADDRESS WHERE RowID = '"+FetchedAddressId+"'";
        PreparedStatement preparedStatement2 = connection.prepareStatement(FetchAddressValues);
        ResultSet resultSet2 = preparedStatement2.executeQuery();
        String FetchedHouseNr = resultSet2.getString(1);
        String FetchedStreetNr = resultSet2.getString(2);
        String FetchedPostCode = resultSet2.getString(3);
        String FetchedCity = resultSet2.getString(4);
        String query ="UPDATE ADDRESS SET ";
        try {
            connection.setAutoCommit(false);
            if(HouseNr != null){
                query += " HouseNr = '" + HouseNr + "'";
            }
            else{
                query += " HouseNr = '" + FetchedHouseNr + "'";
            }
            if(Street != null){
                query += ", Street = '" +  Street + "'";
            }
            else{
                query += ", Street = '" + FetchedStreetNr + "'";
            }
            if(PostalCode != null){
                query += ", PostCode = '" + PostalCode + "'";
            }
            else{
                query += ", PostCode = '" + FetchedPostCode + "'";
            }
            if(City != null){
                query += ", City = '" + City + "'";
            }
            else{
                query += ", City = '" + FetchedCity + "'";
            }
            query += " WHERE ID = " + FetchedAddressId ;
            PreparedStatement preparedStatementAddressUpdate = connection.prepareStatement(query);
            preparedStatementAddressUpdate.executeUpdate();
            ResultSet generatedKeys = preparedStatementAddressUpdate.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementAddressUpdate.close();
            connection.commit();
        }
        catch(SQLException e){
            try {
                connection.rollback();
            }catch (SQLException e1){
                e1.printStackTrace();
            }
            Map<String,Object> entity = new HashMap<>();
            return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
        }finally {
            try {
                connection.close();
            }catch (SQLException e2){
                Map<String,Object> entity = new HashMap<>();
                entity.put("message", e2.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
            }
        }
        return Response.created(uriInfo.getAbsolutePathBuilder().path(returnID).build()).build();
    }

}