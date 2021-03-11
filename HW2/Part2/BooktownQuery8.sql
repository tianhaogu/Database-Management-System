SELECT DISTINCT P.Publisher_ID, P.Name FROM Publishers P 
JOIN Editions E ON P.Publisher_ID = E.Publisher_ID
JOIN Books B ON E.Book_ID = B.Book_ID
WHERE B.Author_ID IN
(SELECT B.Author_ID FROM Books B GROUP BY B.Author_ID HAVING COUNT(B.Book_ID)=3)
ORDER BY P.Publisher_ID DESC;