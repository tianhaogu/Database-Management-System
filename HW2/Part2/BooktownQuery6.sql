SELECT DISTINCT B.Title, E.Publication_Date, A.Author_ID, A.First_Name, A.Last_Name FROM Editions E
JOIN Books B ON E.Book_ID = B.Book_ID JOIN Authors A ON B.Author_ID = A.Author_ID
WHERE B.Author_ID IN 
(SELECT DISTINCT B1.Author_ID FROM Books B1 JOIN Editions E1 ON B1.Book_ID = E1.Book_ID WHERE E1.Publication_Date BETWEEN '2003-01-01' AND '2008-12-31')
ORDER BY A.Author_ID ASC, B.Title ASC, E.Publication_Date DESC;