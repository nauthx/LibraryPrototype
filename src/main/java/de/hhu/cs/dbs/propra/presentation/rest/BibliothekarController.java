package de.hhu.cs.dbs.propra.presentation.rest;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataParam;
import javax.annotation.security.RolesAllowed;
import javax.crypto.ExemptionMechanism;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
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
public class BibliothekarController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;



    @Path("genres")
    @RolesAllowed("EMPLOYEE")
    @POST
    public Response addGenre(@FormDataParam("bezeichnung") String Genre ) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        Connection connection = null;
        String returnID;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String userSql ="INSERT INTO GENRE VALUES (?)";
            PreparedStatement preparedStatementUser = connection.prepareStatement(userSql);
            preparedStatementUser.setObject(1,Genre);
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
    @Path("artikel")
    @RolesAllowed("EMPLOYEE")
    @POST
    public Response addArtikel(@FormDataParam("autorid") Integer AuthorID,
                               @FormDataParam("genreid") Integer GenreID,
                               @FormDataParam("mediumid")Integer MediumID,
                               @FormDataParam("isbn")String ISBN,
                               @FormDataParam("erscheinungsdatum") String PublicationDate,
                               @FormDataParam("beschreibung") String Description,
                               @FormDataParam("bezeichnung") String Title,
                               @FormDataParam("coverbild") InputStream file) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        Connection connection = dataSource.getConnection();
        String returnID;
        String FindOut = "SELECT NAME FROM GENRE WHERE ROWID = '"+GenreID+"'";
        PreparedStatement preparedStatement = connection.prepareStatement(FindOut);
        ResultSet resultSet = preparedStatement.executeQuery();
        String GenreName = resultSet.getString(1);
        try {
            String MediumType;
            switch(MediumID){
                case 1:
                    MediumType = "CD";
                    break;
                case 2:
                    MediumType = "Hardcover";
                    break;
                case 3:
                    MediumType = "Softcover";
                    break;
                case 4: MediumType = "DVD";
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + MediumID);
            }
            String ItemQuery ="INSERT INTO ITEM VALUES (?,?,?,?,?,?)";

            PreparedStatement preparedStatementUser = connection.prepareStatement(ItemQuery);
           // connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            if(file != null) {
                byte[] bytes;
                bytes = IOUtils.toByteArray(file);
                preparedStatementUser.setObject(6,bytes);

            }
            preparedStatementUser.setObject(1,ISBN);
            preparedStatementUser.setObject(2,Description);
            preparedStatementUser.setObject(3,PublicationDate);
            preparedStatementUser.setObject(4,Title);
            preparedStatementUser.setObject(5,MediumType);
            preparedStatementUser.executeUpdate();
            ResultSet generatedKeys = preparedStatementUser.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementUser.close();

            String AuthorQuery = "INSERT INTO AUTHOR_WRITES_BOOK VALUES(?,?)";
            PreparedStatement AuthorStatementUser = connection.prepareStatement(AuthorQuery);
            AuthorStatementUser.setObject(1,AuthorID);
            AuthorStatementUser.setObject(2,ISBN);
            AuthorStatementUser.executeUpdate();
            AuthorStatementUser.close();
            connection.commit();

            String GenreQuery = "INSERT INTO ITEM_HAS_GENRE VALUES(?,?)";
            PreparedStatement GenreStatementUser = connection.prepareStatement(GenreQuery);
            GenreStatementUser.setObject(1,ISBN);
            GenreStatementUser.setObject(2,GenreName);
            GenreStatementUser.executeUpdate();
            GenreStatementUser.close();
            connection.commit();

        }catch (SQLException | IOException e){
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

    /*@Path("mitarbeiter")
    @RolesAllowed({"EMPLOYEE"})
    @GET
    public List<Map<String, Object>> GetMitarbeiter( ) throws SQLException{
        Connection connection = null;
        List<Map<String,Object> > entities = new ArrayList<>();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String query = "SELECT FirstName,LastName FROM USER JOIN LIBRARIAN WHERE USER.Email = LIBRARIAN.Email ";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new HashMap<>();
                entity.put("FirstName", resultSet.getObject(1));
                entity.put("LastName", resultSet.getObject(2));
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
    }*/


    @Path("exemplare")
    @RolesAllowed("EMPLOYEE")
    @POST // curl -X POST "http://localhost:8080/exemplare" -H  "accept: */*" -H  "Content-Type: multipart/form-data" -F "artikelid=3" -F "preis=500" -F "regal=9" -F "etage=5" -u nauth100@hhu.de:Test1
    public Response addExemplare(@FormDataParam("artikelid") String ISBN,
                                 @FormDataParam("preis") Double Price,
                                 @FormDataParam("regal") Integer Shelf,
                                 @FormDataParam("etage") Integer Floor) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        Connection connection = dataSource.getConnection();
        String returnID;
        String CollectIDs = "SELECT max(ROWID) FROM BOOK_COPY";
        PreparedStatement preparedStatement = connection.prepareStatement(CollectIDs);
        ResultSet resultSet = preparedStatement.executeQuery();

        Integer MaxID = resultSet.getInt(1) + 1;
        try {
            connection.setAutoCommit(false);
            String userSql ="INSERT INTO BOOK_COPY VALUES (?,?,?,?,?)";
            PreparedStatement preparedStatementBookCopy = connection.prepareStatement(userSql);
            preparedStatementBookCopy.setObject(1,MaxID);
            preparedStatementBookCopy.setObject(2,Price);
            preparedStatementBookCopy.setObject(3,ISBN);
            preparedStatementBookCopy.setObject(4,Floor);
            preparedStatementBookCopy.setObject(5,Shelf);
            preparedStatementBookCopy.executeUpdate();
            ResultSet generatedKeys = preparedStatementBookCopy.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementBookCopy.close();
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

    @Path("autoren")
    @RolesAllowed("EMPLOYEE")
    @POST // curl -X POST "http://localhost:8080/autoren" -H  "accept: */*" -H  "Content-Type: multipart/form-data" -F "vorname=Stanley" -F "nachname=Hudson" -u nauth100@hhu.de:Test1
    public Response addAuthor(@FormDataParam("vorname") String FirstName,
                                 @FormDataParam("nachname") String LastName ) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        Connection connection = dataSource.getConnection();
        String returnID;
        String CollectIDs = "SELECT max(ROWID) FROM AUTHOR";
        PreparedStatement preparedStatement = connection.prepareStatement(CollectIDs);
        ResultSet resultSet = preparedStatement.executeQuery();

        Integer MaxID = resultSet.getInt(1) + 1;
        try {
            connection.setAutoCommit(false);
            String userSql ="INSERT INTO AUTHOR VALUES (?,?,?)";
            PreparedStatement preparedStatementBookCopy = connection.prepareStatement(userSql);
            preparedStatementBookCopy.setObject(1,MaxID);
            preparedStatementBookCopy.setObject(2,FirstName);
            preparedStatementBookCopy.setObject(3,LastName);
            preparedStatementBookCopy.executeUpdate();
            ResultSet generatedKeys = preparedStatementBookCopy.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementBookCopy.close();
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

    @Path("exemplare/{exemplarid}")
    @RolesAllowed("EMPLOYEE")
    @DELETE // curl -X DELETE "http://localhost:8080/exemplare/2" -H  "accept: */*" -u nauth100@hhu.de:Test1
    public Response RemoveExemplar(@PathParam("exemplarid") String ExemplarToBeRemoved) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        Connection connection = dataSource.getConnection();
        String returnID;
        List<Map<String,Object> > entities = new ArrayList<>();
        String TestSQL ="SELECT Count(RowID) FROM BOOK_COPY WHERE ROWID = ?";
        PreparedStatement preparedStatementBookDeleteFetch = connection.prepareStatement(TestSQL);
        preparedStatementBookDeleteFetch.setString(1,ExemplarToBeRemoved);
        ResultSet resultSetCount = preparedStatementBookDeleteFetch.executeQuery();
        int Count =  resultSetCount.getInt(1);
        preparedStatementBookDeleteFetch.close();
        if (Count == 0) {
            return Response.status(404).build();
        }
        try {
            connection.setAutoCommit(false);
            String userSql ="DELETE FROM BOOK_COPY WHERE ROWID = ?";
            PreparedStatement preparedStatementBookDelete = connection.prepareStatement(userSql);
            preparedStatementBookDelete.setString(1,ExemplarToBeRemoved);
            preparedStatementBookDelete.executeUpdate();
            ResultSet generatedKeys = preparedStatementBookDelete.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementBookDelete.close();
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
        return Response.status(204).build();
    }

    @Path("artikel/{artikelid}")
    @RolesAllowed("EMPLOYEE")
    @DELETE // curl -X DELETE "http://localhost:8080/artikel/2" -H  "accept: */*" -u nauth100@hhu.de:Test1
    public Response RemoveArtikel(@PathParam("artikelid") String ArtikelToBeRemoved) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        Connection connection = dataSource.getConnection();
        String returnID;
        List<Map<String,Object> > entities = new ArrayList<>();
        String TestSQL ="SELECT Count(RowID) FROM ITEM WHERE ROWID = ?";
        PreparedStatement preparedStatementBookDeleteFetch = connection.prepareStatement(TestSQL);
        preparedStatementBookDeleteFetch.setString(1,ArtikelToBeRemoved);
        ResultSet resultSetCount = preparedStatementBookDeleteFetch.executeQuery();
        int Count =  resultSetCount.getInt(1);
        preparedStatementBookDeleteFetch.close();
        if (Count == 0) {
            return Response.status(404).build();
        }
        try {
            connection.setAutoCommit(false);
            String userSql ="DELETE FROM ITEM WHERE RowID = ?";
            PreparedStatement preparedStatementBookDelete = connection.prepareStatement(userSql);
            preparedStatementBookDelete.setString(1,ArtikelToBeRemoved);
            preparedStatementBookDelete.executeUpdate();
            ResultSet generatedKeys = preparedStatementBookDelete.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementBookDelete.close();
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
       return Response.status(204).build();
    }



    @Path("autoren/{autorid}")
    @RolesAllowed("EMPLOYEE")
    @DELETE // curl -X DELETE "http://localhost:8080/autoren/7" -H  "accept: */*" -u nauth100@hhu.de:Test1
    public Response RemoveAuthor(@PathParam("autorid") String AuthorToBeRemoved) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        Connection connection = dataSource.getConnection();
        String returnID;
        boolean Flag = false;
        List<Map<String,Object> > entities = new ArrayList<>();
        String TestSQL ="SELECT Count(RowID) FROM AUTHOR WHERE ROWID = ?";
        PreparedStatement preparedStatementBookDeleteFetch = connection.prepareStatement(TestSQL);
        preparedStatementBookDeleteFetch.setString(1,AuthorToBeRemoved);
        ResultSet resultSetCount = preparedStatementBookDeleteFetch.executeQuery();
        int Count =  resultSetCount.getInt(1);
        preparedStatementBookDeleteFetch.close();

       if (Count == 0) {
           return Response.status(404).build();
       }
        try {
            connection.setAutoCommit(false);
            String userSql ="DELETE FROM AUTHOR WHERE ROWID = ?";
            PreparedStatement preparedStatementBookDelete = connection.prepareStatement(userSql);
            preparedStatementBookDelete.setString(1,AuthorToBeRemoved);
            preparedStatementBookDelete.executeUpdate();
            ResultSet generatedKeys = preparedStatementBookDelete.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementBookDelete.close();
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
        return Response.status(204).build();
    }

    @Path("ausleihen/{ausleiheid}")
    @RolesAllowed("EMPLOYEE")
    @PATCH // curl -X PATCH "http://localhost:8080/ausleihen/1?zurueckgegeben=true&beginn=2022-05-05&ende=2023-05-06" -H "accept: */*" -u nauth100@hhu.de:Test1

    public Response PatchBorrow(@PathParam("ausleiheid") Integer BorrowID,
                                @QueryParam("zurueckgegeben") Boolean Returned,
                                @QueryParam("beginn") String Start,
                                @QueryParam("ende")String Deadline) throws SQLException{
        //if (Genre.equals("")) return Response.status(Response.Status.BAD_REQUEST).build();
        if(Start.compareTo(Deadline) > 0){
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        Connection connection = dataSource.getConnection();
        String returnID;
        String TimeStamp = DateTimeFormatter.ofPattern(" HH:mm:ss").format(LocalDateTime.now());

        String Fetch1 = "SELECT Start, Deadline, Returned FROM BORROW WHERE BorrowID = '"+BorrowID+"'";
        PreparedStatement preparedStatement = connection.prepareStatement(Fetch1);
        ResultSet resultSet = preparedStatement.executeQuery();
        String FetchedStart = resultSet.getString(1);
        String FetchedDeadline = resultSet.getString(2);
        String FetchedReturned = resultSet.getString(3);
        String query ="UPDATE BORROW SET ";
        try {
            connection.setAutoCommit(false);
            if(Returned != null){
                query += " Returned = '" + Returned + "'";
            }
            else{
                query += " Returned = '" + FetchedReturned + "'";
            }
            if(Start != null){
                Start += TimeStamp;
                query += ", Start = '" + Start + "'";
            }
            else{
                query += ", Start = '" + FetchedStart + "'";
            }
            if(Deadline != null){
                Deadline += TimeStamp;
                query += ", Deadline = '" + Deadline + "'";
            }
            else{
                query += ", Deadline = '" + FetchedDeadline + "'";
            }
            query += " WHERE BorrowID = '" + BorrowID + "'";
            PreparedStatement preparedStatementBorrowUpdate = connection.prepareStatement(query);
            preparedStatementBorrowUpdate.executeUpdate();
            ResultSet generatedKeys = preparedStatementBorrowUpdate.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementBorrowUpdate.close();
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
//curl -X GET "http://localhost:8080/kunden" -H "accept: application/json;charset=UTF-8"
//curl -X POST "http://localhost:8080/kunden" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "email=nautas@uni-du.de" -F "passwort=Test1" -F "vorname=Nasda" -F "nachname=Asda" -F "geburtsdatum="1995-10-10"" -F "guthaben=2.0" -F "beitragsbefreit=true" -F "adresseid=2"