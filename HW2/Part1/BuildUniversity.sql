CREATE TABLE Students(
  SID INTEGER PRIMARY KEY,
  Name VARCHAR(20) NOT NULL,
  Major VARCHAR(20)
);

CREATE TABLE Projects(
  PID INTEGER PRIMARY KEY,
  P_Name VARCHAR(20) NOT NULL,
  UNIQUE (P_Name)
);

CREATE TABLE Courses(
  CID INTEGER PRIMARY KEY,
  C_Name VARCHAR(20) NOT NULL,
  UNIQUE (C_Name)
);

CREATE TABLE Members(
  PID INTEGER NOT NULL,
  SID INTEGER NOT NULL,
  PRIMARY KEY (PID, SID),
  FOREIGN KEY (PID) REFERENCES Projects,
  FOREIGN KEY (SID) REFERENCES Students
);

CREATE TABLE Enrollments(
  CID INTEGER NOT NULL,
  SID INTEGER NOT NULL,
  PRIMARY KEY (CID, SID),
  FOREIGN KEY (CID) REFERENCES Courses,
  FOREIGN KEY (SID) REFERENCES Students
);

INSERT INTO Students VALUES(1, 'Tianhao', 'Non-CS');
INSERT INTO Students VALUES(2, 'Ruijie', 'CS');
INSERT INTO Students VALUES(3, 'Zhiyuan', NULL);
INSERT INTO Students VALUES(4, 'Jiawei', 'Non-CS');
INSERT INTO Students VALUES(5, 'Zixiang', 'CS');
INSERT INTO Students VALUES(6, 'Diyuan', 'CS');
INSERT INTO Students VALUES(7, 'Ruizhen', 'CS');
INSERT INTO Students VALUES(8, 'Junhan', NULL);
INSERT INTO Students VALUES(9, 'Hongrui', 'CS');
INSERT INTO Students VALUES(10, 'Botao', 'Non-CS');
INSERT INTO Students VALUES(11, 'Yun', 'CS');
INSERT INTO Students VALUES(12, 'Zhangliang', NULL);
INSERT INTO Students VALUES(13, 'Ziting', 'CS');
INSERT INTO Students VALUES(14, 'Yaoyu', 'CS');
INSERT INTO Students VALUES(15, 'Junkai', 'Non-CS');
INSERT INTO Students VALUES(16, 'Hongkun', 'Non-CS');
INSERT INTO Students VALUES(17, 'Jingmin', 'Non-CS');
INSERT INTO Students VALUES(18, 'Kai', NULL);
INSERT INTO Students VALUES(19, 'Chenyu', 'Non-CS');
INSERT INTO Students VALUES(20, 'Yushen', 'Non-CS');
INSERT INTO Students VALUES(21, 'Jiayi', 'Non-CS');
INSERT INTO Students VALUES(22, 'Peiran', 'Non-CS');
INSERT INTO Students VALUES(23, 'Yijun', 'Non-CS');
INSERT INTO Students VALUES(24, 'Hong', NULL);
INSERT INTO Students VALUES(25, 'Shanyi', 'CS');

INSERT INTO Projects VALUES(1, 'Proj1');
INSERT INTO Projects VALUES(2, 'Proj2');
INSERT INTO Projects VALUES(3, 'Proj3');
INSERT INTO Projects VALUES(4, 'Proj4');
INSERT INTO Projects VALUES(5, 'Proj5');
INSERT INTO Projects VALUES(6, 'Proj6');
--INSERT INTO Projects VALUES(7, 'Proj7');
--INSERT INTO Projects VALUES(8, 'Proj8');
--INSERT INTO Projects VALUES(9, 'Proj9');
--INSERT INTO Projects VALUES(10, 'Proj10');

INSERT INTO Courses VALUES(1, 'EECS482');
INSERT INTO Courses VALUES(2, 'EECS483');
INSERT INTO Courses VALUES(3, 'EECS484');
INSERT INTO Courses VALUES(4, 'EECS485');
INSERT INTO Courses VALUES(5, 'EECS280');
INSERT INTO Courses VALUES(6, 'EECS442');
INSERT INTO Courses VALUES(7, 'EECS445');
INSERT INTO Courses VALUES(8, 'EECS492');
INSERT INTO Courses VALUES(9, 'EECS486');
INSERT INTO Courses VALUES(10, 'EECS281');

