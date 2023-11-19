use myapp2;

select * from identity;
select * from profile;
select * from event;
select * from review;
select * from product_inquery;

alter table review add review_date varchar(30);

SELECT r.*
FROM profile p
INNER JOIN review r ON p.brand_name = r.brand_name
WHERE p.identity_id = 1;

DELETE FROM profile WHERE identity_id = 1;

DELETE FROM review WHERE id = 42;

SELECT * FROM review WHERE id = 35;

SELECT * FROM review where brand_name = '듀랑고';
update review set review_date="2023-10-05" where brand_name='듀랑고' and review_date is null limit 3;
update review set review_date="2023-0-05" where id < 31;
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

update product_inquery set inquery_answer = "상품설명 페이지를 참고해주세요!" where id = 1;

update product_inquery set inquery_content = "사용환경이 어떻게 되나요?" where id = 1;