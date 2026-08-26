DROP SCHEMA IF EXISTS lbaw23152 CASCADE;

CREATE SCHEMA IF NOT EXISTS lbaw23152;

SET search_path TO lbaw23152;

DROP TABLE IF EXISTS authenticated_user CASCADE;
DROP TABLE IF EXISTS address CASCADE;
DROP TABLE IF EXISTS auction CASCADE;
DROP TABLE IF EXISTS item CASCADE;
DROP TABLE IF EXISTS bid CASCADE;
DROP TABLE IF EXISTS transaction CASCADE;
DROP TABLE IF EXISTS notification CASCADE;
DROP TABLE IF EXISTS review CASCADE;
DROP TABLE IF EXISTS report_auction CASCADE;
DROP TABLE IF EXISTS following CASCADE;

DROP TYPE IF EXISTS AUCTION_STATUS CASCADE;
DROP TYPE IF EXISTS ITEM_CONDITION CASCADE;
DROP TYPE IF EXISTS ROLES CASCADE;
DROP TYPE IF EXISTS NOTIFICATION_TYPE CASCADE;
DROP DOMAIN IF EXISTS EURO;
DROP DOMAIN IF EXISTS TODAY;

---------------------------Types--------------------
CREATE TYPE AUCTION_STATUS AS ENUM ('ACTIVE', 'CLOSED', 'CANCELLED');
CREATE TYPE ITEM_CONDITION AS ENUM ('NEW', 'LIKE NEW', 'EXCELLENT', 'GOOD', 'USED');
CREATE TYPE ROLES AS ENUM ('USER', 'ADMIN');
CREATE TYPE NOTIFICATION_TYPE AS ENUM ('AUCTION WON', 'AUCTION OUTBID', 'AUCTION SOLD', 'REPORT', 'AUCTION END', 'AUCTION BID', 'AUCTION CANCELLED', 'REVIEW');
CREATE DOMAIN EURO AS NUMERIC(20, 2);
CREATE DOMAIN TODAY AS TIMESTAMP DEFAULT CURRENT_TIMESTAMP CHECK (VALUE <= CURRENT_TIMESTAMP);



------------------Tables---------------------------

CREATE TABLE authenticated_user (
    id SERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    rating FLOAT,
    picture TEXT DEFAULT 'default.jpg',
    balance EURO DEFAULT 0,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    role ROLES NOT NULL DEFAULT 'USER'
);

CREATE TABLE address (
    id SERIAL PRIMARY KEY,
    street TEXT NOT NULL,
    postal_code TEXT NOT NULL,
    city TEXT NOT NULL,
    country TEXT NOT NULL,
    "user" INTEGER REFERENCES authenticated_user(id) ON DELETE SET NULL
);

CREATE TABLE item (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    category TEXT,
    brand TEXT,
    color TEXT,
    picture TEXT,
    condition ITEM_CONDITION NOT NULL
);

CREATE TABLE auction (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    start_date TIMESTAMP DEFAULT now(),
    end_date TIMESTAMP NOT NULL,
    starting_price EURO NOT NULL,
    current_price EURO NOT NULL,
    status AUCTION_STATUS NOT NULL DEFAULT 'ACTIVE',
    "owner" INTEGER REFERENCES authenticated_user(id) ON DELETE SET NULL,
    "item" INTEGER REFERENCES item(id) ON DELETE SET NULL
);

CREATE TABLE bid (
    id SERIAL PRIMARY KEY,
    value EURO NOT NULL,
    creation_date TIMESTAMP DEFAULT now(),
    "user" INTEGER REFERENCES authenticated_user(id) ON DELETE SET NULL,
    "auction" INTEGER REFERENCES auction(id) ON DELETE SET NULL
);

CREATE TABLE transaction (
    id SERIAL PRIMARY KEY,
    value EURO NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    description TEXT,
    "user" INTEGER REFERENCES authenticated_user(id) ON DELETE SET NULL
);

