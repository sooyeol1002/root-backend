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

SELECT * 
FROM review 
WHERE brand_name = '듀랑고' AND (review_answer IS NULL OR review_answer = '') 
ORDER BY id DESC 
LIMIT 5 OFFSET 1;

SELECT COUNT(*) 
FROM review 
WHERE brand_name = '듀랑고' AND (review_answer IS NULL OR review_answer = '');

SELECT * FROM product_inquery 
WHERE inquery_answer IS NOT NULL AND inquery_answer <> '' 
AND product_name LIKE '듀랑고' LIMIT 5 OFFSET 0;

SELECT * FROM product_inquery 
WHERE (inquery_answer IS NULL OR inquery_answer = '') 
AND product_name LIKE '듀랑고' LIMIT 5 OFFSET 0;