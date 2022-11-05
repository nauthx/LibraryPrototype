.mode quote
.mode line
.mode column
.width 12 -6
INSERT INTO USER VALUES('Nassim','Uthman','Test1','nauth100@hhu.de','2021-10-10');
INSERT INTO USER VALUES('Aastik','Malhotra','Test1','cheap@hhu.in','2021-10-10');
INSERT INTO USER VALUES('Julia','Polish','GhostedMe','julia@hhu.pl','2021-10-10');
INSERT INTO USER VALUES('Elon','Musk','Test1','elon@tesla.com','2021-10-10');
INSERT INTO USER VALUES('Bill','Gates','Test1','billy@ms.com','1995-10-10');
INSERT INTO USER VALUES('Jeff','Bezos','Test1','MoneyBaldGuy@amazon.com','2021-10-10');
INSERT INTO USER VALUES('Joe','Rogan','Test1','GorillaVsBear@jme.com','2021-10-10');
INSERT INTO USER VALUES('Customer','Dummy','Test1','Dummy@jme.com','2021-10-10');
INSERT INTO USER VALUES('Homeless','Guy','Test1','streets@crack.com','2021-10-10');
INSERT INTO ADDRESS VALUES(1,'Silicon Valley','100th','90210','Los Angeles');
INSERT INTO ADDRESS VALUES(2,'Broadway Street','5th','56488','New York City');
INSERT INTO ADDRESS VALUES(3,'Saibara','1st','56432','Omuta-shi');
INSERT INTO ADDRESS VALUES(4,'Hang Wai Ind Centre',30 ,'123456','TuenMun');
INSERT INTO ADDRESS VALUES(6,'Akihabara',40 ,'15895','Tokyo');

INSERT INTO LIBRARIAN VALUES('julia@hhu.pl','0049123456');
INSERT INTO LIBRARIAN VALUES('cheap@hhu.in','004999999');
INSERT INTO LIBRARIAN VALUES('nauth100@hhu.de','004900009');
INSERT INTO CUSTOMER VALUES('Dummy@jme.com',10.99,'true',1);
INSERT INTO CUSTOMER VALUES('elon@tesla.com',05.99,'false',2);
INSERT INTO CUSTOMER VALUES('GorillaVsBear@jme.com',20.99,'true',3);
INSERT INTO CUSTOMER VALUES('MoneyBaldGuy@amazon.com',01.99,'true',1);
INSERT INTO CUSTOMER VALUES('streets@crack.com',0.00,'true',1);
INSERT INTO ITEM VALUES(1,'A story from ancient Arabia','2021-01-01','Aladin','DVD',readfile('Aladdin.png'));
INSERT INTO ITEM VALUES(2,'Solving Mysteries and save the queen of england','2021-01-01','Sherlock Holmes','Hardcover',readfile('Sherlock.png'));
INSERT INTO ITEM VALUES(3,'The ins and outs of the C Programming Language','2021-01-01','The C Programming Language','Softcover',readfile('C.png'));
INSERT INTO LOCATION VALUES(1,2);
INSERT INTO LOCATION VALUES(2,2);
INSERT INTO LOCATION VALUES(3,2);
INSERT INTO LOCATION VALUES(4,2);
INSERT INTO LOCATION VALUES(5,2);
INSERT INTO LOCATION VALUES(5,3);
INSERT INTO LOCATION VALUES(5,4);
INSERT INTO LOCATION VALUES(5,5);
INSERT INTO LOCATION VALUES(5,6);
INSERT INTO LOCATION VALUES(5,7);
INSERT INTO LOCATION VALUES(5,8);
INSERT INTO LOCATION VALUES(5,9);
INSERT INTO BOOK_COPY VALUES(1,20.0,1,1,2);
INSERT INTO BOOK_COPY VALUES(2,20.0,2,2,2);
INSERT INTO BOOK_COPY VALUES(3,20.0,3,3,2);
INSERT INTO BOOK_COPY VALUES(4,20.0,1,4,2);
INSERT INTO BOOK_COPY VALUES(5,20.0,2,5,2);
INSERT INTO BOOK_COPY VALUES(6,20.0,3,5,3);
INSERT INTO BOOK_COPY VALUES(7,20.0,3,5,4);
INSERT INTO BOOK_COPY VALUES(8,20.0,3,5,5);
INSERT INTO BOOK_COPY VALUES(9,20.0,3,5,6);
INSERT INTO BOOK_COPY VALUES(10,20.0,3,5,7);
INSERT INTO BOOK_COPY VALUES(11,20.0,3,5,8);
INSERT INTO BOOK_COPY VALUES(12,20.0,3,5,9);
INSERT INTO GENRE VALUES('Mathematics');
INSERT INTO GENRE VALUES('Physics');
INSERT INTO GENRE VALUES('ComputerScience');
INSERT INTO GENRE VALUES('Biology');
INSERT INTO GENRE VALUES('Law');
INSERT INTO GENRE VALUES('Linguistics');
INSERT INTO GENRE VALUES('Crime');
INSERT INTO GENRE VALUES('Adventure');
INSERT INTO AUTHOR VALUES(1,'Dennis','Ritchie');
INSERT INTO AUTHOR VALUES(2,'Brian','Kerninghan');
INSERT INTO AUTHOR VALUES(3,'JK','Rowling');
INSERT INTO AUTHOR VALUES(4,'Masashi','Kishimoto');
INSERT INTO AUTHOR VALUES(5,'Ai','Water');
INSERT INTO AUTHOR VALUES(6,'Bruce','Christoff');
INSERT INTO AUTHOR VALUES(7,'Walt','Disney');
INSERT INTO ITEM_HAS_GENRE VALUES(1,'Adventure');
INSERT INTO ITEM_HAS_GENRE VALUES(2,'Crime');
INSERT INTO ITEM_HAS_GENRE VALUES(3,'ComputerScience');
INSERT INTO AUTHOR_WRITES_BOOK VALUES(2,3);
INSERT INTO AUTHOR_WRITES_BOOK VALUES(1,3);
INSERT INTO AUTHOR_WRITES_BOOK VALUES(7,1);
INSERT INTO BORROW VALUES(1,'2021-01-01 10:10:10','2021-03-03 10:10:10','false','elon@tesla.com',1);
INSERT INTO BORROW VALUES(2,'2021-01-01 10:10:10','2021-03-03 10:10:10','false','GorillaVsBear@jme.com',2);
INSERT INTO BORROW VALUES(3,'2021-01-01 10:10:10','2021-03-03 10:10:10','false','MoneyBaldGuy@amazon.com',3);
INSERT INTO ITEM_RECOMMENDS_ITEM VALUES(1,3);
INSERT INTO ITEM_RECOMMENDS_ITEM VALUES (2,1);