CREATE TABLE notification (
    id SERIAL PRIMARY KEY,
    message TEXT NOT NULL,
    type NOTIFICATION_TYPE NOT NULL,
    creation_date TIMESTAMP DEFAULT now(),
    read BOOLEAN DEFAULT FALSE,
    "user" INTEGER REFERENCES authenticated_user(id) ON DELETE SET NULL
);

CREATE TABLE review (
    id SERIAL PRIMARY KEY,
    rating INTEGER NOT NULL CHECK (rating > 0 AND rating <= 5),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    date TIMESTAMP DEFAULT now(),
    "reviewer" INTEGER REFERENCES authenticated_user(id) ON DELETE SET NULL,
    "reviewed" INTEGER REFERENCES authenticated_user(id) ON DELETE SET NULL,
    "auction" INTEGER REFERENCES auction(id) ON DELETE SET NULL
);

CREATE TABLE report_auction (
    id SERIAL PRIMARY KEY,
    reason TEXT NOT NULL,
    creation_date TIMESTAMP DEFAULT now(),
    "user" INTEGER REFERENCES authenticated_user(id) ON DELETE SET NULL,
    "auction" INTEGER REFERENCES auction(id) ON DELETE SET NULL
);

CREATE TABLE following (
    PRIMARY KEY ("user", auction),
    auction INTEGER NOT NULL REFERENCES auction(id) ON DELETE CASCADE,
    notifications BOOLEAN DEFAULT TRUE,
    start_date TIMESTAMP DEFAULT now(),
    "user" INTEGER REFERENCES authenticated_user(id) ON DELETE CASCADE
);

------------------Indexes---------------------------

CREATE INDEX active_auctions ON auction USING hash (status) WHERE status='ACTIVE';

CREATE INDEX bid_on_auction ON bid USING hash (auction);

CREATE INDEX notification_users ON notification USING hash ("user");

---FTS

ALTER TABLE auction ADD COLUMN tsvectors TSVECTOR;

CREATE FUNCTION auction_search_update() RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        NEW.tsvectors := (
            setweight(to_tsvector('english', NEW.title), 'A') ||
            setweight(to_tsvector('english', NEW.description), 'B')
        );
    END IF;
    IF TG_OP = 'UPDATE' THEN
        IF (NEW.description <> OLD.description OR NEW.title <> OLD.title) THEN
            NEW.tsvectors := (
                setweight(to_tsvector('english', NEW.title), 'A') ||
                setweight(to_tsvector('english', NEW.description), 'B')
            );
        END IF;
    END IF;
    RETURN NEW;
END $$
LANGUAGE plpgsql;

CREATE TRIGGER auction_search_update
    BEFORE INSERT OR UPDATE ON auction
    FOR EACH ROW
    EXECUTE PROCEDURE auction_search_update();

CREATE INDEX search_idx ON auction USING GIN (tsvectors);

---------------Triggers-----------------------------
--t01
CREATE OR REPLACE FUNCTION update_current_price_function() RETURNS TRIGGER AS
$BODY$
DECLARE
    auction_end_date TIMESTAMP;
BEGIN
    SELECT end_date INTO auction_end_date
    FROM auction
    WHERE id = NEW.auction;
    
    IF auction_end_date <= NOW() THEN
        RAISE EXCEPTION 'The auction has already ended.';
    END IF;
    
    IF NEW.value >= (SELECT current_price * 1.05 FROM auction WHERE id = NEW.auction) THEN
        UPDATE auction
        SET current_price = NEW.value
        WHERE id = NEW.auction;
    ELSE
        RAISE EXCEPTION 'New bid must be at least 5%% higher than the current bid.';
    END IF;
    
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER update_current_price
    AFTER INSERT ON bid
    FOR EACH ROW
    EXECUTE PROCEDURE update_current_price_function();

    
--t02
CREATE OR REPLACE FUNCTION check_previous_bid_function() RETURNS TRIGGER AS
$BODY$
DECLARE
    highest_bidder INTEGER;
