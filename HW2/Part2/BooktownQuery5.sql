SELECT DISTINCT A.Author_ID, A.First_Name, A.Last_Name FROM Authors A JOIN Books B ON A.Author_ID = B.Author_ID 
WHERE NOT EXISTS (SELECT DISTINCT B1.Subject_ID FROM Books B1 JOIN Authors A1 ON B1.Author_ID = A1.Author_ID WHERE A1.Last_Name = 'Rowling' AND A1.First_Name = 'J. K.'
                  MINUS
                  SELECT DISTINCT B.Subject_ID FROM Books B WHERE B.Author_ID = A.Author_ID)
ORDER BY A.Last_Name ASC, A.Author_ID DESC;
