SELECT B.Title, SUM(E.Pages) AS Total_Pages FROM Books B, Editions E WHERE B.Book_ID = E.Book_ID
GROUP BY B.Title HAVING Count(*)>=1
ORDER BY Total_Pages DESC;