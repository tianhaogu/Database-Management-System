CREATE VIEW VIEW_USER_INFORMATION AS SELECT USERS.USER_ID, USERS.FIRST_NAME, USERS.LAST_NAME, USERS.YEAR_OF_BIRTH, USERS.MONTH_OF_BIRTH, USERS.DAY_OF_BIRTH,
    USERS.GENDER, CC.CITY_NAME AS CURRENT_CITY, CC.STATE_NAME AS CURRENT_STATE, CC.COUNTRY_NAME AS CURRENT_COUNTRY, CH.CITY_NAME AS HOMETOWN_CITY, 
    CH.STATE_NAME AS HOMETOWN_STATE, CH.COUNTRY_NAME AS HOMETOWN_COUNTRY, PROGRAMS.INSTITUTION AS INSTITUTION_NAME, EDUCATION.PROGRAM_YEAR,
    PROGRAMS.CONCENTRATION AS PROGRAM_CONCENTRATION, PROGRAMS.DEGREE AS PROGRAM_DEGREE FROM USERS 
    LEFT JOIN USER_CURRENT_CITIES ON USERS.USER_ID = USER_CURRENT_CITIES.USER_ID LEFT JOIN CITIES CC ON USER_CURRENT_CITIES.CURRENT_CITY_ID = CC.CITY_ID
    LEFT JOIN USER_HOMETOWN_CITIES ON USERS.USER_ID = USER_HOMETOWN_CITIES.USER_ID LEFT JOIN CITIES CH ON USER_HOMETOWN_CITIES.HOMETOWN_CITY_ID = CH.CITY_ID
    LEFT JOIN EDUCATION ON USERS.USER_ID = EDUCATION.USER_ID LEFT JOIN PROGRAMS ON EDUCATION.PROGRAM_ID = PROGRAMS.PROGRAM_ID;


CREATE VIEW VIEW_ARE_FRIENDS AS SELECT * FROM FRIENDS;

CREATE VIEW VIEW_PHOTO_INFORMATION AS SELECT ALBUMS.ALBUM_ID, ALBUMS.ALBUM_OWNER_ID AS OWNER_ID, ALBUMS.COVER_PHOTO_ID, ALBUMS.ALBUM_NAME, 
    ALBUMS.ALBUM_CREATED_TIME, ALBUMS.ALBUM_MODIFIED_TIME, ALBUMS.ALBUM_LINK, ALBUMS.ALBUM_VISIBILITY, PHOTOS.PHOTO_ID, PHOTOS.PHOTO_CAPTION, 
    PHOTOS.PHOTO_CREATED_TIME, PHOTOS.PHOTO_MODIFIED_TIME, PHOTOS.PHOTO_LINK FROM ALBUMS LEFT JOIN PHOTOS ON ALBUMS.ALBUM_ID = PHOTOS.ALBUM_ID;

CREATE VIEW VIEW_EVENT_INFORMATION AS SELECT USER_EVENTS.EVENT_ID, USER_EVENTS.EVENT_CREATOR_ID, USER_EVENTS.EVENT_NAME, USER_EVENTS.EVENT_TAGLINE, 
    USER_EVENTS.EVENT_DESCRIPTION, USER_EVENTS.EVENT_HOST, USER_EVENTS.EVENT_TYPE, USER_EVENTS.EVENT_SUBTYPE, USER_EVENTS.EVENT_ADDRESS, 
    CITIES.CITY_NAME, CITIES.STATE_NAME, CITIES.COUNTRY_NAME, USER_EVENTS.EVENT_START_TIME, USER_EVENTS.EVENT_END_TIME FROM USER_EVENTS
    LEFT JOIN CITIES ON USER_EVENTS.EVENT_CITY_ID = CITIES.CITY_ID;

CREATE VIEW VIEW_TAG_INFORMATION AS SELECT TAG_PHOTO_ID AS PHOTO_ID, TAG_SUBJECT_ID, TAG_CREATED_TIME, TAG_X AS TAG_X_COORDINATE, TAG_Y AS TAG_Y_COORDINATE FROM TAGS;