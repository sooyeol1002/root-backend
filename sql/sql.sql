use myapp2;

select * from identity;
select * from profile;
select * from event;
select * from review;
select * from review_answer;
select * from product_inquery;

SELECT r.*
FROM profile p
INNER JOIN review r ON p.brand_name = r.brand_name
WHERE p.identity_id = 1;