INSERT INTO Members VALUES(1, 1);
INSERT INTO Members VALUES(1, 4);
INSERT INTO Members VALUES(1, 9);
INSERT INTO Members VALUES(2, 5);
INSERT INTO Members VALUES(2, 7);
INSERT INTO Members VALUES(3, 6);
INSERT INTO Members VALUES(3, 1);
INSERT INTO Members VALUES(3, 15);
INSERT INTO Members VALUES(3, 16);
INSERT INTO Members VALUES(3, 17);
INSERT INTO Members VALUES(3, 18);
INSERT INTO Members VALUES(3, 19);
INSERT INTO Members VALUES(4, 1);
INSERT INTO Members VALUES(4, 19);
INSERT INTO Members VALUES(4, 20);
INSERT INTO Members VALUES(4, 9);


INSERT INTO Enrollments VALUES(1, 1);
INSERT INTO Enrollments VALUES(1, 2);
INSERT INTO Enrollments VALUES(1, 5);
INSERT INTO Enrollments VALUES(1, 6);
INSERT INTO Enrollments VALUES(1, 7);
INSERT INTO Enrollments VALUES(1, 9);
INSERT INTO Enrollments VALUES(1, 12);

INSERT INTO Enrollments VALUES(2, 23);
INSERT INTO Enrollments VALUES(2, 10);
INSERT INTO Enrollments VALUES(2, 13);
INSERT INTO Enrollments VALUES(2, 14);
INSERT INTO Enrollments VALUES(2, 11);

INSERT INTO Enrollments VALUES(3, 1);
INSERT INTO Enrollments VALUES(3, 15);
INSERT INTO Enrollments VALUES(3, 16);
INSERT INTO Enrollments VALUES(3, 17);
INSERT INTO Enrollments VALUES(3, 18);
INSERT INTO Enrollments VALUES(3, 19);
INSERT INTO Enrollments VALUES(3, 2);
INSERT INTO Enrollments VALUES(3, 5);
INSERT INTO Enrollments VALUES(3, 6);
INSERT INTO Enrollments VALUES(3, 7);
INSERT INTO Enrollments VALUES(3, 9);

INSERT INTO Enrollments VALUES(4, 1);
INSERT INTO Enrollments VALUES(4, 19);
INSERT INTO Enrollments VALUES(4, 20);
INSERT INTO Enrollments VALUES(4, 9);
INSERT INTO Enrollments VALUES(4, 11);

INSERT INTO Enrollments VALUES(5, 1);
INSERT INTO Enrollments VALUES(5, 21);
INSERT INTO Enrollments VALUES(5, 14);
INSERT INTO Enrollments VALUES(5, 13);

INSERT INTO Enrollments VALUES(6, 15);
INSERT INTO Enrollments VALUES(6, 21);
INSERT INTO Enrollments VALUES(6, 22);
INSERT INTO Enrollments VALUES(6, 23);
INSERT INTO Enrollments VALUES(6, 24);
INSERT INTO Enrollments VALUES(6, 25);

INSERT INTO Enrollments VALUES(7, 1);
INSERT INTO Enrollments VALUES(7, 3);
INSERT INTO Enrollments VALUES(7, 4);
INSERT INTO Enrollments VALUES(7, 8);
INSERT INTO Enrollments VALUES(7, 10);
INSERT INTO Enrollments VALUES(7, 2);
INSERT INTO Enrollments VALUES(7, 5);
INSERT INTO Enrollments VALUES(7, 6);
INSERT INTO Enrollments VALUES(7, 7);
INSERT INTO Enrollments VALUES(7, 9);

INSERT INTO Enrollments VALUES(8, 12);
INSERT INTO Enrollments VALUES(8, 15);
INSERT INTO Enrollments VALUES(8, 16);
INSERT INTO Enrollments VALUES(8, 17);
INSERT INTO Enrollments VALUES(8, 18);
INSERT INTO Enrollments VALUES(8, 25);
INSERT INTO Enrollments VALUES(8, 11);

INSERT INTO Enrollments VALUES(9, 1);
INSERT INTO Enrollments VALUES(9, 19);
INSERT INTO Enrollments VALUES(9, 20);
INSERT INTO Enrollments VALUES(9, 21);
INSERT INTO Enrollments VALUES(9, 22);
INSERT INTO Enrollments VALUES(9, 9);
INSERT INTO Enrollments VALUES(9, 25);

INSERT INTO Enrollments VALUES(10, 2);
INSERT INTO Enrollments VALUES(10, 5);
INSERT INTO Enrollments VALUES(10, 6);
INSERT INTO Enrollments VALUES(10, 7);
INSERT INTO Enrollments VALUES(10, 9);