BEGIN
    SELECT "user"
    INTO highest_bidder
    FROM bid
    WHERE auction = NEW.auction
    ORDER BY value DESC LIMIT 1;

    IF highest_bidder = NEW."user" THEN
        RAISE EXCEPTION 'You cannot bid again if you are winning the auction.';
    END IF;
    
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER check_previous_bid
    BEFORE INSERT ON bid
    FOR EACH ROW
    EXECUTE PROCEDURE check_previous_bid_function();
    
--t03
CREATE OR REPLACE FUNCTION extend_auction_end_time_function() RETURNS TRIGGER AS
$BODY$
BEGIN
    IF (SELECT end_date FROM auction WHERE id = NEW.auction) - INTERVAL '15 minutes' <= NEW.creation_date THEN
        UPDATE auction
        SET end_date = end_date + INTERVAL '30 minutes'
        WHERE id = NEW.auction;
    END IF;
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER extend_auction_end_time
    AFTER INSERT ON bid
    FOR EACH ROW
    EXECUTE FUNCTION extend_auction_end_time_function();
    
--t04
CREATE OR REPLACE FUNCTION check_auction_duration_function() RETURNS TRIGGER AS
$BODY$
DECLARE
    auction_duration INTERVAL;
BEGIN
    auction_duration := NEW.end_date - NEW.start_date;

    IF auction_duration <= INTERVAL '1 day' OR auction_duration >= INTERVAL '30 days' THEN
        RAISE EXCEPTION 'Auction duration must be more than 1 day and less than 30 days.';
    END IF;

    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER check_auction_duration
    BEFORE INSERT ON auction
    FOR EACH ROW
    EXECUTE PROCEDURE check_auction_duration_function();
    
--t05
CREATE OR REPLACE FUNCTION prevent_self_bidding_function() RETURNS TRIGGER AS
$BODY$
BEGIN
   IF EXISTS (
       SELECT 1
       FROM auction
       WHERE auction.id = NEW.auction AND auction.owner = NEW.user
   ) THEN
       RAISE EXCEPTION 'Owners are not allowed to bid on their own auctions.';
   END IF;
   RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER prevent_self_bidding
    BEFORE INSERT ON bid
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_self_bidding_function();

--t06
CREATE OR REPLACE FUNCTION rate_won_auctions_function() RETURNS TRIGGER AS
$BODY$
DECLARE 
    highest_bidder INTEGER;
    auction_owner INTEGER;
BEGIN
    SELECT "user" INTO highest_bidder 
    FROM bid 
    WHERE "auction" = NEW."auction" 
    ORDER BY value DESC LIMIT 1;
    
    SELECT "owner" INTO auction_owner
    FROM auction
    WHERE id = NEW."auction";
    
    IF (SELECT status FROM auction WHERE id = NEW."auction") = 'CLOSED' AND
       NEW."reviewer" = highest_bidder AND
       NEW."reviewed" = auction_owner AND
       (SELECT COUNT(id) FROM review WHERE "reviewer" = NEW."reviewer" AND "reviewed" = NEW."reviewed" AND "auction" = NEW."auction") = 0 THEN
        RETURN NEW;
    ELSE
        RAISE EXCEPTION 'To rate an auctioneer, a user has to win an auction of theirs and not have already reviewed them for that auction.';
    END IF;   
END
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER rate_won_auctions
    BEFORE INSERT ON review
    FOR EACH ROW
    EXECUTE PROCEDURE rate_won_auctions_function();
    
--t07
CREATE OR REPLACE FUNCTION check_starting_price_function() RETURNS TRIGGER AS
$BODY$
BEGIN
    IF NEW.starting_price <= 0 OR NEW.current_price <> NEW.starting_price THEN
        RAISE EXCEPTION 'Starting price must be higher than 0, and the current price must be the same as the starting price.';
    END IF;
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER check_starting_price
    BEFORE INSERT OR UPDATE OF starting_price ON auction
    FOR EACH ROW
    EXECUTE PROCEDURE check_starting_price_function();

