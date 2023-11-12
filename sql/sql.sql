use myapp2;

select * from identity;
select * from profile;
select * from event;
select * from review;
select * from product_inquery;

SELECT r.*
FROM profile p
INNER JOIN review r ON p.brand_name = r.brand_name
WHERE p.identity_id = 1;

DELETE FROM profile WHERE identity_id = 1;

SELECT * FROM review WHERE id = 35;

SELECT * FROM review 
WHERE review_answer IS NOT NULL AND review_answer <> '' AND brand_name = '듀랑고' 
LIMIT 5 OFFSET 0;

SELECT COUNT(*) FROM review 
WHERE review_answer IS NOT NULL AND review_answer <> '' AND brand_name = '듀랑고';