SELECT A0.First_Name, A0.Last_Name FROM Authors A0 WHERE A0.Author_ID IN 
(SELECT DISTINCT A.Author_ID FROM Authors A, Books B1, Books B2, Subjects S1, Subjects S2 
WHERE A.Author_ID = B1.Author_ID AND B1.Subject_ID = S1.Subject_ID AND A.Author_ID = B2.Author_ID AND B2.Subject_ID = S2.Subject_ID
AND S1.Subject = 'Children/YA' AND S2.Subject = 'Fiction')
ORDER BY A0.First_Name ASC, A0.Last_Name ASC;