--t08
CREATE OR REPLACE FUNCTION check_wallet_balance_function() RETURNS TRIGGER AS
$BODY$
BEGIN
    IF NEW.balance < 0 THEN
        RAISE EXCEPTION 'Wallet balance cannot be negative.';
    END IF;
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER check_wallet_balance
    BEFORE UPDATE OF balance ON authenticated_user
    FOR EACH ROW
    EXECUTE PROCEDURE check_wallet_balance_function();
    
--t09
CREATE OR REPLACE FUNCTION check_and_update_user_balance_and_create_transaction_function() RETURNS TRIGGER AS
$BODY$
DECLARE 
    user_balance EURO;
    auction_title TEXT;
BEGIN
    SELECT balance INTO user_balance
    FROM authenticated_user
    WHERE id = NEW."user";  
    
    IF user_balance < NEW.value THEN
        RAISE EXCEPTION 'Insufficient funds to place the bid.';
    END IF;
    
    SELECT title INTO auction_title
    FROM auction
    WHERE id = NEW.auction;
    
    UPDATE authenticated_user
    SET balance = balance - NEW.value
    WHERE id = NEW."user";
    
    INSERT INTO transaction (value, transaction_date, description, "user")
    VALUES (-NEW.value, NOW(), 'Bid on auction: ' || auction_title, NEW."user");
    
    RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER check_and_update_user_balance_and_create_transaction
    BEFORE INSERT ON bid
    FOR EACH ROW
    EXECUTE PROCEDURE check_and_update_user_balance_and_create_transaction_function();
    
--t10
CREATE OR REPLACE FUNCTION update_user_rating_function() RETURNS TRIGGER AS
$BODY$
DECLARE 
    new_avg_rating FLOAT;
BEGIN
    SELECT AVG(rating) INTO new_avg_rating
    FROM review
    WHERE reviewed = NEW.reviewed;
    
    UPDATE authenticated_user
    SET rating = new_avg_rating
    WHERE id = NEW.reviewed;
    
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER update_user_rating
    AFTER INSERT OR UPDATE OF rating ON review
    FOR EACH ROW
    EXECUTE PROCEDURE update_user_rating_function();

--t11
CREATE OR REPLACE FUNCTION update_deleted_user_info_function() RETURNS TRIGGER AS
$BODY$
DECLARE
    has_active_auction BOOLEAN;
    has_highest_bid BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1
        FROM auction
        WHERE "owner" = OLD.id
        AND status = 'ACTIVE'
    ) INTO has_active_auction;

    IF has_active_auction THEN
        RAISE EXCEPTION 'Cannot delete user with active auctions.';
    END IF;

    SELECT EXISTS (
        SELECT 1
        FROM auction AS a
        JOIN bid AS b ON a.id = b."auction"
        WHERE b."user" = OLD.id
        AND a.status = 'ACTIVE'
        AND b.value = (SELECT MAX(value) FROM bid WHERE "auction" = a.id)
    ) INTO has_highest_bid;

    IF has_highest_bid THEN
        RAISE EXCEPTION 'Cannot delete user with the current highest bid in an active auction.';
    END IF;

    UPDATE bid
    SET "user" = NULL
    WHERE "user" = OLD.id;

    UPDATE review
    SET reviewer = NULL
    WHERE reviewer = OLD.id;

    UPDATE review
    SET reviewed = NULL
    WHERE reviewed = OLD.id;

    UPDATE notification
    SET "user" = NULL
    WHERE "user" = OLD.id;

    UPDATE report_auction
    SET "user" = NULL
    WHERE "user" = OLD.id;

    RETURN OLD;
END;
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER update_deleted_user_info
BEFORE DELETE ON authenticated_user
FOR EACH ROW
EXECUTE FUNCTION update_deleted_user_info_function();
    
