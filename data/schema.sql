CREATE TABLE USER(
	FirstName Varchar(100) NOT NULL check ( FirstName NOT GLOB '*[^ -~]*' AND FirstName GLOB '*[^ ]*'),
    LastName Varchar(100) NOT NULL check (LastName NOT GLOB '*[^ -~]*' AND LastName GLOB '*[^ ]*'),
    Password Varchar(100) NOT NULL check (length(Password) >=4),
    Email Varchar(100) NOT NULL COLLATE NOCASE CHECK       
        (substr(Email, 1,instr(Email,'@')-1) NOT GLOB '*[^a-zA-Z0-9]*'
        AND length (substr(Email, 1,instr(Email,'@')-1)) >= 1
        AND substr(Email, instr(Email,'@'),1) like '@'
        AND substr ( Email , instr(Email,'@')+1 ,( instr(Email,'.') - (instr(Email,'@')+1)) ) NOT GLOB '*[^a-zA-Z0-9]*'
        AND substr( Email , instr( Email ,'.') , 1) like '.'
        AND substr ( Email ,instr(Email,'.')+1) NOT GLOB '*[^a-zA-Z]*'
        AND Email GLOB '*[^ ]*'),
        DateOfBirth DATE NOT NULL,
        primary key (Email)
);

CREATE TABLE LIBRARIAN(
	Email Varchar(100) NOT NULL COLLATE NOCASE,
	PhoneNr Varchar(100) NOT NULL COLLATE NOCASE CHECK(length(PhoneNr) > 0 AND PhoneNr like '0049%' ),
	UNIQUE(PhoneNr),
	PRIMARY KEY (Email),
	FOREIGN KEY (Email) REFERENCES USER (Email) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE CUSTOMER(
		Email Varchar(100) NOT NULL COLLATE NOCASE,
		Credit FLOAT NOT NULL DEFAULT 0 CHECK (Credit >= 0 AND length(Credit) - instr(Credit, '.') <=2 ),
		Exemption BOOLEAN CHECK (Exemption IN ('true', 'false')),
		ID LONG NOT NULL,
		PRIMARY KEY (Email),
		FOREIGN KEY (Email) REFERENCES USER (Email) ON UPDATE CASCADE ON DELETE CASCADE,
		FOREIGN KEY (ID) REFERENCES Address (ID)
);

CREATE TABLE ADDRESS(
	ID INTEGER NOT NULL CHECK(ID >= 1),
	Street Varchar(100) NOT NULL CHECK (Street NOT GLOB '*[^ -~]*' AND Street GLOB '*[^ ]*' AND length(Street) > 0),
	HouseNr Varchar(100) NOT NULL CHECK (length(HouseNr) > 0 AND HouseNr NOT GLOB '*[^a-zA-Z0-9]*' ),
	PostCode VARCHAR(15) NOT NULL CHECK (length(PostCode) > 0 AND length(PostCode) < 7),
	City Varchar(100) NOT NULL CHECK (City NOT GLOB '*[^ -~]*' AND City GLOB '*[^ ]*' AND length(City) > 0),
	UNIQUE(City,HouseNr,PostCode,Street),
	PRIMARY KEY (ID)
);

CREATE TABLE BOOK_COPY(
	ID INTEGER NOT NULL CHECK (ID >= 0),
	PRICE FLOAT NOT NULL CHECK (PRICE > 0),
	ISBN Varchar NOT NULL CHECK(length(ISBN) > 0),
	Floor INTEGER NOT NULL,
	ShelfNr INTEGER NOT NULL CHECK (ShelfNr > 0),
	PRIMARY KEY (ID),
	FOREIGN KEY (ISBN) REFERENCES ITEM (ISBN) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (Floor,ShelfNr) REFERENCES LOCATION (Floor,ShelfNr) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE LOCATION(
	Floor INTEGER NOT NULL,
	ShelfNr INTEGER NOT NULL CHECK (ShelfNr > 0),
	PRIMARY KEY (Floor,ShelfNr)
);

CREATE TABLE ITEM(
	ISBN Varchar(100) NOT NULL CHECK (length(ISBN) > 0 AND ISBN NOT GLOB '*[^!-~]*'),
	Description TEXT NOT NULL CHECK (Description NOT GLOB '*[^ -~]*' AND Description GLOB '*[^ ]*' AND length(Description) > 0),
	PublicationDate DATE NOT NULL CHECK (PublicationDate IS date(PublicationDate)),
	TITLE TEXT NOT NULL CHECK(TITLE NOT GLOB '*[^ -~]*' AND TITLE GLOB '*[^ ]*' AND length(TITLE) > 0),
	MEDIUM Varchar(100) NOT NULL CHECK(MEDIUM IN ('Hardcover','Softcover','CD','DVD')),
	COVERPHOTO BLOB CHECK (COVERPHOTO = NULL OR Hex(COVERPHOTO) LIKE '89504E470D0A1A0A%'),
	PRIMARY KEY (ISBN)
);

CREATE TABLE BORROW(
	BorrowID INTEGER NOT NULL CHECK (BorrowID >= 0),
	Start Datetime DEFAULT current_timestamp NOT NULL CHECK (Start is datetime(Start)),
	Deadline datetime NOT NULL CHECK (Deadline IS datetime(Deadline)),
	Returned BOOLEAN CHECK (Returned IN ('true','false')),
	Email Varchar(100) NOT NULL COLLATE NOCASE,
	ID INTEGER NOT NULL,
	PRIMARY KEY (BorrowID),
	FOREIGN KEY (ID) REFERENCES BOOK_COPY(ID) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (Email) REFERENCES CUSTOMER (Email) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE GENRE(
	Name Varchar(100) NOT NULL COLLATE NOCASE CHECK (Name NOT GLOB '*[^a-zA-Z]*' AND length(Name) > 0),
	PRIMARY KEY (Name)
);

CREATE TABLE AUTHOR(
	ID INTEGER NOT NULL CHECK (ID >= 0),
	FirstName Varchar(100) NOT NULL CHECK (FirstName NOT GLOB '*[^a-zA-Z]*' AND length(FirstName) > 0 ),
	LastName Varchar(100) NOT NULL CHECK (LastName NOT GLOB '*[^a-zA-Z]*' AND length(LastName) > 0 ),
	PRIMARY KEY (ID)
);	

CREATE TABLE ITEM_HAS_GENRE(
	ISBN Varchar(100) NOT NULL,
	Name Varchar(100) NOT NULL,
	PRIMARY KEY (ISBN,Name),
	FOREIGN KEY (ISBN) REFERENCES ITEM (ISBN) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (Name) REFERENCES GENRE (Name) ON UPDATE CASCADE ON DELETE CASCADE
);
CREATE TABLE AUTHOR_WRITES_BOOK(
	ID INTEGER NOT NULL,
	ISBN Varchar(100) NOT NULL,
	PRIMARY KEY (ID,ISBN),
	FOREIGN KEY (ID) REFERENCES AUTHOR (ID) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (ISBN) REFERENCES ITEM (ISBN) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE ITEM_RECOMMENDS_ITEM(
	ISBN1 VARCHAR(100) NOT NULL,
	ISBN2 VARCHAR(100) NOT NULL,
	PRIMARY KEY (ISBN1,ISBN2),
	FOREIGN KEY (ISBN1) REFERENCES ITEM(ISBN)ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY (ISBN2) REFERENCES ITEM(ISBN)ON UPDATE CASCADE ON DELETE CASCADE
);


CREATE TRIGGER DateIntervalCheck BEFORE INSERT ON BORROW BEGIN SELECT CASE WHEN NEW.Start > NEW.Deadline THEN RAISE(ABORT, 'ERROR: START MUST BE BEFORE THE DEADLINE') END; END;

/*CREATE TRIGGER NotYetReturnedError BEFORE INSERT ON BORROW BEGIN SELECT CASE WHEN NEW.Returned = 'false' THEN RAISE(ABORT, 'ERROR: Book Copy was not returned therefore it cannot be lent') END; END;*/

CREATE TRIGGER LimitReached BEFORE INSERT ON BORROW BEGIN SELECT CASE WHEN ((SELECT count(ID) FROM BORROW WHERE [EMAIL] = NEW.Email) = 5) THEN RAISE(ABORT, 'ERROR: Limit Reached, unable to borrow more than 5 items') END; END;

CREATE TRIGGER AddressUpdateError BEFORE UPDATE ON ADDRESS BEGIN SELECT CASE WHEN ((SELECT count(CUSTOMER.Email) FROM CUSTOMER WHERE ID = NEW.ID) >= 2) THEN RAISE(ABORT, 'ERROR: Unable to update Addresses that belong to more than 1 CUSTOMER') END; END;

CREATE TRIGGER AddressDeleteError BEFORE DELETE ON ADDRESS BEGIN SELECT CASE WHEN ((SELECT count(CUSTOMER.Email) FROM CUSTOMER WHERE ADDRESS.ID = NEW.ADDRESS.ID) >= 2) THEN RAISE(ABORT, 'ERROR: Unable to Delete Addresses that belong to more than 1 CUSTOMER')END; END;

CREATE TRIGGER CUSTOMER_EMAIL BEFORE INSERT ON CUSTOMER BEGIN SELECT CASE WHEN(NOT(substr(NEW.Email, 1,instr(NEW.Email,'@')-1) NOT GLOB '*[^a-zA-Z0-9]*' AND substr(NEW.Email, instr(NEW.Email,'@'),1) like '@' AND substr (NEW.Email , instr(NEW.Email,'@')+1 ,( instr(NEW.Email,'.') - (instr(NEW.Email,'@')+1)) ) NOT GLOB '*[^a-zA-Z0-9]*' AND substr(NEW.Email , instr(NEW.Email ,'.') , 1) like '.' AND substr (NEW.Email ,instr(NEW.Email,'.')+1) NOT GLOB '*[^a-zA-Z]*' AND NEW.Email GLOB '*[^ ]*')) THEN RAISE (Abort,'ERROR: Email must be in the following format X@Y.Z') END; END;

CREATE TRIGGER LIBRARIAN_EMAIL BEFORE INSERT ON LIBRARIAN BEGIN SELECT CASE WHEN(NOT(substr(NEW.Email, 1,instr(NEW.Email,'@')-1) NOT GLOB '*[^a-zA-Z0-9]*' AND substr(NEW.Email, instr(NEW.Email,'@'),1) like '@' AND substr (NEW.Email , instr(NEW.Email,'@')+1 ,( instr(NEW.Email,'.') - (instr(NEW.Email,'@')+1)) ) NOT GLOB '*[^a-zA-Z0-9]*' AND substr(NEW.Email , instr(NEW.Email ,'.') , 1) like '.' AND substr (NEW.Email ,instr(NEW.Email,'.')+1) NOT GLOB '*[^a-zA-Z]*' AND NEW.Email GLOB '*[^ ]*')) THEN RAISE (Abort,'ERROR: Email must be in the following format X@Y.Z') END; END;

CREATE TRIGGER TEST_PASWORD BEFORE INSERT ON USER BEGIN SELECT case when (new.password GLOB  '*[^!-~]*' AND NEW.Password NOT GLOB '*[A-Z]*' AND NEW.Password NOT GLOB '*[0-9]*' OR NEW.Password GLOB '*[0-9][0-9]*') then raise(ABORT , 'ERROR: Invalid Password!') END; END;

CREATE TRIGGER DATE_CHECK BEFORE INSERT ON USER BEGIN SELECT CASE WHEN (NOT (NEW.DateOfBirth is date(NEW.DateOfBirth)))  THEN RAISE(ABORT, 'ERROR: Error in date must be in YYYY-MM-DD Format ') END; END;

CREATE TRIGGER FirstNameCheck BEFORE INSERT ON USER BEGIN SELECT CASE WHEN ( NOT (NEW.FirstName NOT GLOB '*[^ -~]*' AND NEW.FirstName GLOB '*[^ ]*' AND NEW.FirstName NOT GLOB '*[^!-~]*' ))  THEN RAISE(ABORT, 'ERROR: First Name invalid ') END; END;

CREATE TRIGGER LastNameCheck BEFORE INSERT ON USER BEGIN SELECT CASE WHEN ( NOT (NEW.LastName NOT GLOB '*[^ -~]*' AND NEW.LastName GLOB '*[^ ]*' AND NEW.LastName NOT GLOB '*[^!-~]*' ))  THEN RAISE(ABORT, 'ERROR: Last Name invalid ') END; END;

CREATE TRIGGER BookCopyNotYetReturned BEFORE INSERT ON BORROW WHEN ((SELECT COUNT(*) FROM BORROW WHERE Returned = 'false' AND BORROW.ID = NEW.ID ) >= 1) BEGIN SELECT RAISE(ABORT,'This Book Copy has not yet been returned'); END;
