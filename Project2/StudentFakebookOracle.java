package project2;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

/*
    The StudentFakebookOracle class is derived from the FakebookOracle class and implements
    the abstract query functions that investigate the database provided via the <connection>
    parameter of the constructor to discover specific information.
*/
public final class StudentFakebookOracle extends FakebookOracle {
    // [Constructor]
    // REQUIRES: <connection> is a valid JDBC connection
    public StudentFakebookOracle(Connection connection) {
        oracle = connection;
    }
    
    @Override
    // Query 0
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the total number of users for which a birth month is listed
    //        (B) Find the birth month in which the most users were born
    //        (C) Find the birth month in which the fewest users (at least one) were born
    //        (D) Find the IDs, first names, and last names of users born in the month
    //            identified in (B)
    //        (E) Find the IDs, first names, and last name of users born in the month
    //            identified in (C)
    public BirthMonthInfo findMonthOfBirthInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT COUNT(*) AS Birthed, Month_of_Birth " + 
                "FROM " + UsersTable + " " + 
                "WHERE Month_of_Birth IS NOT NULL " + 
                "GROUP BY Month_of_Birth " + 
                "ORDER BY Birthed DESC, Month_of_Birth ASC");
            int mostMonth = 0;
            int leastMonth = 0;
            int total = 0;
            while (rst.next()) { 
                if (rst.isFirst()) { 
                    mostMonth = rst.getInt(2); 
                }
                if (rst.isLast()) { 
                    leastMonth = rst.getInt(2); 
                }
                total += rst.getInt(1); 
            }
            BirthMonthInfo info = new BirthMonthInfo(total, mostMonth, leastMonth);
            rst = stmt.executeQuery(
                "SELECT User_ID, First_Name, Last_Name " + 
                "FROM " + UsersTable + " " + 
                "WHERE Month_of_Birth = " + mostMonth + " " + 
                "ORDER BY User_ID"); 
            while (rst.next()) {
                info.addMostPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }
            rst = stmt.executeQuery(
                "SELECT User_ID, First_Name, Last_Name " + 
                "FROM " + UsersTable + " " + 
                "WHERE Month_of_Birth = " + leastMonth + " " + 
                "ORDER BY User_ID"); 
                
            while (rst.next()) {
                info.addLeastPopularBirthMonthUser(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }
            rst.close();
            stmt.close(); 
            return info;
        }
        
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return new BirthMonthInfo(-1, -1, -1);
        }
    }
    
    @Override
    // Query 1
    // -----------------------------------------------------------------------------------
    // GOALS: (A) The first name(s) with the most letters
    //        (B) The first name(s) with the fewest letters
    //        (C) The first name held by the most users
    //        (D) The number of users whose first name is that identified in (C)
    public FirstNameInfo findNameInfo() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            FirstNameInfo info1 = new FirstNameInfo();
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT First_Name, LENGTH(First_Name) AS Name_Length " + 
                "FROM " + UsersTable + " " + 
                "WHERE LENGTH(First_Name) IN (SELECT MAX(LENGTH(First_Name)) FROM " + UsersTable + " UNION SELECT MIN(LENGTH(First_Name)) " + 
                "FROM " +  UsersTable + ") " + 
                "ORDER BY Name_Length DESC, First_Name ASC");
            int longLength = 0;
            int shortLength = 0;
            boolean isfst = true;
            while (rst.next()) {
                if (rst.isFirst()) {
                    longLength = rst.getInt(2);
                    info1.addLongName(rst.getString(1));
                }
                else {
                    if (isfst  == true) {
                        if (rst.getInt(2) != longLength) {
                            isfst = false;
                            shortLength = rst.getInt(2);
                            info1.addShortName(rst.getString(1));
                        }
                        else {
                            info1.addLongName(rst.getString(1));
                        }
                    }
                    else {
                        info1.addShortName(rst.getString(1));
                    }
                }
            }
            rst = stmt.executeQuery(
                "SELECT DISTINCT First_Name, COUNT(*) AS Count_Name " + 
                "FROM " + UsersTable + " " + 
                "GROUP BY First_Name HAVING COUNT(*) = (SELECT MAX(COUNT(*)) " + 
                "FROM " + UsersTable + " " + 
                "GROUP BY First_Name) ORDER BY First_Name ASC");
            long numCommon = 0;
            while (rst.next()) {
                info1.addCommonName(rst.getString(1));
                if (rst.isFirst()) {
                    numCommon = rst.getInt(2);
                }
            }
            info1.setCommonNameCount(numCommon);
            rst.close();
            stmt.close();
            return info1;
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                FirstNameInfo info = new FirstNameInfo();
                info.addLongName("Aristophanes");
                info.addLongName("Michelangelo");
                info.addLongName("Peisistratos");
                info.addShortName("Bob");
                info.addShortName("Sue");
                info.addCommonName("Harold");
                info.addCommonName("Jessica");
                info.setCommonNameCount(42);
                return info;
            */
            //return new FirstNameInfo();                // placeholder for compilation
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return new FirstNameInfo();
        }
    }
    
    @Override
    // Query 2
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users without any friends
    //
    // Be careful! Remember that if two users are friends, the Friends table only contains
    // the one entry (U1, U2) where U1 < U2.
    public FakebookArrayList<UserInfo> lonelyUsers() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");       
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT U.User_ID, U.First_Name, U.Last_Name " + 
                "FROM " + UsersTable + " U " + 
                "WHERE U.User_ID NOT IN " + 
                "(SELECT F1.User1_ID FROM " + FriendsTable + " F1 UNION SELECT F2.User2_ID FROM " + FriendsTable + " F2) " + 
                "ORDER BY U.User_ID ASC");
            while (rst.next()) {
                results.add(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }
            rst.close();
            stmt.close();
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(15, "Abraham", "Lincoln");
                UserInfo u2 = new UserInfo(39, "Margaret", "Thatcher");
                results.add(u1);
                results.add(u2);
            */
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        
        return results;
    }
    
    @Override
    // Query 3
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of users who no longer live
    //            in their hometown (i.e. their current city and their hometown are different)
    public FakebookArrayList<UserInfo> liveAwayFromHome() throws SQLException {
        FakebookArrayList<UserInfo> results = new FakebookArrayList<UserInfo>(", ");
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT U.User_ID, U.First_Name, U.Last_Name " + 
                "FROM " + UsersTable + " U " + 
                "INNER JOIN " + CurrentCitiesTable + " UCI ON U.User_ID = UCI.User_ID " + 
                "INNER JOIN " + HometownCitiesTable + " UHI ON U.User_ID = UHI.User_ID " + 
                "WHERE UCI.Current_City_ID IS NOT NULL AND UHI.Hometown_City_ID IS NOT NULL " + 
                "AND UCI.Current_City_ID <> UHI.Hometown_City_ID " + 
                "ORDER BY U.User_ID ASC");
            while (rst.next()) {
                results.add(new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3)));
            }
            rst.close();
            stmt.close();
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(9, "Meryl", "Streep");
                UserInfo u2 = new UserInfo(104, "Tom", "Hanks");
                results.add(u1);
                results.add(u2);
            */
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        
        return results;
    }
    
    @Override
    // Query 4
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, links, and IDs and names of the containing album of the top
    //            <num> photos with the most tagged users
    //        (B) For each photo identified in (A), find the IDs, first names, and last names
    //            of the users therein tagged
    public FakebookArrayList<TaggedPhotoInfo> findPhotosWithMostTags(int num) throws SQLException {
        FakebookArrayList<TaggedPhotoInfo> results = new FakebookArrayList<TaggedPhotoInfo>("\n");
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT Tag_Photo_ID, Album_ID, Photo_Link, Album_Name, Count_Tag " + 
                "FROM " + "(SELECT DISTINCT T.Tag_Photo_ID, P.Album_ID, P.Photo_Link, A.Album_Name, COUNT(*) AS Count_Tag " + 
                "FROM " + TagsTable + " T, " + PhotosTable + " P, " + AlbumsTable + " A " + 
                "WHERE T.Tag_Photo_ID = P.Photo_ID AND P.Album_ID = A.Album_ID " + 
                "GROUP BY T.Tag_Photo_ID, P.Album_ID, P.Photo_Link, A.Album_Name " + 
                "ORDER BY COUNT(*) DESC, T.Tag_Photo_ID ASC) " + 
                "WHERE ROWNUM <= " + num);
            long tpid = 0; long abid = 0;
            String pt_link = ""; String ab_name = "";
            Statement stmt4 = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly);
            while (rst.next()) {
                tpid = rst.getLong(1);
                abid = rst.getLong(2);
                pt_link = rst.getString(3);
                ab_name = rst.getString(4);
                PhotoInfo p = new PhotoInfo(tpid, abid, pt_link, ab_name);
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                ResultSet rst4 = stmt4.executeQuery(
                    "SELECT U.User_ID, U.First_Name, U.Last_Name " + 
                    "FROM " + TagsTable + " T " +  
                    "JOIN " + UsersTable + " U ON T.Tag_Subject_ID = U.User_ID " + 
                    "WHERE T.Tag_Photo_ID = " + tpid + 
                    " ORDER BY U.User_ID ASC");
                long uid = 0;
                String fname = ""; String lname = "";
                while (rst4.next()) {
                    uid = rst4.getLong(1);
                    fname = rst4.getString(2);
                    lname = rst4.getString(3);
                    UserInfo u = new UserInfo(uid, fname, lname);
                    tp.addTaggedUser(u);
                }
                results.add(tp);
                if (rst.isLast()) {
                    rst4.close();
                }
            }
            stmt4.close();
            rst.close();
            stmt.close();
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                PhotoInfo p = new PhotoInfo(80, 5, "www.photolink.net", "Winterfell S1");
                UserInfo u1 = new UserInfo(3901, "Jon", "Snow");
                UserInfo u2 = new UserInfo(3902, "Arya", "Stark");
                UserInfo u3 = new UserInfo(3903, "Sansa", "Stark");
                TaggedPhotoInfo tp = new TaggedPhotoInfo(p);
                tp.addTaggedUser(u1);
                tp.addTaggedUser(u2);
                tp.addTaggedUser(u3);
                results.add(tp);
            */
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return results;
    }
    
    @Override
    // Query 5
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, last names, and birth years of each of the two
    //            users in the top <num> pairs of users that meet each of the following
    //            criteria:
    //              (i) same gender
    //              (ii) tagged in at least one common photo
    //              (iii) difference in birth years is no more than <yearDiff>
    //              (iv) not friends
    //        (B) For each pair identified in (A), find the IDs, links, and IDs and names of
    //            the containing album of each photo in which they are tagged together
    public FakebookArrayList<MatchPair> matchMaker(int num, int yearDiff) throws SQLException {
        FakebookArrayList<MatchPair> results = new FakebookArrayList<MatchPair>("\n");
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT X.UU1, X.UU2, X.UY1, X.UY2, X.COMMON FROM " + 
                "(SELECT U1.User_ID AS UU1, U2.User_ID AS UU2, U1.Year_Of_Birth AS UY1, U2.Year_Of_Birth AS UY2, COUNT(*) AS COMMON " + 
                "FROM " + UsersTable + " U1, " + UsersTable + " U2, " + TagsTable + " T1, " + TagsTable + " T2, " + 
                PhotosTable + " P, " + AlbumsTable + " A " + 
                "WHERE (U1.User_ID < U2.User_ID AND U1.Gender = U2.Gender AND ABS(U1.Year_Of_Birth - U2.Year_Of_Birth) <= " + yearDiff + ") " +  
                "AND (T1.Tag_Photo_ID = T2.Tag_Photo_ID AND T1.Tag_Subject_ID < T2.Tag_Subject_ID " +  
                "AND U1.User_ID = T1.Tag_Subject_ID AND U2.User_ID = T2.Tag_Subject_ID) " +  
                "AND (P.Photo_ID = T1.Tag_Photo_ID AND A.Album_ID = P.Album_ID) " + 
                "AND NOT EXISTS " +  
                "(SELECT DISTINCT F.User1_ID, F.User2_ID FROM " + FriendsTable + " F " + 
                "WHERE F.User1_ID = U1.User_ID AND F.User2_ID = U2.User_ID) " + 
                "GROUP BY U1.User_ID, U2.User_ID, U1.Year_Of_Birth, U2.Year_Of_Birth " +  
                "ORDER BY COUNT(*) DESC, U1.User_ID ASC, U2.User_ID ASC) X " + 
                "WHERE ROWNUM <= " + num);
            long[][] userArray = new long[num][4];
            int pair = 0;
            while (rst.next()) {
                userArray[pair][0] = rst.getLong(1);
                userArray[pair][1] = rst.getLong(2);
                userArray[pair][2] = rst.getLong(3);
                userArray[pair][3] = rst.getLong(4);
                pair += 1;
            }
            int smaller = 0;
            if (num <= pair) {
                smaller = num;
            }
            else {
                smaller = pair;
            }
            //int smaller = Math.min(pair, num);
            String fst1 = ""; String lst1 = "";
            String fst2 = ""; String lst2 = "";
            long tpid = 0; String ppl = "";
            long aid = 0; String aname = "";
            for (int i = 0; i < smaller; i++) {
                rst = stmt.executeQuery(
                    "SELECT U1.First_Name, U1.Last_Name, U2.First_Name, U2.Last_Name " +  
                    "FROM " + UsersTable + " U1, " + UsersTable +  " U2 " + 
                    "WHERE U1.User_ID = " + userArray[i][0] + " AND U2.User_ID = " + userArray[i][1]);
                rst.first();
                fst1 = rst.getString(1); lst1 = rst.getString(2);
                fst2 = rst.getString(3); lst2 = rst.getString(4);
                rst = stmt.executeQuery(
                    "SELECT DISTINCT T1.Tag_Photo_ID, P.Photo_Link, A.Album_ID, A.Album_Name " + 
                    "FROM " + TagsTable + " T1, " + TagsTable + " T2, " + PhotosTable + " P, " + AlbumsTable + " A " + 
                    "WHERE T1.Tag_Subject_ID = " + userArray[i][0] + " AND T2.Tag_Subject_ID = " + userArray[i][1] + 
                    " AND T1.Tag_Photo_ID = T2.Tag_Photo_ID " + 
                    "AND P.Photo_ID = T1.Tag_Photo_ID AND A.Album_ID = P.Album_ID");
                UserInfo u1 = new UserInfo(userArray[i][0], fst1, lst1);
                UserInfo u2 = new UserInfo(userArray[i][1], fst2, lst2);
                MatchPair mp = new MatchPair(u1, userArray[i][2], u2, userArray[i][3]);
                while (rst.next()) {
                    tpid = rst.getLong(1); ppl = rst.getString(2);
                    aid = rst.getLong(3); aname = rst.getString(4);
                    PhotoInfo p = new PhotoInfo(tpid, aid, ppl, aname);
                    mp.addSharedPhoto(p);
                }
                results.add(mp);
                rst.close();
                stmt.close();
            }
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(93103, "Romeo", "Montague");
                UserInfo u2 = new UserInfo(93113, "Juliet", "Capulet");
                MatchPair mp = new MatchPair(u1, 1597, u2, 1597);
                PhotoInfo p = new PhotoInfo(167, 309, "www.photolink.net", "Tragedy");
                mp.addSharedPhoto(p);
                results.add(mp);
            */
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        
        return results;
    }
    
    @Override
    // Query 6
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the IDs, first names, and last names of each of the two users in
    //            the top <num> pairs of users who are not friends but have a lot of
    //            common friends
    //        (B) For each pair identified in (A), find the IDs, first names, and last names
    //            of all the two users' common friends
    public FakebookArrayList<UsersPair> suggestFriends(int num) throws SQLException {
        FakebookArrayList<UsersPair> results = new FakebookArrayList<UsersPair>("\n");
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            String view1 = "CREATE OR REPLACE VIEW All_Friends AS\n" +
                "SELECT USER1_ID AS u1d, USER2_ID AS u2d\n" +
                "FROM " + FriendsTable + "\n" +
                "UNION\n" +
                "SELECT USER2_ID, USER1_ID FROM " + FriendsTable + 
                " ORDER BY u1d, u2d";
            stmt.executeUpdate(view1);
            String view2 = "CREATE OR REPLACE VIEW Common_Friends AS " + 
                "SELECT AF1.u1d AS u1d, AF2.u2d AS u2d, AF1.u2d AS u3d FROM All_Friends AF1, All_Friends AF2 " + 
                "WHERE AF1.u1d < AF2.u2d AND AF1.u2d = AF2.u1d AND (AF1.u1d, AF2.u2d) NOT IN " + 
                "(SELECT F.User1_ID, F.User2_ID FROM " + FriendsTable + " F)";
            stmt.executeUpdate(view2);
            ResultSet rst = stmt.executeQuery(
                "SELECT u1d, u2d, Common FROM (\n" +
                "SELECT u1d, u2d, COUNT(u3d) AS Common FROM Common_Friends\n" +
                "GROUP BY u1d, u2d\n" +
                "ORDER BY Common DESC, u1d, u2d)\n" +
                "WHERE ROWNUM <= " + num);
            Statement stmt6 = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly);
            while (rst.next()) {
                ResultSet rst6 = stmt6.executeQuery(
                    "SELECT U1.First_Name, U1.Last_Name, U2.First_Name, U2.Last_Name, CF.u3d, U3.First_Name, U3.Last_Name " + 
                    "FROM Common_Friends CF, " + UsersTable + " U1, " + UsersTable + " U2, " + UsersTable + " U3 " + 
                    "WHERE CF.u1d = " + rst.getLong(1) + " AND U1.User_ID = CF.u1d " + 
                    "AND CF.u2d = " + rst.getLong(2) + " AND U2.User_ID = CF.u2d " + 
                    "AND U3.User_ID = CF.u3d ORDER BY CF.u3d");
                UsersPair up = null;
                while (rst6.next()) {
                    if (rst6.isFirst()) {
                        UserInfo u1 = new UserInfo(rst.getLong(1), rst6.getString(1), rst6.getString(2));
                        UserInfo u2 = new UserInfo(rst.getLong(2), rst6.getString(3), rst6.getString(4));
                        up = new UsersPair(u1, u2);
                    }
                    UserInfo u3 = new UserInfo(rst6.getLong(5), rst6.getString(6), rst6.getString(7));
                    up.addSharedFriend(u3);
                }
                if (up != null) {
                    results.add(up);
                }
            }
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(16, "The", "Hacker");
                UserInfo u2 = new UserInfo(80, "Dr.", "Marbles");
                UserInfo u3 = new UserInfo(192, "Digit", "Le Boid");
                UsersPair up = new UsersPair(u1, u2);
                up.addSharedFriend(u3);
                results.add(up);
            */
            stmt.executeUpdate("DROP VIEW All_Friends");
            stmt.executeUpdate("DROP VIEW Common_Friends");
            rst.close();
            stmt.close();
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return results;
    }
    
    @Override
    // Query 7
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the name of the state or states in which the most events are held
    //        (B) Find the number of events held in the states identified in (A)
    public EventStateInfo findEventStates() throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT C.State_Name, COUNT(*) AS Num_Events " + 
                "FROM " + EventsTable + " E LEFT JOIN " + CitiesTable + " C ON C.City_ID = E.Event_City_ID " + 
                "GROUP BY C.State_Name " +  
                "HAVING COUNT(*) = (SELECT MAX(COUNT(*)) " + 
                "FROM " + EventsTable + " E LEFT JOIN " + CitiesTable + " C ON C.City_ID = E.Event_City_ID GROUP BY C.State_Name) " +  
                "ORDER BY C.State_Name ASC");
            long count = 0;
            while (rst.next()) {
                count += rst.getLong(2);
            }
            EventStateInfo info7 = new EventStateInfo(count);
            rst.beforeFirst();
            while (rst.next()) {
                info7.addState(rst.getString(1));
            }
            rst.close();
            stmt.close();
            return info7;
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                EventStateInfo info = new EventStateInfo(50);
                info.addState("Kentucky");
                info.addState("Hawaii");
                info.addState("New Hampshire");
                return info;
            */
            //return new EventStateInfo(-1);                // placeholder for compilation
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return new EventStateInfo(-1);
        }
    }
    
    @Override
    // Query 8
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find the ID, first name, and last name of the oldest friend of the user
    //            with User ID <userID>
    //        (B) Find the ID, first name, and last name of the youngest friend of the user
    //            with User ID <userID>
    public AgeInfo findAgeInfo(long userID) throws SQLException {
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT DISTINCT User_ID, First_Name, Last_Name, Year_Of_Birth, Month_Of_Birth, Day_Of_Birth FROM " +
                "(SELECT DISTINCT U.User_ID, U.First_Name, U.Last_Name, U.Year_Of_Birth, U.Month_Of_Birth, U.Day_Of_Birth " +  
                "FROM " + UsersTable + " U, " + FriendsTable + " F " + //+ FriendsTable + " F2, " + 
                "WHERE ((F.User1_ID = " + userID + " AND F.User2_ID = U.User_ID) OR (F.User2_ID = " + userID + " AND F.User1_ID = U.User_ID)) " + 
                "ORDER BY U.Year_Of_Birth ASC, U.Month_Of_Birth ASC, U.Day_Of_Birth ASC, U.User_ID DESC) " + 
                "WHERE ROWNUM <= 1");
            rst.first();
            long userid = rst.getLong(1);
            String first_name = rst.getString(2);
            String last_name = rst.getString(3);
            UserInfo oldest = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
            rst = stmt.executeQuery(
                "SELECT DISTINCT User_ID, First_Name, Last_Name, Year_Of_Birth, Month_Of_Birth, Day_Of_Birth FROM " +
                "(SELECT DISTINCT U.User_ID, U.First_Name, U.Last_Name, U.Year_Of_Birth, U.Month_Of_Birth, U.Day_Of_Birth " +  
                "FROM " + UsersTable + " U, " + FriendsTable + " F " + //FriendsTable + " F2 " + 
                "WHERE ((F.User1_ID = " + userID + " AND F.User2_ID = U.User_ID) OR (F.User2_ID = " + userID + " AND F.User1_ID = U.User_ID)) " + 
                "ORDER BY U.Year_Of_Birth DESC, U.Month_Of_Birth DESC, U.Day_Of_Birth DESC, U.User_ID DESC) " + 
                "WHERE ROWNUM <= 1");
            rst.first();
            userid = rst.getLong(1);
            first_name = rst.getString(2);
            last_name = rst.getString(3);
            UserInfo youngest = new UserInfo(rst.getLong(1), rst.getString(2), rst.getString(3));
            AgeInfo info8 = new AgeInfo(oldest, youngest);
            rst.close();
            stmt.close();
            return info8;
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo old = new UserInfo(12000000, "Galileo", "Galilei");
                UserInfo young = new UserInfo(80000000, "Neil", "deGrasse Tyson");
                return new AgeInfo(old, young);
            */
            //return new AgeInfo(new UserInfo(-1, "UNWRITTEN", "UNWRITTEN"), new UserInfo(-1, "UNWRITTEN", "UNWRITTEN"));                // placeholder for compilation
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
            return new AgeInfo(new UserInfo(-1, "ERROR", "ERROR"), new UserInfo(-1, "ERROR", "ERROR"));
        }
    }
    
    @Override
    // Query 9
    // -----------------------------------------------------------------------------------
    // GOALS: (A) Find all pairs of users that meet each of the following criteria
    //              (i) same last name
    //              (ii) same hometown
    //              (iii) are friends
    //              (iv) less than 10 birth years apart
    public FakebookArrayList<SiblingInfo> findPotentialSiblings() throws SQLException {
        FakebookArrayList<SiblingInfo> results = new FakebookArrayList<SiblingInfo>("\n");     
        try (Statement stmt = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly)) {
            ResultSet rst = stmt.executeQuery(
                "SELECT User1_ID, User2_ID " + 
                "FROM " + FriendsTable + " F, " + UsersTable + " U1, " + UsersTable + " U2, " +  
                HometownCitiesTable + " UHI1, " + HometownCitiesTable + " UHI2 " +
                "WHERE (U1.User_ID = F.User1_ID AND U2.User_ID = F.User2_ID " + 
                "AND U1.Last_Name = U2.Last_Name AND ABS(U1.Year_Of_Birth - U2.Year_Of_Birth) < 10) " + 
                "AND (UHI1.User_ID = F.User1_ID AND UHI2.User_ID = F.User2_ID AND UHI1.Hometown_City_ID = UHI2.Hometown_City_ID) " + 
                "ORDER BY User1_ID ASC, User2_ID ASC");
            Statement stmt9 = oracle.createStatement(FakebookOracleConstants.AllScroll, FakebookOracleConstants.ReadOnly);
            while (rst.next()) {
                long user1id = rst.getLong(1);
                long user2id = rst.getLong(2);
                String sib1_first_name = "";
                String sib1_last_name = "";
                String sib2_first_name = "";
                String sib2_last_name = "";
                ResultSet rst9 = stmt9.executeQuery(
                    "SELECT First_Name, Last_Name FROM " + UsersTable + " WHERE User_ID = " + user1id + " OR User_ID = " + user2id);
                rst9.first();
                sib1_first_name = rst9.getString(1);
                sib1_last_name = rst9.getString(2);
                UserInfo u1 = new UserInfo(user1id, sib1_first_name, sib1_last_name);
                rst9.next();
                sib2_first_name = rst9.getString(1);
                sib2_last_name = rst9.getString(2);
                UserInfo u2 = new UserInfo(user2id, sib2_first_name, sib2_last_name);
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
                rst9.close();
            }
            stmt9.close();
            rst.close();
            stmt.close();
            /*
                EXAMPLE DATA STRUCTURE USAGE
                ============================================
                UserInfo u1 = new UserInfo(81023, "Kim", "Kardashian");
                UserInfo u2 = new UserInfo(17231, "Kourtney", "Kardashian");
                SiblingInfo si = new SiblingInfo(u1, u2);
                results.add(si);
            */
        }
        catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        
        return results;
    }
    
    // Member Variables
    private Connection oracle;
    private final String UsersTable = FakebookOracleConstants.UsersTable;
    private final String CitiesTable = FakebookOracleConstants.CitiesTable;
    private final String FriendsTable = FakebookOracleConstants.FriendsTable;
    private final String CurrentCitiesTable = FakebookOracleConstants.CurrentCitiesTable;
    private final String HometownCitiesTable = FakebookOracleConstants.HometownCitiesTable;
    private final String ProgramsTable = FakebookOracleConstants.ProgramsTable;
    private final String EducationTable = FakebookOracleConstants.EducationTable;
    private final String EventsTable = FakebookOracleConstants.EventsTable;
    private final String AlbumsTable = FakebookOracleConstants.AlbumsTable;
    private final String PhotosTable = FakebookOracleConstants.PhotosTable;
    private final String TagsTable = FakebookOracleConstants.TagsTable;
}