--t12
CREATE OR REPLACE FUNCTION handle_auction_closure() RETURNS TRIGGER AS
$BODY$
DECLARE
    final_bid EURO;
    winning_bidder INTEGER;
    follower_id INTEGER;
BEGIN
    IF OLD.status = 'ACTIVE' AND OLD.end_date <= NOW() THEN
        NEW.status := 'CLOSED';
    END IF;

    IF NEW.status = 'CLOSED' THEN

        SELECT value, "user" INTO final_bid, winning_bidder
        FROM bid
        WHERE "auction" = NEW.id
        ORDER BY value DESC LIMIT 1;

        IF final_bid IS NOT NULL AND winning_bidder IS NOT NULL THEN
            INSERT INTO notification (message, type, creation_date, "user")
            VALUES (
                'Your auction "' || NEW.title || '" was sold for ' || final_bid || ' EURO.',
                'AUCTION SOLD',
                NOW(),
                NEW."owner"
            );

            UPDATE authenticated_user
            SET balance = balance + final_bid
            WHERE id = NEW."owner";
            
            INSERT INTO transaction (value, transaction_date, description, "user")
            VALUES (
                final_bid,
                NOW(),
                'Received funds from auction ' || NEW.title,
                NEW."owner"
            );

            INSERT INTO notification (message, type, creation_date, "user")
            VALUES (
                'Congratulations! You have won the auction for "' || NEW.title || '".',
                'AUCTION WON',
                NOW(),
                winning_bidder
            );

        ELSE
            INSERT INTO notification (message, type, creation_date, "user")
            VALUES (
                'Your auction "' || NEW.title || '" was not sold because there were no bids.',
                'AUCTION END',
                NOW(),
                NEW."owner"
            );
        END IF;

        FOR follower_id IN
            SELECT "user" FROM following WHERE auction = NEW.id
        LOOP
            INSERT INTO notification (message, type, creation_date, "user")
            VALUES (
                'The auction "' || NEW.title || '" has ended.',
                'AUCTION END',
                NOW(),
                follower_id
            );
        END LOOP;
    END IF;

    RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER auction_closure_trigger
AFTER UPDATE OF status ON auction
FOR EACH ROW
WHEN (OLD.status = 'ACTIVE' AND NEW.status = 'CLOSED')
EXECUTE FUNCTION handle_auction_closure();
    
--t13
CREATE OR REPLACE FUNCTION auto_follow_auction_function() RETURNS TRIGGER AS
$BODY$
BEGIN
   IF NOT EXISTS (
       SELECT 1
       FROM following
       WHERE following."user" = NEW."user" AND following.auction = NEW.auction
   ) THEN
       INSERT INTO following ("user", auction, start_date)
       VALUES (NEW."user", NEW.auction, NOW());
   END IF;
   RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER auto_follow_auction
    AFTER INSERT ON bid
    FOR EACH ROW
    EXECUTE PROCEDURE auto_follow_auction_function();

--t14
CREATE OR REPLACE FUNCTION notify_owner_new_bid_function() RETURNS TRIGGER AS
$BODY$
DECLARE
    follower RECORD;
BEGIN
    INSERT INTO notification (message, type, creation_date, "user")
    SELECT
        'A new bid has been placed on your auction: "' || auction.title || '"',
        'AUCTION BID',
        NOW(),
        auction.owner
    FROM
        auction
    WHERE
        auction.id = NEW.auction;
    
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER notify_owner_new_bid
    AFTER INSERT ON bid
    FOR EACH ROW
    EXECUTE PROCEDURE notify_owner_new_bid_function();

    
--t15
CREATE OR REPLACE FUNCTION notify_and_refund_outbid_function() RETURNS TRIGGER AS
$BODY$
DECLARE
    outbid_user INT;
    auction_title TEXT;
    outbid_value EURO;
