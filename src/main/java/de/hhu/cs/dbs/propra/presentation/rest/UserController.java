package de.hhu.cs.dbs.propra.presentation.rest;

import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Path("/")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class UserController {
    @Inject
    private DataSource dataSource;

    @Context
    private SecurityContext securityContext;

    @Context
    private UriInfo uriInfo;


    @Path("kunden")
    @GET
    public List<Map<String, Object>> getAllUsers(@QueryParam("beitragsbefreit") Boolean Exemption,
                                                 @QueryParam("guthaben") Double Credit) throws SQLException {

        Connection connection = null;
        List<Map<String, Object>> entities = new ArrayList<>();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String query = "SELECT ID,Credit,Exemption,CUSTOMER.Email,Password,FirstName,LastName,DateOfBirth FROM CUSTOMER JOIN USER WHERE USER.EMAIL = CUSTOMER.EMAIL ";
            if (Exemption != null)
                query += "AND Exemption = '" + Exemption + "'";
            if (Credit != null)
                query += "AND Credit >= '" + Credit + "'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new HashMap<>();
                entity.put("ROWID", resultSet.getObject(1));
                entity.put("Credit", resultSet.getObject(2));
                entity.put("Exemption", resultSet.getObject(3));
                entity.put("Email", resultSet.getObject(4));
                entity.put("Password", resultSet.getObject(5));
                entity.put("FirstName", resultSet.getObject(6));
                entity.put("LastName", resultSet.getObject(7));
                entity.put("DateOfBirth", resultSet.getObject(8));
                entities.add(entity);
            }
            preparedStatement.close();
            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            Map<String, Object> entity = new HashMap<>();
        } finally {
            try {
                connection.close();
            } catch (SQLException e2) {
                Map<String, Object> entity = new HashMap<>();
                entity.put("message", e2.getMessage());
            }
        }
        return entities;
    }

    @Path("kunden")
    @POST
    public Response addCostumer(
            @FormDataParam("passwort") String Password,
            @FormDataParam("vorname") String FirstName,
            @FormDataParam("nachname") String LastName,
            @FormDataParam("email") String Email,
            @FormDataParam("geburtsdatum") String DateOfBirth,
            @FormDataParam("guthaben") Double Credit,
            @FormDataParam("beitragsbefreit") Boolean Exemption,
            @FormDataParam("adresseid") Integer AddressID) {

        //if (Email.equals("") || FirstName.equals("") || LastName.equals("") || Password.equals("") || DateOfBirth.equals("") || (Credit == null) ||  (Exemption == null) || (AddressID == null) ) return Response.status(Response.Status.BAD_REQUEST).build();
        System.out.println("ERROR CATCHING!");

        Connection connection = null;
        System.out.println("ERROR CATCHING!");
        String returnID;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String StringVersionOfExemption = Boolean.toString(Exemption);
            // add user first
            String userSql = "INSERT INTO USER VALUES (?,?,?,?,?)";

            PreparedStatement preparedStatementUser = connection.prepareStatement(userSql);
            preparedStatementUser.setObject(1, FirstName);
            preparedStatementUser.setObject(2, LastName);
            preparedStatementUser.setObject(3, Password);
            preparedStatementUser.setObject(4, Email);
            preparedStatementUser.setObject(5, DateOfBirth);
            preparedStatementUser.executeUpdate();
            preparedStatementUser.close();

            //add customer
            String CostumerSql = "INSERT INTO CUSTOMER VALUES (?,?,?,?)";

            PreparedStatement preparedStatementCostumer = connection.prepareStatement(CostumerSql);
            preparedStatementCostumer.setObject(1, Email);
            preparedStatementCostumer.setObject(2, Credit);
            preparedStatementCostumer.setObject(3, StringVersionOfExemption);
            preparedStatementCostumer.setObject(4, AddressID);
            preparedStatementCostumer.executeUpdate();
            ResultSet generatedKeys = preparedStatementCostumer.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementCostumer.close();


            connection.commit();

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            Map<String, Object> entity = new HashMap<>();
            System.out.println("HASMAP RETURNING");

            return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
        } finally {
            try {
                connection.close();
            } catch (SQLException e2) {
                Map<String, Object> entity = new HashMap<>();
                entity.put("message", e2.getMessage());
                System.out.println("REACHED CATCH e2");

                return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
            }
        }


        return Response.created(uriInfo.getAbsolutePathBuilder().path(returnID).build()).build();
    }




    @Path("bibliothekare")
    @GET
    public List<Map<String, Object>> getAllBiblothekkar(@QueryParam("telefonnummer") String PhoneNr) throws SQLException{
        Connection connection = null;
        List<Map<String,Object> > entities = new ArrayList<>();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String query = "SELECT LIBRARIAN.RowID,PhoneNr,LIBRARIAN.Email,Password,FirstName,LastName,DateOfBirth FROM LIBRARIAN JOIN USER WHERE LIBRARIAN.EMAIL = USER.EMAIL ";
            if(PhoneNr != null)
                query += "AND PhoneNr = '"+PhoneNr+"'"+";";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new HashMap<>();
                entity.put("ROWID", resultSet.getObject(1));
                entity.put("PhoneNr", resultSet.getObject(2));
                entity.put("Email", resultSet.getObject(3));
                entity.put("Password", resultSet.getObject(4));
                entity.put("FirstName", resultSet.getObject(5));
                entity.put("LastName", resultSet.getObject(6));
                entity.put("DateOfBirth", resultSet.getObject(7));
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


    @Path("/bibliothekare")
    @POST
    public Response addBiblothekar( @FormDataParam("email") String Email,
                                    @FormDataParam("passwort") String Password,
                                    @FormDataParam("vorname") String FirstName,
                                    @FormDataParam("nachname") String LastName,
                                    @FormDataParam("geburtsdatum") String DateOfBirth,
                                    @FormDataParam("telefonnummer") String PhoneNr){

        if (Email.equals("") || FirstName.equals("") || LastName.equals("") || Password.equals("") || DateOfBirth.equals("") || PhoneNr.equals("")) return Response.status(Response.Status.fromStatusCode(400)).build();
        Connection connection = null;
        String returnID;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);

            // add user
            String userSql ="INSERT INTO USER VALUES (?,?,?,?,?)";
            PreparedStatement preparedStatementUser = connection.prepareStatement(userSql);
            preparedStatementUser.setObject(1,FirstName);
            preparedStatementUser.setObject(2,LastName);
            preparedStatementUser.setObject(3,Password);
            preparedStatementUser.setObject(4,Email);
            preparedStatementUser.setObject(5,DateOfBirth);
            preparedStatementUser.executeUpdate();
            preparedStatementUser.close();

            //add customer
            String CostumerSql ="INSERT INTO LIBRARIAN VALUES (?,?)";
            PreparedStatement preparedStatementCostumer= connection.prepareStatement(CostumerSql);
            preparedStatementCostumer.setObject(1,Email);
            preparedStatementCostumer.setObject(2,PhoneNr);
            preparedStatementCostumer.executeUpdate();
            ResultSet generatedKeys = preparedStatementCostumer.getGeneratedKeys();
            returnID = Integer.toString(generatedKeys.getInt(1));
            preparedStatementCostumer.close();
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

    @Path("genres")
    @GET
    public  List<Map<String, Object>> getAllGenres(@QueryParam("bezeichnung") String Genre) throws SQLException{
        Connection connection = null;
        List<Map<String,Object> > entities = new ArrayList<>();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String query = "SELECT ROWID,Name FROM GENRE WHERE 1=1";
            if(Genre != null)
                query += "AND Name = '"+Genre+"'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new HashMap<>();
                entity.put("ROWID", resultSet.getObject(1));
                entity.put("Name", resultSet.getObject(2));
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

    @Path("artikel")
    @GET
    public List<Map<String, Object>> getAllArtikel(@QueryParam("isbn")              String isbn,
                                  @QueryParam("bezeichnung")       String Title,
                                  @QueryParam("beschreibung")      String Description,
                                  @QueryParam("coverbild")         String CoverPhoto,
                                  @QueryParam("erscheinungsdatum") String PublicationDate) throws SQLException{
        Connection connection = null;
        List<Map<String,Object> > entities = new ArrayList<>();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String query = "SELECT ROWID,ISBN,PublicationDate,Description,Title,CoverPhoto FROM ITEM WHERE 1=1 ";
            if(isbn != null)
                query += "AND ISBN = '"+isbn+"'";
            if(PublicationDate != null)
                query += "AND PublicationDate >= '"+PublicationDate+"'";
            if(Title != null)
                query += "AND Title = '"+Title+"'";
            if(CoverPhoto != null)
                query += "AND CoverPhoto = '"+CoverPhoto+"'";
            if(Description != null)
                query += "AND Description = '"+Description+"'";
            query += ";";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> entity;

            while (resultSet.next()) {
                entity = new HashMap<>();
                entity.put("ROWID", resultSet.getObject(1));
                entity.put("isbn", resultSet.getObject(2));
                entity.put("PublicationDate", resultSet.getObject(3));
                entity.put("Description", resultSet.getObject(4));
                entity.put("Title", resultSet.getObject(5));
                entity.put("CoverPhoto", resultSet.getObject(6));
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


    @Path("exemplare")
    @GET
    public List<Map<String, Object>> getAllExemplare(@QueryParam("preis") Double Price,
                                    @QueryParam("ausgeliehen") String Borrowed) throws SQLException{
        Connection connection = null;
        List<Map<String,Object> > entities = new ArrayList<>();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String query = "SELECT BOOK_COPY.ID,BOOK_COPY.ISBN,BOOK_COPY.PRICE FROM BOOK_COPY JOIN BORROW WHERE BOOK_COPY.ID = BORROW.ID ";
            if(Price != null)
                query += "AND Price >= '"+Price+"'";
            if(Borrowed != null) {
                //Since we want the ones that are borrowed currently aka haven't been returned yet
                if(Borrowed.equals("true")){
                    Borrowed = "false";
                }
                else{
                    Borrowed = "true";
                }
                query += "AND Returned = '" + Borrowed + "'";
            }
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new HashMap<>();
                entity.put("BOOK_COPY.ID", resultSet.getObject(1));
                entity.put("BOOK_COPY.ISBN", resultSet.getObject(2));
                entity.put("BOOK_COPY.PRICE", resultSet.getObject(3));
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


    @Path("mitarbeiter")
    @GET
    public Response RedirectToBibliothekare(){
       return Response.status(Response.Status.MOVED_PERMANENTLY)
                .location(uriInfo.getBaseUriBuilder().path("/bibliothekare").build())
                .build();
    }


    @Path("autoren")
    @GET
    public List<Map<String, Object>>  getAllAuthors(  @QueryParam("vorname") String FirstName,
                                    @QueryParam("nachname") String LastName) throws SQLException{
        Connection connection = null;
        List<Map<String,Object> > entities = new ArrayList<>();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String query = "SELECT ID,FirstName,LastName FROM AUTHOR WHERE 1=1 ";
            if(FirstName != null)
                query += "AND FirstName = '"+FirstName+"'";
            if(LastName != null)
                query += "AND LastName = '"+LastName+"'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new HashMap<>();
                entity.put("ID", resultSet.getObject(1));
                entity.put("FirstName", resultSet.getObject(2));
                entity.put("LastName", resultSet.getObject(3));
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

    @Path("adressen")
    @GET
    public List<Map<String, Object>> getAllAddresses(  @QueryParam("hausnummer") String HouseNr,
                                      @QueryParam("strasse") String Street,
                                      @QueryParam("plz") String PostCode,
                                      @QueryParam("stadt") String City) throws SQLException{
        Connection connection = null;
        List<Map<String,Object> > entities = new ArrayList<>();
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            String query = "SELECT ID,PostCode,City,Street,HouseNr FROM ADDRESS WHERE 1=1 ";
            if(PostCode != null)
                query += "AND PostCode = '"+PostCode+"'";
            if(Street != null)
                query += "AND Street = '"+Street+"'";
            if(City != null)
                query += "AND City = '"+City+"'";
            if(HouseNr != null)
                query += "AND HouseNr = '"+HouseNr+"'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            Map<String, Object> entity;
            while (resultSet.next()) {
                entity = new HashMap<>();
                entity.put("ID", resultSet.getObject(1));
                entity.put("PostCode", resultSet.getObject(2));
                entity.put("City", resultSet.getObject(3));
                entity.put("Street", resultSet.getObject(4));
                entity.put("HouseNr", resultSet.getObject(5));

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
    public String toHex(String arg) {
        return String.format("%x", new BigInteger(1, arg.getBytes()));
    }

}
//curl -X GET "http://localhost:8080/kunden" -H "accept: application/json;charset=UTF-8"
//curl -X POST "http://localhost:8080/kunden" -H "accept: */*" -H "Content-Type: multipart/form-data" -F "email=nautas@uni-du.de" -F "passwort=Test1" -F "vorname=Nasda" -F "nachname=Asda" -F "geburtsdatum="1995-10-10"" -F "guthaben=2.0" -F "beitragsbefreit=true" -F "adresseid=2"