BEGIN
    SELECT bid."user", bid.value INTO outbid_user, outbid_value
    FROM bid
    WHERE bid.auction = NEW.auction AND bid."user" != NEW."user"
    ORDER BY bid.value DESC
    LIMIT 1;
    
    SELECT auction.title INTO auction_title
    FROM auction
    WHERE auction.id = NEW.auction;
    
    IF outbid_user IS NOT NULL THEN
        UPDATE authenticated_user
        SET balance = balance + outbid_value
        WHERE id = outbid_user;
        
        INSERT INTO transaction (value, transaction_date, description, "user")
        VALUES (outbid_value, NOW(), 'Refund for being outbid on auction: "' || auction_title || '"', outbid_user);
        
        INSERT INTO notification (message, type, creation_date, "user")
        VALUES (
            'You have been outbid on the auction: "' || auction_title || '". Your bid has been refunded.',
            'AUCTION OUTBID',
            NOW(),
            outbid_user
        );
    END IF;
    
    RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER notify_and_refund_outbid
    AFTER INSERT ON bid
    FOR EACH ROW
    EXECUTE PROCEDURE notify_and_refund_outbid_function();
    
--t16
CREATE OR REPLACE FUNCTION notify_followers_auction_canceled_function() RETURNS TRIGGER AS
$BODY$
DECLARE
    bid_count INTEGER;
BEGIN
    SELECT COUNT(*)
    INTO bid_count
    FROM bid
    WHERE "auction" = NEW.id;

    IF bid_count > 0 THEN
        RAISE EXCEPTION 'An auction with bids cannot be canceled.';
    ELSIF NEW.status = 'CANCELLED' THEN
        INSERT INTO notification (message, type, creation_date, "user")
        SELECT 
            'The auction "' || NEW.title || '" has been cancelled.',
            'AUCTION CANCELLED',
            NOW(),
            following."user"
        FROM following
        WHERE following.auction = NEW.id;
    END IF;

    RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER notify_followers_auction_canceled
    AFTER UPDATE OF status ON auction
    FOR EACH ROW
    WHEN (NEW.status = 'CANCELLED')
    EXECUTE PROCEDURE notify_followers_auction_canceled_function();                        
    
--t17
CREATE OR REPLACE FUNCTION prevent_admin_bid_function() RETURNS TRIGGER AS
$BODY$
BEGIN
    IF EXISTS (SELECT 1 FROM authenticated_user WHERE id = NEW.user AND role = 'ADMIN') THEN
        RAISE EXCEPTION 'Admin users are not allowed to place bids.';
    END IF;
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER prevent_admin_bid
    BEFORE INSERT ON bid
    FOR EACH ROW
    EXECUTE PROCEDURE prevent_admin_bid_function(); 
    
--t18
CREATE OR REPLACE FUNCTION validate_rating_function() RETURNS TRIGGER AS
$BODY$
BEGIN
    IF NEW.rating < 1 OR NEW.rating > 5 THEN
        RAISE EXCEPTION 'Rating value must be between 1 and 5.';
    END IF;
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER validate_rating
    BEFORE INSERT OR UPDATE OF rating ON review
    FOR EACH ROW
    EXECUTE PROCEDURE validate_rating_function(); 
    
--t19
CREATE OR REPLACE FUNCTION notify_admins_report_function() RETURNS TRIGGER AS
$BODY$
BEGIN
    INSERT INTO notification (message, type, creation_date, "user")
    SELECT 
        'A new report has been submitted for auction: ' || (SELECT title FROM auction WHERE id = NEW.auction),
        'REPORT',
        NOW(),
        authenticated_user.id
    FROM authenticated_user
    WHERE role = 'ADMIN';
    
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER notify_admins_report
    AFTER INSERT ON report_auction
    FOR EACH ROW
    EXECUTE PROCEDURE notify_admins_report_function();

    
--t20
CREATE OR REPLACE FUNCTION notify_followers_price_increase_function() RETURNS TRIGGER AS
$BODY$
DECLARE
    follower RECORD;
BEGIN
    FOR follower IN (SELECT "user" FROM following WHERE auction = NEW.id)
    LOOP
        INSERT INTO notification (message, type, creation_date, "user")
        VALUES ('The current price for the auction "' || NEW.title || '" has been increased to ' || NEW.current_price || ' euros.', 
                'AUCTION BID', NOW(), follower."user");
    END LOOP;
    
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER notify_followers_price_increase
    AFTER UPDATE OF current_price ON auction
    FOR EACH ROW
    WHEN (OLD.current_price IS DISTINCT FROM NEW.current_price)
    EXECUTE PROCEDURE notify_followers_price_increase_function();
    
--t21
CREATE OR REPLACE FUNCTION notify_owner_after_review_function() RETURNS TRIGGER AS
$BODY$
DECLARE
    auction_owner INTEGER;
    reviewer_username TEXT;
BEGIN
    SELECT INTO auction_owner owner FROM auction WHERE id = NEW.auction;
    
    SELECT INTO reviewer_username username FROM authenticated_user WHERE id = NEW.reviewer;
    
    INSERT INTO notification (message, type, creation_date, "user")
    VALUES ('User ' || reviewer_username || ' has given ' || NEW.rating || ' stars in a review for your auction "' 
            || (SELECT title FROM auction WHERE id = NEW.auction) || '".', 
            'REVIEW', NOW(), auction_owner);
    
    RETURN NEW;
END;
$BODY$
language plpgsql;

CREATE TRIGGER notify_owner_after_review
    AFTER INSERT ON review
    FOR EACH ROW
    EXECUTE PROCEDURE notify_owner_after_review_function();
    
--t22
CREATE OR REPLACE FUNCTION prevent_admin_create_auction() RETURNS TRIGGER AS
$BODY$
DECLARE 
    user_role ROLES;
BEGIN
    SELECT role INTO user_role 
    FROM authenticated_user
    WHERE id = NEW.owner;
    
    IF user_role = 'ADMIN' THEN
        RAISE EXCEPTION 'Admins are not allowed to create auctions.';
    END IF;
    
    RETURN NEW;
END;
$BODY$
LANGUAGE plpgsql;

CREATE TRIGGER prevent_admin_create_auction_trigger
    BEFORE INSERT ON auction
    FOR EACH ROW
    EXECUTE FUNCTION prevent_admin_create_auction();

-- users
INSERT INTO authenticated_user (username, first_name, last_name, email, password, role, balance) VALUES
('sarah_m', 'Sarah', 'Mitchell', 'sarahm@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'ADMIN', 50),
('michael_j', 'Michael', 'Johnson', 'michaelj@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'USER', 50),
('linda_w', 'Linda', 'Williams', 'lindaw@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'USER', 1000),
('james_b', 'James', 'Brown', 'jamesb@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'USER', 50),
('patricia_k', 'Patricia', 'King', 'patriciak@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'ADMIN', 50),
('david_l', 'David', 'Lee', 'davidl@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'USER', 50),
('susan_g', 'Susan', 'Garcia', 'susang@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'USER', 50),
('robert_a', 'Robert', 'Adams', 'roberta@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'USER', 50),
('karen_t', 'Karen', 'Taylor', 'karent@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'USER', 50),
('brian_h', 'Brian', 'Hernandez', 'brianh@example.com', '$2y$10$A08dPuR/FTe6jvAJ4g1UJuZgxaC.NA7RSBRa/MkdC59UNQyv3/zQK', 'USER', 50);

-- addresses
INSERT INTO address (street, postal_code, city, country, "user") VALUES
('123 Main St', '10001', 'New York', 'USA', 1),
('456 Elm St', '20002', 'Washington', 'USA', 2),
('789 Oak St', '30003', 'Atlanta', 'USA', 3),
('101 Maple St', '40004', 'Chicago', 'USA', 4),
('102 Pine St', '50005', 'Denver', 'USA', 5),
('103 Birch St', '60006', 'Los Angeles', 'USA', 6),
('104 Cedar St', '70007', 'San Francisco', 'USA', 7),
('105 Redwood St', '80008', 'Seattle', 'USA', 8),
('106 Spruce St', '90009', 'Miami', 'USA', 9),
('107 Aspen St', '10010', 'Boston', 'USA', 10);

-- items
INSERT INTO item (name, category, brand, color, picture, condition) VALUES
('Floral Tee', 'T-Shirt', 'Bershka', 'White', 'pictures/floral_tee.jpg', 'NEW'),
('Cotton Socks', 'Socks', 'Nike', 'Black', 'pictures/cotton_socks.jpg', 'NEW'),
('Denim Jacket', 'Jacket', 'Pull & Bear', 'Blue', 'pictures/denim_jacket.jpg', 'LIKE NEW'),
('Silk Scarf', 'Scarf', 'BOSS', 'Red', 'pictures/silk_scarf.jpg', 'NEW'),
('Leather Belt', 'Belt', 'Diesel', 'Brown', 'pictures/leather_belt.jpg', 'GOOD'),
('Running Shoes', 'Shoes', 'Asics', 'Grey', 'pictures/running_shoes.jpg', 'EXCELLENT'),
('Winter Gloves', 'Gloves', 'Adidas', 'Black', 'pictures/winter_gloves.jpg', 'NEW'),
('Wool Hat', 'Hat', 'The North Face', 'Beige', 'pictures/wool_hat.jpg', 'LIKE NEW'),
('Sunglasses', 'Sunglasses', 'DICCI', 'Black', 'pictures/sunglasses.jpg', 'NEW');

-- Create auctions for the 10 items and associate each with a user
INSERT INTO auction (title, description, end_date, starting_price, current_price, "owner", "item")
VALUES
('Floral Tee', 'Size M', NOW() + INTERVAL '7 days', 10.00, 10.00, 2, 1),
('Cotton Socks', 'Size L', NOW() + INTERVAL '6 days', 15.00, 15.00, 3, 2),
('Silk Scarf', 'One Size', NOW() + INTERVAL '5 days', 25.00, 25.00, 2, 4),
('Leather Belt', 'Size 32', NOW() + INTERVAL '6 days', 30.00, 30.00, 6, 5),
('Running Shoes', 'Size 9', NOW() + INTERVAL '5 days', 35.00, 35.00, 7, 6),
('Winter Gloves', 'Size S', NOW() + INTERVAL '6 days', 40.00, 40.00, 8, 7),
('Wool Hat', 'One Size', NOW() + INTERVAL '5 days', 45.00, 45.00, 9, 8),
('Sunglasses', 'One Size', NOW() + INTERVAL '6 days', 50.00, 50.00, 10, 9);

INSERT INTO auction (title, description, start_date, end_date, starting_price, current_price, "owner", "item")
VALUES
('Denim Jacket', 'Size XL', NOW() - INTERVAL '7 days', NOW() + INTERVAL '15 seconds', 20.00, 20.00, 4, 3);

INSERT INTO bid (value, "user", "auction", creation_date)
VALUES 
(30.00, 3, 9, NOW() - INTERVAL '1 hour'),
(35.00, 3, 4, NOW() + INTERVAL '1 hour'),
(50.00, 3, 6, NOW() + INTERVAL '3 hours'),
(45.00, 4, 5, NOW() + INTERVAL '1 hours'),
(40.00, 8, 4, NOW() + INTERVAL '5 hours'),
(29.00, 9, 1, NOW() + INTERVAL '1 hours'),
(24.00, 10, 2, NOW() + INTERVAL '6 hours');

INSERT INTO report_auction (reason, "user", "auction") VALUES
('The auction listing appears to be fraudulent or misleading, with significant discrepancies between the description and the actual item.', 8, 2),
('The auction is selling items that are illegal or prohibited by the platforms terms of service.', 7, 5),
('he user is posting multiple identical or very similar auctions in a short period, which seems like spam or misuse of the platform.', 9, 9),
('Fake item.', 1, 6),
('The auction is selling counterfeit items.', 7, 7);